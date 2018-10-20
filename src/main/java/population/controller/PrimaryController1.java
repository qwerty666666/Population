/*
 * Population
 * Copyright (C) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package population.controller;

import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import population.App;
import population.PopulationApplication;
import population.controller.base.AbstractController;
import population.model.*;
import population.util.Resources.StringResource;
import population.util.TaskParser;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;


public class PrimaryController1 extends AbstractController {
    @FXML
    protected HBox debugPanel;
    protected File taskFile = null;

    @FXML
    private TabPane mainTabPane;

    private FileChooser getTaskFileChooser(String title) {
        FileChooser fileChooser = getFileChooser(title);
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(getString("TopMenu.Task"), "*.pmt"));
        return fileChooser;
    }

    private FileChooser getFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        String workDirectory = getApplication().getWorkDirectory();
        if (workDirectory != null) {
            File file = new File(workDirectory);
            if (file.exists()) {
                fileChooser.setInitialDirectory(file);
            }
        }
        return fileChooser;
    }


    /**
     * set title for main window
     * @param title title
     */
    private void setTitle(String title) {
        getStage().setTitle(title == null || title.isEmpty() ? this.getDefaultTitle() : title);
    }


    /**
     * @return task name from file or empty string if file is null
     */
    private String getTaskNameFromFile(File file) {
        if (file == null) {
            return "";
        }
        return file.getName();
    }


    /**
     * @return title displayed by default
     */
    private String getDefaultTitle() {
        return StringResource.getString("App.WindowTitle");
    }


    @FXML
    public void debug() {
        int a = 0;
    }


    @Override
    public void initialize() {
        debugPanel.setVisible(App.isDev());
        debugPanel.setManaged(App.isDev());

        this.setTitle(this.getDefaultTitle());

        mainTabPane.getSelectionModel().select(2);
        //((TabPane)this.getStage().getScene().lookup("#resultsChartTabs")).getSelectionModel().select(3);
        File file = new File("C:\\Users\\user\\Desktop\\test.pmt");
        openTaskFromFile(file);
    }


    public void openTask() {
        File file = getTaskFileChooser(getString("App.OpenTaskDialogTitle"))
                .showOpenDialog(this.getStage().getScene().getWindow());
        openTaskFromFile(file);
    }


    public void openTaskFromFile(File file) {
        if (file == null) {
            return;
        }

        this.taskFile = file;

        TaskV4 task = TaskParser.parse(file);
        if (task == null) {
            return;
        }

        getApplication().setWorkDirectory(file.getParent());

        this.setTitle(this.getTaskNameFromFile(file));

        App.setTask(task);
    }


    public void clearTask() {
        taskFile = null;
        App.clearTask();
        setTitle(null);
    }


    public void saveTaskAs() {
        File file = getTaskFileChooser(getString("App.SaveTaskDialogTitle"))
                .showSaveDialog(this.getStage().getScene().getWindow());
        if (file == null) {
            return;
        }
        String path = file.getPath();
        int dotIndex = path.indexOf('.');
        if (dotIndex < 0) {
            path += ".pmt";
            file = new File(path);
        }
        TaskParser.encodeV4(file, App.getTask());
        taskFile = file;
        getApplication().setWorkDirectory(file.getParent());
        setTitle(this.getTaskNameFromFile(file));
    }


    public void saveTask() {
        if (taskFile == null) {
            saveTaskAs();
        } else {
            TaskParser.encodeV4(taskFile, App.getTask());
        }
    }


    public void about() {
//        getApplication().showAboutDialog();
    }


    public void quit() {
        Stage stage = this.getApplication().getPrimaryStage();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }


    public void selectLangRussian() {
        selectLanguage("ru");
    }


    public void selectLangEnglish() {
        selectLanguage("en");
    }


    private void selectLanguage(String langTag) {
        PopulationApplication application = getApplication();
        if (application.selectLanguage(langTag)) {
            ResourceBundle resources = getResources();
            application.showAlert(resources.getString("TopMenu.Lang"), null,
                    resources.getString("App.ChangeLang.Info"), Alert.AlertType.INFORMATION);
        }
    }
}
