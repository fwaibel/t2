package tech.stackable.t2;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "camunda.bpm.generate-unique-process-engine-name=true",
                // this is only needed if a SpringBootProcessApplication is used for the test
                "camunda.bpm.generate-unique-process-application-name=true",
                "spring.datasource.generate-unique-name=true",
        }
)
class T2ServerProvisioningWorkflowTests {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Test
    void contextLoads() {
        assertThat(processEngine).isNotNull();
    }

    @Test
    void processesAreLoaded() {
        ProcessDefinition pd = processEngine.getRepositoryService().
                createProcessDefinitionQuery()
                .processDefinitionKey("default-ionos-deployment").singleResult();

        assertThat(pd).isNotNull();
    }

    @Test
    void defaultIonosDeploymentWorkflow() {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("default-ionos-deployment");

        ProcessInstance actual = runtimeService.createProcessInstanceQuery()
                .processInstanceId(pi.getId()).singleResult();

        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    }
}
