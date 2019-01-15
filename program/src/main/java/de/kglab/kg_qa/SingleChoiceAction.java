package de.kglab.kg_qa;

interface SingleChoiceAction {
    String requestInput();
    void actOnInput(String userInput);
}
