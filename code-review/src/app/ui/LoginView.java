package app.ui;

import app.application.service.LoginService;
import app.domain.entity.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Login view for user authentication.
 * Displays email and password fields and validates credentials.
 */
public class LoginView {
    
    private final LoginService loginService;
    private final Runnable onLoginSuccess;
    
    private Stage stage;
    private TextField emailField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button loginButton;
    
    public LoginView(LoginService loginService, Runnable onLoginSuccess) {
        this.loginService = loginService;
        this.onLoginSuccess = onLoginSuccess;
    }
    
    /**
     * Shows the login window.
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Code Review Assistant - Login");
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        
        // Title
        Label titleLabel = new Label("Code Review Assistant");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label subtitleLabel = new Label("Please login to continue");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
        
        // Form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(15);
        form.setAlignment(Pos.CENTER);
        
        Label emailLabel = new Label("Email:");
        emailField = new TextField();
        emailField.setPrefWidth(250);
        emailField.setPromptText("demo@example.com");
        
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setPrefWidth(250);
        passwordField.setPromptText("Enter password");
        
        form.add(emailLabel, 0, 0);
        form.add(emailField, 1, 0);
        form.add(passwordLabel, 0, 1);
        form.add(passwordField, 1, 1);
        
        // Error label
        errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        
        // Login button
        loginButton = new Button("Login");
        loginButton.setPrefWidth(120);
        loginButton.setStyle("-fx-font-size: 14px;");
        loginButton.setOnAction(e -> handleLogin());
        
        // Enter key support
        passwordField.setOnAction(e -> handleLogin());
        
        root.getChildren().addAll(titleLabel, subtitleLabel, form, errorLabel, loginButton);
        
        Scene scene = new Scene(root, 450, 350);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        
        // Focus on email field
        emailField.requestFocus();
    }
    
    /**
     * Handles the login button action.
     */
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();
        
        // Clear previous error
        errorLabel.setVisible(false);
        
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            showError("Please enter your email");
            return;
        }
        
        if (password == null || password.isEmpty()) {
            showError("Please enter your password");
            return;
        }
        
        // Disable button during authentication
        loginButton.setDisable(true);
        
        // Authenticate
        Optional<User> userOpt = loginService.authenticate(email, password);
        
        if (userOpt.isPresent()) {
            // Login successful
            User user = userOpt.get();
            UserSession.setCurrentUser(user);
            stage.close();
            onLoginSuccess.run();
        } else {
            // Login failed
            showError("Invalid email or password");
            loginButton.setDisable(false);
            passwordField.clear();
            passwordField.requestFocus();
        }
    }
    
    /**
     * Displays an error message.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
