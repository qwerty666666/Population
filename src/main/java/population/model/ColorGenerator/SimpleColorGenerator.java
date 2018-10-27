package population.model.ColorGenerator;

import javafx.scene.paint.Color;

import java.lang.reflect.Field;
import java.util.*;


/**
 * Generate colors sequence from javafx.scene.paint.Color constants
 */
public class SimpleColorGenerator implements ColorGenerator {
    private static List<Color> colors;
    static {
        /*
         * set colors from javafx.scene.paint.Color constants
         */
        // this colors will be first in color list
        List<Color> defaultColors = new ArrayList<>(Arrays.asList(
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
            List<Color> reflectionColors = new ArrayList<>();
            Class colorClass = Class.forName("javafx.scene.paint.Color");

            if (colorClass == null) {
                throw new ClassNotFoundException();
            }

            Field[] fields = colorClass.getFields();
            for (Field field : fields) {
                Object obj = field.get(null);
                if (obj instanceof Color) {
                    reflectionColors.add((Color) obj);
                }
            }
            SimpleColorGenerator.colors = reflectionColors;

            // use default colors first
            SimpleColorGenerator.colors.remove(Color.TRANSPARENT);
            int i = 0;
            for (Color color: defaultColors) {
                Collections.swap(SimpleColorGenerator.colors, i++, SimpleColorGenerator.colors.indexOf(color));
            }

        } catch (Exception e) {
            SimpleColorGenerator.colors = defaultColors;
        }
    }
    private int nextIndex = 0;


    @Override
    public Color getNext() {
        return colors.get(nextIndex++ % colors.size());
    }
}

