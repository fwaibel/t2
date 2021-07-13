package tech.stackable.t2;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static tech.stackable.t2.process.T2ProcessVariables.VAR_DRY_RUN;
import static tech.stackable.t2.process.TerraformInitActivity.VAR_DATACENTER;
import static tech.stackable.t2.process.TerraformInitActivity.VAR_WORKING_DIRECTORY;

@Profile("cli")
@Component
@CommandLine.Command(description = "T2 command-line runner",
        name = "t2",
        mixinStandardHelpOptions = true,
        version = "0.1.0")
public class T2Command implements Callable<Void> {

    @Autowired
    private RuntimeService runtimeService;

    @CommandLine.Option(names = "--dry-run", paramLabel = "DRY_RUN", description = "Make a dry-run. False by default.")
    private boolean dryRun;

    @CommandLine.Option(names = { "-w", "--working-directory" }, paramLabel = "WORKING_DIRECTORY", description = "the working directory")
    private File workingDirectory = new File("/tmp");

    @CommandLine.Option(names = { "-k", "--process-instance-key"}, paramLabel = "PROCESS_INSTANCE_KEY", description = "Process instance key")
    private String processInstanceKey = "dsl-ionos-deployment";

    @CommandLine.Parameters(index = "0", paramLabel = "Datacenter", description = "Name of the datacenter.\nImportant: Should not contain *non* T2 nodes!")
    private String datacenter;

    @CommandLine.Parameters(index = "1", paramLabel = "Cluster Definition", description = "YAML file containing the cluster definition.")
    private File clusterDefinition;

    @Override
    public Void call() {
        Map<String, Object> params = new HashMap<>();
        params.put(VAR_DRY_RUN, dryRun);
        params.put(VAR_WORKING_DIRECTORY, workingDirectory.getAbsolutePath());
        params.put(VAR_DATACENTER, datacenter);
        params.put()
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(processInstanceKey, params);

        if (pi.isEnded()) {
            System.exit(0);
        } else {
            System.exit(1);
        }

        return null;
    }
}