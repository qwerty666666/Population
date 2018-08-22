package population.component.UIComponents;

import javafx.animation.*;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;


/**
 * ScrollPane with VBox content.
 * Items in VBox can be dragged within that pane.
 */
public class DraggableVerticalScrollPane extends ScrollPane {
    /** max scroll speed while dragging item per sec */
    private static final double MAX_SCROLL_SPEED = 1500;
    /** min scroll speed while dragging item per sec */
    private static final double MIN_SCROLL_SPEED = 20;
    /** position from top and bottom of ScrollPane after which dragging cause scrolling.
     * Value from 0.0 to 1.0 */
    private static final double START_SCROLL_POSITION = 0.15;

    /** scroll was started while dragging item */
    private boolean draggingScrollStarted = false;
    /** time when last dragging scroll occurred */
    private long lastDraggingScrollTime;
    /** used to react only on drag in same DraggableVBox */
    private boolean dragStarted = false;

    /** content of this ScrollPane */
    protected DraggableVBox content;


    /**
     * scrolling on dragging near bounds
     */
    private EventHandler<DragEvent> draggingScroll = new EventHandler<DragEvent>() {
        @Override
        public void handle(DragEvent event) {
            if (!dragStarted)
                return;

            double height = getHeight();
            double upperDraggingScrollBound = height * START_SCROLL_POSITION;
            double lowerDraggingScrollBound = height - height * START_SCROLL_POSITION;
            double mouseY = event.getY();
            boolean mouseInDraggingScrollArea = false;  // cause scrolling
            double distToBound = 0;                     // distance to closest bound if mouse in dragging area

            if (mouseY < upperDraggingScrollBound) {
                mouseInDraggingScrollArea = true;
                distToBound = mouseY - upperDraggingScrollBound;
            } else if (mouseY > lowerDraggingScrollBound) {
                mouseInDraggingScrollArea = true;
                distToBound = mouseY - lowerDraggingScrollBound;
            }

            if (mouseInDraggingScrollArea) {
                long curTime = System.currentTimeMillis();
                if (draggingScrollStarted) {
                    double speed = Math.signum(distToBound) * (MIN_SCROLL_SPEED +
                            Math.abs(distToBound) / upperDraggingScrollBound * (MAX_SCROLL_SPEED - MIN_SCROLL_SPEED));
                    setVvalue(getVvalue() + speed * (curTime - lastDraggingScrollTime) / 1000 / content.getHeight());
                } else {
                    draggingScrollStarted = true;
                }
                lastDraggingScrollTime = curTime;
            } else {
                draggingScrollStarted = false;
            }
        }
    };


    public DraggableVerticalScrollPane() {
        content = new DraggableVBox();
        this.setContent(content);
        this.setOnDragOver(draggingScroll);
    }


    /**
     * add item to children list
     * all draggable elements must be added by this method
     * @param index index at which item will be inserted
     * @param node item
     */
    public void addItem(int index, Region node) {
        content.addItem(index, node);
    }


    /**
     * VBox with draggable items
     */
    public class DraggableVBox extends VBox {
        /** index of dragged node in getChildren()*/
        private int draggingSourceNodeIndex;
        /** empty node replaces draggingNode*/
        private final Pane emptyNode = new Pane();
        /** index of emptyNode in children. Used for performance */
        private int emptyNodeIndex;
        /** animate extend empty space for inserted node  */
        private final Timeline nodeInsertTimeline = new Timeline();


        DraggableVBox() {
            super();

            emptyNode.setOnDragDropped(onDragDropped);
            emptyNode.setOnDragOver(onDragOver);
        }


        /**
         * animate extend empty space for inserted node
         * @param index index at which insert node
         * @param node node
         */
        private void insertNodeWithGrowSpaceAnimation(int index, Region node) {
            // get node size
            getChildren().add(index, node);
            node.applyCss();
            layout();
            node.setVisible(false);
            double nodeHeight = node.getHeight();

            // animate empty node grow height for extend empty space in inserting node position
            final Pane emptyNode = new Pane();
            emptyNode.setMinSize(node.getWidth(), 0);
            getChildren().add(index, emptyNode);

            // stop last insert animation
            if (nodeInsertTimeline.getOnFinished() != null
                    && nodeInsertTimeline.getStatus() != Animation.Status.STOPPED) {
                nodeInsertTimeline.getOnFinished().handle(null);
            }
            nodeInsertTimeline.stop();
            nodeInsertTimeline.getKeyFrames().clear();

            // start new animation
            nodeInsertTimeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(150),
                    new KeyValue(emptyNode.minHeightProperty(), nodeHeight, Interpolator.EASE_IN)
            ));
            nodeInsertTimeline.setOnFinished(event -> {
                getChildren().remove(emptyNode);
                node.setVisible(true);
            });
            nodeInsertTimeline.play();
        }


        /**
         * add item to children list
         * all draggable elements must be added by this method
         * @param index index at which item will be inserted
         * @param node item
         */
        void addItem(int index, Region node) {
            insertNodeWithGrowSpaceAnimation(index, node);

            node.setOnDragOver(onDragOver);
            node.setOnDragDropped(onDragDropped);

            node.setOnDragDetected(event -> {
                dragStarted = true;

                // init dragboard
                final Dragboard dragboard = node.startDragAndDrop(TransferMode.MOVE);
                dragboard.setDragView(node.snapshot(null, null), event.getX(), event.getY());
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.putString("");
                dragboard.setContent(clipboardContent);

                // replace node with emptyNode
                draggingSourceNodeIndex = emptyNodeIndex = getChildren().indexOf(node);
                emptyNode.setMinSize(node.getWidth(), node.getHeight());
                getChildren().remove(emptyNode);
                getChildren().set(draggingSourceNodeIndex, emptyNode);

                event.consume();
            });

            node.setOnDragDone(event -> {
                dragStarted = false;
                getChildren().remove(emptyNode);

                // if transfer failed or occurs out of application then restore it in initial position
                Point2D mouseLoc = new Point2D(event.getScreenX(), event.getScreenY());
                Window window = getScene().getWindow();
                Rectangle2D windowBounds = new Rectangle2D(window.getX(), window.getY(),
                        window.getWidth(), window.getHeight());
                if ((event.getTransferMode() != TransferMode.MOVE || !windowBounds.contains(mouseLoc))
                        && !getChildren().contains(node)) {
                    getChildren().add(draggingSourceNodeIndex, node);
                }

                event.consume();
            });

            node.setOnDragEntered(event -> {
                // move empty node to target node
                if (dragStarted && event.getGestureSource() != node) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    int ind = getChildren().indexOf(node);
                    if (ind != emptyNodeIndex) {
                        getChildren().remove(emptyNodeIndex);
                        getChildren().add(ind, emptyNode);
                        emptyNodeIndex = ind;
                    }
                }
            });
        }


        private final EventHandler<DragEvent> onDragDropped = event -> {
            // add source node to children at position of target node
            boolean success = false;
            if (dragStarted && event.getGestureTarget() instanceof Node) {
                success = true;
                getChildren().add(getChildren().indexOf(event.getGestureTarget()), (Node) event.getGestureSource());
            }
            event.setDropCompleted(success);
            event.consume();
        };

        private final EventHandler<DragEvent> onDragOver = event -> {
            if (dragStarted && event.getGestureSource() != event.getPickResult().getIntersectedNode()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
        };

    }


}