package tech.stackable.t2.process;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class TerraformAdapter implements JavaDelegate {

    @Override
    public void execute(DelegateExecution ctx) throws Exception {

        System.out.println("terraform adapter '" + ctx.getVariable("name") + "'");
    }

}
