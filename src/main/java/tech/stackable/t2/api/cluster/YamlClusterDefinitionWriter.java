package tech.stackable.t2.api.cluster;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;

public class YamlClusterDefinitionWriter implements Converter<Map<String, Object>, String> {

    public String convert(Map<String, Object> clusterDefinition) {
        StringWriter stringWriter = new StringWriter();
        try {
            new ObjectMapper(new YAMLFactory()).writeValue(stringWriter, clusterDefinition);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to stringify cluster definition", e);
        }
        return stringWriter.toString();
    }
}
