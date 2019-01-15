package de.kglab.kg_qa;

interface MultipleChoiceAction {
    String[] requestInput();
    void actOnInput(String[] userInput);
}
