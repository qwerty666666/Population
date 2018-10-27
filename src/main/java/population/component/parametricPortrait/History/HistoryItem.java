package population.component.parametricPortrait.History;


import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import population.component.UIComponents.IconButton;
import population.component.parametricPortrait.ParametricPortraitNode;



/**
 * Represents item in history
 */
public class HistoryItem extends StackPane {
    /** snapshot thumbnail width */
    private static final double THUMBNAIL_WIDTH = 110;
    /** snapshot thumbnail height */
    private static final double THUMBNAIL_HEIGHT = 110;
    private ParametricPortraitNode parametricPortraitNode;
    /** button for ParametricPortrait thumbnail */
    private IconButton thumbnail;
    /** history to which this item belongs */
    private History history;


    public HistoryItem(History history, ParametricPortraitNode parametricPortraitNode) {
        this.parametricPortraitNode = parametricPortraitNode;
        this.history = history;
        setNail();
        setDeleteButton();
    }


    /**
     * Add delete button to item
     */
    private void setDeleteButton() {
        ImageView iv = new ImageView(new Image(
            getClass().getResourceAsStream("/population/resource/images/remove-icon.png")
        ));
        iv.setFitHeight(17);
        iv.setFitWidth(17);
        IconButton deleteButton = new IconButton(iv);
        deleteButton.setPadding(new Insets(3));
        deleteButton.setOnMouseClicked(event -> this.history.remove(this));

        // set button position to right side of parametricPortraitNode thumbnail
        setAlignment(deleteButton, Pos.TOP_LEFT);
        ChangeListener<? super Number> listener = (observable, oldValue, newValue) ->
            setMargin(deleteButton, new Insets(
                (getHeight() - thumbnail.getHeight()) / 2 + 5,
                0, 0,
                (getWidth()  + thumbnail.getWidth()) / 2 + 5)
            );
        this.widthProperty().addListener(listener);
        this.heightProperty().addListener(listener);
        thumbnail.widthProperty().addListener(listener);
        thumbnail.heightProperty().addListener(listener);

        this.getChildren().add(deleteButton);
    }


    /**
     * Add thumbnail button to item
     */
    private void setNail() {
        ImageView iv = new ImageView(parametricPortraitNode.getThumbnail());
        iv.setFitWidth(THUMBNAIL_WIDTH);
        iv.setFitHeight(THUMBNAIL_HEIGHT);
        thumbnail = new IconButton(iv);

        // put thumbnail to container to apply rounded border to the thumbnail
        BorderPane container = new BorderPane(iv);
        Rectangle clip = new Rectangle(iv.getFitWidth(), iv.getFitHeight());
        clip.setArcWidth(4);
        clip.setArcHeight(4);
        iv.setClip(clip);
        container.setBorder(new Border(new BorderStroke(Color.valueOf("#ccc"),
            BorderStrokeStyle.SOLID, new CornerRadii(3), BorderWidths.DEFAULT)));
        thumbnail.setGraphic(container);

        this.getChildren().add(thumbnail);
    }


    /**
     * Update thumbnail view
     */
    public void updateNailImage() {
        thumbnail.getImageView().setImage(parametricPortraitNode.getThumbnail());
    }


    /**
     * @return parametric portrait associated with item
     */
    public ParametricPortraitNode getParametricPortraitNode() {
        return parametricPortraitNode;
    }


    public IconButton getThumbnail() {
        return thumbnail;
    }
}