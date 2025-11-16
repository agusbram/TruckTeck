package ar.edu.iua.TruckTeck.integration.tms.model.business;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.TruckTeck.model.Order;
import ar.edu.iua.TruckTeck.model.OrderStatusLog;
import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.FoundException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.enums.OrderState;
import ar.edu.iua.TruckTeck.model.persistence.OrderRepository;
import ar.edu.iua.TruckTeck.model.persistence.OrderStatusLogRepository;

/**
 * Implementación de la lógica de negocio para la integración con TMS (Terminal Management System).
 * <p>
 * Esta clase gestiona los pesajes de camiones realizados por el sistema externo de balanza.
 * Coordina el flujo de estados de las órdenes durante el proceso de carga:
 * </p>
 * 
 * <p><b>Flujo de Estados:</b></p>
 * <ol>
 *   <li><b>PENDING</b> → Orden creada, esperando pesaje inicial</li>
 *   <li><b>TARA_REGISTERED</b> → Pesaje inicial registrado, camión puede cargar</li>
 *   <li><b>LOADING</b> → Carga en proceso o finalizada, esperando pesaje final</li>
 *   <li><b>FINALIZED</b> → Pesaje final registrado, orden completada</li>
 * </ol>
 * 
 * <p><b>Funcionalidades principales:</b></p>
 * <ul>
 *   <li>Registro de pesaje inicial (tara del camión vacío)</li>
 *   <li>Generación de códigos de activación únicos de 5 dígitos</li>
 *   <li>Registro de pesaje final (camión cargado)</li>
 *   <li>Auditoría de cambios de estado mediante OrderStatusLog</li>
 * </ul>
 * 
 * <p>
 * Esta clase es invocada por los endpoints REST del controlador TmsRestController,
 * que son consumidos por el sistema externo de balanza.
 * </p>
 * 
 * @see ar.edu.iua.TruckTeck.integration.tms.controllers.TmsRestController
 * @see ar.edu.iua.TruckTeck.model.enums.OrderState
 */
@Service
public class OrderTmsBusiness implements IOrderTmsBusiness {

    private static final Logger log = LoggerFactory.getLogger(OrderTmsBusiness.class);

    /**
     * Repositorio para acceso a datos de órdenes.
     */
    @Autowired
    private OrderRepository orderRepository;

    /**
     * Repositorio para registro de auditoría de cambios de estado.
     */
    @Autowired
    private OrderStatusLogRepository orderStatusLogRepository;

    /**
     * Registra la tara inicial de una orden basada en el número de orden y el peso inicial del camión vacío.
     * 
     * Flujo:
     * 1. Busca la orden por número de orden
     * 2. Valida que la orden esté en estado PENDING
     * 3. Genera código de activación de 5 dígitos
     * 4. Registra el peso inicial y fecha/hora
     * 5. Cambia estado a TARA_REGISTERED
     * 6. Registra el cambio de estado en el log
     * 
     * @param number número de orden (ej: "ORD-001")
     * @param initialWeight peso del camión vacío (tara) en kg
     * @return Order actualizada con el pesaje inicial registrado
     * @throws BusinessException si ocurre un error en la lógica de negocio o la orden no está en estado PENDING
     * @throws NotFoundException si no se encuentra la orden con el número especificado
     * @throws FoundException si ya existe un código de activación duplicado (muy improbable)
     */
    @Override
    public Order registerInitialWeighing(String number, Double initialWeight) 
            throws BusinessException, NotFoundException, FoundException {

        log.info("TMS: Registrando pesaje inicial para camión {} con peso {}", number, initialWeight);

        try {
            // 1. Buscar una orden pendiente para este camión por dominio
            // Optional<Order> orderOpt = orderRepository.findByTrucknumberAndState(number, OrderState.PENDING);
            Optional<Order> orderOpt = orderRepository.findByNumber(number);
            if (orderOpt.isEmpty()) {
                throw new NotFoundException(
                    "No se encontró una orden pendiente de pesaje inicial con el numero: " + number
                );
            }
            // 2. Obtener la orden
            Order order = orderOpt.get();

            // 3. Validar que la orden esté en el estado correcto
            if (order.getState() != OrderState.PENDING) {
                throw new BusinessException(
                    "La orden " + order.getNumber() + " no está en estado PENDING. Estado actual: " + order.getState()
                );
            }

            // 4. Generar código de activación único de 5 dígitos
            String activationCode = generateActivationCode();

            // 5. Registrar datos del pesaje inicial
            order.setInitialWeight(initialWeight);
            order.setActivationCode(activationCode);
            order.setInitialWeighing(LocalDateTime.now());

            // 6. Cambiar estado a TARA_REGISTERED
            OrderState previousState = order.getState();
            order.setState(OrderState.TARA_REGISTERED);

            // 7. Guardar la orden
            Order savedOrder = orderRepository.save(order);

            // 8. Registrar el cambio de estado en el log
            logStateChange(savedOrder, previousState, OrderState.TARA_REGISTERED, 
                "TMS", "Pesaje inicial registrado. Peso: " + initialWeight + " kg");

            log.info("TMS: Pesaje inicial registrado exitosamente. Orden: {}, Código: {}", 
                savedOrder.getNumber(), activationCode);

            return savedOrder;

        } catch (NotFoundException e) {
            log.error("TMS: Error al registrar pesaje inicial: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("TMS: Error inesperado al registrar pesaje inicial", e);
            throw new BusinessException("Error al registrar el pesaje inicial: " + e.getMessage());
        }
    }

    /**
     * Registra el peso final de una orden basada en el número de orden y el peso final del camión cargado.
     * 
     * Flujo:
     * 1. Busca la orden por número de orden
     * 2. Valida que esté en estado LOADING (cerrada para carga)
     * 3. Valida que tenga pesaje inicial registrado
     * 4. Registra el peso final y fecha/hora
     * 5. Cambia estado a FINALIZED
     * 6. Registra el cambio de estado en el log
     * 7. Retorna la orden con los datos de conciliación
     * 
     * @param number número de orden (ej: "ORD-001")
     * @param finalWeight peso del camión cargado en kg
     * @return Order actualizada con el pesaje final y datos de conciliación
     * @throws BusinessException si ocurre un error en la lógica de negocio o la orden no está en estado LOADING
     * @throws NotFoundException si no se encuentra la orden con el número especificado
     * @throws FoundException (no se usa en este método, pero está en la firma de la interfaz)
     */
    @Override
    public Order registerFinalWeighing(String number, Double finalWeight) 
            throws BusinessException, NotFoundException, FoundException {

        log.info("TMS: Registrando pesaje final para código de activación: {}", number);

        try {
            // // 1. Buscar la orden por código de activación
            // Optional<Order> orderOpt = orderRepository.findByActivationCode(activationCode);

            // 1. Buscar la orden por número de orden
            Optional<Order> orderOpt = orderRepository.findByNumber(number);
            if (orderOpt.isEmpty()) {
                throw new NotFoundException(
                    "No se encontró una orden con el numero de orden: " + number
                );
            }
            Order order = orderOpt.get();

            // 2. Validar que la orden esté en estado LOADING (cerrada para carga)
            if (order.getState() != OrderState.LOADING) {
                throw new BusinessException(
                    "La orden " + order.getNumber() + 
                    " no está en estado LOADING (cerrada para carga). Estado actual: " + order.getState()
                );
            }

            // 3. Validacioones:
            if (order.getInitialWeight() == null) {
                throw new BusinessException(
                    "La orden " + order.getNumber() + " no tiene pesaje inicial registrado"
                );
            }
            if (finalWeight < order.getInitialWeight()) {
                throw new BusinessException(
                    "El peso final (" + finalWeight + ") es menor al peso inicial"
                );
            }

            // 4. Registrar el peso final
            order.setFinalWeight(finalWeight);
            order.setEndWeighing(LocalDateTime.now());

            // 5. Cambiar estado a FINALIZED
            OrderState previousState = order.getState();
            order.setState(OrderState.FINALIZED);

            // 6. Guardar la orden
            Order savedOrder = orderRepository.save(order);

            // 7. Registrar el cambio de estado en el log
            logStateChange(savedOrder, previousState, OrderState.FINALIZED, 
                "TMS", "Pesaje final registrado. Peso: " + finalWeight + " kg");

            log.info("TMS: Pesaje final registrado exitosamente. Orden: {}, Peso final: {} kg", 
                savedOrder.getNumber(), finalWeight);

            // 8. Retornar la orden con los datos de conciliación
            // La conciliación se calcula en tiempo real cuando se solicita
            return savedOrder;

        } catch (NotFoundException e) {
            log.error("TMS: Error al registrar pesaje final: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("TMS: Error inesperado al registrar pesaje final", e);
            throw new BusinessException("Error al registrar el pesaje final: " + e.getMessage());
        }
    }

    /**
     * Genera un código de activación aleatorio de 5 dígitos.
     * <p>
     * El código se genera usando números aleatorios entre 00000 y 99999.
     * Se formatea con ceros a la izquierda para garantizar siempre 5 dígitos.
     * </p>
     * 
     * <p><b>Ejemplos:</b></p>
     * <ul>
     *   <li>12345</li>
     *   <li>00789</li>
     *   <li>99999</li>
     * </ul>
     * 
     * @return String con exactamente 5 dígitos numéricos
     */
    private String generateActivationCode() {
        Random random = new Random();
        int code = random.nextInt(100000); // Genera número entre 0 y 99999
        return String.format("%05d", code); // Formatea con ceros a la izquierda si es necesario
    }

    /**
     * Registra un cambio de estado en el log de auditoría (OrderStatusLog).
     * <p>
     * Crea un registro de auditoría cada vez que una orden cambia de estado,
     * permitiendo trazabilidad completa del proceso de carga.
     * </p>
     * 
     * <p><b>Información registrada:</b></p>
     * <ul>
     *   <li>Número de orden</li>
     *   <li>Estado anterior y nuevo estado</li>
     *   <li>Timestamp del cambio</li>
     *   <li>Actor/usuario que realizó el cambio</li>
     *   <li>Observaciones adicionales</li>
     * </ul>
     * 
     * <p>
     * Si ocurre un error al guardar el log, se registra en los logs de aplicación
     * pero NO se lanza excepción para no interrumpir el flujo principal del negocio.
     * </p>
     * 
     * @param order Orden que cambió de estado
     * @param previousState Estado anterior de la orden
     * @param newState Nuevo estado de la orden
     * @param user Usuario o sistema que realizó el cambio (ej: "TMS", "Operador Juan")
     * @param observation Observaciones adicionales sobre el cambio (ej: "Pesaje inicial registrado. Peso: 8500.5 kg")
     */
    private void logStateChange(Order order, OrderState previousState, OrderState newState, 
                                 String user, String observation) {
        try {
            OrderStatusLog statusLog = new OrderStatusLog();
            statusLog.setOrderNumber(order.getId());
            statusLog.setFromState(previousState);
            statusLog.setToState(newState);
            statusLog.setTimestamp(LocalDateTime.now());
            statusLog.setActor(user);
            statusLog.setNote(observation);

            orderStatusLogRepository.save(statusLog);

            log.debug("Estado de orden {} registrado en auditoría: {} -> {}", 
                order.getNumber(), previousState, newState);

        } catch (Exception e) {
            log.error("Error al registrar cambio de estado en el log de auditoría", e);
            // No lanzamos excepción para no interrumpir el flujo principal del negocio
        }
    }
}

