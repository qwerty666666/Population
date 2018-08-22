package population.component.UIComponents;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 * Button filling with Image
 */
public class IconButton extends Button {
    private ImageView imageView;

    public IconButton(ImageView image) {
        imageView = image;
        setGraphic(image);
        this.setPadding(new Insets(5, 7, 5, 7));
        this.getStyleClass().add("icon-button");
    }

    public ImageView getImageView() {
        return imageView;
    }
}
