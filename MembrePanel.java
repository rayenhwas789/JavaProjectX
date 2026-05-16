import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MembrePanel extends JPanel implements AdminUI.Searchable {

    private final AdminUI ui;
    private final MembreDAO dao = new MembreDAO();

    private DefaultTableModel model;
    private JTable table;

    private JLabel lblTotal, lblJournalier, lblMensuel, lblAnnuel;

    private final Color GREEN     = new Color(22, 163, 74);
    private final Color L_ROW_ALT = new Color(249, 250, 251);
    private final Color D_ROW_ALT = new Color(22, 30, 46);
    private final Color L_SEL     = new Color(219, 234, 254);
    private final Color D_SEL     = new Color(30, 58, 100);

    public MembrePanel(AdminUI ui) {
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
        lblTotal      = new JLabel("0");
        lblJournalier = new JLabel("0");
        lblMensuel    = new JLabel("0");
        lblAnnuel     = new JLabel("0");

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        statsPanel.add(makeStatCard("Total membres",  lblTotal,      ui.getColorBlue()));
        statsPanel.add(makeStatCard("Journalier",     lblJournalier, GREEN));
        statsPanel.add(makeStatCard("Mensuel",        lblMensuel,    ui.getColorOrange()));
        statsPanel.add(makeStatCard("Annuel",         lblAnnuel,     ui.getColorPurple()));

        // --- Toolbar ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JButton btnAdd  = makeBtn("Ajouter","add",   ui.getColorBlue());
        JButton btnEdit = makeBtn("Modifier", "edit", ui.getColorOrange());
        JButton btnDel  = makeBtn("Supprimer", "delete",ui.getColorRed());

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDel);

        btnAdd.addActionListener(e  -> addDialog());
        btnEdit.addActionListener(e -> editDialog());
        btnDel.addActionListener(e  -> delete());

        // --- Table ---
        String[] cols = {"ID", "Nom", "Prénom", "Email", "Abonnement"};
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

                // Nom et Prénom alignés à gauche, reste centré
                setHorizontalAlignment(col == 1 || col == 2 ? LEFT : CENTER);

                // Colonne Abonnement colorée
                if (col == 4 && val != null) {
                    switch (val.toString()) {
                        case "JOURNALIER" -> { setForeground(GREEN);               setFont(new Font("Segoe UI", Font.BOLD, 13)); }
                        case "MENSUEL"    -> { setForeground(uiRef.getColorOrange()); setFont(new Font("Segoe UI", Font.BOLD, 13)); }
                        case "ANNUEL"     -> { setForeground(uiRef.getColorPurple()); setFont(new Font("Segoe UI", Font.BOLD, 13)); }
                    }
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
                    ui.setStatus("  Membre sélectionné : "
                            + model.getValueAt(row, 1) + " " + model.getValueAt(row, 2));
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
        List<Membre> list = dao.getAll();
        int total = 0, j = 0, m = 0, a = 0;

        for (Membre x : list) {
            model.addRow(new Object[]{
                    x.getId(), x.getNom(), x.getPrenom(),
                    x.getEmail(), x.getTypeAbonnement()
            });
            total++;
            switch (x.getTypeAbonnement()) {
                case "JOURNALIER" -> j++;
                case "MENSUEL"    -> m++;
                case "ANNUEL"     -> a++;
            }
        }

        lblTotal.setText(String.valueOf(total));
        lblJournalier.setText(String.valueOf(j));
        lblMensuel.setText(String.valueOf(m));
        lblAnnuel.setText(String.valueOf(a));
        ui.setStatus("  " + total + " membres chargés");
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
    private void addDialog() {
        JDialog d = makeDialog("Ajouter un membre");

        JTextField fNom    = makeField(d);
        JTextField fPrenom = makeField(d);
        JTextField fEmail  = makeField(d);
        JTextField fMdp    = makeField(d);
        JComboBox<String> fType = makeCombo(d, "JOURNALIER", "MENSUEL", "ANNUEL");

        d.add(makeForm(
                new String[]{"Nom", "Prénom", "Email", "Mot de passe", "Type"},
                new JComponent[]{fNom, fPrenom, fEmail, fMdp, fType}
        ), BorderLayout.CENTER);

        d.add(makeDialogActions(d, () -> {
            String nom = fNom.getText().trim();
            if (nom.isEmpty()) { alert(d, "Le nom est obligatoire !"); return; }
            dao.add(new Membre(0,
                    nom,
                    fPrenom.getText().trim(),
                    fEmail.getText().trim(),
                    fMdp.getText(),
                    fType.getSelectedItem().toString()
            ));
            load();
            ui.setStatus("  Membre \"" + nom + "\" ajouté !");
            d.dispose();
        }), BorderLayout.SOUTH);

        d.setVisible(true);
    }

    // ===================== EDIT =====================
    private void editDialog() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { alert(null, "Sélectionnez un membre !"); return; }
        int row = table.convertRowIndexToModel(viewRow);
        int id  = Integer.parseInt(model.getValueAt(row, 0).toString());

        JDialog d = makeDialog("Modifier le membre");

        JTextField fNom    = makeField(d);
        JTextField fPrenom = makeField(d);
        JTextField fEmail  = makeField(d);
        JComboBox<String> fType = makeCombo(d, "JOURNALIER", "MENSUEL", "ANNUEL");

        fNom.setText(model.getValueAt(row, 1).toString());
        fPrenom.setText(model.getValueAt(row, 2).toString());
        fEmail.setText(model.getValueAt(row, 3).toString());
        fType.setSelectedItem(model.getValueAt(row, 4).toString());

        d.add(makeForm(
                new String[]{"Nom", "Prénom", "Email", "Type"},
                new JComponent[]{fNom, fPrenom, fEmail, fType}
        ), BorderLayout.CENTER);

        d.add(makeDialogActions(d, () -> {
            String nom = fNom.getText().trim();
            dao.update(id, new Membre(id,
                    nom,
                    fPrenom.getText().trim(),
                    fEmail.getText().trim(),
                    "",
                    fType.getSelectedItem().toString()
            ));
            load();
            ui.setStatus("  Membre modifié !");
            d.dispose();
        }), BorderLayout.SOUTH);

        d.setVisible(true);
    }

    // ===================== DELETE =====================
    private void delete() {
        int r = table.getSelectedRow();
        if (r < 0) { alert(null, "Sélectionnez un membre !"); return; }
        int row = table.convertRowIndexToModel(r);
        String nom = model.getValueAt(row, 1) + " " + model.getValueAt(row, 2);
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());

        if (JOptionPane.showConfirmDialog(ui,
                "Supprimer \"" + nom + "\" ?", "Confirmation",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dao.delete(id);
            load();
            ui.setStatus("  Membre \"" + nom + "\" supprimé !");
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

    private JDialog makeDialog(String title) {
        JDialog d = new JDialog(ui, title, true);
        d.setSize(400, 340);
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
        return btn;
    }
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