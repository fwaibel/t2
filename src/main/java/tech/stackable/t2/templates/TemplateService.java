package tech.stackable.t2.templates;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import tech.stackable.t2.api.cluster.controller.MalformedClusterDefinitionException;
import tech.stackable.t2.api.cluster.domain.Cluster;
import tech.stackable.t2.security.SshKey;
import tech.stackable.t2.wireguard.WireguardService;

@Service
public class TemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateService.class);

    @Autowired
    private WireguardService wireguardService;

    @Autowired
    @Qualifier("workspaceDirectory")
    private Path workspaceDirectory;

    @Autowired
    @Qualifier("templateDirectory")
    private Path templateDirectory;

    @Autowired
    @Qualifier("credentials")
    private Properties credentials;

    @Value("${t2.dns.cluster-domain}")
    private String domain;

    @Value("${t2.templates.default}")
    private String defaultTemplateName;

    @Autowired
    private SshKey sshKey;

    /**
     * Creates a working directory for the given cluster, using the given cluster
     * definition (JSON)
     * 
     * @param cluster           cluster metadata
     * @param clusterDefinition cluster definition as requested by the client. If
     *                          missing, default cluster definition of the template
     *                          is used
     * @return working directory path
     * @throws RuntimeException if directory does not exist
     */
    public Path createWorkingDirectory(Cluster cluster, Map<String, Object> clusterDefinition) {
        Path workingDirectory = workspaceDirectory.resolve(cluster.getId().toString());

        if (Files.exists(workingDirectory) && Files.isDirectory(workingDirectory)) {
            throw new RuntimeException(String.format("Working directory for cluster %s could not be created because it already exists.", cluster.getId()));
        }

        String templateName = this.defaultTemplateName;
        if (clusterDefinition != null && clusterDefinition.containsKey("template") && clusterDefinition.get("template") instanceof String) {
            templateName = (String) clusterDefinition.get("template");
        }

        Path templatePath = this.templateDirectory.resolve(templateName);
        if (!Files.exists(templatePath) || !Files.isDirectory(templatePath)) {
            throw new MalformedClusterDefinitionException(MessageFormat.format("The template {0} does not exist.", templateName));
        }

        try {

            Map<String, Object> templateVariables = new HashMap<>();

            // Generate Keypairs for Wireguard
            String natPrivateKey = this.wireguardService.generatePrivateKey();
            String natPublicKey = this.wireguardService.generatePublicKey(natPrivateKey);
            List<String> clientPrivateKeys = Stream.generate(wireguardService::generatePrivateKey).limit(8).collect(Collectors.toList());
            List<String> clientPublicKeys = clientPrivateKeys.stream().map(this.wireguardService::generatePublicKey).collect(Collectors.toList());

            // Additional props that can be used in a template
            templateVariables.put("t2_ssh_key_public", sshKey.getPublicKeyPath().toString());
            templateVariables.put("t2_ssh_key_private", sshKey.getPrivateKeyPath().toString());
            templateVariables.put("datacenter_name", MessageFormat.format("t2-{0}", cluster.getId()));
            templateVariables.put("public_hostname", MessageFormat.format("{0}.{1}", cluster.getShortId(), this.domain));
            templateVariables.put("wireguard_client_public_keys", clientPublicKeys);
            templateVariables.put("wireguard_client_private_keys", clientPrivateKeys);
            templateVariables.put("wireguard_nat_public_key", natPublicKey);
            templateVariables.put("wireguard_nat_private_key", natPrivateKey);

            // Use cluster definition provided in request or load the default from the
            // template files
            if (clusterDefinition != null) {
                templateVariables.put("clusterDefinition", clusterDefinition);
            } else {
                templateVariables.put("clusterDefinition", new ObjectMapper().readValue(templatePath.resolve("cluster.json").toFile(), Map.class));
            }

            Files.createDirectory(workingDirectory);
            FileUtils.copyDirectory(templatePath.resolve("files/").toFile(), workingDirectory.toFile());

            processTemplates(cluster, workingDirectory, templateVariables);

        } catch (IOException | TemplateException e) {
            LOGGER.error("Working directory for cluster {} could not be created.", cluster.getId(), e);
            throw new RuntimeException(String.format("Working directory for cluster %s could not be created.", cluster.getId()));
        }
        return workingDirectory;
    }

    /**
     * Processes all the (Freemarker) template files in the newly created working
     * directory.
     * 
     * All files containing .fm will be processed and replaced by the same file
     * without the '.fm' part in the name
     * 
     * @param cluster           cluster metadata
     * @param workingDirectory  working dir
     * @param templateVariables (nested) map of variables to use while processing
     *                          template
     */
    private void processTemplates(Cluster cluster, Path workingDirectory, Map<String, Object> templateVariables) throws IOException, TemplateException {

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_30);
        cfg.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
        cfg.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        cfg.setSetting("number_format", "0.####");

        Files.walk(workingDirectory)
                .filter(Files::isRegularFile)
                .filter(path -> StringUtils.contains(path.getFileName().toString(), ".fm"))
                .forEach(path -> {
                    Path processedFile = path.getParent().resolve(StringUtils.replace(path.getFileName().toString(), ".fm", ""));
                    try {
                        InputStreamReader reader = new InputStreamReader(FileUtils.openInputStream(path.toFile()));
                        Template template = new Template(StringUtils.replace(path.getFileName().toString(), ".fm", ""), reader, cfg);
                        String processedTemplateContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, templateVariables);
                        Files.writeString(processedFile, processedTemplateContent);
                        Files.delete(path);
                    } catch (IOException | TemplateException e) {
                        LOGGER.error("Working directory for cluster {} could not be created.", cluster.getId(), e);
                        throw new RuntimeException(String.format("Working directory for cluster %s could not be created.", cluster.getId()));
                    }
                });
    }

    /**
     * Gets an (existing!) working dir for the given cluster
     * 
     * @param cluster cluster metadata
     * @return working directory path
     * @throws RuntimeException if directory does not exist
     */
    public Path getWorkingDirectory(Cluster cluster) {
        Path workingDirectory = workspaceDirectory.resolve(cluster.getId().toString());

        if (!Files.exists(workingDirectory) || !Files.isDirectory(workingDirectory)) {
            throw new RuntimeException(String.format("Working directory for cluster %s does not exist.", cluster.getId()));
        }

        return workingDirectory;
    }

}
