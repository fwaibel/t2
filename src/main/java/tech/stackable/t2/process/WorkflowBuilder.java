package tech.stackable.t2.process;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractActivityBuilder;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;

public class WorkflowBuilder {

    private final String name;

    @SuppressWarnings("rawtypes")
    private AbstractFlowNodeBuilder nodeBuilder;
    private BpmnModelInstance bpmnModelInstance;
    private ProcessBuilder process;

    public WorkflowBuilder(String name) {
        this.name = name;
    }

    public static WorkflowBuilder newWorkflow(String name) {
        WorkflowBuilder builder = new WorkflowBuilder(name);
        return builder.start();
    }

    public BpmnModelInstance getModel() {
        if (bpmnModelInstance == null) {
            bpmnModelInstance = nodeBuilder.done();
        }
        return bpmnModelInstance;
    }

    public WorkflowBuilder start() {
        process = Bpmn.createExecutableProcess(name);
        nodeBuilder = process.startEvent("Start-" + name);
        return this;
    }

    public WorkflowBuilder end() {
        nodeBuilder = nodeBuilder.endEvent("End-" + name);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public WorkflowBuilder activity(String name, Class adapterClass) {
        // this is very handy and could also be done inline above directly
        String id = "Activity-" + name.replace(" ", "-"); // risky thing ;-)
        nodeBuilder = nodeBuilder.serviceTask(id).name(name).camundaClass(adapterClass.getName());
        return this;
    }

    public WorkflowBuilder springActivity(String name, String beanName) {
        // this is very handy and could also be done inline above directly
        String id = "Activity-" + name.replace(" ", "-"); // risky thing ;-)
//        saga = saga.serviceTask(id).name(name).camundaDelegateExpression("#{" + beanName+ ".execute(execution)}");
        nodeBuilder = nodeBuilder.serviceTask(id).name(name).camundaDelegateExpression("#{" + beanName+ "}");
        return this;
    }

    public WorkflowBuilder exclusiveGateway(String name) {
        String id = "ExclusiveGateway-" + name.replace(" ", "-"); // risky thing ;-)
        nodeBuilder = nodeBuilder.exclusiveGateway(id).name(name);
        return this;
    }

    public WorkflowBuilder endGateway(String name) {
        nodeBuilder = nodeBuilder.endEvent("End-" + name);
        return this;
    }

    public WorkflowBuilder moveToLastGateway() {
        nodeBuilder = nodeBuilder.moveToLastGateway();
        return this;
    }

    public WorkflowBuilder conditionalSpringActivity(String conditionName, String condition, String name, String beanName) {
        if (!(nodeBuilder instanceof AbstractFlowNodeBuilder)) {
            throw new RuntimeException("Conditional activity can only be specified right after exclusive gateway");
        }
        nodeBuilder.condition(conditionName, condition);
        springActivity(name, beanName);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public WorkflowBuilder compensationActivity(String name, Class adapterClass) {
        if (!(nodeBuilder instanceof AbstractActivityBuilder)) {
            throw new RuntimeException("Compensation activity can only be specified right after activity");
        }

        String id = "Activity-" + name.replace(" ", "-") + "-compensation"; // risky thing ;-)

        ((AbstractActivityBuilder) nodeBuilder)
                .boundaryEvent()
                .compensateEventDefinition()
                .compensateEventDefinitionDone()
                .compensationStart()
                .serviceTask(id).name(name).camundaClass(adapterClass.getName())
                .compensationDone();

        return this;
    }

    public WorkflowBuilder triggerCompensationOnAnyError() {
        process.eventSubProcess()
                .startEvent("ErrorCaught").error("java.lang.Throwable")
                .intermediateThrowEvent("ToBeCompensated").compensateEventDefinition().compensateEventDefinitionDone()
                .endEvent("ErrorHandled");

        return this;
    }

}
