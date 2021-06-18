package tech.stackable.t2.process;

import org.camunda.bpm.engine.delegate.VariableScope;

import java.nio.file.Path;
import java.nio.file.Paths;

public class T2ProcessVariables {

    private VariableScope variableScope;

    public T2ProcessVariables(VariableScope variableScope) {
        this.variableScope = variableScope;
    }

    public String getDatacenter() {
        return (String) variableScope.getVariable(TerraformActivity.VAR_DATACENTER);
    }

    public void setDatacenter(String datacenter) {
        variableScope.setVariable(TerraformActivity.VAR_DATACENTER, datacenter);
    }

    public Path getWorkingDirectory() {
        return Paths.get((String) variableScope.getVariable(TerraformActivity.VAR_WORKING_DIRECTORY));
    }

    public void setWorkingDirectory(Path workingDirectory) {
        variableScope.setVariable(TerraformActivity.VAR_WORKING_DIRECTORY, workingDirectory.toFile().getAbsolutePath());
    }

}