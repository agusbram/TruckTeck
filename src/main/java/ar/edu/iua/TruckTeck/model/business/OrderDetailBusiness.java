package ar.edu.iua.TruckTeck.model.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;
import ar.edu.iua.TruckTeck.model.persistence.OrderDetailRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderDetailBusiness implements IOrderDetailBusiness {

    @Autowired
    private OrderDetailRepository detailDAO;

    public Double calculateAverageTemperature(Long orderId) throws BusinessException, NotFoundException {
        Double avgTemp;

        try {
            avgTemp = detailDAO.findAverageTemperatureByOrderId(orderId);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().message("Error al obtener la temperatura promedio.").build();
        }
        if (avgTemp == null) {
            throw NotFoundException.builder().message("No se encontraron detalles de orden para el ID proporcionado: " + orderId).build();
        }

        return avgTemp;
    }

    public Double calculateAverageDensity(Long orderId) throws BusinessException, NotFoundException {
        Double avgDensity;

        try {
            avgDensity = detailDAO.findAverageDensityByOrderId(orderId);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().message("Error al obtener la temperatura promedio.").build();
        }
        if (avgDensity == null) {
            throw NotFoundException.builder().message("No se encontraron detalles de orden para el ID proporcionado: " + orderId).build();
        }

        return avgDensity;
    }

    public Double calculateAverageCaudal(Long orderId) throws BusinessException, NotFoundException {
        Double avgFlow;

        try {
            avgFlow = detailDAO.findAverageCaudalByOrderId(orderId);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw BusinessException.builder().message("Error al obtener la temperatura promedio.").build();
        }
        if (avgFlow == null) {
            throw NotFoundException.builder().message("No se encontraron detalles de orden para el ID proporcionado: " + orderId).build();
        }
        return avgFlow;
    }
}
