package tech.stackable.t2;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import tech.stackable.t2.process.SagaBuilder;
import tech.stackable.t2.process.TerraformAdapter;
import tech.stackable.t2.terraform.TerraformResult;
import tech.stackable.t2.terraform.TerraformService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;
import static tech.stackable.t2.process.TerraformActivity.VAR_DATACENTER;
import static tech.stackable.t2.process.TerraformActivity.VAR_WORKING_DIRECTORY;

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

    @MockBean
    private TerraformService terraformService;

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

        assertThat(actual.isEnded()).isTrue();

        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    }

    @Test
    void ionosDslExample() {
        SagaBuilder saga = SagaBuilder.newSaga("dsl-ionos-deployment") //
                .activity("terraformAdapter", TerraformAdapter.class) //
                .springActivity("terraformBean", "terraformActivity") //
//                .compensationActivity("Cancel car", CancelCarAdapter.class) //
//                .activity("Book hotel", BookHotelAdapter.class) //
//                .compensationActivity("Cancel hotel", CancelHotelAdapter.class) //
//                .activity("Book flight", BookFlightAdapter.class) //
//                .compensationActivity("Cancel flight", CancelFlightAdapter.class) //
                .end() //
//                .triggerCompensationOnAnyError()
                ;

        processEngine.getRepositoryService().createDeployment() //
                .addModelInstance("dsl-ionos-deployment.bpmn", saga.getModel()) //
                .deploy();

        // write to file for further inspection
        File file = new File("dsl-ionos-deployment.bpmn");
        Bpmn.writeModelToFile(file, saga.getModel());

        when(terraformService.init(notNull(), notNull())).thenReturn(TerraformResult.SUCCESS);

        Map<String, Object> params = new HashMap<>();
        params.put(VAR_WORKING_DIRECTORY, "/tmp");
        params.put(VAR_DATACENTER, "datacenter-junit");
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("dsl-ionos-deployment", params);

        verify(terraformService, times(1)).init(notNull(), eq("datacenter-junit"));

        assertThat(pi.isEnded()).isTrue();
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    }
}
