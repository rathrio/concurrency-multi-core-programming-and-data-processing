import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Ex1 {
    private static int[][] pixels;
    private static int width;
    private static int height;

    private static void loadImage(String path) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));

            // Remove file type info
            lines.remove(0);

            // Remove comment
            lines.remove(0);

            String dimensions = lines.remove(0);

            // Remove max value
            lines.remove(0);

            String[] dimensionsArray = dimensions.split("\\s");

            width = Integer.parseInt(dimensionsArray[0]);
            height = Integer.parseInt(dimensionsArray[1]);

            pixels = new int[width][height];

            int x = 0;
            int y = 0;

            for (String line : lines) {
                int value = Integer.parseInt(line);
                pixels[x][y] = value;

                x = (x + 1) % width;
                if (x == 0) {
                    y++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        loadImage("images/lena.pgm");
    }
}
