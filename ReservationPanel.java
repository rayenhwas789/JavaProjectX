import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public class ReservationPanel extends JPanel implements AdminUI.Searchable {

    private final AdminUI ui;
    private final ReservationDAO dao        = new ReservationDAO();
    private final SalleDeReunionDAO salleDao = new SalleDeReunionDAO();
    private final BureauDAO         bureauDao = new BureauDAO();
    private final MembreDAO         membreDao = new MembreDAO();

    private DefaultTableModel model;
    private JTable table;

    private JLabel lblTotal, lblSalle, lblBureau;

    private final Color L_ROW_ALT = new Color(249, 250, 251);
    private final Color L_SEL     = new Color(219, 234, 254);
    private final Color D_SEL     = new Color(30, 58, 100);
    private final Color BLUE      = new Color(37, 99, 235);
    private final Color ORANGE    = new Color(234, 88, 12);

    public ReservationPanel(AdminUI ui) {
        this.ui = ui;
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        build();
        load();
    }

    // ===================== BUILD =====================
    private void build() {
        removeAll();

        // --- Stats ---
        lblTotal  = new JLabel("0");
        lblSalle  = new JLabel("0");
        lblBureau = new JLabel("0");

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        statsPanel.add(makeStatCard("Total réservations", lblTotal,  ui.getColorBlue()));
        statsPanel.add(makeStatCard("Salles réservées",   lblSalle,  ui.getColorGreen()));
        statsPanel.add(makeStatCard("Bureaux réservés",   lblBureau, ui.getColorOrange()));

        // --- Toolbar ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JButton btnAdd  = makeBtn("Ajouter","add",   ui.getColorBlue());
        JButton btnEdit = makeBtn("Modifier","edit",  ui.getColorOrange());
        JButton btnDel  = makeBtn("Supprimer","delete", ui.getColorRed());

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDel);

        btnAdd.addActionListener(e  -> dialogAjouter());
        btnEdit.addActionListener(e -> dialogModifier());
        btnDel.addActionListener(e  -> supprimer());

        // --- Table ---
        String[] cols = {"ID", "Membre", "Type", "Salle / Bureau", "Date début", "Date fin"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(ui.getWhite());
        table.setSelectionBackground(ui.isDarkMode() ? D_SEL : L_SEL);
        table.setSelectionForeground(ui.getText());

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(ui.getBg());
        header.setForeground(ui.getSubtext());
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ui.getBorder()));

        final AdminUI uiRef = this.ui;

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {

                super.getTableCellRendererComponent(t, val, sel, foc, row, col);

                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(uiRef.getText());
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

                setHorizontalAlignment(col == 1 || col == 3 ? LEFT : CENTER);

                // Colonne Type colorée
                if (col == 2 && val != null) {
                    boolean isSalle = val.toString().equalsIgnoreCase("Salle");
                    setForeground(isSalle ? uiRef.getColorGreen() : uiRef.getColorOrange());
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                }

                if (!sel) {
                    setBackground(row % 2 == 0 ? uiRef.getWhite() : L_ROW_ALT);
                } else {
                    setBackground(uiRef.isDarkMode() ? D_SEL : L_SEL);
                }

                return this;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0)
                    ui.setStatus("  Réservation sélectionnée : #"
                            + model.getValueAt(row, 0) + " — " + model.getValueAt(row, 1));
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(ui.getBorder()));
        scroll.getViewport().setBackground(ui.getWhite());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.add(toolbar, BorderLayout.NORTH);
        tablePanel.add(scroll,  BorderLayout.CENTER);

        add(statsPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    // ===================== LOAD =====================
    private void load() {
        model.setRowCount(0);
        List<Reservation> list = dao.getToutesReservations();
        List<Membre>      membres = membreDao.getAll();
        List<SalleDeReunion> salles  = salleDao.getToutesLesSalles();
        List<Bureau>      bureaux = bureauDao.getTousLesBureaux();

        int cntSalle = 0, cntBureau = 0;

        for (Reservation r : list) {
            String membreLabel = membres.stream()
                    .filter(m -> m.getId() == r.getIdMembre())
                    .map(m -> m.getNom() + " " + m.getPrenom())
                    .findFirst().orElse("ID " + r.getIdMembre());

            String ressource = "";
            if ("Salle".equalsIgnoreCase(r.getType()) && r.getIdSalleReunion() != null) {
                ressource = salles.stream()
                        .filter(s -> s.getId() == r.getIdSalleReunion())
                        .map(SalleDeReunion::getNom)
                        .findFirst().orElse("Salle #" + r.getIdSalleReunion());
                cntSalle++;
            } else if ("Bureau".equalsIgnoreCase(r.getType()) && r.getIdBureau() != null) {
                ressource = bureaux.stream()
                        .filter(b -> b.getId() == r.getIdBureau())
                        .map(Bureau::getNom)
                        .findFirst().orElse("Bureau #" + r.getIdBureau());
                cntBureau++;
            }

            model.addRow(new Object[]{
                    r.getId(),
                    membreLabel,
                    r.getType(),
                    ressource,
                    r.getDateDebut(),
                    r.getDateFin()
            });
        }

        lblTotal.setText(String.valueOf(list.size()));
        lblSalle.setText(String.valueOf(cntSalle));
        lblBureau.setText(String.valueOf(cntBureau));
        ui.setStatus("  " + list.size() + " réservations chargées");
    }

    // ===================== SEARCH =====================
    @Override
    public void onSearch(String query) {
        if (query.isEmpty()) { table.setRowSorter(null); return; }
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
    }

    // ===================== ADD =====================
    private void dialogAjouter() {
        JDialog d = makeDialog("Ajouter une réservation", 420, 360);

        // Dropdowns chargés depuis la BDD
        JComboBox<ComboItem> fMembre = buildMembreCombo(d);
        JComboBox<String>    fType   = makeCombo(d, "Salle", "Bureau");
        JComboBox<ComboItem> fRessource = buildRessourceCombo(d, "Salle");
        JTextField fDebut = makeField(d);
        JTextField fFin   = makeField(d);

        fDebut.setToolTipText("Format : YYYY-MM-DD");
        fFin.setToolTipText("Format : YYYY-MM-DD");

        // Rechargement dynamique de la liste ressource selon le type
        fType.addActionListener(e -> {
            String type = fType.getSelectedItem().toString();
            updateRessourceCombo(fRessource, type);
        });

        d.add(makeForm(
                new String[]{"Membre", "Type", "Salle / Bureau", "Date début", "Date fin"},
                new JComponent[]{fMembre, fType, fRessource, fDebut, fFin}
        ), BorderLayout.CENTER);

        d.add(makeDialogActions(d, () -> {
            try {
                ComboItem membre = (ComboItem) fMembre.getSelectedItem();
                ComboItem res    = (ComboItem) fRessource.getSelectedItem();
                String type = fType.getSelectedItem().toString();

                if (membre == null || res == null) { alert(d, "Sélectionnez membre et ressource !"); return; }

                Timestamp debut = Timestamp.valueOf(fDebut.getText().trim());
                Timestamp fin   = Timestamp.valueOf(fFin.getText().trim());

                Integer idSalle  = type.equalsIgnoreCase("Salle")  ? res.id : null;
                Integer idBureau = type.equalsIgnoreCase("Bureau") ? res.id : null;

                dao.ajouterReservation(new Reservation(0, debut, fin, type,
                        membre.id, idSalle, idBureau));
                load();
                ui.setStatus("  Réservation ajoutée !");
                d.dispose();
            } catch (IllegalArgumentException ex) {
                alert(d, "Date invalide ! Utilisez le format YYYY-MM-DD.");
            }
        }), BorderLayout.SOUTH);

        d.setVisible(true);
    }

    // ===================== MODIFY =====================
    private void dialogModifier() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { alert(null, "Sélectionnez une réservation !"); return; }
        int row = table.convertRowIndexToModel(viewRow);
        int id  = Integer.parseInt(model.getValueAt(row, 0).toString());

        // Récupérer l'objet original
        Reservation orig = dao.getToutesReservations().stream()
                .filter(r -> r.getId() == id).findFirst().orElse(null);
        if (orig == null) return;

        JDialog d = makeDialog("Modifier la réservation", 420, 360);

        JComboBox<ComboItem> fMembre    = buildMembreCombo(d);
        JComboBox<String>    fType      = makeCombo(d, "Salle", "Bureau");
        JComboBox<ComboItem> fRessource = buildRessourceCombo(d, orig.getType());
        JTextField fDebut = makeField(d);
        JTextField fFin   = makeField(d);

        // Pré-remplir
        selectComboById(fMembre, orig.getIdMembre());
        fType.setSelectedItem(orig.getType());
        int resId = "Salle".equalsIgnoreCase(orig.getType())
                ? (orig.getIdSalleReunion() != null ? orig.getIdSalleReunion() : -1)
                : (orig.getIdBureau() != null ? orig.getIdBureau() : -1);
        selectComboById(fRessource, resId);
        fDebut.setText(orig.getDateDebut().toString());
        fFin.setText(orig.getDateFin().toString());

        fType.addActionListener(e -> {
            String type = fType.getSelectedItem().toString();
            updateRessourceCombo(fRessource, type);
        });

        d.add(makeForm(
                new String[]{"Membre", "Type", "Salle / Bureau", "Date début", "Date fin"},
                new JComponent[]{fMembre, fType, fRessource, fDebut, fFin}
        ), BorderLayout.CENTER);

        d.add(makeDialogActions(d, () -> {
            try {
                ComboItem membre = (ComboItem) fMembre.getSelectedItem();
                ComboItem res    = (ComboItem) fRessource.getSelectedItem();
                String type = fType.getSelectedItem().toString();

                if (membre == null || res == null) { alert(d, "Sélectionnez membre et ressource !"); return; }

                Timestamp debut = Timestamp.valueOf(fDebut.getText().trim());
                Timestamp fin   = Timestamp.valueOf(fFin.getText().trim());

                Integer idSalle  = type.equalsIgnoreCase("Salle")  ? res.id : null;
                Integer idBureau = type.equalsIgnoreCase("Bureau") ? res.id : null;

                dao.modifierReservation(id, new Reservation(id, debut, fin, type,
                        membre.id, idSalle, idBureau));
                load();
                ui.setStatus("  Réservation modifiée !");
                d.dispose();
            } catch (IllegalArgumentException ex) {
                alert(d, "Date invalide ! Utilisez le format YYYY-MM-DD.");
            }
        }), BorderLayout.SOUTH);

        d.setVisible(true);
    }

    // ===================== DELETE =====================
    private void supprimer() {
        int r = table.getSelectedRow();
        if (r < 0) { alert(null, "Sélectionnez une réservation !"); return; }
        int row = table.convertRowIndexToModel(r);
        int id  = Integer.parseInt(model.getValueAt(row, 0).toString());

        if (JOptionPane.showConfirmDialog(ui,
                "Supprimer la réservation #" + id + " ?", "Confirmation",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dao.supprimerReservation(id);
            load();
            ui.setStatus("  Réservation #" + id + " supprimée !");
        }
    }

    // ===================== COMBO HELPERS =====================

    /** Item affiché dans une JComboBox avec un id interne */
    private static class ComboItem {
        final int id;
        final String label;
        ComboItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private JComboBox<ComboItem> buildMembreCombo(JDialog d) {
        JComboBox<ComboItem> c = new JComboBox<>();
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBackground(ui.getBg());
        c.setForeground(ui.getText());
        for (Membre m : membreDao.getAll())
            c.addItem(new ComboItem(m.getId(), m.getNom() + " " + m.getPrenom()));
        return c;
    }

    private JComboBox<ComboItem> buildRessourceCombo(JDialog d, String type) {
        JComboBox<ComboItem> c = new JComboBox<>();
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBackground(ui.getBg());
        c.setForeground(ui.getText());
        fillRessourceCombo(c, type);
        return c;
    }

    private void fillRessourceCombo(JComboBox<ComboItem> c, String type) {
        c.removeAllItems();
        if ("Salle".equalsIgnoreCase(type)) {
            for (SalleDeReunion s : salleDao.getSallesDisponibles())
                c.addItem(new ComboItem(s.getId(), s.getNom()));
        } else {
            for (Bureau b : bureauDao.getBureauxDisponibles())
                c.addItem(new ComboItem(b.getId(), b.getNom()));
        }
    }

    private void updateRessourceCombo(JComboBox<ComboItem> c, String type) {
        fillRessourceCombo(c, type);
        c.revalidate();
        c.repaint();
    }

    private void selectComboById(JComboBox<ComboItem> c, int id) {
        for (int i = 0; i < c.getItemCount(); i++) {
            if (c.getItemAt(i).id == id) { c.setSelectedIndex(i); return; }
        }
    }

    // ===================== UI HELPERS =====================
    private JPanel makeStatCard(String label, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 6));
        card.setBackground(ui.getWhite());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ui.getBorder()),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(ui.getSubtext());
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);
        card.add(lbl);
        card.add(valueLabel);
        return card;
    }

    private JButton makeBtn(String text, String iconType, Color color) {
        JButton btn = new JButton();

        btn.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0)); // layout interne

        IconLabel icon = new IconLabel(iconType, color, Color.WHITE) {
            { setPreferredSize(new Dimension(18, 18)); }
        };           JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btn.add(icon);
        btn.add(label);

        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        return btn;
    }

    private JDialog makeDialog(String title, int w, int h) {
        JDialog d = new JDialog(ui, title, true);
        d.setSize(w, h);
        d.setLocationRelativeTo(ui);
        d.setLayout(new BorderLayout(10, 10));
        d.getContentPane().setBackground(ui.getWhite());
        return d;
    }

    private JPanel makeForm(String[] labels, JComponent[] fields) {
        JPanel p = new JPanel(new GridLayout(labels.length, 2, 10, 10));
        p.setBackground(ui.getWhite());
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        for (int i = 0; i < labels.length; i++) {
            JLabel l = new JLabel(labels[i]);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            l.setForeground(ui.getSubtext());
            p.add(l);
            p.add(fields[i]);
        }
        return p;
    }
    private JButton makeBtnSave(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn; }

    private JPanel makeDialogActions(JDialog d, Runnable onSave) {
        JButton btnCancel = new JButton("Annuler");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(9, 17, 9, 17));
        btnCancel.addActionListener(e -> d.dispose());

        JButton btnSave = makeBtnSave("Enregistrer",ui.getColorBlue());
        btnSave.addActionListener(e -> onSave.run());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        p.setBackground(ui.getWhite());
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ui.getBorder()));
        p.add(btnCancel);
        p.add(btnSave);
        return p;
    }

    private JTextField makeField(JDialog d) {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(ui.getBg());
        f.setForeground(ui.getText());
        f.setCaretColor(ui.getText());
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ui.getBorder()),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private JComboBox<String> makeCombo(JDialog d, String... items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setBackground(ui.getBg());
        c.setForeground(ui.getText());
        return c;
    }

    private void alert(Component parent, String msg) {
        JOptionPane.showMessageDialog(
                parent != null ? parent : ui, msg, "Attention", JOptionPane.WARNING_MESSAGE);
    }
}