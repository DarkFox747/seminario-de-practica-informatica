package app.ui.common;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

/**
 * Componente reutilizable para mostrar indicador de carga.
 * Se usa durante operaciones pesadas (análisis, queries, etc.)
 */
public class LoadingIndicator extends VBox {
    
    private final ProgressIndicator progressIndicator;
    private final Label messageLabel;
    
    public LoadingIndicator() {
        this("Loading...");
    }
    
    public LoadingIndicator(String message) {
        super(10);
        setAlignment(Pos.CENTER);
        
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxWidth(50);
        progressIndicator.setMaxHeight(50);
        
        messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        
        getChildren().addAll(progressIndicator, messageLabel);
        
        setVisible(false);
        setManaged(false);
    }
    
    public void show(String message) {
        messageLabel.setText(message);
        setVisible(true);
        setManaged(true);
    }
    
    public void hide() {
        setVisible(false);
        setManaged(false);
    }
    
    public void setMessage(String message) {
        messageLabel.setText(message);
    }
}
