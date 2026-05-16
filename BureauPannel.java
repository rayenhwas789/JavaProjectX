import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * BureauPannel – Gestion des bureaux
 * Design aligné sur SallesPanel.
 */
public class BureauPannel extends JPanel implements AdminUI.Searchable {

    private final AdminUI ui;
    private final BureauDAO dao = new BureauDAO();

    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel lblTotal, lblDispo, lblNonDispo;

    // Couleurs row alternance + sélection
    private final Color L_ROW_ALT = new Color(249, 250, 251);
    private final Color D_ROW_ALT = new Color(22, 30, 46);
    private final Color L_SEL     = new Color(219, 234, 254);
    private final Color D_SEL     = new Color(30, 58, 100);

    // Couleurs fixes statut
    private final Color GREEN = new Color(22, 163, 74);
    private final Color RED   = new Color(220, 38, 38);

    public BureauPannel(AdminUI ui) {
        this.ui = ui;
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        build();
        chargerBureaux();
    }

    // ===================== BUILD =====================
    private void build() {
        removeAll();

        // --- Stats ---
        lblTotal    = new JLabel("0");
        lblDispo    = new JLabel("0");
        lblNonDispo = new JLabel("0");

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        statsPanel.add(makeStatCard("Total bureaux",    lblTotal,    ui.getColorBlue()));
        statsPanel.add(makeStatCard("Disponibles",      lblDispo,    ui.getColorGreen()));
        statsPanel.add(makeStatCard("Non disponibles",  lblNonDispo, ui.getColorRed()));

        // --- Toolbar ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JButton btnAdd  = makeBtn("Ajouter", "add",  ui.getColorBlue());
        JButton btnEdit = makeBtn("Modifier", "edit", ui.getColorOrange());
        JButton btnDel  = makeBtn("Supprimer", "delete",ui.getColorRed());

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDel);

        btnAdd.addActionListener(e  -> dialogAjouter());
        btnEdit.addActionListener(e -> dialogModifier());
        btnDel.addActionListener(e  -> supprimerBureau());

        // --- Table ---
        String[] cols = {"ID", "Nom du bureau", "Disponibilité", "Tarif (DT)"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
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

                setHorizontalAlignment(col == 1 ? LEFT : CENTER);

                if (col == 2 && val != null) {
                    boolean dispo = val.toString().equals("Disponible");
                    setForeground(dispo ? GREEN : RED);
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
                    ui.setStatus("  Bureau sélectionné : " + tableModel.getValueAt(row, 1));
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

    // ===================== CHARGER =====================
    private void chargerBureaux() {
        tableModel.setRowCount(0);
        List<Bureau> list = dao.getTousLesBureaux();
        int dispo = 0, nonDispo = 0;
        for (Bureau b : list) {
            tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getNom(),
                    b.isDisponible() ? "Disponible" : "Non disponible",
                    b.getTarif() + " DT"
            });
            if (b.isDisponible()) dispo++; else nonDispo++;
        }
        lblTotal.setText(String.valueOf(list.size()));
        lblDispo.setText(String.valueOf(dispo));
        lblNonDispo.setText(String.valueOf(nonDispo));
        ui.setStatus("  " + list.size() + " bureaux chargés");
    }

    // ===================== SEARCH =====================
    @Override
    public void onSearch(String query) {
        if (query.isEmpty()) { table.setRowSorter(null); return; }
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
    }

    // ===================== ADD =====================
    private void dialogAjouter() {
        JDialog d = makeDialog("Ajouter un bureau");
        JTextField fNom = makeField(d);
        JTextField fTar = makeField(d);
        JComboBox<String> fDispo = makeCombo(d);

        d.add(makeForm(
                new String[]{"Nom", "Tarif (DT)", "Disponible"},
                new JComponent[]{fNom, fTar, fDispo}), BorderLayout.CENTER);

        d.add(makeDialogActions(d, () -> {
            try {
                String nom = fNom.getText().trim();
                if (nom.isEmpty()) { alert(d, "Le nom est obligatoire !"); return; }
                double tar = Double.parseDouble(fTar.getText().trim());
                boolean dispo = fDispo.getSelectedItem().equals("Oui");
                dao.ajouterBureau(new Bureau(0, nom, dispo, tar));
                chargerBureaux();
                ui.setStatus("  Bureau \"" + nom + "\" ajouté !");
                d.dispose();
            } catch (NumberFormatException ex) {
                alert(d, "Le tarif doit être un nombre !");
            }
        }), BorderLayout.SOUTH);

        d.setVisible(true);
    }

    // ===================== UPDATE =====================
    private void dialogModifier() {
        int row = table.getSelectedRow();
        if (row < 0) { alert(null, "Sélectionnez un bureau !"); return; }

        JDialog d = makeDialog("Modifier le bureau");
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());

        JTextField fNom  = makeField(d);
        JTextField fTar  = makeField(d);
        JComboBox<String> fDispo = makeCombo(d);

        fNom.setText(tableModel.getValueAt(row, 1).toString());
        fTar.setText(tableModel.getValueAt(row, 3).toString().replace(" DT", ""));
        fDispo.setSelectedItem(
                tableModel.getValueAt(row, 2).toString().equals("Disponible") ? "Oui" : "Non");

        d.add(makeForm(
                new String[]{"Nom", "Tarif (DT)", "Disponible"},
                new JComponent[]{fNom, fTar, fDispo}), BorderLayout.CENTER);

        d.add(makeDialogActions(d, () -> {
            try {
                String nom = fNom.getText().trim();
                double tar = Double.parseDouble(fTar.getText().trim());
                boolean dispo = fDispo.getSelectedItem().equals("Oui");
                dao.modifierBureau(id, new Bureau(id, nom, dispo, tar));
                chargerBureaux();
                ui.setStatus("  Bureau modifié !");
                d.dispose();
            } catch (NumberFormatException ex) {
                alert(d, "Le tarif doit être un nombre !");
            }
        }), BorderLayout.SOUTH);

        d.setVisible(true);
    }

    // ===================== DELETE =====================
    private void supprimerBureau() {
        int row = table.getSelectedRow();
        if (row < 0) { alert(null, "Sélectionnez un bureau !"); return; }
        String nom = tableModel.getValueAt(row, 1).toString();
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        if (JOptionPane.showConfirmDialog(ui,
                "Supprimer \"" + nom + "\" ?", "Confirmation",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dao.supprimerBureau(id);
            chargerBureaux();
            ui.setStatus("  Bureau \"" + nom + "\" supprimé !");
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
        d.setSize(380, 280);
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

        JButton btnSave = makeBtnSave("Enregistrer", ui.getColorBlue());
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

    private JComboBox<String> makeCombo(JDialog d) {
        JComboBox<String> c = new JComboBox<>(new String[]{"Oui", "Non"});
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