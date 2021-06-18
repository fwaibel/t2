package tech.stackable.t2.terraform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import tech.stackable.t2.process.ProcessLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Properties;

@Service
public class TerraformService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerraformService.class);

    private final Properties credentials;

    public TerraformService(@Qualifier("credentials") Properties credentials) {
        this.credentials = credentials;
    }

    public TerraformResult init(Path workingDirectory, String datacenter) {
        LOGGER.info("Running Terraform init on {}", workingDirectory);
        int result = this.callTerraform(workingDirectory, datacenter, "init", "-input=false");
        return result == 0 ? TerraformResult.SUCCESS : TerraformResult.ERROR;
    }

    public TerraformResult plan(Path workingDirectory, String datacenter) {
        LOGGER.info("Running Terraform plan on {}", workingDirectory);
        int result = this.callTerraform(workingDirectory, datacenter, "plan", "-detailed-exitcode -input=false");
        switch (result) {
        case 0:
            return TerraformResult.SUCCESS;
        case 2:
            return TerraformResult.CHANGES_PRESENT;
        default:
            return TerraformResult.ERROR;
        }
    }

    public TerraformResult apply(Path workingDirectory, String datacenter) {
        LOGGER.info("Running Terraform apply on {}", workingDirectory);
        int result = this.callTerraform(workingDirectory, datacenter, "apply", "-auto-approve -input=false");
        return result == 0 ? TerraformResult.SUCCESS : TerraformResult.ERROR;
    }

    public TerraformResult destroy(Path workingDirectory, String datacenter) {
        LOGGER.info("Running Terraform destroy on {}", workingDirectory);
        int result = this.callTerraform(workingDirectory, datacenter, "destroy", "-auto-approve");
        return result == 0 ? TerraformResult.SUCCESS : TerraformResult.ERROR;
    }

    public String getIpV4(Path workingDirectory) {
        try {
            return Files.readString(workingDirectory.resolve("ipv4"));
        } catch (IOException e) {
            LOGGER.error("IPv4 Address for Cluster with TF file {} could not be read.", workingDirectory, e);
            return null;
        }
    }

    private int callTerraform(Path workingDirectory, String datacenter, String command, String params) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder()
                    .command("sh", "-c", String.format("terraform %s %s -no-color", command, params))
                    .directory(workingDirectory.toFile());
            this.credentials.forEach((key, value) ->
                    processBuilder.environment().put(String.format("TF_VAR_%s", key), value.toString()));
            processBuilder.environment().put("TF_VAR_ionos_datacenter", datacenter);
            Process process = processBuilder.redirectErrorStream(true).start();
            ProcessLogger outLogger = ProcessLogger.start(process.getInputStream(), workingDirectory.resolve("cluster.log"), MessageFormat.format("terraform-{0}", command));
            int exitCode = process.waitFor();
            outLogger.stop();
            return exitCode;
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error while calling terraform", e);
            throw new RuntimeException("Error while calling terraform", e);
        }
    }

}
