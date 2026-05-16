import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class MainLoginFrame extends JFrame {

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
    private static final Color BLUE        = new Color(37,  99,  235);
    private static final Color BLUE_LIGHT  = new Color(219, 234, 254);
    private static final Color BLUE_DARK   = new Color(29,  78,  216);
    private static final Color AMBER_LIGHT = new Color(250, 238, 218);

    public MainLoginFrame() {
        setTitle("CoWork Space");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

    // ── PANNEAU GAUCHE (identique à LoginAdminFrame) ──────────
    private JPanel buildLeft() {
        JPanel p = new JPanel();
        p.setBackground(BG_LEFT);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(32, 28, 28, 28));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        // Logo
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        JLabel dot = new JLabel("■");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dot.setForeground(GREEN);
        JLabel name = new JLabel("  CoWork Space");
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(TEXT_DARK);
        brand.add(dot); brand.add(name);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titre = new JLabel("<html>Votre espace de<br>travail collaboratif</html>");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titre.setForeground(TEXT_DARK);
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("<html>Gérez vos membres, réservations<br>et espaces depuis un seul tableau de bord.</html>");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(brand);
        top.add(Box.createVerticalStrut(28));
        top.add(titre);
        top.add(Box.createVerticalStrut(10));
        top.add(sub);
        top.add(Box.createVerticalStrut(22));
        top.add(makeCard(GREEN_LIGHT,  "building", "Espaces de bureaux",        "Open space, bureaux privés, salles"));
        top.add(Box.createVerticalStrut(8));
        top.add(makeCard(AMBER_LIGHT,  "people", "142 membres actifs",         "Freelances, startups, équipes"));
        top.add(Box.createVerticalStrut(8));
        top.add(makeCard(BLUE_LIGHT,   "calendar", "Réservations en temps réel", "Disponibilité instantanée"));

        // Stats
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

    private JPanel makeCard(Color bg, String icon, String title, String desc) {
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_CLR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
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
        IconLabel ic = new IconLabel(icon, bg,iconFg );

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

        card.add(ic,   BorderLayout.WEST);
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

    // ── PANNEAU DROIT — 2 cartes de choix ────────────────────
    private JPanel buildRight() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setPreferredSize(new Dimension(340, 420));

        // Badge
        JPanel badge = makeBadge("● CHOISISSEZ VOTRE ACCÈS", GREEN, GREEN_LIGHT);
        form.add(badge);
        form.add(Box.createVerticalStrut(18));

        JLabel titre = new JLabel("Bienvenue !");
        titre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titre.setForeground(TEXT_DARK);
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(titre);

        JLabel sub = new JLabel("Sélectionnez votre profil pour continuer");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sub);
        form.add(Box.createVerticalStrut(30));

        // Carte Admin
        JPanel adminCard = makeAccessCard(
                "gear", "Administrateur",
                "Gérez membres, espaces,\nréservations et facturation.",
                GREEN, GREEN_LIGHT, GREEN_DARK
        );
        adminCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(adminCard);
        form.add(Box.createVerticalStrut(14));

        // Carte Membre
        JPanel membreCard = makeAccessCard(
                "person", "Membre",
                "Consultez l'historique de vos\nréservations de bureaux et salles.",
                BLUE, BLUE_LIGHT, BLUE_DARK
        );
        membreCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(membreCard);

        // Listeners
        adminCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        adminCard.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                new LoginAdminFrame().setVisible(true);
            }
            @Override public void mouseEntered(MouseEvent e) {
                adminCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(GREEN, 2, true),
                        new EmptyBorder(16, 16, 16, 16)));
            }
            @Override public void mouseExited(MouseEvent e) {
                adminCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                        new EmptyBorder(17, 17, 17, 17)));
            }
        });

        membreCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        membreCard.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                new LoginMembreFrame().setVisible(true);
            }
            @Override public void mouseEntered(MouseEvent e) {
                membreCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BLUE, 2, true),
                        new EmptyBorder(16, 16, 16, 16)));
            }
            @Override public void mouseExited(MouseEvent e) {
                membreCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                        new EmptyBorder(17, 17, 17, 17)));
            }
        });

        outer.add(form);
        return outer;
    }

    private JPanel makeAccessCard(String icon, String titre, String desc,
                                  Color iconColor, Color iconBg, Color borderHover) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(17, 17, 17, 17)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        IconLabel ic = new IconLabel(icon, iconBg, iconColor);


        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel t = new JLabel(titre);
        t.setFont(new Font("Segoe UI", Font.BOLD, 15));
        t.setForeground(TEXT_DARK);
        JLabel d = new JLabel("<html><body style='width:180px'>" + desc + "</body></html>");
        d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        d.setForeground(TEXT_MUTED);
        info.add(t); info.add(Box.createVerticalStrut(3)); info.add(d);

        JLabel arrow = new JLabel("→");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        arrow.setForeground(iconColor);

        card.add(ic,    BorderLayout.WEST);
        card.add(info,  BorderLayout.CENTER);
        card.add(arrow, BorderLayout.EAST);
        return card;
    }

    private JPanel makeBadge(String text, Color fg, Color bg) {
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badge.setMaximumSize(new Dimension(250, 28));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(fg);
        badge.add(lbl);
        return badge;
    }
    // ── Classe utilitaire : icône dessinée en Java 2D ──────────
}
