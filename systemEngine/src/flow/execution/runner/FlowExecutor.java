package flow.execution.runner;


import dto.DTOFreeInputsFromUser;
import flow.execution.FlowExecutionResult;
import statistic.FlowAndStepStatisticData;
import statistic.StatisticData;
import steps.api.StepResult;
import flow.api.StepUsageDeclaration;
import flow.execution.FlowExecution;
import flow.execution.context.StepExecutionContext;
import flow.execution.context.StepExecutionContextImpl;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public class FlowExecutor {

    public void executeFlow(FlowExecution flowExecution, DTOFreeInputsFromUser freeInputs, FlowAndStepStatisticData flowStatisticData) {
        System.out.println("Starting execution of flow " + flowExecution.getFlowDefinition().getName() + " [ID: " + flowExecution.getUniqueId() + "]");
        Instant startTime = Instant.now();
        flowExecution.setStartTime(startTime);

        StepExecutionContext context = new StepExecutionContextImpl
                (flowExecution.getFlowDefinition().getStepAndIOName2DDMap(), flowExecution.getFlowDefinition().getOutputName2aliasMap(),
                        flowExecution.getFlowDefinition().getInputName2aliasMap(), flowExecution.getFlowDefinition().getAlias2StepNameMap(),
                        flowExecution.getIOlist(), flowExecution.getFlowDefinition().getName2DDMap(), flowExecution.getFlowDefinition().getName2AliasMap());

        for (Map.Entry<String, Object> entry : freeInputs.getFreeInputMap().entrySet()) {
            context.storeDataValue(entry.getKey(), entry.getValue());
        }

        // start actual execution
        for (int i = 0; i < flowExecution.getFlowDefinition().getFlowSteps().size(); i++) {
            context.setCurrInvokingStep(flowExecution.getFlowDefinition().getFlowSteps().get(i).getFinalStepName(), flowExecution.getFlowDefinition().getFlowSteps().get(i).getStepDefinition().name());
            StepUsageDeclaration stepUsageDeclaration = flowExecution.getFlowDefinition().getFlowSteps().get(i);
            StepResult stepResult = stepUsageDeclaration.getStepDefinition().invoke(context);

            if (stepResult.equals(StepResult.FAILURE)) {
                flowExecution.setFlowExecutionResult(FlowExecutionResult.WARNING);
                StatisticData stepStatistic = new StatisticData(flowExecution.getFlowDefinition().getFlowSteps().get(i).getStepDefinition().name(), context.getCurrInvokingStep().getTotalStepTime());
                updateStepStatisticData(flowStatisticData, stepStatistic);
                if (!(stepUsageDeclaration.skipIfFail())) {
                    flowExecution.setFlowExecutionResult(FlowExecutionResult.FAILURE);
                    break;
                }
            } else if (stepResult.equals(StepResult.WARNING)) {
                flowExecution.setFlowExecutionResult(FlowExecutionResult.WARNING);
                StatisticData stepStatistic = new StatisticData(flowExecution.getFlowDefinition().getFlowSteps().get(i).getStepDefinition().name(), context.getCurrInvokingStep().getTotalStepTime());
                updateStepStatisticData(flowStatisticData, stepStatistic);
            } else {
                flowExecution.setFlowExecutionResult(FlowExecutionResult.SUCCESS);
                StatisticData stepStatistic = new StatisticData(flowExecution.getFlowDefinition().getFlowSteps().get(i).getStepDefinition().name(), context.getCurrInvokingStep().getTotalStepTime());
                updateStepStatisticData(flowStatisticData, stepStatistic);
            }

            context.setStepResultToCurrInvokingStep(stepResult);
            context.addCurrInvokingStepToStepExecutionList();

            flowExecution.setDataValues(context.getDataValues());
            flowExecution.setStepExecutionDataList(context.getStepExecutionList());
        }
        //statistics
        Instant endTime = Instant.now();
        flowExecution.setEndTime(endTime);
        Duration totalTime = Duration.between(startTime, endTime);
        long totalTimeMillis = totalTime.toMillis();
        flowExecution.setTotalTime(Duration.ofMillis(totalTimeMillis));
        StatisticData currFlowStatistic = new StatisticData(flowExecution.getFlowDefinition().getName(), totalTime);
        updateFlowStatisticData(flowStatisticData, currFlowStatistic, totalTime);
    }

    void updateStepStatisticData (FlowAndStepStatisticData flowStatisticData, StatisticData stepStatistic){
        if (flowStatisticData.getStepsStatisticData().stream().noneMatch(step -> step.getName().equals(stepStatistic.getName()))) {
            flowStatisticData.addStepToStatistic(stepStatistic);
        } else {
            flowStatisticData.getStepsStatisticData().stream()
                    .filter(step -> step.getName().equals(stepStatistic.getName()))
                    .findFirst()
                    .get()
                    .incrementTimesRun();
            flowStatisticData.getStepsStatisticData().stream()
                    .filter(step -> step.getName().equals(stepStatistic.getName()))
                    .findFirst()
                    .get()
                    .addToTotalTime(stepStatistic.getTotalTime());
        }
    }
    void updateFlowStatisticData (FlowAndStepStatisticData flowStatisticData, StatisticData currFlowStatistic, Duration totalTime){
        if ((flowStatisticData.getFlowsStatisticData().stream().noneMatch(flow -> flow.getName().equals(currFlowStatistic.getName())))) {
            flowStatisticData.addFlowToStatistic(currFlowStatistic);
        } else {
            flowStatisticData.getFlowsStatisticData().stream()
                    .filter(step -> step.getName().equals(currFlowStatistic.getName()))
                    .findFirst()
                    .get()
                    .incrementTimesRun();
            flowStatisticData.getFlowsStatisticData().stream()
                    .filter(step -> step.getName().equals(currFlowStatistic.getName()))
                    .findFirst()
                    .get()
                    .addToTotalTime(totalTime);
        }
    }
}
