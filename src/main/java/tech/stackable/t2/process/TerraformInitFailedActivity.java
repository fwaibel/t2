package tech.stackable.t2.process;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("camunda")
public class TerraformInitFailedActivity implements JavaDelegate {
    @Override
    public void execute(DelegateExecution ctx) throws Exception {
        System.out.println("Terraform init failed :(");
    }
}
