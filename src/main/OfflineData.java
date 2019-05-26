package main;

import javafx.collections.ObservableList;

import java.util.ArrayList;

public class OfflineData {
    private ArrayList<String> analytes = new ArrayList<>();
    private ArrayList<String> bges = new ArrayList<>();
    private ArrayList<String> matrixes = new ArrayList<>();

    public ArrayList<String> getAnalytes() {
        return analytes;
    }

    public void setAnalytes(ArrayList<String> analytes) {
        this.analytes = analytes;
    }

    public ArrayList<String> getBges() {
        return bges;
    }

    public void setBges(ArrayList<String> bges) {
        this.bges = bges;
    }

    public ArrayList<String> getMatrixes() {
        return matrixes;
    }

    public void setMatrixes(ArrayList<String> matrixes) {
        this.matrixes = matrixes;
    }
}
