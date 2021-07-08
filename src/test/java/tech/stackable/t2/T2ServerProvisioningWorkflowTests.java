package tech.stackable.t2;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import tech.stackable.t2.ansible.AnsibleResult;
import tech.stackable.t2.ansible.AnsibleService;
import tech.stackable.t2.terraform.TerraformResult;
import tech.stackable.t2.terraform.TerraformService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;
import static tech.stackable.t2.process.AnsibleRunActivity.VAR_ANSIBLE_RESULT;
import static tech.stackable.t2.process.TerraformInitActivity.*;

@ActiveProfiles("camunda")
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

    @MockBean
    private AnsibleService ansibleService;

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

        assertThat(pi.isEnded()).isTrue();

        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    }

    @Test
    void ionosDslHappyPathExample() {
        when(terraformService.init(notNull(), notNull())).thenReturn(TerraformResult.SUCCESS);
        when(terraformService.plan(notNull(), notNull())).thenReturn(TerraformResult.SUCCESS);
        when(terraformService.apply(notNull(), notNull())).thenReturn(TerraformResult.SUCCESS);
        when(ansibleService.run(notNull())).thenReturn(AnsibleResult.SUCCESS);

        Map<String, Object> params = new HashMap<>();
        params.put(VAR_WORKING_DIRECTORY, "/tmp");
        params.put(VAR_DATACENTER, "datacenter-junit");
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("dsl-ionos-deployment", params);

        verify(terraformService, times(1)).init(notNull(), eq("datacenter-junit"));
        verify(terraformService, times(1)).plan(notNull(), eq("datacenter-junit"));
        verify(terraformService, times(1)).apply(notNull(), eq("datacenter-junit"));
        verify(ansibleService, times(1)).run(notNull());

        assertThat(pi.isEnded()).isTrue();
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

        runtimeService.createVariableInstanceQuery().processInstanceIdIn(pi.getId()).list();

        assertThat(processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId()).variableName(VAR_TERRAFORM_RESULT).singleResult().getValue()).isEqualTo("SUCCESS");
        assertThat(processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId()).variableName(VAR_ANSIBLE_RESULT).singleResult().getValue()).isEqualTo("SUCCESS");
    }

    @Test
    void ionosDslTerraformInitFails() {
        when(terraformService.init(notNull(), notNull())).thenReturn(TerraformResult.ERROR);

        Map<String, Object> params = new HashMap<>();
        params.put(VAR_WORKING_DIRECTORY, "/tmp");
        params.put(VAR_DATACENTER, "datacenter-junit");
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("dsl-ionos-deployment", params);

        verify(terraformService, times(1)).init(notNull(), eq("datacenter-junit"));
        verify(terraformService, times(0)).plan(notNull(), eq("datacenter-junit"));
        verify(terraformService, times(0)).apply(notNull(), eq("datacenter-junit"));

        assertThat(pi.isEnded()).isTrue();
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);

        assertThat(processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId()).variableName(VAR_TERRAFORM_RESULT).singleResult().getValue()).isEqualTo("ERROR");
        assertThat(processEngine.getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(pi.getId()).variableName(VAR_ANSIBLE_RESULT).singleResult()).isNull();
    }

    @Test
    void ionosDslTerraformPlanFails() {
        when(terraformService.init(notNull(), notNull())).thenReturn(TerraformResult.SUCCESS);
        when(terraformService.plan(notNull(), notNull())).thenReturn(TerraformResult.ERROR);

        Map<String, Object> params = new HashMap<>();
        params.put(VAR_WORKING_DIRECTORY, "/tmp");
        params.put(VAR_DATACENTER, "datacenter-junit");
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("dsl-ionos-deployment", params);

        verify(terraformService, times(1)).init(notNull(), eq("datacenter-junit"));
        verify(terraformService, times(1)).plan(notNull(), eq("datacenter-junit"));
        verify(terraformService, times(0)).apply(notNull(), eq("datacenter-junit"));

        assertThat(pi.isEnded()).isTrue();
        assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(0);
    }
}
