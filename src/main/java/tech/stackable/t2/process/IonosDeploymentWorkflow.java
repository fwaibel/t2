package tech.stackable.t2.process;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Profile("camunda")
@Component
public class IonosDeploymentWorkflow {

    private final ProcessEngine processEngine;

    public IonosDeploymentWorkflow(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @PostConstruct
    public void init() {

        WorkflowBuilder workflowBuilder = WorkflowBuilder.newWorkflow("dsl-ionos-deployment") //
                .springActivity("Terraform init", "terraformInitActivity") //
                .exclusiveGateway("init")
                .conditionalSpringActivity("ERROR", "#{terraformResult == 'ERROR'}", "Terraform init failed", "terraformInitFailedActivity")
                .moveToLastGateway()

                .conditionalSpringActivity("SUCCESS", "#{terraformResult != 'ERROR'}", "Terraform plan", "terraformPlanActivity")
                .exclusiveGateway("plan")
                .conditionalSpringActivity("ERROR", "#{terraformResult == 'ERROR'}", "Terraform plan failed", "terraformPlanFailedActivity")
                .moveToLastGateway()

                .conditionalSpringActivity("SUCCESS", "#{terraformResult != 'ERROR'}", "Terraform apply", "terraformApplyActivity")
                .exclusiveGateway("apply")
                .conditionalSpringActivity("ERROR", "#{terraformResult == 'ERROR'}", "Terraform apply failed", "terraformApplyFailedActivity")
                .moveToLastGateway()

                .conditionalSpringActivity("SUCCESS", "#{terraformResult != 'ERROR'}", "Ansible run", "ansibleRunActivity")
                .exclusiveGateway("run")
                .conditionalSpringActivity("ERROR", "#{ansibleResult == 'ERROR'}", "Ansible run failed", "ansibleRunFailedActivity")
                .springActivity("Terraform destroy", "terraformDestroyActivity") //
                .moveToLastGateway()

                .end() //
                ;
        BpmnModelInstance modelInstance = workflowBuilder.getModel();

        processEngine.getRepositoryService().createDeployment() //
                .addModelInstance("dsl-ionos-deployment.bpmn", modelInstance) //
                .deploy();

        // write to file for further inspection
        File file = new File("dsl-ionos-deployment.bpmn");
        Bpmn.writeModelToFile(file, modelInstance);
    }
}
