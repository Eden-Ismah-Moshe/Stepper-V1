package flow.api;

import datadefinition.api.DataDefinitions;
import exceptions.*;
import flow.mapping.FlowCustomMapping;
import statistic.StatisticData;
import steps.api.DataDefinitionDeclaration;
import flow.mapping.FlowAutomaticMapping;
import flow.api.FlowIO.IO;
import flow.api.FlowIO.SingleFlowIOData;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class FlowDefinitionImpl implements FlowDefinition, Serializable {
    private final String name;
    private final String description;
    private final List<String> flowOutputs;
    private final List<StepUsageDeclaration> steps;
    private boolean isFlowReadOnly;
    private final Map<String, DataDefinitions> stepAndIOName2DD;
    private final Map<String, DataDefinitions> name2DD;
    private final Map<String, String> name2Alias;
    private final Map<String, String> InputName2Alias;
    private final Map<String, String> OutputName2Alias;
    private final Map<String, String> MapAlias2StepName;
    private final List<SingleFlowIOData> IOlist;
    private final List<CustomMapping> customMapping;
    private final List<SingleFlowIOData> freeInputs;
    private final StatisticData flowStatisticData;

    public FlowDefinitionImpl(String name, String description) {
        this.name = name;
        this.description = description;
        this.flowOutputs = new ArrayList<>();
        this.isFlowReadOnly = true;
        this.steps = new LinkedList<>();
        this.stepAndIOName2DD = new HashMap<>();
        this.name2DD = new HashMap<>();
        this.name2Alias = new HashMap<>();
        this.IOlist = new ArrayList<>();
        this.MapAlias2StepName = new HashMap<>();
        this.InputName2Alias = new HashMap<>();
        this.OutputName2Alias = new HashMap<>();
        this.customMapping = new ArrayList<>();
        this.freeInputs = new LinkedList<>();
        this.flowStatisticData = new StatisticData(name); ///maybe without name
    }

    @Override
    public String getName() { return name;}
    @Override
    public String getDescription() {
        return description;
    }
    @Override
    public List<String> getFlowFormalOutputs() {
        return flowOutputs;
    }
    @Override
    public List<StepUsageDeclaration> getFlowSteps() { return steps;}
    @Override
    public boolean getFlowReadOnly() {
        return steps.stream().noneMatch(step ->(!step.getStepDefinition().isReadonly()));
    }
    @Override
    public Map<String, DataDefinitions> getStepAndIOName2DDMap() { return stepAndIOName2DD;}

    //newwwwwwwwwwwwww
    @Override
    public Map<String, DataDefinitions> getName2DDMap() { return name2DD;}

    @Override
    public Map<String, String> getName2AliasMap() { return name2Alias;}
    @Override
    public String getInputAliasFromMap(String stepName, String originalInputName) {
        return InputName2Alias.get(stepName + "." + originalInputName);
    }
    @Override
    public String getOutputAliasFromMap(String stepName, String originalOutputName) {
        return OutputName2Alias.get(stepName + "." + originalOutputName);
    }
    @Override
    public DataDefinitions getDDFromMap(String stepName, String InputName) {
        String key = stepName  + "." + InputName;
        return stepAndIOName2DD.get(key);}
    @Override
    public Map<String, String> getAlias2StepNameMap() {
        return MapAlias2StepName;
    }
    @Override
    public Map<String, String> getInputName2aliasMap() {
        return InputName2Alias;
    }
    @Override
    public Map<String, String> getOutputName2aliasMap() {
        return OutputName2Alias;
    }
    @Override
    public List<SingleFlowIOData> getIOlist() {
        return IOlist;
    }
    @Override
    public void addElementToIoList(SingleFlowIOData IOElement) {
        IOlist.add(IOElement);
    }
    @Override
    public List<SingleFlowIOData> getFlowFreeInputs() {return this.freeInputs;}
    @Override
    public List<CustomMapping> getCustomMappingList(){
        return customMapping;
    }
    @Override
    public List<SingleFlowIOData> getMandatoryInputsList(){
        return freeInputs;
    }
    @Override
    public List<String> getListOfStepsWithCurrInput(String inputName){
        List<String> stepsWithCurrInput = new LinkedList<>();

        for(SingleFlowIOData input : freeInputs){
            if(input.getFinalName() == inputName){
                stepsWithCurrInput.add(input.getStepName());
            }
        }
        return stepsWithCurrInput;
    }
    @Override
    public SingleFlowIOData getElementFromIOList(String stepName, String dataName) {
        for (SingleFlowIOData obj : IOlist) {
            if (obj.getStepName().equals(stepName) && obj.getFinalName().equals(dataName)) {
                return obj;
            }
        }
        return null;
    }
    @Override
    public void setFlowReadOnly() {
        this.isFlowReadOnly = checkIfFlowIsReadOnly();
    }
    @Override
    public void addStepToFlow(StepUsageDeclaration stepUsageDeclaration) {
        steps.add(stepUsageDeclaration);
    }
    @Override
     public void addFlowOutput(String outputName) {
        flowOutputs.add(outputName);
    }
    @Override
    public void addToStepAndIOName2DDMap(String stepName,String inputName ,DataDefinitions DD) {
        String result = stepName + "." + inputName;
        stepAndIOName2DD.put(result, DD);
    }
    //newwwwwwwwwww
    @Override
    public void addToName2DDMap(String inputName ,DataDefinitions DD) {
       name2DD.put(inputName, DD);
    }
    @Override
    public void addToName2AliasMap(String inputName ,String alias) {
        name2Alias.put(inputName,alias);
    }

    @Override
    public void addToInputName2AliasMap(String stepName, String inputName, String alias) {
        String result = stepName + "." + inputName;
        InputName2Alias.put(result, alias);
    }
    @Override
    public void addToOutputName2AliasMap(String stepName, String outputName, String alias) {
        String result = stepName + "." + outputName;
        OutputName2Alias.put(result, alias);
    }
    @Override
    public void addToAlias2StepNameMap(String stepName, String alias) {
        MapAlias2StepName.put(stepName, alias);
    }
    @Override
    public void addToCustomMapping(CustomMapping obj){
        customMapping.add(obj);
    }
    @Override
    public void addToMandatoryInputsList(SingleFlowIOData mandatoryInput){
        freeInputs.add(mandatoryInput);
    }
    @Override
    public boolean checkIfFlowIsReadOnly() {
        return steps.stream().anyMatch(step -> !(step.getStepDefinition().isReadonly()));
    }
    @Override
    public boolean stepExist(String stepName){
        boolean isPresent =
                getFlowSteps()
                        .stream()
                        .anyMatch(name -> name.getFinalStepName().equals(stepName));

        if (!isPresent) {
          return false;
        }
        return true;
    }
    @Override
    public boolean dataExist(String stepName, String dataName) {

        Map<String, String> inputs = InputName2Alias.keySet().stream()
                .filter(key -> key.startsWith(stepName))
                .collect(Collectors.toMap(key -> key, key -> InputName2Alias.get(key)));
        boolean isPresentInInput = inputs.values().stream().anyMatch(data -> data.equals(dataName));


        Map<String, String> outputs = OutputName2Alias.keySet().stream()
                .filter(key -> key.startsWith(stepName))
                .collect(Collectors.toMap(key -> key, key -> OutputName2Alias.get(key)));
        boolean isPresentInOutput = outputs.values().stream().anyMatch(data -> data.equals(dataName));

        if (!isPresentInInput && !isPresentInOutput) {
            return false;
        }
        return true;
    }

    @Override
    public void validateIfOutputsHaveSameName() throws OutputsWithSameName{
        boolean isPresent =
                flowOutputs
                        .stream()
                        .anyMatch(name -> Collections
                                .frequency(flowOutputs, name) > 1);

        if (isPresent) {
            throw new OutputsWithSameName();
        }
    }
    @Override
    public void flowOutputsIsNotExists() throws UnExistsOutput {

        if(!(getFlowFormalOutputs().get(0).equals(""))) {
            for (String output : getFlowFormalOutputs()) {
                boolean isPresent = getIOlist()
                        .stream()
                        .anyMatch(data -> data.getFinalName().equals(output));
                if (!isPresent) {
                    throw new UnExistsOutput();
                }
            }
        }
    }
    @Override
    public void freeInputsWithSameNameAndDifferentType() throws FreeInputsWithSameNameAndDifferentType{
        for (SingleFlowIOData currData :  freeInputs) {
            boolean isPresent =
                    freeInputs
                            .stream()
                            .filter(data -> data.getFinalName().equals(currData.getFinalName()))
                            .anyMatch(data -> !data.getDD().equals(currData.getDD()));

            if (isPresent) {
               throw new FreeInputsWithSameNameAndDifferentType();
            }
        }
    }

    @Override
    public void mandatoryInputsIsUserFriendly() throws MandatoryInputsIsntUserFriendly {
        boolean isPresent =
                freeInputs
                        .stream()
                        .anyMatch(data -> !data.getDD().isUserFriendly());

        if (isPresent) {
            throw new MandatoryInputsIsntUserFriendly();
        }
    }
    @Override
    public boolean doesSourceStepBeforeTargetStep(String sourceStepName, String targetStepName){
        int sourceIndex=0;
        int targetIndex=0;
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getFinalStepName().equals(sourceStepName)) {
                sourceIndex = i;
            }
        }
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getFinalStepName().equals(targetStepName)) {
                targetIndex = i;
            }
        }
        if (sourceIndex > targetIndex) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isTheSameDD (String sourceStepName, String sourceDataName,String targetStepName, String targetDataName ) {
        String sourceKey = sourceStepName + "." + sourceDataName;
        String targetKey = targetStepName + "." + targetDataName;
        if(!(stepAndIOName2DD.get(sourceKey).equals(stepAndIOName2DD.get(targetKey)))){
           return false;
       }
       return true;
    }
    @Override
    public void initMandatoryInputsList(){
        freeInputs.addAll(getIOlist()
                .stream()
                .filter(data -> data.getType().equals(IO.INPUT))
                .filter(data -> data.getOptionalOutput().isEmpty()).collect(Collectors.toList()));
    }
}
