package population.model.ColorGenerator;

import javafx.scene.paint.Color;

import java.lang.reflect.Field;
import java.util.*;


/**
 * Generate colors sequence from javafx.scene.paint.Color constants
 */
public class SimpleColorGenerator implements ColorGenerator {
    private static List<Color> defaultColors;
    static {
        /*
         * set defaultColors from javafx.scene.paint.Color constants
         */
        // this colors will be first in color list
        List<Color> customColors = new ArrayList<>(Arrays.asList(
            Color.RED,
            Color.YELLOW,
            Color.GREEN,
            Color.BLUE,
            Color.LIME,
            Color.FIREBRICK,
            Color.DARKMAGENTA,
            Color.MAROON)
        );

        try {
            List<Color> colors = new ArrayList<>();
            Class colorClass = Class.forName("javafx.scene.paint.Color");

            if (colorClass == null) {
                throw new ClassNotFoundException();
            }

            Field[] fields = colorClass.getFields();
            for (Field field : fields) {
                Object obj = field.get(null);
                if (obj instanceof Color) {
                    colors.add((Color) obj);
                }
            }
            defaultColors = colors;

            // use custom colors first
            defaultColors.remove(Color.TRANSPARENT);
            int i = 0;
            for (Color color: customColors) {
                Collections.swap(defaultColors, i++, defaultColors.indexOf(color));
            }

        } catch (Exception e) {
            defaultColors = customColors;
        }
    }
    private int nextIndex = 0;


    @Override
    public Color getNext() {
        return defaultColors.get(nextIndex++ % defaultColors.size());
    }
}

