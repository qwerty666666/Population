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

import population.App;
import population.PopulationApplication;
import population.component.Calculator;
import population.component.ChartSeries;
import population.component.TickLabelFormatter;
import population.controller.base.AbstractController;
import population.model.*;
import population.util.TaskParser;
import population.util.Utils;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class PrimaryController1 extends AbstractController {
    // TODO remove in view
    public TabPane mMainTabPane;
    protected File taskFile = null;

    private FileChooser getTaskFileChooser(String title) {
        FileChooser fileChooser = getFileChooser(title);
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter(getString("task"), "*.pmt"));
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

    private void setTitle(File file) {
        String applicationName = getString("application_name");
        getStage().setTitle(
                file == null ? applicationName : file.getName() + " - " + applicationName);
    }

    private void setTitle() {
        setTitle(null);
    }

    @FXML
    public void debug() {
        int a = 0;
    }

    @Override
    public void initialize() {
//        mMainTabPane.getSelectionModel().select(2);
//        File file = new File("C:\\Users\\user\\Desktop\\test.pmt");
//        openTaskFromFile(file);
    }

    public void openTask() {
        File file = getTaskFileChooser(getString("open_task"))
                .showOpenDialog(mMainTabPane.getScene().getWindow());

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

        App.setTask(task);
    }

    public void clearTask() {
        taskFile = null;
        App.clearTask();
        setTitle();
    }

    public void saveTaskAs() {
        File file = getTaskFileChooser(getString("save_task"))
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
        Task task = new Task();
        TaskParser.encodeV4(file, App.getTask());
        taskFile = file;
        getApplication().setWorkDirectory(file.getParent());
        setTitle(file);
    }

    public void saveTask() {
        if (taskFile == null) {
            saveTaskAs();
        } else {
            TaskParser.encodeV4(taskFile, App.getTask());
        }
    }

    public void about() {
        getApplication().showAboutDialog();
    }

    public void quit() {
        Platform.exit();
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
            application.showAlert(resources.getString("lang"), null,
                    resources.getString("lang_change"), Alert.AlertType.INFORMATION);
        }
    }
}
