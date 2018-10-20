package population.model.ColorGenerator;

import javafx.scene.paint.Color;

/**
 * Generate colors sequence
 */
public interface ColorGenerator {
    /**
     * @return next generated color
     */
    Color getNext();
}
