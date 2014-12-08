package net.javainthebox.syobo;

import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.event.ActionListener;
import static java.lang.Math.PI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.transform.Rotate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Syobo extends Application {

    private static final double DISTANCE = 5.0;
    private static Rotate rotate = new Rotate(180, 16.0, 16.0, 0.0, new Point3D(0.0, 1.0, 0.0));

    private ImageView cursor;
    private ImageView syobochim;

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        initImage(root);

        Screen screen = Screen.getPrimary();
        Scene scene = new Scene(root, screen.getBounds().getWidth(), screen.getBounds().getHeight());
        scene.setFill(null);
        scene.setCursor(Cursor.NONE);

        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.show();

        startSwingEDT();
    }

    private void initImage(Group root) {
        syobochim = new ImageView(new Image(getClass().getResource("syobochim.png").toString()));
        root.getChildren().add(syobochim);

        cursor = new ImageView(new Image(getClass().getResource("backpaper.png").toString()));
        root.getChildren().add(cursor);
    }

    private void startSwingEDT() {
        // AWTでマウスの位置を 50 秒ごとに検出
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Timer timer = new Timer(50, e -> {
                    PointerInfo info = MouseInfo.getPointerInfo();

                    Platform.runLater(() -> {
                        updateLocation(info.getLocation().getX(), info.getLocation().getY());
                    });
                });
                timer.start();
            }
        });
    }

    private void updateLocation(double mx, double my) {
        cursor.setTranslateX(mx);
        cursor.setTranslateY(my);

        double tx = syobochim.getTranslateX();
        double ty = syobochim.getTranslateY();

        // 近傍であれば位置の更新を行わない
        double d = (mx - tx) * (mx - tx) + (my - ty) * (my - ty);
        if (d < DISTANCE * DISTANCE) {
            return;
        }

        // カーソルとsyobochimの角度を算出
        double theta = Math.atan2(my - ty, mx - tx);

        // 移動分を算出
        double dx = DISTANCE * Math.cos(theta);
        double dy = DISTANCE * Math.sin(theta);
        syobochim.setTranslateX(tx + dx);
        syobochim.setTranslateY(ty + dy);

        // 角度に応じて回転
        // カーソルの右側にsyobocimが位置している場合は反転
        syobochim.getTransforms().removeIf(trans -> trans.equals(rotate));
        if (theta > PI / 2.0 || theta < -PI / 2.0) {
            syobochim.getTransforms().add(rotate);
            syobochim.setRotate(theta * 180.0 / PI - 180.0);
        } else {
            syobochim.setRotate(theta * 180.0 / PI);
        }
    }

    public static void main(String... args) {
        launch(args);
    }

}
