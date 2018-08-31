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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import population.util.Resources.StringResource;

public final class TransitionType {
    public static final int LINEAR = 0;
    public static final int SOLUTE = 1;
    public static final int BLEND = 2;
    public static final ObservableList<Number> TYPES =
            FXCollections.observableArrayList(new Number[]{LINEAR, SOLUTE, BLEND});

    private TransitionType() {
    }

    public static String getName(int type) {
        switch (type) {
            case LINEAR: {
                return StringResource.getString("Transitions.Type.Linear");
            }
            case SOLUTE: {
                return StringResource.getString("Transitions.Type.Solute");
            }
            case BLEND: {
                return StringResource.getString("Transitions.Type.Blend");
            }
            default: {
                return StringResource.getString("App.UnnamedStub");
            }
        }
    }
}
