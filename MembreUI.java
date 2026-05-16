import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MembreUI extends JFrame {

    // Palette identique à AdminUI
    private static final Color L_SIDEBAR  = new Color(30,  36,  51);
    private static final Color L_ACT      = new Color(37,  99,  235);
    private static final Color L_NAV_TEXT = new Color(156, 163, 175);
    private static final Color L_BG       = new Color(249, 250, 251);
    private static final Color L_WHITE    = Color.WHITE;
    private static final Color L_BORDER   = new Color(229, 231, 235);
    private static final Color L_TEXT     = new Color(17,  24,  39);
    private static final Color L_SUBTEXT  = new Color(107, 114, 128);
    private static final Color GREEN      = new Color(22,  163, 74);
    private static final Color BLUE       = new Color(37,  99,  235);
    private static final Color ORANGE     = new Color(249, 115, 22);
    private static final Color RED_LOGOUT = new Color(220, 38,  38);
    private static final Color L_SEL      = new Color(219, 234, 254);
    private static final Color L_ROW_ALT  = new Color(249, 250, 251);
    private static final Color WHITE      = Color.WHITE;

    private final Membre membre;
    private final ReservationDAO    resDAO = new ReservationDAO();
    private final SalleDeReunionDAO salDAO = new SalleDeReunionDAO();
    private final BureauDAO         burDAO = new BureauDAO();

    // ── NOUVEAU : gestionnaire de notifications ──
    private final NotificationManager notifManager = new NotificationManager();

    private JLabel lblStatus;
    private String activeSection = "bureaux";

    public MembreUI(Membre membre) {
        this.membre = membre;
        setTitle("CoWork Space — Espace Membre");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        getContentPane().removeAll();
        add(buildSidebar(),   BorderLayout.WEST);
        add(buildMain(),      BorderLayout.CENTER);
        revalidate(); repaint();
    }

    // ── SIDEBAR ──────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setBackground(L_SIDEBAR);
        p.setPreferredSize(new Dimension(210, 0));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JLabel logo = new JLabel("CoWorking");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logo.setForeground(WHITE);
        logo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel logoSub = new JLabel("Espace Membre");
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        logoSub.setForeground(new Color(107, 114, 128));
        logoSub.setAlignmentX(LEFT_ALIGNMENT);
        logoSub.setBorder(BorderFactory.createEmptyBorder(2, 0, 14, 0));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(55, 65, 81));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        p.add(logo); p.add(logoSub); p.add(sep);
        p.add(Box.createVerticalStrut(12));
        p.add(sectionLabel("MENU"));

        // Avatar + nom
        JPanel avatar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        avatar.setOpaque(false);
        avatar.setAlignmentX(LEFT_ALIGNMENT);
        avatar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel av = new JLabel(membre.getNom().substring(0, 1).toUpperCase(), SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(L_ACT); g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        av.setFont(new Font("Segoe UI", Font.BOLD, 13));
        av.setForeground(WHITE);
        av.setPreferredSize(new Dimension(30, 30));
        av.setOpaque(false);

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);
        JLabel nm = new JLabel(membre.getNom());
        nm.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nm.setForeground(WHITE);
        JLabel ab = new JLabel(membre.getTypeAbonnement());
        ab.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ab.setForeground(L_NAV_TEXT);
        namePanel.add(nm); namePanel.add(ab);

        avatar.add(av); avatar.add(namePanel);
        p.add(avatar);
        p.add(Box.createVerticalStrut(8));

        // Nav items avec IconLabel
        p.add(navBtn("chair",   "Bureaux",          "bureaux"));
        p.add(Box.createVerticalStrut(4));
        p.add(navBtn("building","Salles de réunion", "salles"));
        p.add(Box.createVerticalStrut(4));
        p.add(navBtn("invoice", "Factures",          "factures"));
        p.add(Box.createVerticalStrut(4));

        p.add(Box.createVerticalGlue());
        p.add(sectionLabel("COMPTE"));

        // Déconnexion
        JPanel logoutRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        logoutRow.setOpaque(false);
        logoutRow.setAlignmentX(LEFT_ALIGNMENT);
        logoutRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logoutRow.setCursor(new Cursor(Cursor.HAND_CURSOR));

        IconLabel logoutIcon = new IconLabel("circle", new Color(60, 30, 30), new Color(239, 68, 68));
        logoutIcon.setPreferredSize(new Dimension(28, 28));

        JLabel logoutLbl = new JLabel("Déconnexion");
        logoutLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoutLbl.setForeground(new Color(239, 68, 68));

        logoutRow.add(logoutIcon);
        logoutRow.add(logoutLbl);
        logoutRow.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose(); new MainLoginFrame().setVisible(true);
            }
        });
        p.add(logoutRow);

        return p;
    }

    private JButton navBtn(String iconType, String label, String section) {
        boolean active = section.equals(activeSection);

        Color iconBg = active ? new Color(59, 119, 255) : new Color(45, 55, 72);
        Color iconFg = WHITE;

        IconLabel icon = new IconLabel(iconType, iconBg, iconFg);
        icon.setPreferredSize(new Dimension(28, 28));

        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (active) {
                    g2.setColor(L_ACT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 5));
        btn.add(icon);

        JLabel text = new JLabel(label);
        text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        text.setForeground(active ? WHITE : L_NAV_TEXT);
        btn.add(text);

        btn.setText("");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(active ? WHITE : L_NAV_TEXT);
        btn.setBackground(L_SIDEBAR);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> { activeSection = section; buildUI(); });

        return btn;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(75, 85, 99));
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 0));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return l;
    }

    // ── MAIN ─────────────────────────────────────────────────
    private JPanel buildMain() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(L_BG);
        p.add(buildTopBar(),    BorderLayout.NORTH);
        p.add(buildContent(),   BorderLayout.CENTER);
        p.add(buildStatusBar(), BorderLayout.SOUTH);
        return p;
    }

    // ── TOP BAR (modifiée : cloche ajoutée à droite) ─────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(L_WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, L_BORDER),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        // --- Titre (côté gauche) ---
        String sectionTitle;
        String sectionSub;
        switch (activeSection) {
            case "bureaux"  -> { sectionTitle = "Bureaux";           sectionSub = "Historique de vos réservations de bureaux"; }
            case "salles"   -> { sectionTitle = "Salles de réunion"; sectionSub = "Historique de vos réservations de salles"; }
            case "factures" -> { sectionTitle = "Factures";          sectionSub = "Consultez vos factures"; }
            default         -> { sectionTitle = "";                  sectionSub = ""; }
        }

        JLabel title = new JLabel(sectionTitle);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(L_TEXT);

        JLabel sub = new JLabel(sectionSub);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(L_SUBTEXT);

        JPanel titleGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        titleGroup.setBackground(L_WHITE);
        titleGroup.add(title);
        titleGroup.add(sub);

        bar.add(titleGroup, BorderLayout.WEST);

        // --- Cloche 🔔 (côté droit) ---
        List<Notification> notifs = notifManager.getNotifications(membre);
        int count = notifs.size();

        String bellText = count > 0 ? "\uD83D\uDD14 " + count : "\uD83D\uDD14";

        JButton bellBtn = new JButton(bellText);
        bellBtn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        bellBtn.setForeground(count > 0 ? Color.WHITE : L_SUBTEXT);
        bellBtn.setBackground(count > 0 ? new Color(220, 38, 38) : L_BG);
        bellBtn.setFocusPainted(false);
        bellBtn.setBorderPainted(false);
        bellBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bellBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        bellBtn.addActionListener(e -> showNotifications(notifs, bellBtn));

        bar.add(bellBtn, BorderLayout.EAST);

        return bar;
    }

    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(L_BG);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Stats
        List<Reservation> all = resDAO.getReservationsParMembre(membre.getId());
        long cntBureau = all.stream().filter(r -> "Bureau".equalsIgnoreCase(r.getType())).count();
        long cntSalle  = all.stream().filter(r -> "Salle".equalsIgnoreCase(r.getType())).count();

        JLabel lblTotal  = new JLabel(String.valueOf(all.size()));
        JLabel lblBureau = new JLabel(String.valueOf(cntBureau));
        JLabel lblSalle  = new JLabel(String.valueOf(cntSalle));

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        stats.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        stats.add(statCard("Total réservations", lblTotal,  BLUE));
        stats.add(statCard("Bureaux réservés",   lblBureau, ORANGE));
        stats.add(statCard("Salles réservées",   lblSalle,  GREEN));

        JScrollPane table;

        if (activeSection.equals("bureaux")) {
            table = buildTable("Bureau");
        } else if (activeSection.equals("salles")) {
            table = buildTable("Salle");
        } else {
            table = buildFacturesTable();
        }

        p.add(stats, BorderLayout.NORTH);
        p.add(table, BorderLayout.CENTER);
        return p;
    }

    private JPanel statCard(String label, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 6));
        card.setBackground(L_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(L_BORDER),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(L_SUBTEXT);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);
        card.add(lbl); card.add(valueLabel);
        return card;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        bar.setBackground(L_WHITE);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, L_BORDER));
        JPanel dot = new JPanel();
        dot.setBackground(GREEN);
        dot.setPreferredSize(new Dimension(8, 8));
        lblStatus = new JLabel("  Bienvenue, " + membre.getNom() + " !");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(L_SUBTEXT);
        bar.add(dot); bar.add(lblStatus);
        return bar;
    }

    private JScrollPane buildTable(String type) {
        String[] cols = {"ID", "Espace", "Date début", "Date fin", "Durée"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Reservation>    reservations = resDAO.getReservationsParMembre(membre.getId());
        List<SalleDeReunion> salles        = salDAO.getToutesLesSalles();
        List<Bureau>         bureaux       = burDAO.getTousLesBureaux();

        for (Reservation r : reservations) {
            if (!r.getType().equalsIgnoreCase(type)) continue;

            String nomEspace = "";
            if ("Salle".equalsIgnoreCase(type) && r.getIdSalleReunion() != null) {
                nomEspace = salles.stream()
                        .filter(s -> s.getId() == r.getIdSalleReunion())
                        .map(SalleDeReunion::getNom)
                        .findFirst().orElse("Salle #" + r.getIdSalleReunion());
            } else if ("Bureau".equalsIgnoreCase(type) && r.getIdBureau() != null) {
                nomEspace = bureaux.stream()
                        .filter(b -> b.getId() == r.getIdBureau())
                        .map(Bureau::getNom)
                        .findFirst().orElse("Bureau #" + r.getIdBureau());
            }

            long diffMs = r.getDateFin().getTime() - r.getDateDebut().getTime();
            long jours  = Math.max(1, (long) Math.ceil(diffMs / (1000.0 * 60 * 60 * 24)));
            String duree = jours + (jours > 1 ? " jours" : " jour");

            model.addRow(new Object[]{
                    r.getId(), nomEspace,
                    r.getDateDebut().toString().substring(0, 16),
                    r.getDateFin().toString().substring(0, 16),
                    duree
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(L_WHITE);
        table.setSelectionBackground(L_SEL);
        table.setSelectionForeground(L_TEXT);

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setBackground(L_BG);
        th.setForeground(L_SUBTEXT);
        th.setPreferredSize(new Dimension(0, 38));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, L_BORDER));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(L_TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(col == 1 ? LEFT : CENTER);
                setBackground(sel ? L_SEL : (row % 2 == 0 ? L_WHITE : L_ROW_ALT));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(L_BORDER));
        scroll.getViewport().setBackground(L_WHITE);
        return scroll;
    }

    private JScrollPane buildFacturesTable() {
        String[] cols = {"ID", "Réservation", "Date", "Montant", "Statut"};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        FactureDAO facDAO = new FactureDAO();

        List<Reservation> reservations = resDAO.getReservationsParMembre(membre.getId());
        List<Facture> factures = facDAO.getToutesFactures();

        Set<Integer> resIds = reservations.stream()
                .map(Reservation::getId)
                .collect(Collectors.toSet());

        for (Facture f : factures) {
            if (resIds.contains(f.getIdReservation())) {
                model.addRow(new Object[]{
                        f.getId(),
                        "Rés. #" + f.getIdReservation(),
                        f.getDateFacture(),
                        f.getMontant() + " DT",
                        f.getStatut()
                });
            }
        }

        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(L_WHITE);
        table.setSelectionBackground(L_SEL);
        table.setSelectionForeground(L_TEXT);

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 11));
        th.setBackground(L_BG);
        th.setForeground(L_SUBTEXT);
        th.setPreferredSize(new Dimension(0, 38));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, L_BORDER));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(L_TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(col == 1 ? LEFT : CENTER);
                setBackground(sel ? L_SEL : (row % 2 == 0 ? L_WHITE : L_ROW_ALT));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(L_BORDER));
        scroll.getViewport().setBackground(L_WHITE);
        return scroll;
    }

    // ── NOUVEAU : affiche le popup de notifications ───────────
    private void showNotifications(List<Notification> notifs, JButton bellBtn) {

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(L_BORDER));

        if (notifs.isEmpty()) {
            JMenuItem empty = new JMenuItem("  Aucune nouvelle notification");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setForeground(L_SUBTEXT);
            empty.setEnabled(false);
            popup.add(empty);
        } else {
            JLabel header = new JLabel("  Notifications (" + notifs.size() + ")");
            header.setFont(new Font("Segoe UI", Font.BOLD, 12));
            header.setForeground(L_SUBTEXT);
            header.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
            popup.add(header);
            popup.addSeparator();

            for (Notification n : notifs) {
                JMenuItem item = new JMenuItem(n.getMessage());
                item.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));

                // Couleur selon le type
                switch (n.getType()) {
                    case FACTURE_IMPAYEE       -> item.setForeground(new Color(220, 38, 38));  // rouge
                    case RESERVATION_BIENTOT   -> item.setForeground(new Color(249, 115, 22)); // orange
                    case RESERVATION_CONFIRMEE -> item.setForeground(new Color(22, 163, 74));  // vert
                }

                item.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                popup.add(item);
            }
        }

        // Afficher le popup juste en dessous du bouton cloche
        popup.show(bellBtn, 0, bellBtn.getHeight());
    }
}
