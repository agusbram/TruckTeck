package ar.edu.iua.TruckTeck.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Clase utilitaria para operaciones relacionadas con JSON,
 * facilitando la serialización, deserialización y extracción de valores
 * desde objetos {@link JsonNode}.
 *
 * <p>Incluye métodos para configurar un {@link ObjectMapper} con
 * serializadores o deserializadores personalizados, así como métodos
 * de conveniencia para obtener valores primitivos de nodos JSON
 * con valores por defecto.</p>
 */
public final class JsonUtiles {

    /**
     * Construye un {@link ObjectMapper} configurado con un formato de fecha
     * específico y un serializador personalizado opcional.
     *
     * @param clazz       La clase objetivo a la cual se aplicará el serializador.
     * @param ser         El serializador personalizado para la clase dada. Puede ser {@code null}.
     * @param dateFormat  El formato de fecha a utilizar. Si es {@code null}, se usa por defecto
     *                    {@code "yyyy-MM-dd'T'HH:mm:ssZ"}.
     * @return Una instancia configurada de {@link ObjectMapper}.
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ObjectMapper getObjectMapper(Class clazz, StdSerializer ser, String dateFormat) {
		ObjectMapper mapper = new ObjectMapper();
		String defaultFormat = "yyyy-MM-dd'T'HH:mm:ssZ";
		if (dateFormat != null)
			defaultFormat = dateFormat;
		SimpleDateFormat df = new SimpleDateFormat(defaultFormat, Locale.getDefault());
		SimpleModule module = new SimpleModule();
		if (ser != null) {
			module.addSerializer(clazz, ser);
		}
		mapper.setDateFormat(df);
		mapper.registerModule(module);
		return mapper;

	}
	
    /**
    * Construye un {@link ObjectMapper} configurado con un formato de fecha
    * específico y un deserializador personalizado opcional.
    *
    * @param clazz       La clase objetivo a la cual se aplicará el deserializador.
    * @param deser       El deserializador personalizado para la clase dada. Puede ser {@code null}.
    * @param dateFormat  El formato de fecha a utilizar. Si es {@code null}, se usa por defecto
    *                    {@code "yyyy-MM-dd'T'HH:mm:ssZ"}.
    * @return Una instancia configurada de {@link ObjectMapper}.
    */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ObjectMapper getObjectMapper(Class clazz, StdDeserializer deser, String dateFormat) {
		ObjectMapper mapper = new ObjectMapper();
		String defaultFormat = "yyyy-MM-dd'T'HH:mm:ssZ";
		if (dateFormat != null)
			defaultFormat = dateFormat;
		SimpleDateFormat df = new SimpleDateFormat(defaultFormat, Locale.getDefault());
		SimpleModule module = new SimpleModule();
		if (deser != null) {
			module.addDeserializer(clazz, deser);
		}
		mapper.setDateFormat(df);
		mapper.registerModule(module);
		return mapper;
	}

	/**
	 * Obtiene una cadena con la siguiente lógica:
	 * 1) Busca en cada uno de los atributos definidos en el arreglo "attrs",
	 *    el primero que encuentra será el valor retornado.
	 * 2) Si no se encuentra ninguno de los atributos del punto 1), se
	 *    retorna "defaultValue".
	 * Ejemplo: supongamos que "node" represente: {"code":"c1, "codigo":"c11", "stock":true}
	 *   getString(node, String[]{"codigo","cod"},"-1") retorna: "cl1"
	 *   getString(node, String[]{"cod_prod","c_prod"},"-1") retorna: "-1"
	 * @param node
	 * @param attrs
	 * @param defaultValue
	 * @return
	 */

	public static String getString(JsonNode node, String[] attrs, String defaultValue) {
		String r = null;
		for (String attr : attrs) {
			if (node.get(attr) != null) {
				r = node.get(attr).asText();
				break;
			}
		}
		if (r == null)
			r = defaultValue;
		return r;
	}

    /**
    * Obtiene un valor numérico de tipo {@code double} desde un nodo JSON.
    *
    * <p>La búsqueda se realiza en el orden de los atributos definidos
    * en el arreglo {@code attrs}. El primero encontrado y válido
    * será retornado.</p>
    *
    * <p>Si no se encuentra ninguno, se devuelve el valor por defecto.</p>
    *
    * @param node         El nodo JSON desde el cual se extraerá el valor.
    * @param attrs        Lista de nombres de atributos a buscar.
    * @param defaultValue Valor por defecto a retornar si no se encuentra ninguno.
    * @return El valor encontrado o {@code defaultValue} en caso contrario.
    */
	public static double getDouble(JsonNode node, String[] attrs, double defaultValue) {
		Double r = null;
		for (String attr : attrs) {
			if (node.get(attr) != null && node.get(attr).isDouble()) {
				r = node.get(attr).asDouble();
				break;
			}
		}
		if (r == null)
			r = defaultValue;
		return r;
	}

    /**
    * Obtiene un valor booleano desde un nodo JSON.
    *
    * <p>La búsqueda se realiza en el orden de los atributos definidos
    * en el arreglo {@code attrs}. El primero encontrado y válido
    * será retornado.</p>
    *
    * <p>Si no se encuentra ninguno, se devuelve el valor por defecto.</p>
    *
    * @param node         El nodo JSON desde el cual se extraerá el valor.
    * @param attrs        Lista de nombres de atributos a buscar.
    * @param defaultValue Valor por defecto a retornar si no se encuentra ninguno.
    * @return El valor encontrado o {@code defaultValue} en caso contrario.
    */
	public static boolean getBoolean(JsonNode node, String[] attrs, boolean defaultValue) {
		Boolean r = null;
		for (String attr : attrs) {
			if (node.get(attr) != null && node.get(attr).isBoolean()) {
				r = node.get(attr).asBoolean();
				break;
			}
		}
		if (r == null)
			r = defaultValue;
		return r;
	}

}