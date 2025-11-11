package com.saadsafi.todoapp.controller;

// --- ALL IMPORTS ---
import com.saadsafi.todoapp.dao.CategoryDAO;
import com.saadsafi.todoapp.dao.TaskDAO;
import com.saadsafi.todoapp.dao.SubtaskDAO;
import com.saadsafi.todoapp.model.Category;
import com.saadsafi.todoapp.model.Priority;
import com.saadsafi.todoapp.model.Status;
import com.saadsafi.todoapp.model.Subtask;
import com.saadsafi.todoapp.model.Task;
import com.saadsafi.todoapp.model.User;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.scene.layout.StackPane;
public class MainAppController implements Initializable {

    private User currentUser;
    
    // DAOs
    private CategoryDAO categoryDAO;
    private TaskDAO taskDAO;
    private SubtaskDAO subtaskDAO;
    
    // Observable Lists
    private ObservableList<Category> categoryList;
    private ObservableList<Task> taskList; // This is our MASTER list
    private ObservableList<Subtask> subtaskList; 
    
    // --- LISTS FOR FILTERING/SORTING ---
    private FilteredList<Task> filteredTaskList;
    private SortedList<Task> sortedTaskList;
    
    private Task currentSelectedTask; 

    // --- NEW: Stores the current filter selection ---
    private String currentFilter = "Show All";

    // --- FXML Variables ---
    @FXML private ListView<Category> categoryListView;
    @FXML private Button addCategoryButton;
    @FXML private Button logoutButton;
    @FXML private TextField searchBar;
    @FXML private Button filterButton;
    @FXML private Button sortButton;
    @FXML private ListView<Task> taskListView;
    @FXML private Button addTaskButton;
    @FXML private VBox taskDetailsPane;
    @FXML private TextField taskTitleField;
    @FXML private TextArea taskDescriptionArea;
    @FXML private DatePicker taskDueDate;
    @FXML private ComboBox<Priority> taskPriorityComboBox;
    @FXML private ComboBox<Category> taskCategoryComboBox;
    @FXML private ListView<Subtask> subtaskListView;
    @FXML private Button deleteTaskButton;
    @FXML private Button saveTaskButton;
    @FXML private TextField newSubtaskField;
    @FXML private Button addSubtaskButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("MainAppController initialized.");
        
        // --- DAOs ---
        this.categoryDAO = new CategoryDAO();
        this.taskDAO = new TaskDAO();
        this.subtaskDAO = new SubtaskDAO();
        
        // --- Lists ---
        this.categoryList = FXCollections.observableArrayList();
        this.taskList = FXCollections.observableArrayList(); // Master list
        this.subtaskList = FXCollections.observableArrayList();
        
        // --- Setup Filtered & Sorted Lists ---
        this.filteredTaskList = new FilteredList<>(taskList, p -> true); // Show all by default

        // 2. Add listener to search bar to change the filter
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(); 
        });

        // 3. Wrap the FilteredList in a SortedList
        this.sortedTaskList = new SortedList<>(filteredTaskList);
        
        // 4. *** THIS IS THE LINE I REMOVED. IT WAS THE ERROR. ***
        // sortedTaskList.comparatorProperty().bind(taskListView.comparatorProperty()); 

        // --- Link Lists to ListViews ---
        categoryListView.setItems(categoryList);
        taskListView.setItems(sortedTaskList); // <-- Correctly bound
        subtaskListView.setItems(subtaskList);
        
        // Call setup methods
        setupDetailPaneControls(); 
        addCategorySelectionListener();
        addTaskSelectionListener();
        setupCategoryContextMenu();
        setupSubtaskListView();
        
        // --- WIRE UP ALL BUTTONS ---
        saveTaskButton.setOnAction(this::handleSaveTaskButton);
        deleteTaskButton.setOnAction(this::handleDeleteTaskButton);
        addTaskButton.setOnAction(this::handleAddTaskButton);
        addCategoryButton.setOnAction(this::handleAddCategoryButton);
        logoutButton.setOnAction(this::handleLogoutButton);
        addSubtaskButton.setOnAction(this::handleAddNewSubtask);
        sortButton.setOnAction(this::handleSortButton);
        filterButton.setOnAction(this::handleFilterButton); // <-- Filter button
        
        // Customize Task display
        setupTaskCellFactory();
        
        // Hide details pane
        taskDetailsPane.setVisible(false);
    }    
    
    public void initData(User user) {
        this.currentUser = user;
        System.out.println("User logged in: " + currentUser.getUsername());
        
        loadCategories();
        
        if (!categoryList.isEmpty()) {
            categoryListView.getSelectionModel().selectFirst();
        }
    }
    
    // --- NEW: METHOD FOR FILTER BUTTON ---
    @FXML
    private void handleFilterButton(ActionEvent event) {
        List<String> choices = List.of("Show All", "Show Pending Only", "Show Completed Only", "Show Due Today", "Show High Priority");
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>(currentFilter, choices);
        dialog.setTitle("Filter Tasks");
        dialog.setHeaderText("Select a filter to apply:");
        dialog.setContentText("Filter by:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(choice -> {
            currentFilter = choice; // Store the new filter choice
            applyFilters(); // Re-apply all filters
        });
    }

    // --- NEW: COMBINED FILTER LOGIC ---
    /**
     * Applies both the Search Bar filter AND the Dropdown filter
     * to the task list.
     */
    private void applyFilters() {
        String searchText = searchBar.getText().toLowerCase();
        
        filteredTaskList.setPredicate(task -> {
            // 1. Check Search Bar
            boolean searchMatches = false;
            if (searchText == null || searchText.isEmpty()) {
                searchMatches = true;
            } else {
                if (task.getTitle().toLowerCase().contains(searchText)) {
                    searchMatches = true;
                } else if (task.getDescription() != null && task.getDescription().toLowerCase().contains(searchText)) {
                    searchMatches = true;
                }
            }

            // 2. Check Filter Dropdown
            boolean filterMatches = false;
            switch (currentFilter) {
                case "Show Pending Only":
                    filterMatches = (task.getStatus() == Status.PENDING);
                    break;
                case "Show Completed Only":
                    filterMatches = (task.getStatus() == Status.COMPLETED);
                    break;
                case "Show Due Today":
                    filterMatches = (task.getDueDate() != null && task.getDueDate().isEqual(LocalDate.now()));
                    break;
                case "Show High Priority":
                    filterMatches = (task.getPriority() == Priority.HIGH);
                    break;
                case "Show All":
                default:
                    filterMatches = true;
                    break;
            }
            
            // 3. Both must be true
            return searchMatches && filterMatches;
        });
    }
    
    
    @FXML
    private void handleSortButton(ActionEvent event) {
        List<String> choices = List.of("Default (None)", "Priority (High-Low)", "Due Date (Soonest)", "Title (A-Z)");
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Sort Tasks");
        dialog.setHeaderText("How would you like to sort the tasks?");
        dialog.setContentText("Sort by:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(choice -> {
            // 4. *** APPLY COMPARATOR TO THE SORTEDLIST ***
            switch (choice) {
                case "Priority (High-Low)":
                    sortedTaskList.setComparator(Comparator.comparing(Task::getPriority));
                    break;
                case "Due Date (Soonest)":
                    sortedTaskList.setComparator(
                        Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                    );
                    break;
                case "Title (A-Z)":
                    sortedTaskList.setComparator(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "Default (None)":
                default:
                    sortedTaskList.setComparator(null); 
                    break;
            }
        });
    }

    
    @FXML
    private void handleLogoutButton(ActionEvent event) {
        Alert confirmationAlert = showAlert(
                Alert.AlertType.CONFIRMATION,
                "Logout?",
                "Are you sure you want to log out?"
        );

        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginScreen.fxml"));
                Parent root = loader.load();

                Stage loginStage = new Stage();
                loginStage.setTitle("Todo App - Login");
                Scene scene = new Scene(root);
                
                try {
                    String cssPath = "/styles/loginscreen.css";
                    scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
                } catch (Exception e) {
                    System.err.println("Could not load loginscreen.css: " + e.getMessage());
                }

                loginStage.setScene(scene);
                loginStage.setResizable(false);
                loginStage.show();

                Stage mainAppStage = (Stage) logoutButton.getScene().getWindow();
                mainAppStage.close();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not load login screen.");
            }
        } else {
            System.out.println("Logout cancelled.");
        }
    }
    
    
    // --- CATEGORY METHODS ---
    
    private void setupCategoryContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete Category");

        deleteMenuItem.setOnAction((ActionEvent event) -> {
            Category selectedCategory = categoryListView.getSelectionModel().getSelectedItem();
            if (selectedCategory != null) {
                handleDeleteCategory(selectedCategory);
            }
        });

        contextMenu.getItems().add(deleteMenuItem);
        categoryListView.setContextMenu(contextMenu);
    }
    
    private void handleDeleteCategory(Category categoryToDelete) {
        Alert confirmationAlert = showAlert(
                Alert.AlertType.CONFIRMATION, 
                "Delete Category?", 
                "Are you sure you want to delete '" + categoryToDelete.getCategoryName() + "'?\n\n" +
                "Tasks in this category will not be deleted."
        );

        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = categoryDAO.deleteCategory(categoryToDelete.getCategoryId());

            if (success) {
                System.out.println("Category deleted.");
                categoryList.remove(categoryToDelete);
                loadTasks(null); 
                taskDetailsPane.setVisible(false);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not delete category.");
            }
        }
    }
    
    @FXML
    private void handleAddCategoryButton(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Category");
        dialog.setHeaderText("Create a new task category");
        dialog.setContentText("Category Name:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String categoryName = result.get().trim();
            
            Category newCategory = categoryDAO.createCategory(categoryName, currentUser.getUserId());
            
            if (newCategory != null) {
                System.out.println("New category created: " + newCategory.getCategoryName());
                categoryList.add(newCategory);
                categoryListView.getSelectionModel().select(newCategory);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not create new category in database.");
            }
        }
    }
    
    private void loadCategories() {
        List<Category> categoriesFromDB = categoryDAO.getCategoriesByUserId(currentUser.getUserId());
        categoryList.clear();
        categoryList.addAll(categoriesFromDB);
        System.out.println("Loaded " + categoriesFromDB.size() + " categories.");
        
        taskCategoryComboBox.setItems(categoryList);
    }
    
    private void addCategorySelectionListener() {
        categoryListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    System.out.println("Category selected: " + newValue.getCategoryName());
                    loadTasks(newValue);
                    
                    taskDetailsPane.setVisible(false);
                    currentSelectedTask = null;
                } else {
                    loadTasks(null);
                    taskDetailsPane.setVisible(false);
                    currentSelectedTask = null;
                }
            }
        );
    }

    // --- TASK METHODS ---

    private void addTaskSelectionListener() {
        taskListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    System.out.println("Task selected: " + newValue.getTitle());
                    currentSelectedTask = newValue;
                    displayTaskDetails(newValue);
                } else {
                    currentSelectedTask = null;
                    taskDetailsPane.setVisible(false);
                }
            }
        );
    }
    
    private void displayTaskDetails(Task task) {
        this.currentSelectedTask = task;
        taskDetailsPane.setVisible(true);
        
        taskTitleField.setText(task.getTitle());
        taskDescriptionArea.setText(task.getDescription());
        taskDueDate.setValue(task.getDueDate());
        taskPriorityComboBox.setValue(task.getPriority());
        taskCategoryComboBox.setValue(task.getCategory());
        
        loadSubtasks(task.getTaskId());
    }
    
    @FXML
    private void handleAddTaskButton(ActionEvent event) {
        System.out.println("Add New Task button clicked.");
        currentSelectedTask = null;
        taskListView.getSelectionModel().clearSelection();

        taskTitleField.clear();
        taskDescriptionArea.clear();
        taskDueDate.setValue(null);
        
        taskPriorityComboBox.setValue(Priority.MEDIUM);
        taskCategoryComboBox.setValue(categoryListView.getSelectionModel().getSelectedItem());
        
        subtaskList.clear();
        
        taskDetailsPane.setVisible(true);
        taskTitleField.requestFocus();
    }
    
    @FXML
    private void handleDeleteTaskButton(ActionEvent event) {
        if (currentSelectedTask == null) {
            showAlert(Alert.AlertType.ERROR, "No Task Selected", "Please select a task to delete.");
            return;
        }

        Alert confirmationAlert = showAlert(
                Alert.AlertType.CONFIRMATION, 
                "Delete Task?", 
                "Are you sure you want to permanently delete this task?\n\n" + currentSelectedTask.getTitle()
        );

        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = taskDAO.deleteTask(currentSelectedTask.getTaskId());

            if (success) {
                System.out.println("Task deleted successfully!");
                taskList.remove(currentSelectedTask); // Remove from master list
                taskDetailsPane.setVisible(false);
                currentSelectedTask = null;
                showAlert(Alert.AlertType.INFORMATION, "Success", "Task deleted.");
            } else {
                System.err.println("Failed to delete task.");
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete task from the database.");
            }
        } else {
            System.out.println("Delete cancelled.");
        }
    }
    
    @FXML
    private void handleSaveTaskButton(ActionEvent event) {
        if (taskTitleField.getText() == null || taskTitleField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Task title cannot be empty.");
            return;
        }
        
        String newTitle = taskTitleField.getText();
        String newDescription = taskDescriptionArea.getText();
        Priority newPriority = taskPriorityComboBox.getValue();
        LocalDate newDueDate = taskDueDate.getValue();
        Category newCategory = taskCategoryComboBox.getValue();

        if (currentSelectedTask != null) {
            // --- UPDATE LOGIC ---
            System.out.println("Saving changes to existing task ID: " + currentSelectedTask.getTaskId());
            
            currentSelectedTask.setTitle(newTitle);
            currentSelectedTask.setDescription(newDescription);
            currentSelectedTask.setPriority(newPriority);
            currentSelectedTask.setDueDate(newDueDate);
            currentSelectedTask.setCategory(newCategory);

            boolean success = taskDAO.updateTask(currentSelectedTask);

            if (success) {
                System.out.println("Task updated successfully!");
                showAlert(Alert.AlertType.INFORMATION, "Success", "Task updated successfully.");

                taskListView.refresh();
                
                Category currentCategory = categoryListView.getSelectionModel().getSelectedItem();
                if (newCategory == null || (currentCategory != null && newCategory.getCategoryId() != currentCategory.getCategoryId())) {
                    loadTasks(currentCategory);
                    taskDetailsPane.setVisible(false);
                }

            } else {
                System.err.println("Failed to update task.");
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update task in the database.");
            }

        } else {
            // --- CREATE LOGIC ---
            System.out.println("Saving a NEW task.");
            
            Task newTask = new Task(0, currentUser.getUserId(), newTitle, newDescription,
                                    newPriority.name(), newDueDate, Status.PENDING.name(), newCategory);
            
            Task createdTask = taskDAO.createTask(newTask);
            
            if (createdTask != null) {
                System.out.println("New task created with ID: " + createdTask.getTaskId());
                showAlert(Alert.AlertType.INFORMATION, "Success", "New task created!");
                
                taskList.add(createdTask); // Add to master list
                taskListView.getSelectionModel().select(createdTask);
                
            } else {
                System.err.println("Failed to create task.");
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create task in the database.");
            }
        }
    }
    
    private void loadTasks(Category selectedCategory) {
        taskList.clear(); // Clear master list
        
        if (selectedCategory == null) {
            System.out.println("No category selected. Task list cleared.");
            return;
        }
        
        List<Task> tasksFromDB = taskDAO.getTasksByUserAndCategory(
                currentUser.getUserId(), 
                selectedCategory.getCategoryId()
        );
        
        taskList.addAll(tasksFromDB); // Add to master list
        
        System.out.println("Loaded " + tasksFromDB.size() + " tasks for category " + selectedCategory.getCategoryName());
    }
    
//    private void setupTaskCellFactory() {
//        taskListView.setCellFactory(param -> new ListCell<Task>() {
//            @Override
//            protected void updateItem(Task item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null || item.getTitle() == null) {
//                    setText(null);
//                } else {
//                    setText(item.getTitle());
//                }
//            }
//        });
//    }
    private void setupTaskCellFactory() {
        taskListView.setCellFactory(param -> new TaskCell(taskDAO));
    }

    // --- SUBTASK METHODS ---
    
    private void loadSubtasks(int taskId) {
        List<Subtask> subtasksFromDB = subtaskDAO.getSubtasksByTaskId(taskId);
        subtaskList.clear();
        subtaskList.addAll(subtasksFromDB);
        System.out.println("Loaded " + subtasksFromDB.size() + " subtasks.");
    }
    
    @FXML
    private void handleAddNewSubtask(ActionEvent event) {
        String title = newSubtaskField.getText();
        if (title == null || title.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Subtask title cannot be empty.");
            return;
        }
        
        if (currentSelectedTask == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "No task selected to add subtask to.");
            return;
        }
        
        Subtask newSubtask = subtaskDAO.createSubtask(title, currentSelectedTask.getTaskId());
        
        if (newSubtask != null) {
            subtaskList.add(newSubtask);
            newSubtaskField.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create subtask.");
        }
    }
    
    private void setupSubtaskListView() {
        subtaskListView.setCellFactory(new Callback<ListView<Subtask>, ListCell<Subtask>>() {
            @Override
            public ListCell<Subtask> call(ListView<Subtask> param) {
                return new SubtaskCell(subtaskDAO, subtaskList);
            }
        });
    }

    // --- GENERIC HELPER METHODS ---
    
    private Alert showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        if (alertType != Alert.AlertType.CONFIRMATION) {
            alert.showAndWait();
        }
        
        return alert;
    }
    
    private void setupDetailPaneControls() {
        taskPriorityComboBox.setItems(FXCollections.observableArrayList(Priority.values()));
        
        taskCategoryComboBox.setItems(categoryList);
        taskCategoryComboBox.setCellFactory(param -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getCategoryName() == null) {
                    setText(null);
                } else {
                    setText(item.getCategoryName());
                }
            }
        });
        taskCategoryComboBox.setButtonCell(new ListCell<Category>() {
             @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getCategoryName() == null) {
                    setText(null);
                } else {
                    setText(item.getCategoryName());
                }
            }
        });
    }
} // <-- END OF MainAppController CLASS


// --- SUBTASKCELL CLASS (STAYS THE SAME, BUT IS CORRECT) ---
class SubtaskCell extends ListCell<Subtask> {
    private HBox hbox = new HBox(10);
    private CheckBox checkBox = new CheckBox();
    private Label titleLabel = new Label();
    private TextField titleField = new TextField();
    private StackPane titlePane = new StackPane(); // Use StackPane to swap Label and Field
    
    private SubtaskDAO subtaskDAO;
    private ObservableList<Subtask> subtaskList;

    public SubtaskCell(SubtaskDAO subtaskDAO, ObservableList<Subtask> subtaskList) {
        super();
        this.subtaskDAO = subtaskDAO;
        this.subtaskList = subtaskList;
        
        // Add style classes from our CSS
        titleLabel.getStyleClass().add("subtask-label");
        titleField.getStyleClass().add("subtask-field");
        hbox.getStyleClass().add("subtask-cell-hbox");

        // Layout
        titlePane.getChildren().addAll(titleLabel, titleField);
        HBox.setHgrow(titlePane, javafx.scene.layout.Priority.ALWAYS);
        hbox.getChildren().addAll(checkBox, titlePane);

        setupClickEvents();
        setupContextMenu();
        setupCheckBoxListener();
    }

    // --- Logic to swap between Label and TextField ---
    private void setupClickEvents() {
        // Double-click on the label to start editing
        titleLabel.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                showTextField(true);
            }
        });

        // Press 'Enter' on the text field to save
        titleField.setOnAction(event -> {
            saveAndHideTextField();
        });

        // Click outside the text field to save
        titleField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Lost focus
                saveAndHideTextField();
            }
        });
    }
    
    private void setupContextMenu() {
        MenuItem deleteItem = new MenuItem("Delete Subtask");
        deleteItem.setOnAction(event -> {
            Subtask subtask = getItem();
            if (subtask != null) {
                if (subtaskDAO.deleteSubtask(subtask.getSubtaskId())) {
                    subtaskList.remove(subtask);
                }
            }
        });
        setContextMenu(new ContextMenu(deleteItem));
    }
    
    private void setupCheckBoxListener() {
        checkBox.setOnAction(event -> {
            Subtask subtask = getItem();
            if (subtask != null) {
                if (checkBox.isSelected()) {
                    subtask.setStatus(Status.COMPLETED);
                } else {
                    subtask.setStatus(Status.PENDING);
                }
                subtaskDAO.updateSubtask(subtask);
                updateStyle(subtask); // Instantly apply strikethrough
            }
        });
    }

    /**
     * Shows/Hides the text field
     */
    private void showTextField(boolean show) {
        titleField.setVisible(show);
        titleField.setManaged(show);
        titleLabel.setVisible(!show);
        titleLabel.setManaged(!show);
        
        if (show) {
            titleField.requestFocus();
            titleField.selectAll();
        }
    }
    
    /**
     * Saves the text field content and switches back to label
     */
    private void saveAndHideTextField() {
        Subtask subtask = getItem();
        if (subtask != null) {
            subtask.setTitle(titleField.getText());
            subtaskDAO.updateSubtask(subtask);
            titleLabel.setText(titleField.getText()); // Update label
        }
        showTextField(false);
    }

    /**
     * Applies strikethrough style
     */
    private void updateStyle(Subtask subtask) {
        String style;
        if (subtask.getStatus() == Status.COMPLETED) {
            style = "-fx-strikethrough: true; -fx-text-fill: #9CA3AF;";
        } else {
            style = "-fx-strikethrough: false; -fx-text-fill: #1F2937;";
        }
        titleLabel.setStyle(style);
        titleField.setStyle(style);
    }

    @Override
    protected void updateItem(Subtask subtask, boolean empty) {
        super.updateItem(subtask, empty);
        if (empty || subtask == null) {
            setText(null);
            setGraphic(null);
        } else {
            // Set text for both label and field
            titleLabel.setText(subtask.getTitle());
            titleField.setText(subtask.getTitle());
            
            checkBox.setSelected(subtask.getStatus() == Status.COMPLETED);
            updateStyle(subtask);
            
            setGraphic(hbox);
            
            // Ensure label is visible by default
            showTextField(false); 
        }
    }
}