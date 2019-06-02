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

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import population.App;
import population.controller.base.AbstractController;

public class HelpPopupController extends AbstractController {
    private Image image;
    public ImageView appIcon;

    @Override
    public void initialize() {
        appIcon.setImage(image);
    }

    public final void setImage(Image image) {
        this.image = image;
    }

    public void close() {
        getStage().close();
    }
}
