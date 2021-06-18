package tech.stackable.t2.process;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import tech.stackable.t2.terraform.TerraformService;

@Component
public class TerraformActivity implements JavaDelegate {

    public static final String VAR_WORKING_DIRECTORY = "workingDirectory";
    public static final String VAR_DATACENTER = "datacenter";

    private final TerraformService terraformService;

    public TerraformActivity(TerraformService terraformService) {
        this.terraformService = terraformService;
    }

    @Override
    public void execute(DelegateExecution ctx) throws Exception {
        T2ProcessVariables vars = new T2ProcessVariables(ctx);

        terraformService.init(vars.getWorkingDirectory(), vars.getDatacenter());
    }

}
