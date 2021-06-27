package tech.stackable.t2.process;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tech.stackable.t2.terraform.TerraformResult;
import tech.stackable.t2.terraform.TerraformService;

@Component
@Profile("camunda")
public class TerraformApplyActivity implements JavaDelegate {

    private final TerraformService terraformService;

    public TerraformApplyActivity(TerraformService terraformService) {
        this.terraformService = terraformService;
    }

    @Override
    public void execute(DelegateExecution ctx) throws Exception {
        T2ProcessVariables vars = new T2ProcessVariables(ctx);

        TerraformResult terraformResult = terraformService.apply(vars.getWorkingDirectory(), vars.getDatacenter());
        vars.setTerraformResult(terraformResult);
    }

}
