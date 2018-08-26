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
package population.model;

import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class TransitionMode {
    public static final int SIMPLE = 0;
    public static final int RETAINING = 1;
    public static final int REMOVING = 2;
    public static final int RESIDUAL = 3;
    public static final int INHIBITOR = 4;
    public static final ObservableList<Number> MODES = FXCollections
            .observableArrayList(new Number[]{SIMPLE, RETAINING, REMOVING, RESIDUAL, INHIBITOR});

    private TransitionMode() {
    }

    public static String getName(int mode, ResourceBundle resources) {
        switch (mode) {
            case SIMPLE: {
                return resources.getString("States.Mode.Simple");
            }
            case RETAINING: {
                return resources.getString("mode_retaining");
            }
            case REMOVING: {
                return resources.getString("mode_removing");
            }
            case RESIDUAL: {
                return resources.getString("States.Mode.Residual");
            }
            case INHIBITOR: {
                return resources.getString("States.Mode.Inhibitor");
            }
            default: {
                return resources.getString("App.UnnamedStub");
            }
        }
    }
}
