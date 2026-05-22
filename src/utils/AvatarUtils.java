package utils;

import javafx.beans.binding.Bindings;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public final class AvatarUtils {

    private AvatarUtils() {
    }

    public static void applyCircularAvatar(ImageView imageView, Image image) {
        if (imageView == null || image == null) {
            return;
        }

        imageView.setPreserveRatio(false);
        imageView.setImage(image);

        Circle clip = new Circle();
        clip.centerXProperty().bind(imageView.fitWidthProperty().divide(2.0));
        clip.centerYProperty().bind(imageView.fitHeightProperty().divide(2.0));
        clip.radiusProperty().bind(
                Bindings.min(imageView.fitWidthProperty(), imageView.fitHeightProperty()).divide(2.0)
        );

        imageView.setClip(clip);
    }

    public static Image getDefaultAvatar() {
        return new Image(
                AvatarUtils.class.getResourceAsStream("/resources/UsuarioEjemplo.png")
        );
    }
}