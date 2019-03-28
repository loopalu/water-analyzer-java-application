package gui;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class HideableItem<T> {
    private final ObjectProperty<T> object = new SimpleObjectProperty<>();
    private final BooleanProperty hidden = new SimpleBooleanProperty();

    HideableItem(T object) {
        setObject(object);
    }

    private ObjectProperty<T> objectProperty(){
        return this.object;
    }

    T getObject(){
        return this.objectProperty().get();
    }

    private void setObject(T object){
        this.objectProperty().set(object);
    }

    BooleanProperty hiddenProperty(){
        return this.hidden;
    }

    boolean isHidden(){
        return this.hiddenProperty().get();
    }

    void setHidden(boolean hidden){
        this.hiddenProperty().set(hidden);
    }

    @Override
    public String toString() {
        return getObject() == null ? null : getObject().toString();
    }

}