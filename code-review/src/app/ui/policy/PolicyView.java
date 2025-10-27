package app.ui.policy;

import app.domain.entity.SeverityPolicy;
import app.ui.common.ConfirmDialog;
import app.ui.common.LoadingIndicator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista de gestión de políticas.
 * Tabla de policies existentes + formulario para crear/editar.
 */
public class PolicyView extends BorderPane {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private final PolicyController controller;
    
    // Controls
    private Button refreshButton;
    private Button newPolicyButton;
    private LoadingIndicator loadingIndicator;
    
    private TableView<SeverityPolicy> policiesTable;
    
    // Form
    private TextField nameField;
    private TextArea rulesTextArea;
    private Button saveButton;
    private Button cancelButton;
    
    private SeverityPolicy selectedPolicy;
    
    public PolicyView(PolicyController controller) {
        this.controller = controller;
        initializeUI();
        loadInitialData();
    }
    
    private void initializeUI() {
        setPadding(new Insets(20));
        
        // Header
        Label titleLabel = new Label("Policy Management");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadPolicies());
        
        newPolicyButton = new Button("New Policy");
        newPolicyButton.setOnAction(e -> showNewPolicyForm());
        
        HBox toolbar = new HBox(10, refreshButton, newPolicyButton);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        VBox header = new VBox(10, titleLabel, toolbar);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        // Loading
        loadingIndicator = new LoadingIndicator();
        
        // Table
        Label tableLabel = new Label("Existing Policies:");
        tableLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        policiesTable = createPoliciesTable();
        
        VBox tableContainer = new VBox(10, tableLabel, policiesTable);
        
        // Form
        VBox formContainer = createForm();
        formContainer.setVisible(false);
        formContainer.setManaged(false);
        
        // Layout
        VBox content = new VBox(20, header, loadingIndicator, tableContainer, formContainer);
        setCenter(content);
    }
    
    @SuppressWarnings("unchecked")
    private TableView<SeverityPolicy> createPoliciesTable() {
        TableView<SeverityPolicy> table = new TableView<>();
        
        TableColumn<SeverityPolicy, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        TableColumn<SeverityPolicy, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        TableColumn<SeverityPolicy, Integer> versionCol = new TableColumn<>("Version");
        versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));
        versionCol.setPrefWidth(80);
        
        TableColumn<SeverityPolicy, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(80);
        activeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(active ? "YES" : "NO");
                    setStyle(active ? "-fx-text-fill: green; -fx-font-weight: bold;" 
                                   : "-fx-text-fill: gray;");
                }
            }
        });
        
        TableColumn<SeverityPolicy, String> createdCol = new TableColumn<>("Created At");
        createdCol.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCreatedAt().format(DATE_FORMATTER)
            )
        );
        createdCol.setPrefWidth(150);
        
        TableColumn<SeverityPolicy, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button activateButton = new Button("Activate");
            
            {
                editButton.setOnAction(e -> {
                    SeverityPolicy policy = getTableView().getItems().get(getIndex());
                    showEditPolicyForm(policy);
                });
                
                activateButton.setOnAction(e -> {
                    SeverityPolicy policy = getTableView().getItems().get(getIndex());
                    activatePolicy(policy);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, activateButton);
                    setGraphic(buttons);
                }
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, versionCol, activeCol, createdCol, actionsCol);
        table.setPrefHeight(300);
        
        return table;
    }
    
    private VBox createForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        // Name
        Label nameLabel = new Label("Policy Name:");
        nameField = new TextField();
        nameField.setPrefWidth(300);
        nameField.setPromptText("e.g., Default Security Policy");
        
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        
        // Rules JSON
        Label rulesLabel = new Label("Rules (JSON):");
        rulesTextArea = new TextArea();
        rulesTextArea.setPrefRowCount(10);
        rulesTextArea.setPromptText("Enter policy rules in JSON format...");
        
        grid.add(rulesLabel, 0, 1);
        grid.add(rulesTextArea, 1, 1);
        
        // Buttons
        saveButton = new Button("Save");
        saveButton.setOnAction(e -> savePolicy());
        
        cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> hideForm());
        
        HBox buttonBox = new HBox(10, saveButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label formTitle = new Label("Policy Form");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        VBox formContainer = new VBox(15, 
            formTitle,
            grid, 
            buttonBox
        );
        formContainer.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5; " +
                              "-fx-background-color: #f9f9f9; -fx-background-radius: 5;");
        formContainer.setPadding(new Insets(15));
        
        return formContainer;
    }
    
    private void loadInitialData() {
        loadPolicies();
    }
    
    private void loadPolicies() {
        loadingIndicator.show("Loading policies...");
        refreshButton.setDisable(true);
        
        controller.loadPolicies(policies -> {
            policiesTable.getItems().clear();
            policiesTable.getItems().addAll(policies);
            loadingIndicator.hide();
            refreshButton.setDisable(false);
        });
    }
    
    private void showNewPolicyForm() {
        selectedPolicy = null;
        nameField.setText("");
        nameField.setEditable(true);
        rulesTextArea.setText("");
        
        VBox form = (VBox) getCenter();
        form.getChildren().get(form.getChildren().size() - 1).setVisible(true);
        form.getChildren().get(form.getChildren().size() - 1).setManaged(true);
    }
    
    private void showEditPolicyForm(SeverityPolicy policy) {
        selectedPolicy = policy;
        nameField.setText(policy.getName());
        nameField.setEditable(false); // No cambiar nombre en update
        rulesTextArea.setText(policy.getRulesJson());
        
        VBox form = (VBox) getCenter();
        form.getChildren().get(form.getChildren().size() - 1).setVisible(true);
        form.getChildren().get(form.getChildren().size() - 1).setManaged(true);
    }
    
    private void hideForm() {
        VBox form = (VBox) getCenter();
        form.getChildren().get(form.getChildren().size() - 1).setVisible(false);
        form.getChildren().get(form.getChildren().size() - 1).setManaged(false);
        selectedPolicy = null;
    }
    
    private void savePolicy() {
        String name = nameField.getText();
        String rulesJson = rulesTextArea.getText();
        
        if (selectedPolicy == null) {
            // Create new
            controller.createPolicy(name, rulesJson, success -> {
                if (success) {
                    hideForm();
                    loadPolicies();
                }
            });
        } else {
            // Update existing (new version)
            controller.updatePolicy(selectedPolicy.getId(), rulesJson, success -> {
                if (success) {
                    hideForm();
                    loadPolicies();
                }
            });
        }
    }
    
    private void activatePolicy(SeverityPolicy policy) {
        controller.activatePolicy(policy.getId(), success -> {
            if (success) {
                loadPolicies();
            }
        });
    }
}
