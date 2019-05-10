package main;

import java.util.ArrayList;
import java.util.HashMap;

public class Method {
    private String nameOfTest;
    private String nameOfUser;
    private String userClass;
    private ArrayList analytes = new ArrayList();
    private HashMap<String,Integer> analyteConcentrations = new HashMap<>();
    private String analyteUnit;
    private String matrix;
    private ArrayList bge = new ArrayList();
    private HashMap<String, Integer> bgeConcentrations = new HashMap<>();
    private String bgeUnit;
    private String capillary;
    private String capillaryTotalLength;
    private String capillaryEffectiveLength;
    private String injectionMethod;
    private String injectionChoice;
    private String injectionChoiceValue;
    private String injectionChoiceUnit;
    private String injectionTime;
    private String current;
    private String frequency;
    private String description;
    private String hvValue;

    public String getNameOfUser() {
        return nameOfUser;
    }

    public void setNameOfUser(String nameOfUser) {
        this.nameOfUser = nameOfUser;
    }

    public ArrayList getAnalytes() {
        return analytes;
    }

    public void setAnalytes(ArrayList analytes) {
        this.analytes = analytes;
    }

    public HashMap<String, Integer> getAnalyteConcentrations() {
        return analyteConcentrations;
    }

    public void setAnalyteConcentrations(HashMap<String, Integer> analyteConcentrations) {
        this.analyteConcentrations = analyteConcentrations;
    }

    public String getAnalyteUnit() {
        return analyteUnit;
    }

    public void setAnalyteUnit(String analyteUnit) {
        this.analyteUnit = analyteUnit;
    }

    public String getMatrix() {
        return matrix;
    }

    public void setMatrix(String matrix) {
        this.matrix = matrix;
    }

    public ArrayList getBge() {
        return bge;
    }

    public void setBge(ArrayList bge) {
        this.bge = bge;
    }

    public HashMap<String, Integer> getBgeConcentrations() {
        return bgeConcentrations;
    }

    public void setBgeConcentrations(HashMap<String, Integer> bgeConcentrations) {
        this.bgeConcentrations = bgeConcentrations;
    }

    public String getBgeUnit() {
        return bgeUnit;
    }

    public void setBgeUnit(String bgeUnit) {
        this.bgeUnit = bgeUnit;
    }

    public String getCapillary() {
        return capillary;
    }

    public void setCapillary(String capillary) {
        this.capillary = capillary;
    }

    public String getCapillaryTotalLength() {
        return capillaryTotalLength;
    }

    public void setCapillaryTotalLength(String capillaryTotalLength) {
        this.capillaryTotalLength = capillaryTotalLength;
    }

    public String getCapillaryEffectiveLength() {
        return capillaryEffectiveLength;
    }

    public void setCapillaryEffectiveLength(String capillaryEffectiveLength) {
        this.capillaryEffectiveLength = capillaryEffectiveLength;
    }

    public String getInjectionMethod() {
        return injectionMethod;
    }

    public void setInjectionMethod(String injectionMethod) {
        this.injectionMethod = injectionMethod;
    }

    public String getInjectionChoice() {
        return injectionChoice;
    }

    public void setInjectionChoice(String injectionChoice) {
        this.injectionChoice = injectionChoice;
    }

    public String getInjectionChoiceValue() {
        return injectionChoiceValue;
    }

    public void setInjectionChoiceValue(String injectionChoiceValue) {
        this.injectionChoiceValue = injectionChoiceValue;
    }

    public String getInjectionChoiceUnit() {
        return injectionChoiceUnit;
    }

    public void setInjectionChoiceUnit(String injectionChoiceUnit) {
        this.injectionChoiceUnit = injectionChoiceUnit;
    }

    public String getInjectionTime() {
        return injectionTime;
    }

    public void setInjectionTime(String injectionTime) {
        this.injectionTime = injectionTime;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHvValue() {
        return hvValue;
    }

    public void setHvValue(String hvValue) {
        this.hvValue = hvValue;
    }

    public String getNameOfTest() {
        return nameOfTest;
    }

    public void setNameOfTest(String nameOfTest) {
        this.nameOfTest = nameOfTest;
    }

    public String getUserClass() {
        return userClass;
    }

    public void setUserClass(String userClass) {
        this.userClass = userClass;
    }
}
