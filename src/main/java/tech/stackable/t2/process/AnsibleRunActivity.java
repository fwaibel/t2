package tech.stackable.t2.process;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tech.stackable.t2.ansible.AnsibleResult;
import tech.stackable.t2.ansible.AnsibleService;

@Component
@Profile("camunda")
public class AnsibleRunActivity implements JavaDelegate {

    public static final String VAR_ANSIBLE_RESULT = "ansibleResult";

    private final AnsibleService ansibleService;

    public AnsibleRunActivity(AnsibleService ansibleService) {
        this.ansibleService = ansibleService;
    }

    @Override
    public void execute(DelegateExecution ctx) throws Exception {
        T2ProcessVariables vars = new T2ProcessVariables(ctx);

        AnsibleResult ansibleResult = ansibleService.run(vars.getWorkingDirectory());
        vars.setAnsibleResult(ansibleResult);
    }

}
