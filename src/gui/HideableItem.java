package gui;

import javafx.beans.property.*;

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