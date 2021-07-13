package tech.stackable.t2.api.cluster;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import tech.stackable.t2.api.cluster.controller.MalformedClusterDefinitionException;

import java.util.Map;

public class YamlClusterDefinitionReader implements Converter<String, Map<String, Object>> {

    /**
     * Parses the given cluster definition and returns a map representing the
     * content.
     *
     * @param source raw cluster definition as provided by the request
     * @return cluster definition as map for further processing, <code>null</code>
     *         if no definition provided
     * @throws MalformedClusterDefinitionException if the cluster definition file is
     *                                             not valid
     */
    public Map<String, Object> convert(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        }

        Map<String, Object> clusterDefinitionMap = null;
        if (source != null) {
            try {
                clusterDefinitionMap = new ObjectMapper(new YAMLFactory()).readValue(source, Map.class);
            } catch (JsonProcessingException e) {
                throw new MalformedClusterDefinitionException("The cluster definition does not contain valid YAML/JSON.", e);
            }
        }

        if (!"t2.stackable.tech/v1".equals(clusterDefinitionMap.get("apiVersion"))) {
            throw new MalformedClusterDefinitionException("The apiVersion is either missing or not valid.");
        }

        if (!"Infra".equals(clusterDefinitionMap.get("kind"))) {
            throw new MalformedClusterDefinitionException("The kind of requested resource is either missing or not valid.");
        }

        return clusterDefinitionMap;
    }
}
