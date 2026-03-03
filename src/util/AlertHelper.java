package util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * AlertHelper — fully custom, frameless (no OS chrome) modal dialogs.
 *
 * Usage:
 * boolean ok = AlertHelper.confirm(owner, "Title", "Header", "Body text");
 * boolean ok = AlertHelper.danger(owner, "Title", "Header", "Body text");
 * boolean ok = AlertHelper.showForm(owner, "Title", "Header", gridNode);
 *
 * Unlike JavaFX's Alert, these have NO OS window frame — fully custom look.
 */
public class AlertHelper {

    // ── Button style helpers ──────────────────────────────────────────────

    private static String okNormal(String gradient) {
        return "-fx-background-color: " + gradient + "; -fx-text-fill: white;" +
                " -fx-font-weight: bold; -fx-padding: 9 26; -fx-background-radius: 8;" +
                " -fx-cursor: hand; -fx-font-size: 12px;";
    }

    private static String okHover(String gradient) {
        return okNormal(gradient) +
                " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 2);";
    }

    private static final String CANCEL_NORMAL = "-fx-background-color: transparent; -fx-text-fill: #7f8c8d;" +
            " -fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8;" +
            " -fx-border-radius: 8; -fx-border-color: #bdc3c7; -fx-border-width: 1;" +
            " -fx-cursor: hand; -fx-font-size: 12px;";

    private static final String CANCEL_HOVER = "-fx-background-color: #f0f0f5; -fx-text-fill: #555;" +
            " -fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8;" +
            " -fx-border-radius: 8; -fx-border-color: #bdc3c7; -fx-border-width: 1;" +
            " -fx-cursor: hand; -fx-font-size: 12px;";

    // ── Public API ────────────────────────────────────────────────────────

    /** Standard confirmation – blue/purple OK button. */
    public static boolean confirm(Window owner, String title, String header, String body) {
        return confirm(owner, title, header, body, "Confirm");
    }

    /** Standard confirmation with a custom OK label. */
    public static boolean confirm(Window owner, String title, String header, String body, String okLabel) {
        return build(owner, false, title, header, body, null, okLabel);
    }

    /** Danger confirmation – red OK button (for destructive actions). */
    public static boolean danger(Window owner, String title, String header, String body) {
        return danger(owner, title, header, body, "Delete");
    }

    /** Danger confirmation with a custom OK label. */
    public static boolean danger(Window owner, String title, String header, String body, String okLabel) {
        return build(owner, true, title, header, body, null, okLabel);
    }

    /** Form dialog – pass any Node as the inner content (e.g. a GridPane). */
    public static boolean showForm(Window owner, String title, String header, Node formContent) {
        return build(owner, false, title, header, null, formContent, "Save Changes");
    }

    // ── Core builder ──────────────────────────────────────────────────────

    private static boolean build(Window owner, boolean isDanger,
            String title, String header,
            String body, Node formContent,
            String okLabel) {

        boolean[] confirmed = { false };
        double[] drag = { 0, 0 };

        // ── Stage: TRANSPARENT removes all OS window chrome ──────────────
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null)
            stage.initOwner(owner);

        // ── Root: transparent + padding for drop-shadow room ─────────────
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(16));

        // ── Card ──────────────────────────────────────────────────────────
        VBox card = new VBox(0);
        card.setMinWidth(420);
        card.setMaxWidth(520);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.28));
        shadow.setRadius(30);
        shadow.setOffsetY(8);
        shadow.setSpread(0.04);
        card.setEffect(shadow);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16;");

        // ── Header ────────────────────────────────────────────────────────
        String headerGrad = isDanger
                ? "linear-gradient(to right, #c0392b, #e74c3c)"
                : "linear-gradient(to right, #302b63, #24243e)";
        String icon = isDanger ? "⚠" : "💬";

        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(18, 20, 18, 22));
        headerBox.setStyle("-fx-background-color: " + headerGrad +
                "; -fx-background-radius: 16 16 0 0;");

        // Drag the whole dialog by pressing on the header
        headerBox.setOnMousePressed(e -> {
            drag[0] = e.getSceneX();
            drag[1] = e.getSceneY();
        });
        headerBox.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - drag[0]);
            stage.setY(e.getScreenY() - drag[1]);
        });

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px;");

        VBox titleBlock = new VBox(2);
        Label categoryLbl = new Label(title.toUpperCase());
        categoryLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.50); -fx-font-size: 10px;" +
                " -fx-font-weight: bold;");
        Label headerLbl = new Label(header);
        headerLbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        headerLbl.setWrapText(true);
        headerLbl.setMaxWidth(330);
        titleBlock.getChildren().addAll(categoryLbl, headerLbl);
        HBox.setHgrow(titleBlock, Priority.ALWAYS);

        // ✕ button (closes without confirming)
        Button closeX = new Button("✕");
        String xBase = "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: rgba(255,255,255,0.55);" +
                " -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 3 8; -fx-background-radius: 6;";
        String xHov = "-fx-background-color: rgba(255,255,255,0.20); -fx-text-fill: white;" +
                " -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 3 8; -fx-background-radius: 6;";
        closeX.setStyle(xBase);
        closeX.setOnMouseEntered(e -> closeX.setStyle(xHov));
        closeX.setOnMouseExited(e -> closeX.setStyle(xBase));
        closeX.setOnAction(e -> stage.close());

        headerBox.getChildren().addAll(iconLbl, titleBlock, closeX);

        // ── Body ──────────────────────────────────────────────────────────
        VBox bodyBox = new VBox(14);
        bodyBox.setPadding(new Insets(22, 26, 18, 26));

        if (body != null && !body.isBlank()) {
            Label bodyLbl = new Label(body);
            bodyLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a4a6a; -fx-line-spacing: 3;");
            bodyLbl.setWrapText(true);
            bodyLbl.setMaxWidth(460);
            bodyBox.getChildren().add(bodyLbl);
        }
        if (formContent != null) {
            bodyBox.getChildren().add(formContent);
        }

        // ── Thin divider ──────────────────────────────────────────────────
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #eceff4;");

        // ── Button bar ────────────────────────────────────────────────────
        HBox btnBar = new HBox(12);
        btnBar.setAlignment(Pos.CENTER_RIGHT);
        btnBar.setPadding(new Insets(14, 22, 18, 22));
        btnBar.setStyle("-fx-background-color: #f8f9fc; -fx-background-radius: 0 0 16 16;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(CANCEL_NORMAL);
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(CANCEL_HOVER));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(CANCEL_NORMAL));
        cancelBtn.setOnAction(e -> {
            confirmed[0] = false;
            stage.close();
        });

        String okGradNorm = isDanger
                ? "linear-gradient(to right, #e74c3c, #c0392b)"
                : "linear-gradient(to right, #3498db, #8e44ad)";
        String okGradHov = isDanger
                ? "linear-gradient(to right, #c0392b, #a93226)"
                : "linear-gradient(to right, #2980b9, #7d3c98)";

        Button okBtn = new Button(okLabel);
        okBtn.setStyle(okNormal(okGradNorm));
        okBtn.setOnMouseEntered(e -> okBtn.setStyle(okHover(okGradHov)));
        okBtn.setOnMouseExited(e -> okBtn.setStyle(okNormal(okGradNorm)));
        okBtn.setOnAction(e -> {
            confirmed[0] = true;
            stage.close();
        });

        // Default button so Enter key confirms
        okBtn.setDefaultButton(true);
        cancelBtn.setCancelButton(true);

        btnBar.getChildren().addAll(cancelBtn, okBtn);

        // ── Assemble ──────────────────────────────────────────────────────
        card.getChildren().addAll(headerBox, bodyBox, divider, btnBar);
        root.getChildren().add(card);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.sizeToScene();

        // Center relative to owner window
        stage.setOnShown(e -> {
            if (owner != null) {
                stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
                stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
            }
        });

        stage.showAndWait();
        return confirmed[0];
    }
}
