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
package population.util;

import population.model.Result;
import population.model.TableResult;
import population.model.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.collections.ObservableList;

public final class Utils {
    public static final int MAX_PRECISION = 325;
    public static final String DECIMAL_FORMAT_COMMON = buildDecimalFormat(MAX_PRECISION);
    private static final String QUOTE = "\"";
    private static final String DOUBLE_QUOTE = "\"\"";

    private Utils() {
    }

    public static ExecutorService newExecutor(ThreadFactory threadFactory) {
        int processors = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(processors, processors, 0, TimeUnit.NANOSECONDS,
                new LinkedBlockingQueue<>(), threadFactory);
    }

    public static String buildErrorText(Throwable throwable) {
        return buildErrorText(throwable, Integer.MAX_VALUE, "Stack trace:");
    }

    public static String buildErrorText(Throwable throwable, int maxStackTraceSize,
            String localizedStackTraceMessage) {
        for (; ; ) {
            Throwable cause = throwable.getCause();
            if (cause == null) {
                break;
            }
            throwable = cause;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(throwable.getClass().getSimpleName()).append(": ")
                .append(throwable.getLocalizedMessage()).append(System.lineSeparator())
                .append(System.lineSeparator()).append(localizedStackTraceMessage)
                .append(System.lineSeparator());
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (int i = 0; i < stackTrace.length && i < maxStackTraceSize; i++) {
            stringBuilder.append(stackTrace[i]);
            if (i == maxStackTraceSize - 1 && stackTrace.length > maxStackTraceSize) {
                stringBuilder.append("...");
            } else {
                stringBuilder.append(System.lineSeparator());
            }
        }
        return stringBuilder.toString();
    }

    public static String createRepeatingString(char character, int count) {
        char[] chars = new char[count];
        Arrays.fill(chars, character);
        return new String(chars);
    }

    public static String buildDecimalFormat(int scale) {
        if (scale <= 0) {
            return "#";
        } else {
            return "#." + createRepeatingString('#', scale);
        }
    }

    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    public static <T> void refreshList(ObservableList<T> observableList) {
        ArrayList<T> temp = new ArrayList<>(observableList.size());
        temp.addAll(observableList);
        observableList.clear();
        observableList.addAll(temp);
        temp.clear();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void exportResults(ArrayList<Result> results, File file, char columnSeparator,
            char decimalSeparator, String lineSeparator, String encoding,
            ResourceBundle resources) throws IOException {
        if (results == null || file == null || lineSeparator == null || encoding == null ||
                resources == null) {
            return;
        }
        int start = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;
        for (Result result : results) {
            int startPoint = result.getStartPoint();
            int size = result.getTableData().size() + startPoint;
            if (end <= size) {
                end = size;
            }
            if (startPoint < start) {
                start = startPoint;
            }
        }
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        DecimalFormat formatter = new DecimalFormat(Utils.DECIMAL_FORMAT_COMMON);
        formatter.getDecimalFormatSymbols().setDecimalSeparator(decimalSeparator);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), encoding))) {
            writer.append(QUOTE).append(resources.getString("Transitions.Settings.Step")).append(QUOTE)
                    .append(columnSeparator);
            for (int i = 0; i < results.size(); i++) {
                ArrayList<String> headers = results.get(i).getDataNames();
                for (int j = 0; j < headers.size(); j++) {
                    writer.append(QUOTE).append(headers.get(j).replace(QUOTE, DOUBLE_QUOTE))
                            .append(QUOTE);
                    if (i != results.size() - 1 || j != headers.size() - 1) {
                        writer.append(columnSeparator);
                    }
                }
            }
            writer.append(lineSeparator);
            for (int i = start; i < end; i++) {
                boolean empty = true;
                StringBuilder rowBuilder = new StringBuilder();
                TableResult firstExistent = null;
                for (int j = 0; j < results.size(); j++) {
                    Result result = results.get(j);
                    ArrayList<TableResult> data = result.getTableData();
                    int localIndex = i - result.getStartPoint();
                    if (localIndex >= 0 && localIndex < data.size()) {
                        TableResult localResult = data.get(localIndex);
                        for (int k = 0; k < localResult.valueCount(); k++) {
                            rowBuilder.append(QUOTE)
                                    .append(formatter.format(localResult.getValue(k)))
                                    .append(QUOTE);
                            if (j != results.size() - 1 || k != localResult.valueCount() - 1) {
                                rowBuilder.append(columnSeparator);
                            }
                        }
                        if (firstExistent == null) {
                            firstExistent = localResult;
                        }
                        empty = false;
                    } else {
                        int size = result.getDataNames().size();
                        for (int k = 0; k < size; k++) {
                            rowBuilder.append(QUOTE).append("---").append(QUOTE);
                            if (j != results.size() - 1 || k != size - 1) {
                                rowBuilder.append(columnSeparator);
                            }
                        }
                    }
                }
                if (!empty) {
                    writer.append(QUOTE).append(String.valueOf(firstExistent.getNumber()))
                            .append(QUOTE).append(columnSeparator).append(rowBuilder.toString())
                            .append(lineSeparator);
                }
            }
        }
    }

    public static void exportResults(File file, Result result, char columnSeparator,
            char decimalSeparator, String lineSeparator, String encoding,
            ResourceBundle resources) throws IOException {
        ArrayList<Result> resultList = new ArrayList<>(1);
        resultList.add(result);
        exportResults(resultList, file, columnSeparator, decimalSeparator, lineSeparator, encoding,
                resources);
    }

    public static void setDefaultTaskSettings(HashMap<String, String> taskSettings) {
        taskSettings.put(Task.Keys.STEPS_COUNT, String.valueOf(0));
        taskSettings.put(Task.Keys.HIGHER_ACCURACY, String.valueOf(false));
        taskSettings.put(Task.Keys.ALLOW_NEGATIVE, String.valueOf(false));
        taskSettings.put(Task.Keys.COLUMN_SEPARATOR, String.valueOf(','));
        taskSettings.put(Task.Keys.DECIMAL_SEPARATOR,
                String.valueOf(DecimalFormatSymbols.getInstance().getDecimalSeparator()));
        taskSettings.put(Task.Keys.LINE_SEPARATOR, System.lineSeparator());
        taskSettings.put(Task.Keys.ENCODING, Charset.defaultCharset().name());
    }
}
