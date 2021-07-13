package tech.stackable.t2.process;

import org.camunda.bpm.engine.delegate.VariableScope;
import tech.stackable.t2.ansible.AnsibleResult;
import tech.stackable.t2.api.cluster.YamlClusterDefinitionReader;
import tech.stackable.t2.terraform.TerraformResult;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static tech.stackable.t2.process.AnsibleRunActivity.VAR_ANSIBLE_RESULT;
import static tech.stackable.t2.process.TerraformInitActivity.VAR_TERRAFORM_RESULT;

public class T2ProcessVariables {

    public static final String VAR_DRY_RUN = "dryRun";

    private final VariableScope variableScope;

    public T2ProcessVariables(VariableScope variableScope) {
        this.variableScope = variableScope;
    }

    public String getDatacenter() {
        return (String) variableScope.getVariable(TerraformInitActivity.VAR_DATACENTER);
    }

    public void setDatacenter(String datacenter) {
        variableScope.setVariable(TerraformInitActivity.VAR_DATACENTER, datacenter);
    }

    public Map<String, Object> getClusterDefinition() {
        String clusterDefinition = (String) variableScope.getVariable(PrepareWorkspaceActivity.VAR_CLUSTER_DEFINITION);
        return new YamlClusterDefinitionReader().convert(clusterDefinition);
    }

    public void setClusterDefinition(Map<String, Object> clusterDefinition) {
        variableScope.setVariable(PrepareWorkspaceActivity.VAR_CLUSTER_DEFINITION, clusterDefinition);
    }

    public Path getWorkingDirectory() {
        return Paths.get((String) variableScope.getVariable(TerraformInitActivity.VAR_WORKING_DIRECTORY));
    }

    public void setWorkingDirectory(Path workingDirectory) {
        variableScope.setVariable(TerraformInitActivity.VAR_WORKING_DIRECTORY, workingDirectory.toFile().getAbsolutePath());
    }

    public boolean isDryRun() {
        return Boolean.parseBoolean((String) variableScope.getVariable(VAR_DRY_RUN));
    }

    public void setDryRun(boolean dryRun) {
        variableScope.setVariable(VAR_DRY_RUN, dryRun);
    }

    TerraformResult getTerraformResult() {
        return TerraformResult.valueOf((String) variableScope.getVariable(VAR_TERRAFORM_RESULT));
    }

    public void setTerraformResult(TerraformResult terraformResult) {
        variableScope.setVariable(VAR_TERRAFORM_RESULT, terraformResult.toString());
    }

    AnsibleResult getAnsibleResult() {
        return AnsibleResult.valueOf((String) variableScope.getVariable(VAR_ANSIBLE_RESULT));
    }

    public void setAnsibleResult(AnsibleResult ansibleResult) {
        variableScope.setVariable(VAR_ANSIBLE_RESULT, ansibleResult.toString());
    }
}
