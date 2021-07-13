package tech.stackable.t2.process;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tech.stackable.t2.terraform.TerraformResult;
import tech.stackable.t2.terraform.TerraformService;

@Component
@Profile("camunda")
public class TerraformInitActivity implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerraformInitActivity.class);

    public static final String VAR_WORKING_DIRECTORY = "workingDirectory";
    public static final String VAR_DATACENTER = "datacenter";
    public static final String VAR_TERRAFORM_RESULT = "terraformResult";

    private final TerraformService terraformService;

    public TerraformInitActivity(TerraformService terraformService) {
        this.terraformService = terraformService;
    }

    @Override
    public void execute(DelegateExecution ctx) throws Exception {
        T2ProcessVariables vars = new T2ProcessVariables(ctx);

        LOGGER.info("Calling terraform service init...");
        TerraformResult terraformResult = terraformService.init(vars.getWorkingDirectory(), vars.getDatacenter());
        LOGGER.info("Operation result is {}", terraformResult);
        vars.setTerraformResult(terraformResult);
    }

}
