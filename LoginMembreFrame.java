import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginMembreFrame extends JFrame {

    private static final Color WHITE       = new Color(255, 255, 255);
    private static final Color BG_LEFT     = new Color(247, 246, 243);
    private static final Color TEXT_DARK   = new Color(44,  44,  42);
    private static final Color TEXT_MED    = new Color(68,  68,  65);
    private static final Color TEXT_MUTED  = new Color(136, 135, 128);
    private static final Color TEXT_HINT   = new Color(180, 178, 169);
    private static final Color BORDER_CLR  = new Color(232, 230, 224);
    private static final Color GREEN       = new Color(29,  158, 117);
    private static final Color GREEN_LIGHT = new Color(225, 245, 238);
    private static final Color GREEN_DARK  = new Color(15,  110, 86);
    private static final Color AMBER_LIGHT = new Color(250, 238, 218);
    private static final Color BLUE        = new Color(37,  99,  235);
    private static final Color BLUE_LIGHT  = new Color(219, 234, 254);
    private static final Color BLUE_DARK   = new Color(29,  78,  216);
    private static final Color ERROR_CLR   = new Color(226, 75,  74);

    private JTextField     emailField;
    private JPasswordField pwField;
    private JLabel         errorLabel;
    private JButton        loginBtn;
    private final MembreDAO membreDAO = new MembreDAO();

    public LoginMembreFrame() {
        setTitle("CoWork Space — Membre");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 580);
        setMinimumSize(new Dimension(860, 540));
        setLocationRelativeTo(null);
        setResizable(true);
        setMinimumSize(new Dimension(860, 540));

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(WHITE);
        setContentPane(root);
        root.add(buildLeft());
        root.add(buildRight());
    }

    // ── PANNEAU GAUCHE (identique) ────────────────────────────
    private JPanel buildLeft() {
        JPanel p = new JPanel();
        p.setBackground(BG_LEFT);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(32, 28, 28, 28));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        JLabel dot = new JLabel("■");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dot.setForeground(GREEN);
        JLabel brandName = new JLabel("  CoWork Space");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        brandName.setForeground(TEXT_DARK);
        brand.add(dot); brand.add(brandName);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titre = new JLabel("<html>Votre espace de<br>travail collaboratif</html>");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titre.setForeground(TEXT_DARK);
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html>Consultez vos réservations<br>et gérez votre espace personnel.</html>");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(brand);
        top.add(Box.createVerticalStrut(28));
        top.add(titre);
        top.add(Box.createVerticalStrut(10));
        top.add(sub);
        top.add(Box.createVerticalStrut(22));
        top.add(makeInfoCard(BLUE_LIGHT,   "building", "Mes bureaux réservés",     "Historique complet"));
        top.add(Box.createVerticalStrut(8));
        top.add(makeInfoCard(GREEN_LIGHT,  "calendar", "Mes salles de réunion",    "Toutes vos réservations"));
        top.add(Box.createVerticalStrut(8));
        top.add(makeInfoCard(AMBER_LIGHT,  "invoice", "Mes factures",             "Suivi de vos paiements"));

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.add(buildStats());
        bottom.add(Box.createVerticalStrut(14));
        JLabel footer = new JLabel("© 2026 CoWork Space — Tous droits réservés");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(TEXT_HINT);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(footer);

        p.add(top,    BorderLayout.NORTH);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private JPanel makeInfoCard(Color bg, String icon, String title, String desc) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_CLR); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        Color iconFg;
        if (bg == GREEN_LIGHT)       iconFg = GREEN_DARK;
        else if (bg == AMBER_LIGHT)  iconFg = new Color(180, 110, 20);
        else if (bg == BLUE_LIGHT)   iconFg = BLUE_DARK;
        else                         iconFg = TEXT_MED;
        IconLabel ic = new IconLabel(icon, bg, iconFg);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.setForeground(TEXT_MED);
        JLabel d = new JLabel(desc);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        d.setForeground(TEXT_MUTED);
        info.add(t); info.add(Box.createVerticalStrut(2)); info.add(d);

        card.add(ic, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 3, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.add(statBox("37",  "Réservations aujourd'hui"));
        row.add(statBox("94%", "Taux d'occupation"));
        row.add(statBox("12",  "Espaces disponibles"));
        return row;
    }

    private JPanel statBox(String num, String label) {
        JPanel box = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_CLR); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel n = new JLabel(num);
        n.setFont(new Font("Segoe UI", Font.BOLD, 18));
        n.setForeground(TEXT_DARK);
        n.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel("<html><div style='width:80px'>" + label + "</div></html>");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l.setForeground(TEXT_HINT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(n); box.add(Box.createVerticalStrut(3)); box.add(l);
        return box;
    }

    // ── PANNEAU DROIT — formulaire ────────────────────────────
    private JPanel buildRight() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(320, 460));

        // Badge bleu
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLUE_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badge.setMaximumSize(new Dimension(180, 26));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLbl = new JLabel("● ACCÈS MEMBRE");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(BLUE_DARK);
        badge.add(badgeLbl);

        form.add(badge);
        form.add(Box.createVerticalStrut(18));

        JLabel titleLbl = new JLabel("Connexion");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(TEXT_DARK);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(titleLbl);

        JLabel subLbl = new JLabel("Accédez à votre espace personnel");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(TEXT_MUTED);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(subLbl);
        form.add(Box.createVerticalStrut(26));

        // Champ Email
        form.add(fieldLabel("ADRESSE EMAIL"));
        form.add(Box.createVerticalStrut(6));
        emailField = makeTextField("membre@coworking.com");
        form.add(emailField);
        form.add(Box.createVerticalStrut(16));

        // Champ Mot de passe
        form.add(fieldLabel("MOT DE PASSE"));
        form.add(Box.createVerticalStrut(6));
        pwField = makePasswordField();
        form.add(pwField);
        form.add(Box.createVerticalStrut(8));

        // Erreur
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(ERROR_CLR);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(14));

        // Bouton connexion
        loginBtn = buildLoginButton();
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(14));

        // Bouton retour
        JButton retourBtn = new JButton("← Retour à l'accueil");
        retourBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        retourBtn.setForeground(TEXT_MUTED);
        retourBtn.setBorderPainted(false);
        retourBtn.setContentAreaFilled(false);
        retourBtn.setFocusPainted(false);
        retourBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        retourBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        retourBtn.addActionListener(e -> {
            dispose();
            new MainLoginFrame().setVisible(true);
        });
        form.add(retourBtn);

        outer.add(form);
        return outer;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(TEXT_HINT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField f = new JTextField(22) {
            @Override protected void paintComponent(Graphics g) { paintBg(g, this); super.paintComponent(g); }
            @Override protected void paintBorder(Graphics g) {}
        };
        styleInput(f);
        // Placeholder
        f.setText(placeholder);
        f.setForeground(TEXT_HINT);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TEXT_DARK); }
            }
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(TEXT_HINT); }
            }
        });
        return f;
    }

    private JPasswordField makePasswordField() {
        JPasswordField f = new JPasswordField(22) {
            @Override protected void paintComponent(Graphics g) { paintBg(g, this); super.paintComponent(g); }
            @Override protected void paintBorder(Graphics g) {}
        };
        styleInput(f);
        f.setEchoChar('•');
        return f;
    }

    private void paintBg(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(WHITE);
        g2.fillRoundRect(1, 1, c.getWidth() - 2, c.getHeight() - 2, 8, 8);
        g2.setColor(c.hasFocus() ? BLUE : BORDER_CLR);
        g2.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, 8, 8);
        g2.dispose();
    }
    private void styleInput(JTextField f) {
        f.setOpaque(false);
        f.setForeground(TEXT_DARK);
        f.setCaretColor(BLUE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(new EmptyBorder(10, 14, 10, 14));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { f.repaint(); }
            public void focusLost(FocusEvent e)   { f.repaint(); }
        });
    }

    private JButton buildLoginButton() {
        JButton btn = new JButton("Se connecter  →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? BLUE_DARK :
                        getModel().isRollover() ? new Color(29, 78, 216) : BLUE;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {}
        };
        btn.setForeground(WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(new EmptyBorder(10, 0, 10, 0));
        btn.addActionListener(e -> handleLogin());
        getRootPane().setDefaultButton(btn);
        return btn;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String mdp   = new String(pwField.getPassword());
        errorLabel.setText(" ");

        if (email.isEmpty() || email.equals("membre@coworking.com")) {
            errorLabel.setText("Veuillez saisir votre email."); return;
        }
        if (mdp.isEmpty()) {
            errorLabel.setText("Veuillez saisir votre mot de passe."); return;
        }

        loginBtn.setEnabled(false);
        loginBtn.setText("Connexion en cours...");

        SwingWorker<Membre, Void> worker = new SwingWorker<>() {
            @Override protected Membre doInBackground() {
                return membreDAO.authentifier(email, mdp);
            }
            @Override protected void done() {
                try {
                    Membre membre = get();
                    if (membre != null) {
                        dispose();
                        SwingUtilities.invokeLater(() -> new MembreUI(membre).setVisible(true));
                    } else {
                        errorLabel.setText("Email ou mot de passe incorrect.");
                    }
                } catch (Exception ex) {
                    errorLabel.setText("Erreur de connexion à la base de données.");
                } finally {
                    loginBtn.setText("Se connecter  →");
                    loginBtn.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    // ── Classe utilitaire : icône dessinée en Java 2D ──────────
    static class IconLabel extends JLabel {
        private final String type;  // "gear", "circle", "square", "calendar", "people", "building"
        private final Color bg, fg;

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
    }
}
