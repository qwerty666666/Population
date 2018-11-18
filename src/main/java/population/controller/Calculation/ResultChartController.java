package population.controller.Calculation;

import javafx.scene.control.*;
import population.component.ChartSeries;
import population.component.TickLabelFormatter;
import population.controller.base.AbstractController;
import population.model.Calculator.CalculationResult;
import population.model.TaskV4;
import population.util.Converter;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.converter.DefaultStringConverter;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ResultChartController extends AbstractController {
    @FXML
    public TableView<ChartSeries> resultsChartSettingsTable;
    @FXML
    public TableColumn<ChartSeries, Boolean> chartSettingsTableVisibilityColumn;
    @FXML
    public TableColumn<ChartSeries, String> chartSettingsTableNameColumn;
    @FXML
    public TableColumn<ChartSeries, Number> chartSettingsTableColorColumn;
    @FXML
    public TableColumn<ChartSeries, Number> chartSettingsTableDashColumn;
    @FXML
    public TableColumn<ChartSeries, Number> chartSettingsTableThicknessColumn;

    @FXML
    private LineChart<Number, Number> resultsChart;
    @FXML
    private AnchorPane resultsChartContainer;

    @FXML
    private TabPane calculationTabPane;
    @FXML
    private Tab calculationChartTab;

    protected ObservableList<ChartSeries> seriesData = FXCollections.observableArrayList();
    protected boolean isZoomingChart = false;
    protected final int[] chartBounds = {0, 100};



    /**************************************************
     *
     *                initialization
     *
     *************************************************/

    @Override
    public void initialize() {
        initSettingsTable();
        initChart();
    }


    protected void initSettingsTable() {
        chartSettingsTableVisibilityColumn.setCellFactory(CheckBoxTableCell.forTableColumn(null, null));
        chartSettingsTableNameColumn.setCellFactory(list -> new population.component.UIComponents.TextFieldTableCell<>(new DefaultStringConverter()));
        chartSettingsTableColorColumn.setCellFactory(ComboBoxTableCell.forTableColumn(Converter.COLOR_STRING_CONVERTER, ChartSeries.Color.LIST));
        chartSettingsTableDashColumn.setCellFactory(ComboBoxTableCell.forTableColumn(Converter.DASH_STRING_CONVERTER, ChartSeries.Dash.LIST));
        chartSettingsTableThicknessColumn.setCellFactory(ComboBoxTableCell.forTableColumn(Converter.THICKNESS_STRING_CONVERTER, ChartSeries.Thickness.LIST));

        resultsChartSettingsTable.setPlaceholder(new Rectangle());
        resultsChartSettingsTable.setItems(seriesData);
    }


    protected void initChart() {
        resultsChart.setCreateSymbols(false);
        resultsChart.setAnimated(false);

        // zoom
        Rectangle zoomRect = new Rectangle();
        zoomRect.setManaged(false);
        zoomRect.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
        resultsChartContainer.getChildren().add(zoomRect);

        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
        resultsChart.setOnMousePressed(event -> {
            mouseAnchor.set(new Point2D(event.getX(), event.getY()));
            zoomRect.setWidth(0);
            zoomRect.setHeight(0);
        });

        resultsChart.setOnMouseDragged(event -> {
            double x = event.getX();
            double y = event.getY();
            zoomRect.setX(Math.min(x, mouseAnchor.get().getX()));
            zoomRect.setY(Math.min(y, mouseAnchor.get().getY()));
            zoomRect.setWidth(Math.abs(x - mouseAnchor.get().getX()));
            zoomRect.setHeight(Math.abs(y - mouseAnchor.get().getY()));
        });

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(getApplication().getThreadFactory());
        int[] zoomedBounds = new int[2];
        ScheduledFuture<?>[] refreshChart = new ScheduledFuture<?>[1];

        getStage().widthProperty().addListener((observable, oldValue, newValue) -> {
            if (refreshChart[0] != null) {
                refreshChart[0].cancel(false);
            }
            refreshChart[0] = executor.schedule(() -> {
                if (seriesData.size() == 0) {
                    return;
                }
                if (isZoomingChart) {
                    setResultsChartBounds(zoomedBounds[0], zoomedBounds[1]);
                } else {
                    resetResultsChartBounds();
                }
                Platform.runLater(this::refreshResultsChart);
            }, 500, TimeUnit.MILLISECONDS);
        });

        NumberAxis xAxis = (NumberAxis) resultsChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) resultsChart.getYAxis();
        TickLabelFormatter tickLabelFormatter = new TickLabelFormatter();
        xAxis.setTickLabelFormatter(tickLabelFormatter);
        yAxis.setTickLabelFormatter(tickLabelFormatter);
        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(100);
        xAxis.setTickUnit(10);

        resultsChart.setOnMouseReleased(event -> {
            double zoomRectWidth = zoomRect.getWidth();
            double zoomRectHeight = zoomRect.getHeight();
            if (zoomRectHeight < 5 || zoomRectWidth < 5 || seriesData.size() == 0) {
                zoomRect.setWidth(0);
                zoomRect.setHeight(0);
                resetResultsChartScale();
                return;
            }

            Point2D chartInScene = resultsChart.localToScene(0, 0);
            Point2D xAxisInScene = xAxis.localToScene(0, 0);
            Point2D yAxisInScene = yAxis.localToScene(0, 0);
            double xScale = xAxis.getScale();
            double yScale = yAxis.getScale();
            double zoomAreaWidth = applyScale(xScale, zoomRectWidth);
            double zoomAreaHeight = applyScale(yScale, zoomRectHeight);

            if (zoomAreaWidth < 0.5 || zoomAreaHeight < 0.5) {
                zoomRect.setWidth(0);
                zoomRect.setHeight(0);
                return;
            }

            double zoomAreaX = applyScale(xScale, zoomRect.getX() - yAxisInScene.getX() - yAxis.getWidth());
            double zoomAreaY = applyScale(yScale, xAxisInScene.getY() - zoomRect.getY() - zoomRectHeight - chartInScene.getY());
            double xLowerBound = xAxis.getLowerBound();
            double yLowerBound = yAxis.getLowerBound();

            yAxis.setAutoRanging(false);

            double xNLowerBound = Math.floor(xLowerBound + zoomAreaX);
            double xNUpperBound = Math.ceil(xNLowerBound + zoomAreaWidth);
            double yNLowerBound = Math.floor(yLowerBound + zoomAreaY);
            double yNUpperBound = Math.ceil(yNLowerBound + zoomAreaHeight);
            xNLowerBound = correctLowerBound(xNLowerBound, zoomAreaWidth);
            xNUpperBound = correctUpperBound(xNUpperBound, zoomAreaWidth);
            yNLowerBound = correctLowerBound(yNLowerBound, zoomAreaHeight);
            yNUpperBound = correctUpperBound(yNUpperBound, zoomAreaHeight);

            zoomedBounds[0] = (int) xNLowerBound - 1;
            zoomedBounds[1] = (int) xNUpperBound + 1;

            setResultsChartBounds(zoomedBounds[0], zoomedBounds[1]);
            refreshResultsChart();

            xAxis.setLowerBound(xNLowerBound);
            xAxis.setUpperBound(xNUpperBound);
            yAxis.setLowerBound(yNLowerBound);
            yAxis.setUpperBound(yNUpperBound);

            updateAxisTickUnit(xAxis);
            updateAxisTickUnit(yAxis);

            zoomRect.setWidth(0);
            zoomRect.setHeight(0);

            isZoomingChart = true;
        });
    }

    protected double correctLowerBound(double lower, double size) {
        if (size > 100) {
            return lower - (lower % 10);
        } else if (size > 50) {
            return lower - (lower % 5);
        } else if (size > 10) {
            return lower - (lower % 2);
        } else {
            return lower;
        }
    }

    protected double correctUpperBound(double upper, double size) {
        if (size > 100) {
            return upper + 10 - (upper % 10);
        } else if (size > 50) {
            return upper + 5 - (upper % 5);
        } else if (size > 10) {
            return upper + 2 - (upper % 2);
        } else {
            return upper;
        }
    }

    protected double applyScale(double scale, double value) {
        return value / Math.abs(scale);
    }

    protected void setResultsChartBounds(int start, int end) {
        chartBounds[0] = start;
        chartBounds[1] = end;
    }

    protected void resetResultsChartScale() {
        resetResultsChartBounds();
        refreshResultsChart();
        resultsChart.getYAxis().setAutoRanging(true);
        isZoomingChart = false;
    }

    protected void resetResultsChartBounds() {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (ChartSeries chartSeries : seriesData) {
            ObservableList<XYChart.Data<Number, Number>> data = chartSeries.getData().getData();
            int size = data.size();
            if (size == 0) {
                continue;
            }
            int startPoint = chartSeries.getStartPoint();
            int endPoint = data.get(size - 1).getXValue().intValue();
            if (startPoint < min) {
                min = startPoint;
            }
            if (endPoint > max) {
                max = endPoint;
            }
        }
        if (min == Integer.MAX_VALUE) {
            min = 0;
        }
        if (max == Integer.MIN_VALUE) {
            max = 100;
        }
        setResultsChartBounds(min, max);
        NumberAxis xAxis = (NumberAxis) resultsChart.getXAxis();
        xAxis.setLowerBound(min);
        xAxis.setUpperBound(max);
        updateAxisTickUnit(xAxis);
    }

    protected void updateAxisTickUnit(NumberAxis axis) {
        double size = Math.abs(axis.getUpperBound() - axis.getLowerBound());
        double tick = Math.ceil(size / 10);
        if (tick > 100) {
            tick = tick - (tick % 100);
        } else if (tick > 10) {
            tick = tick - (tick % 10);
        } else if (tick > 5) {
            tick = tick - (tick % 5);
        } else if (tick > 2) {
            tick = tick - (tick % 2);
        }
        axis.setTickUnit(tick);
    }

    protected void refreshResultsChart() {
        resultsChart.getData().clear();
        if (seriesData.size() > 0) {
            resultsChart.setData(buildChart(getResultsChartWidth()));
            refreshResultsChartStyle();
        }
    }

    protected int getResultsChartWidth() {
        return (int) (Math.ceil(resultsChart.getWidth()));
    }

    protected ObservableList<XYChart.Series<Number, Number>> buildChart(int width) {
        return buildChart(chartBounds[0], chartBounds[1], width);
    }

    protected void refreshResultsChartStyle() {
        resultsChart.applyCss();
        Set<Node> legendSet = resultsChart.lookupAll("Label.chart-legend-item");
        Iterator<Node> iterator = legendSet.iterator();
        for (ChartSeries chartSeries : seriesData) {
            chartSeries.setLinePath(null);
            chartSeries.setLegendLabel(null);
        }
        List<ChartSeries> visible = seriesData.stream().filter(ChartSeries::getVisibility)
                .collect(Collectors.toList());
        for (int i = 0; i < visible.size(); i++) {
            ChartSeries chartSeries = visible.get(i);
            if (iterator.hasNext()) {
                chartSeries.setLegendLabel((Label) iterator.next());
            }
            Set<Node> seriesSet = resultsChart.lookupAll(".series" + i);
            for (Node node : seriesSet) {
                if (node instanceof Path) {
                    chartSeries.setLinePath((Path) node);
                    break;
                }
            }
            chartSeries.refreshStyle();
        }
    }

    protected ObservableList<XYChart.Series<Number, Number>> buildChart(int start, int end, int width) {
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        int minStart = Integer.MAX_VALUE;
        int maxStart = Integer.MIN_VALUE;
        for (ChartSeries chartSeries : seriesData) {
            if (!chartSeries.getVisibility()) {
                continue;
            }
            int startPoint = chartSeries.getStartPoint();
            if (startPoint < minStart) {
                minStart = startPoint;
            }
            if (startPoint > maxStart) {
                maxStart = startPoint;
            }
        }
        if (start < minStart) {
            start = minStart;
        }
        int maxSize = 0;
        for (ChartSeries chartSeries : seriesData) {
            if (!chartSeries.getVisibility()) {
                continue;
            }
            XYChart.Series<Number, Number> series = chartSeries.getData();
            int size = series.getData().size() + chartSeries.getStartPoint();
            if (size > maxSize) {
                maxSize = size;
            }
        }
        if (end > maxSize) {
            end = maxSize;
        }
        setResultsChartBounds(start, end);
        int dataWidth;
        if (start < 0 && end >= 0) {
            dataWidth = -1 * start + end;
        } else {
            dataWidth = Math.abs(end - start);
        }
        float fraction = dataWidth / (float) width;

        if (fraction <= 1) {
            ArrayList<XYChart.Series<Number, Number>> chart = new ArrayList<>(seriesData.size());
            for (ChartSeries chartSeries : seriesData) {
                if (!chartSeries.getVisibility()) {
                    continue;
                }
                XYChart.Series<Number, Number> originalSeries = chartSeries.getData();
                ObservableList<XYChart.Data<Number, Number>> originalData = originalSeries.getData();
                ArrayList<XYChart.Data<Number, Number>> data = new ArrayList<>();
                int localStart = chartSeries.getStartPoint();
                for (int j = start; j < end; j++) {
                    int localIndex = j - localStart;
                    if (localIndex >= 0 && localIndex < originalData.size()) {
                        data.add(originalData.get(localIndex));
                    }
                }
                chart.add(new XYChart.Series<>(originalSeries.getName(), FXCollections.observableList(data)));
            }
            return FXCollections.observableList(chart);
        } else {
            ArrayList<XYChart.Series<Number, Number>> chart = new ArrayList<>(seriesData.size());
            int[] indexes = population.component.Calculator.interpolateIndexes(start, end, width);
            for (ChartSeries chartSeries : seriesData) {
                if (!chartSeries.getVisibility()) {
                    continue;
                }
                XYChart.Series<Number, Number> originalSeries = chartSeries.getData();
                ObservableList<XYChart.Data<Number, Number>> originalData = originalSeries.getData();
                ArrayList<XYChart.Data<Number, Number>> data = new ArrayList<>(indexes.length);
                int localStart = chartSeries.getStartPoint();
                for (int index : indexes) {
                    int localIndex = index - localStart;
                    if (localIndex >= 0 && localIndex < originalData.size()) {
                        data.add(originalData.get(localIndex));
                    }
                }
                chart.add(new XYChart.Series<>(originalSeries.getName(),
                        FXCollections.observableList(data)));
            }
            return FXCollections.observableList(chart);
        }
    }

    /*************************************************
     *
     *              FXML Bindings
     *
     *************************************************/

    @FXML
    public void clear() {
        this.seriesData.clear();
        this.resetResultsChartScale();
    }

    /*************************************************
     *
     *
     *
     *************************************************/


    /**
     * Add charts for calculation result
     */
    public void addChartForCalculationResult(CalculationResult result) {
        this.seriesData.addAll(this.getChartSeriesData(result));
        this.refreshResultsChart();
        this.resetResultsChartScale();
    }


    protected List<ChartSeries> getChartSeriesData(CalculationResult result) {
        TaskV4 task = result.getTask();
        double[][] calculationResult = result.getStatesCount();

        // build X Y coordinates data
        List<XYChart.Series<Number, Number>> chart = new ArrayList<>();
        int startPoint = task.getStartPoint();
        for (int i = 0; i < task.getStates().size(); i++) {
            ObservableList<XYChart.Data<Number, Number>> data = FXCollections.observableArrayList();
            for (int step = 0; step < task.getStepsCount(); step++) {
                data.add(new XYChart.Data<>(step + startPoint, calculationResult[step][i]));
            }

            XYChart.Series<Number, Number> series = new XYChart.Series<>(data);
            series.setName(task.getStates().get(i).getName());
            chart.add(series);
        }

        // set default properties to series
        int colorsCount = ChartSeries.Color.ARRAY.length - 8;
        int dashesCount = ChartSeries.Dash.ARRAY.length;
        int thicknessesCount = ChartSeries.Thickness.ARRAY.length;
        AtomicInteger size = new AtomicInteger(this.seriesData.size());

        return chart.stream()
                .map(series -> {
                    int color = size.get() % colorsCount;
                    int dash = (size.get() / colorsCount) % dashesCount;
                    int thickness = (size.get() / (colorsCount * dashesCount)) % thicknessesCount;
                    ChartSeries chartSeries = new ChartSeries(series, task.getStartPoint(), color, dash, thickness, true);

                    // refresh chart when properties change
                    Stream.of(
                            chartSeries.colorProperty(),
                            chartSeries.dashProperty(),
                            chartSeries.thicknessProperty(),
                            chartSeries.visibilityProperty()
                    )
                            .forEach((prop) -> prop.addListener((observable, oldValue, newValue) -> {
                                refreshResultsChart();
                            }));

                    size.incrementAndGet();

                    return chartSeries;
                })
                .collect(Collectors.toList());
    }


    public void openChartTab() {
        this.calculationTabPane.getSelectionModel().select(this.calculationChartTab);
    }
}