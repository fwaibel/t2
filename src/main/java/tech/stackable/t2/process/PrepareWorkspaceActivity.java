package tech.stackable.t2.process;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tech.stackable.t2.templates.TemplateService;
import tech.stackable.t2.terraform.TerraformResult;

@Component
@Profile("camunda")
public class PrepareWorkspaceActivity implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerraformInitActivity.class);

    public static final String VAR_CLUSTER_DEFINITION = "cluster-definition";

    private final TemplateService templateService;

    public PrepareWorkspaceActivity(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Override
    public void execute(DelegateExecution ctx) throws Exception {
        T2ProcessVariables vars = new T2ProcessVariables(ctx);

        TerraformResult terraformResult = templateService.createWorkingDirectory(
                vars.getWorkingDirectory(), vars.getDatacenter());
        vars.setTerraformResult(terraformResult);
    }

}
