package app.ui.common;

import app.domain.value.Severity;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Badge visual para mostrar severidad con color correspondiente.
 */
public class SeverityBadge extends Label {
    
    private static final String STYLE_BASE = "-fx-padding: 4 8; -fx-background-radius: 3; -fx-font-weight: bold; -fx-font-size: 11px;";
    
    public SeverityBadge(Severity severity) {
        super(severity.name());
        
        setFont(Font.font("System", FontWeight.BOLD, 11));
        setPadding(new Insets(4, 8, 4, 8));
        
        Color bgColor;
        Color textColor = Color.WHITE;
        
        switch (severity) {
            case CRITICAL:
                bgColor = Color.web("#d32f2f"); // Rojo oscuro
                break;
            case HIGH:
                bgColor = Color.web("#f57c00"); // Naranja
                break;
            case MEDIUM:
                bgColor = Color.web("#ffa726"); // Naranja claro
                break;
            case LOW:
                bgColor = Color.web("#fdd835"); // Amarillo
                textColor = Color.web("#333");
                break;
            case INFO:
                bgColor = Color.web("#42a5f5"); // Azul
                break;
            default:
                bgColor = Color.web("#999"); // Gris
                break;
        }
        
        setBackground(new Background(new BackgroundFill(bgColor, new CornerRadii(3), Insets.EMPTY)));
        setTextFill(textColor);
    }
}
