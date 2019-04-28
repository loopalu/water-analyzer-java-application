package main;

import javafx.beans.property.SimpleStringProperty;

public class Analyte {
    private final SimpleStringProperty analyte;
    private final SimpleStringProperty concentration;

    public Analyte(String analyte, String concentration) {
        this.analyte = new SimpleStringProperty(analyte);
        this.concentration = new SimpleStringProperty(concentration);
    }

    public String getAnalyte() {
        return analyte.get();
    }

    public void setAnalyte(String analyteString) {
        analyte.set(analyteString);
    }

    public String getConcentration() {
        return concentration.get();
    }

    public void setConcentration(String concentrationString) {
        concentration.set(concentrationString);
    }
}
