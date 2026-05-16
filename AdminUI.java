import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AdminUI – Conteneur principal de l'application CoWorking.
 *
 * Contient : sidebar, header, status bar et un CardLayout central.
 * Les panels de contenu (SallesPanel, BureauxPanel, etc.) sont enregistrés
 * via registerPanel() et affichés dynamiquement via navigateTo().
 */
public class AdminUI extends JFrame {

    // ===== SECTION KEYS =====
    public static final String SEC_DASHBOARD    = "Dashboard";
    public static final String SEC_SALLES       = "Salles de réunion";
    public static final String SEC_BUREAUX      = "Bureaux";
    public static final String SEC_MEMBRES      = "Membres";
    public static final String SEC_RESERVATIONS = "Réservations";
    public static final String SEC_FACTURES     = "Factures";

    // ===== STATE =====
    private boolean darkMode      = false;
    private String  activeSection = SEC_DASHBOARD;   // ← démarre sur le dashboard

    // ===== LAYOUT =====
    private JPanel     sidebar, mainPanel, topBar, statusBar, cardContainer;
    private CardLayout cardLayout;
    private JLabel     pageTitle, pageSub, lblStatus;
    private JTextField txtSearch;
    private JButton    btnToggle;

    // Nav buttons  : sectionKey → button
    private final Map<String, JButton> navMap = new LinkedHashMap<>();

    // Content panels : sectionKey → panel  (enregistrés via registerPanel)
    private final Map<String, JPanel>  contentPanels = new LinkedHashMap<>();
    private DashboardPanel dashboardPanel;

    // ===== COULEURS LIGHT =====
    private final Color L_SIDEBAR  = new Color(30, 36, 51);
    private final Color L_ACT      = new Color(37, 123, 235);
    private final Color L_NAV_TEXT = new Color(156, 163, 175);
    private final Color L_BG       = new Color(249, 250, 251);
    private final Color L_WHITE    = Color.WHITE;
    private final Color L_BORDER   = new Color(229, 231, 235);
    private final Color L_TEXT     = new Color(17, 24, 39);
    private final Color L_SUBTEXT  = new Color(107, 114, 128);

    // ===== COULEURS DARK =====
    private final Color D_SIDEBAR  = new Color(15, 20, 30);
    private final Color D_BG       = new Color(17, 24, 39);
    private final Color D_WHITE    = new Color(26, 35, 50);
    private final Color D_BORDER   = new Color(45, 55, 72);
    private final Color D_TEXT     = new Color(237, 242, 247);
    private final Color D_SUBTEXT  = new Color(113, 128, 150);

    // ===== COULEURS FIXES =====
    private final Color BLUE   = new Color(37, 99, 235);
    private final Color GREEN  = new Color(22, 163, 74);
    private final Color RED    = new Color(220, 38, 38);
    private final Color ORANGE = new Color(249, 115, 22);
    private final Color PURPLE = new Color(124, 58, 237);
    private final Color TEAL   = new Color(13, 148, 136);
    private final Color WHITE  = Color.WHITE;

    // ===================== INTERFACE SEARCHABLE =====================
    /**
     * Tout panel qui supporte la recherche depuis le header implémente cette interface.
     * Exemple : public class SallesPanel extends JPanel implements AdminUI.Searchable { ... }
     */
    public interface Searchable {
        void onSearch(String query);
    }

    // ===================== CONSTRUCTEUR =====================
    public AdminUI() {
        setTitle("CoWorking — Administration");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        buildUI();
        setVisible(true);
        registerPanels();
    }

    /**
     * Enregistre les panels de contenu avant la construction de l'UI.
     * Remplacez les PlaceholderPanel par vos vraies classes au fur et à mesure.
     */
    private void registerPanels() {
        // ── Dashboard en premier ──────────────────────────────────────────────
        dashboardPanel = new DashboardPanel(this);
        registerPanel(SEC_DASHBOARD,    dashboardPanel);
        // ── Panels métier ─────────────────────────────────────────────────────
        registerPanel(SEC_SALLES, new SallesPanel(this));
        registerPanel(SEC_BUREAUX, new BureauPannel(this));
        registerPanel(SEC_MEMBRES, new MembrePanel(this));
        registerPanel(SEC_RESERVATIONS, new ReservationPanel(this));
        registerPanel(SEC_FACTURES, new FacturePanel(this));

        // Afficher le dashboard au démarrage
        navigateTo(SEC_DASHBOARD);
    }

    /**
     * Enregistre (ou remplace) un panel pour une section donnée.
     * Peut être appelé avant ou après buildUI().
     *
     * Exemple d'utilisation depuis l'extérieur :
     *   adminUI.registerPanel(AdminUI.SEC_SALLES, new SallesPanel(adminUI));
     */
    public void registerPanel(String sectionKey, JPanel panel) {
        contentPanels.put(sectionKey, panel);
        if (cardContainer != null) {
            cardContainer.add(panel, sectionKey);
        }
    }

    // ===================== BUILD UI =====================
    private void buildUI() {
        getContentPane().removeAll();
        navMap.clear();

        sidebar   = buildSidebar();
        mainPanel = buildMain();

        add(sidebar,   BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        navigateTo(activeSection);
        revalidate();
        repaint();
    }

    // ===================== SIDEBAR =====================
    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setBackground(bg(L_SIDEBAR, D_SIDEBAR));
        p.setPreferredSize(new Dimension(210, 0));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JLabel logo = new JLabel("CoWorking");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logo.setForeground(WHITE);
        logo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel logoSub = new JLabel("Espace de travail");
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logoSub.setForeground(new Color(107, 114, 128));
        logoSub.setAlignmentX(LEFT_ALIGNMENT);
        logoSub.setBorder(BorderFactory.createEmptyBorder(2, 0, 14, 0));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(55, 65, 81));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        p.add(logo);
        p.add(logoSub);
        p.add(sep);
        p.add(Box.createVerticalStrut(12));
        p.add(sidebarSectionLabel("MENU"));

        String[][] menuItems = {
                {SEC_DASHBOARD,    "dashboard"},   // ← Dashboard en tête
                {SEC_SALLES,       "building"},
                {SEC_BUREAUX,      "chair"},
                {SEC_MEMBRES,      "people"},
                {SEC_RESERVATIONS, "calendar"},
                {SEC_FACTURES,     "invoice"},
        };
        for (String[] item : menuItems) {
            p.add(buildNavItem(item[0], item[1], item[0]));
            p.add(Box.createVerticalStrut(4));
        }

        p.add(Box.createVerticalGlue());
        p.add(sidebarSectionLabel("COMPTE"));
        p.add(buildNavItem("Administrateur", "person", null));
        p.add(Box.createVerticalStrut(10));


        JButton btnLogout = new JButton("  Déconnexion");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setForeground(new Color(239, 68, 68));
        btnLogout.setBackground(new Color(45, 55, 72));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setAlignmentX(LEFT_ALIGNMENT);
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnLogout.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnLogout.addActionListener(e -> {
            dispose();
            new MainLoginFrame().setVisible(true);
        });
        p.add(btnLogout);
        p.add(Box.createVerticalStrut(6));



        return p;
    }

    private JLabel sidebarSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(75, 85, 99));
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 0));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return l;
    }

    private JButton buildNavItem(String label, String iconType, String sectionKey) {
        boolean isActive = sectionKey != null && sectionKey.equals(activeSection);
        boolean[] activeFlag = { isActive }; // flag mutable capturé par les lambdas

        Color iconBg = isActive ? L_ACT : bg(L_SIDEBAR, D_SIDEBAR);
        Color iconFg = isActive ? WHITE : L_NAV_TEXT;

        IconLabel ic = new IconLabel(iconType, iconBg, iconFg) {
            { setPreferredSize(new Dimension(20, 20)); }
        };

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(isActive ? WHITE : bg(L_NAV_TEXT, new Color(107, 114, 128)));

        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (activeFlag[0] || getModel().isRollover()) {
                    g2.setColor(activeFlag[0] ? L_ACT : new Color(45, 55, 72));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btn.add(ic);
        btn.add(lbl);

        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (sectionKey != null) {
            // On stocke aussi ic et lbl dans navMap pour pouvoir les mettre à jour
            navMap.put(sectionKey, btn);
            btn.putClientProperty("activeFlag", activeFlag);
            btn.putClientProperty("iconLabel",  ic);
            btn.putClientProperty("textLabel",  lbl);
            btn.addActionListener(e -> navigateTo(sectionKey));
        }
        return btn;
    }    // ===================== MAIN =====================
    private JPanel buildMain() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bg(L_BG, D_BG));
        p.add(buildTopBar(),    BorderLayout.NORTH);
        p.add(buildCards(),     BorderLayout.CENTER);
        p.add(buildStatusBar(), BorderLayout.SOUTH);
        return p;
    }

    // --- TOP BAR ---
    private JPanel buildTopBar() {
        topBar = new JPanel(new BorderLayout());
        topBar.setBackground(bg(L_WHITE, D_WHITE));
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, bg(L_BORDER, D_BORDER)),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        pageTitle = new JLabel(activeSection);
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pageTitle.setForeground(bg(L_TEXT, D_TEXT));

        pageSub = new JLabel(sectionSubtitle(activeSection));
        pageSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pageSub.setForeground(bg(L_SUBTEXT, D_SUBTEXT));

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setBackground(bg(L_WHITE, D_WHITE));
        titleGroup.add(pageTitle);
        titleGroup.add(pageSub);

        txtSearch = new JTextField("Rechercher...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(bg(L_SUBTEXT, D_SUBTEXT));
        txtSearch.setBackground(bg(L_BG, D_BG));
        txtSearch.setPreferredSize(new Dimension(230, 36));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg(L_BORDER, D_BORDER)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals("Rechercher...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(bg(L_TEXT, D_TEXT));
                }
            }
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Rechercher...");
                    txtSearch.setForeground(bg(L_SUBTEXT, D_SUBTEXT));
                }
            }
        });
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { forwardSearch(); }
            public void removeUpdate(DocumentEvent e)  { forwardSearch(); }
            public void changedUpdate(DocumentEvent e) { forwardSearch(); }
        });

        topBar.add(titleGroup, BorderLayout.WEST);
        topBar.add(txtSearch,  BorderLayout.EAST);
        return topBar;
    }

    // --- CARD CONTAINER ---
    private JPanel buildCards() {
        cardLayout    = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(bg(L_BG, D_BG));

        for (Map.Entry<String, JPanel> e : contentPanels.entrySet()) {
            cardContainer.add(e.getValue(), e.getKey());
        }
        return cardContainer;
    }

    // --- STATUS BAR ---
    private JPanel buildStatusBar() {
        statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        statusBar.setBackground(bg(L_WHITE, D_WHITE));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, bg(L_BORDER, D_BORDER)));

        JPanel dot = new JPanel();
        dot.setBackground(GREEN);
        dot.setPreferredSize(new Dimension(8, 8));

        lblStatus = new JLabel("  Système prêt");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(bg(L_SUBTEXT, D_SUBTEXT));

        statusBar.add(dot);
        statusBar.add(lblStatus);
        return statusBar;
    }

    // ===================== NAVIGATION =====================
    /**
     * Change la section active : met à jour le header, la sidebar et le CardLayout.
     */
    public void navigateTo(String section) {
        activeSection = section;

        if (pageTitle != null) {
            pageTitle.setText(section);
            pageSub.setText(sectionSubtitle(section));
        }

        for (Map.Entry<String, JButton> e : navMap.entrySet()) {
            boolean active = e.getKey().equals(section);
            JButton btn = e.getValue();

            // Met à jour le flag utilisé par paintComponent
            boolean[] flag = (boolean[]) btn.getClientProperty("activeFlag");
            if (flag != null) flag[0] = active;

            // Met à jour la couleur du texte
            JLabel lbl = (JLabel) btn.getClientProperty("textLabel");
            if (lbl != null)
                lbl.setForeground(active ? WHITE : bg(L_NAV_TEXT, new Color(107, 114, 128)));

            // Met à jour la couleur de l'icône
            IconLabel ic = (IconLabel) btn.getClientProperty("iconLabel");
            if (ic != null) {
                ic.setColors(active ? L_ACT : bg(L_SIDEBAR, D_SIDEBAR),
                        active ? WHITE : L_NAV_TEXT);
            }

            btn.repaint();
        }



        if (cardLayout != null) cardLayout.show(cardContainer, section);

        // Rafraîchir le dashboard à chaque fois qu'on y revient
        if (SEC_DASHBOARD.equals(section) && dashboardPanel != null) {
            dashboardPanel.refresh();
        }
        setStatus("  Section : " + section);
    }
    // ===================== SEARCH FORWARD =====================
    private void forwardSearch() {
        String txt = txtSearch.getText();
        if (txt.equals("Rechercher...")) txt = "";
        JPanel active = contentPanels.get(activeSection);
        if (active instanceof Searchable) ((Searchable) active).onSearch(txt);
    }

    // ===================== HELPERS PUBLICS =====================
    /** Met à jour le message de la status bar. */
    public void    setStatus(String msg)           { if (lblStatus != null) lblStatus.setText(msg); }
    public boolean isDarkMode()                    { return darkMode; }
    public void    refreshDashboard()              { if (dashboardPanel != null) dashboardPanel.refresh(); }


    // Résolution couleur light/dark — utilisable depuis les panels enfants
    public Color   bg(Color light, Color dark)     { return darkMode ? dark : light; }
    public Color   getBg()                         { return bg(L_BG,      D_BG);      }
    public Color   getWhite()                      { return bg(L_WHITE,   D_WHITE);   }
    public Color   getBorder()                     { return bg(L_BORDER,  D_BORDER);  }
    public Color   getText()                       { return bg(L_TEXT,    D_TEXT);    }
    public Color   getSubtext()                    { return bg(L_SUBTEXT, D_SUBTEXT); }
    public Color   getColorBlue()                  { return BLUE;   }
    public Color   getColorGreen()                 { return GREEN;  }
    public Color   getColorRed()                   { return RED;    }
    public Color   getColorOrange()                { return ORANGE; }
    public Color   getColorPurple()                { return PURPLE; }
    public Color   getColorTeal()                  { return TEAL;   }
    public Color   getWhiteFixed()                 { return WHITE;  }

    // ===================== SUBTITLE =====================
    private String sectionSubtitle(String section) {
        return switch (section) {
            case SEC_DASHBOARD    -> "Vue d'ensemble — statistiques et indicateurs";
            case SEC_SALLES       -> "Gérez vos salles de réunion";
            case SEC_BUREAUX      -> "Gérez vos espaces de bureaux";
            case SEC_MEMBRES      -> "Gérez les membres et abonnements";
            case SEC_RESERVATIONS -> "Suivez toutes les réservations";
            case SEC_FACTURES     -> "Consultez et émettez les factures";
            default               -> "";
        };
    }

    // ===================== PLACEHOLDER =====================



    static class PlaceholderPanel extends JPanel {
        PlaceholderPanel(AdminUI ui, String section, String icon) {
            setLayout(new GridBagLayout());
            setOpaque(false);

            JPanel box = new JPanel(new GridLayout(3, 1, 0, 10));
            box.setBackground(ui.getWhite());
            box.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ui.getBorder()),
                    BorderFactory.createEmptyBorder(32, 56, 32, 56)));

            JLabel ico = new JLabel(icon, SwingConstants.CENTER);
            ico.setFont(new Font("Segoe UI", Font.PLAIN, 42));

            JLabel title = new JLabel(section, SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 16));
            title.setForeground(ui.getText());

            JLabel sub = new JLabel("Panel en cours de chargement...", SwingConstants.CENTER);
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            sub.setForeground(ui.getSubtext());

            box.add(ico);
            box.add(title);
            box.add(sub);
            add(box);
        }
    }

    // ===================== ENTRY POINT =====================
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(AdminUI::new);
    }
}
