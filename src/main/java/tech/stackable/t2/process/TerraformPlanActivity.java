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
public class TerraformPlanActivity implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerraformPlanActivity.class);

    private final TerraformService terraformService;

    public TerraformPlanActivity(TerraformService terraformService) {
        this.terraformService = terraformService;
    }

    @Override
    public void execute(DelegateExecution ctx) throws Exception {
        T2ProcessVariables vars = new T2ProcessVariables(ctx);

        LOGGER.info("Calling terraform service plan...");
        TerraformResult terraformResult = terraformService.plan(vars.getWorkingDirectory(), vars.getDatacenter());
        LOGGER.info("Operation result is {}", terraformResult);
        vars.setTerraformResult(terraformResult);
    }

}
