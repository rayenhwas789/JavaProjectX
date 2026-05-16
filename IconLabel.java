import javax.swing.*;
import java.awt.*;

class IconLabel extends JLabel {
    private String type;  // "gear", "circle", "square", "calendar", "people", "building"
    private Color bg, fg;

    IconLabel(String type, Color bg, Color fg) {
        super();
        this.type = type; this.bg = bg; this.fg = fg;
        setPreferredSize(new Dimension(36, 36));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        // Fond arrondi
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, w, h, 10, 10);

        g2.setColor(fg);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int cx = w / 2, cy = h / 2;

        switch (type) {
            case "gear" -> drawGear(g2, cx, cy);
            case "circle" -> {
                g2.drawOval(cx - 9, cy - 9, 18, 18);
                g2.fillOval(cx - 4, cy - 4, 8, 8);
            }
            case "square" -> {
                g2.setStroke(new BasicStroke(2.5f));
                g2.fillRect(cx - 6, cy - 6, 12, 12);
            }
            case "building" -> {
                g2.fillRect(cx - 8, cy - 9, 16, 14);
                g2.fillRect(cx - 3, cy + 5, 6, 4);
                g2.setColor(bg);
                g2.fillRect(cx - 5, cy - 6, 3, 3);
                g2.fillRect(cx + 2, cy - 6, 3, 3);
                g2.fillRect(cx - 5, cy - 1, 3, 3);
                g2.fillRect(cx + 2, cy - 1, 3, 3);
            }
            case "people" -> {
                g2.fillOval(cx - 8, cy - 9, 7, 7);
                g2.fillOval(cx + 1, cy - 9, 7, 7);
                g2.fillArc(cx - 11, cy - 2, 10, 8, 180, 180);
                g2.fillArc(cx + 1,  cy - 2, 10, 8, 180, 180);
            }
            case "calendar" -> {
                g2.drawRoundRect(cx - 9, cy - 8, 18, 16, 3, 3);
                g2.fillRect(cx - 5, cy - 10, 2, 5);
                g2.fillRect(cx + 3, cy - 10, 2, 5);
                g2.fillRect(cx - 7, cy - 2, 14, 1);
                // Petite croix = date
                g2.fillRect(cx - 2, cy + 2, 4, 1);
                g2.fillRect(cx, cy,   1, 4);
            }
            case "person" -> {
                // Tête
                g2.fillOval(cx - 5, cy - 10, 10, 10);
                // Corps
                g2.fillArc(cx - 8, cy + 1, 16, 10, 180, 180);
            }
            case "chair" -> {
                // Dossier
                g2.fillRoundRect(cx - 6, cy - 10, 12, 7, 3, 3);
                // Assise
                g2.fillRoundRect(cx - 7, cy - 1, 14, 4, 2, 2);
                // Pied gauche
                g2.fillRect(cx - 5, cy + 3, 2, 7);
                // Pied droit
                g2.fillRect(cx + 3, cy + 3, 2, 7);
                // Barre centrale
                g2.fillRect(cx - 1, cy - 3, 2, 4);
            }
            case "invoice" -> {
                // Feuille de papier
                g2.setColor(fg);
                g2.fillRoundRect(cx - 8, cy - 10, 14, 18, 2, 2);
                // Coin plié (oreille)
                g2.setColor(bg);
                g2.fillRect(cx + 2, cy - 10, 4, 4);
                g2.setColor(fg);
                g2.drawLine(cx + 2, cy - 10, cx + 6, cy - 6);
                // Lignes de texte simulées
                g2.setColor(bg);
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 6, cy - 4, cx + 3, cy - 4);
                g2.drawLine(cx - 6, cy,     cx + 3, cy    );
                g2.drawLine(cx - 6, cy + 4, cx + 1, cy + 4);
            }
            case "delete" -> {
                // Couvercle
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - 7, cy - 6, cx + 7, cy - 6);
                g2.drawLine(cx - 3, cy - 8, cx + 3, cy - 8);
                // Corps poubelle
                g2.drawRoundRect(cx - 6, cy - 5, 12, 13, 2, 2);
                // Lignes intérieures
                g2.drawLine(cx - 2, cy - 2, cx - 2, cy + 5);
                g2.drawLine(cx + 2, cy - 2, cx + 2, cy + 5);
            }

            case "add" -> {
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Croix +
                g2.drawLine(cx, cy - 5, cx, cy + 5);
                g2.drawLine(cx - 5, cy, cx + 5, cy);
            }

            case "edit" -> {
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Ligne (texte)
                g2.drawLine(cx - 7, cy + 6, cx + 5, cy + 6);

                // Stylo
                g2.drawLine(cx - 3, cy + 3, cx + 6, cy - 6);

                // Pointe
                g2.drawLine(cx + 6, cy - 6, cx + 8, cy - 8);
            }

            case "download" -> {
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Flèche vers le bas
                g2.drawLine(cx, cy - 8, cx, cy + 4);
                g2.drawLine(cx - 5, cy - 1, cx, cy + 4);
                g2.drawLine(cx + 5, cy - 1, cx, cy + 4);
                // Ligne du bas (plateau)
                g2.drawLine(cx - 7, cy + 8, cx + 7, cy + 8);
            }

            case "availability" -> {
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Cercle extérieur
                g2.drawOval(cx - 8, cy - 8, 16, 16);
                // Aiguilles horloge
                g2.drawLine(cx, cy, cx, cy - 5);      // heure
                g2.drawLine(cx, cy, cx + 4, cy + 2);  // minute
                // Petit point centre
                g2.fillOval(cx - 1, cy - 1, 3, 3);
            }
            case "dashboard" -> {
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // 4 carrés égaux en grille 2x2
                g2.drawRect(cx - 8, cy - 8, 6, 6); // haut gauche
                g2.drawRect(cx + 2, cy - 8, 6, 6); // haut droite
                g2.drawRect(cx - 8, cy + 2, 6, 6); // bas gauche
                g2.drawRect(cx + 2, cy + 2, 6, 6); // bas droite
            }
        }
        g2.dispose();
    }

    private void drawGear(Graphics2D g2, int cx, int cy) {
        // Corps central
        g2.fillOval(cx - 6, cy - 6, 12, 12);
        g2.setColor(g2.getBackground()); // trou
        // Dents (8 rectangles rotatifs)
        Graphics2D g3 = (Graphics2D) g2.create();
        g3.translate(cx, cy);
        for (int i = 0; i < 8; i++) {
            g3.rotate(Math.PI / 4);
            g3.setColor(fg);
            g3.fillRoundRect(-2, 7, 4, 5, 2, 2);
        }
        g3.dispose();
        // Trou central
        g2.setColor(bg);
        g2.fillOval(cx - 3, cy - 3, 6, 6);
    }
    // Dans IconLabel.java
    void setColors(Color bg, Color fg) {
        this.bg = bg;
        this.fg = fg;
        repaint();
    }
}
