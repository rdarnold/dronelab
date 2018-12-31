package dronelab.gui;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Tooltip;
import dronelab.Scenario;
import dronelab.collidable.*;
import dronelab.utils.*;

public class LabelTextField extends HBox {
    Label label;
    TextField textField;

    public LabelTextField() {
        super();
        init(null);
    }

    public LabelTextField(String strLabel) {
        super();
        init(strLabel);
    }

    private void init(String strLabel) {
        label = new Label();
        if (strLabel != null) {
            label.setText(strLabel);
        }
        textField = new TextField();
        getChildren().addAll(label, textField);
        setSpacing(10);
        setAlignment(Pos.CENTER_RIGHT);
    }

    public void setLabelPrefWidth(int prefWidth) {
        label.setPrefWidth(prefWidth);
    }

    public void setFieldPrefWidth(int prefWidth) {
        textField.setPrefWidth(prefWidth);
    }

    public void setLabelText(String strText) {
        label.setText(strText);
    }

    public void setFieldText(String strText) {
        textField.setText(strText);
    }

    public void setFieldText(double num) {
        textField.setText("" + num);
    }

    public void setFieldText(int num) {
        textField.setText("" + num);
    }

    public void setFieldText(double num, int numDecimals) {
        switch (numDecimals) {
            case 0:
                textField.setText("" + Math.round(num));
                break;
            case 1:
                textField.setText("" + Utils.round1Decimal(num));
                break;
            case 2:
                textField.setText("" + Utils.round2Decimals(num));
                break;
            case 3:
                textField.setText("" + Utils.round3Decimals(num));
                break;
            default:
                textField.setText("" + num);
                break;
        }
    }

    public void setTooltip(Tooltip tip) {
        textField.setTooltip(tip);
    }

    public String getLabelText() {
        return label.getText();
    }

    public String getFieldText() {
        return textField.getText();
    }

    public double getValue() {
        return getValueAsDouble();
    }

    public double getValueAsDouble() {
        return Utils.tryParseDouble(textField.getText());
    }

    public int getValueAsInt() {
        return Utils.tryParseInt(textField.getText());
    }

    public long getValueAsLong() {
        return (long)Utils.tryParseInt(textField.getText());
    }
}