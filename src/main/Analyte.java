package main;

import javafx.beans.property.SimpleStringProperty;

/**
 * Class for analyte/BGE objects in concentration table.
 */
public class Analyte {
    private final SimpleStringProperty analyte;
    private final SimpleStringProperty concentration;

    /**
     * Constructs the analyte/BGE object.
     *
     * @param analyte Name of the analyte/BGE.
     * @param concentration Concentration of the analyte.
     */
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
