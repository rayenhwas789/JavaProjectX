import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class FacturePanel extends JPanel implements AdminUI.Searchable {

    private final AdminUI        ui;
    private final FactureDAO     dao       = new FactureDAO();
    private final ReservationDAO resDao    = new ReservationDAO();
    private final MembreDAO      membreDao = new MembreDAO();

    private DefaultTableModel model;
    private JTable            table;

    private JLabel lblTotal, lblPayee, lblAttente, lblRevenus;

    private final Color L_ROW_ALT = new Color(249, 250, 251);
    private final Color L_SEL     = new Color(219, 234, 254);
    private final Color D_SEL     = new Color(30,  58, 100);

    private final Color GREEN  = new Color(22,  163, 74);
    private final Color ORANGE = new Color(234, 88,  12);
    private final Color RED    = new Color(220, 38,  38);
    private final Color BLUE   = new Color(59,  130, 246);

    // Nom du club affiché dans le PDF
    private static final String space_NAME = "CoWorking Space";

    public FacturePanel(AdminUI ui) {
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
        lblTotal   = new JLabel("0");
        lblPayee   = new JLabel("0");
        lblAttente = new JLabel("0");
        lblRevenus = new JLabel("0 DT");

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        statsPanel.add(makeStatCard("Total factures",  lblTotal,   ui.getColorBlue()));
        statsPanel.add(makeStatCard("Payées",          lblPayee,   GREEN));
        statsPanel.add(makeStatCard("En attente",      lblAttente, ORANGE));
        statsPanel.add(makeStatCard("Revenus totaux",  lblRevenus, ui.getColorPurple()));

        // --- Toolbar ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JButton btnGen    = makeBtn("Générer","add",        ui.getColorBlue());
        JButton btnStatut = makeBtn("Changer statut","edit",  GREEN);
        JButton btnDel    = makeBtn("Supprimer","delete",       RED);

        toolbar.add(btnGen);
        toolbar.add(btnStatut);
        toolbar.add(btnDel);

        btnGen.addActionListener(e    -> dialogGenerer());
        btnStatut.addActionListener(e -> dialogChangerStatut());
        btnDel.addActionListener(e    -> supprimer());

        // --- Table ---
        // Colonnes : ID | Membre | Réservation | Date | Montant | Statut | PDF
        String[] cols = {"ID", "Membre", "Réservation", "Date", "Montant (DT)", "Statut", "Reçu PDF"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 6 ? JButton.class : Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(44);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(ui.getWhite());
        table.setSelectionBackground(ui.isDarkMode() ? D_SEL : L_SEL);
        table.setSelectionForeground(ui.getText());

        // Largeur fixe de la colonne PDF
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(6).setMinWidth(110);
        tcm.getColumn(6).setMaxWidth(130);
        tcm.getColumn(6).setPreferredWidth(120);
        tcm.getColumn(0).setMaxWidth(60);
        tcm.getColumn(0).setPreferredWidth(55);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setBackground(ui.getBg());
        header.setForeground(ui.getSubtext());
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ui.getBorder()));

        final AdminUI uiRef = this.ui;

        // Renderer standard (colonnes 0-5)
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                setForeground(uiRef.getText());
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setHorizontalAlignment(col == 1 ? LEFT : CENTER);

                if (col == 5 && val != null) {
                    switch (val.toString()) {
                        case "PAYÉE"      -> { setForeground(GREEN);  setFont(new Font("Segoe UI", Font.BOLD, 13)); }
                        case "EN_ATTENTE" -> { setForeground(ORANGE); setFont(new Font("Segoe UI", Font.BOLD, 13)); }
                        case "ANNULÉE"    -> { setForeground(RED);    setFont(new Font("Segoe UI", Font.BOLD, 13)); }
                    }
                }
                if (col == 4) setFont(new Font("Segoe UI", Font.BOLD, 13));

                if (!sel) setBackground(row % 2 == 0 ? uiRef.getWhite() : L_ROW_ALT);
                else      setBackground(uiRef.isDarkMode() ? D_SEL : L_SEL);

                return this;
            }
        };
        for (int i = 0; i < 6; i++) tcm.getColumn(i).setCellRenderer(cellRenderer);

        // Renderer + Editor pour la colonne PDF (colonne 6)
        tcm.getColumn(6).setCellRenderer(new PdfButtonRenderer());
        tcm.getColumn(6).setCellEditor(new PdfButtonEditor(new JCheckBox()));

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0)
                    ui.setStatus("  Facture #" + model.getValueAt(row, 0)
                            + " — " + model.getValueAt(row, 1)
                            + " — " + model.getValueAt(row, 4));
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

        List<Facture> factures = dao.getToutesFactures();
        List<Membre>  membres  = membreDao.getAll();

        int total = 0, payee = 0, attente = 0;

        for (Facture f : factures) {
            String membreLabel = membres.stream()
                    .filter(m -> m.getId() == f.getIdMembre())
                    .map(m -> m.getNom() + " " + m.getPrenom())
                    .findFirst().orElse("Membre #" + f.getIdMembre());

            model.addRow(new Object[]{
                    f.getId(),
                    membreLabel,
                    "Rés. #" + f.getIdReservation(),
                    f.getDateFacture(),
                    String.format("%.2f DT", f.getMontant()),
                    f.getStatut(),
                    "Télécharger"          // valeur affichée dans le bouton
            });

            total++;
            switch (f.getStatut()) {
                case "PAYÉE"      -> payee++;
                case "EN_ATTENTE" -> attente++;
            }
        }

        double revenus = dao.getTotalRevenus();
        lblTotal.setText(String.valueOf(total));
        lblPayee.setText(String.valueOf(payee));
        lblAttente.setText(String.valueOf(attente));
        lblRevenus.setText(String.format("%.0f DT", revenus));
        ui.setStatus("  " + total + " factures chargées");
    }

    // ===================== SEARCH =====================
    @Override
    public void onSearch(String query) {
        if (query.isEmpty()) { table.setRowSorter(null); return; }
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
    }

    // ===================== PDF DOWNLOAD =====================
    private void telechargerPdf(int modelRow) {
        int    factureId  = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
        String membre     = model.getValueAt(modelRow, 1).toString();
        String resLabel   = model.getValueAt(modelRow, 2).toString();          // "Rés. #5"
        String dateF      = model.getValueAt(modelRow, 3).toString();
        String montantStr = model.getValueAt(modelRow, 4).toString()
                .replace(" DT", "").replace(",", ".");
        String statut     = model.getValueAt(modelRow, 5).toString();

        double montant;
        try { montant = Double.parseDouble(montantStr); }
        catch (NumberFormatException ex) { montant = 0; }

        // Récupérer les détails de la réservation
        int resId = 0;
        try { resId = Integer.parseInt(resLabel.replace("Rés. #", "").trim()); }
        catch (NumberFormatException ignored) {}

        String typeRes  = "—";
        String dateDebut = "—";
        String dateFin   = "—";

        final int finalResId = resId;
        for (Reservation r : resDao.getToutesReservations()) {
            if (r.getId() == finalResId) {
                typeRes  = r.getType();
                dateDebut = r.getDateDebut() != null ? r.getDateDebut().toString() : "—";
                dateFin   = r.getDateFin()   != null ? r.getDateFin().toString()   : "—";
                break;
            }
        }

        // Choix du dossier de sauvegarde
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Enregistrer le reçu PDF");
        fc.setSelectedFile(new File("Facture_" + String.format("%06d", factureId) + ".pdf"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));

        if (fc.showSaveDialog(ui) != JFileChooser.APPROVE_OPTION) return;

        String path = fc.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";

        // Génération dans un thread séparé pour ne pas bloquer l'UI
        final String finalPath  = path;
        final String finalType  = typeRes;
        final String finalDebut = dateDebut;
        final String finalFin   = dateFin;
        final double finalMt    = montant;
        final int    fId        = factureId;
        final int    frId       = resId;

        ui.setStatus("  Génération du PDF en cours…");

        new Thread(() -> {
            try {
                FacturePdfExporter.generate(
                        finalPath, fId, membre, frId,
                        finalType, finalDebut, finalFin,
                        dateF, finalMt, statut, space_NAME);

                SwingUtilities.invokeLater(() -> {
                    ui.setStatus("  PDF enregistré : " + finalPath);
                    int choice = JOptionPane.showConfirmDialog(ui,
                            "✅ Reçu PDF généré avec succès !\n\n"
                                    + "Emplacement : " + finalPath + "\n\n"
                                    + "Voulez-vous ouvrir le fichier maintenant ?",
                            "PDF généré", JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        try { Desktop.getDesktop().open(new File(finalPath)); }
                        catch (Exception ex) {
                            alert(null, "Impossible d'ouvrir le PDF : " + ex.getMessage());
                        }
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        alert(null, "Erreur lors de la génération du PDF :\n" + ex.getMessage()));
            }
        }).start();
    }

    // ===================== PDF BUTTON RENDERER =====================
    /** Affiche un bouton stylé dans la cellule. */
    private class PdfButtonRenderer extends JPanel implements TableCellRenderer {
        private final IconLabel icon = new IconLabel("download", Color.WHITE,BLUE) {
            { setPreferredSize(new Dimension(16, 16)); }
        };
        private final JLabel lbl = new JLabel("Télécharger");

        PdfButtonRenderer() {
            setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(BLUE);
            add(icon);
            add(lbl);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            setBackground(isSelected ? (FacturePanel.this.ui.isDarkMode() ? D_SEL : L_SEL) : (row % 2 == 0 ? FacturePanel.this.ui.getWhite() : L_ROW_ALT));
            lbl.setText(value != null ? value.toString() : "Télécharger");
            return this;
        }
    }
    // ===================== PDF BUTTON EDITOR =====================
    /** Gère le clic sur le bouton PDF. */
    private class PdfButtonEditor extends DefaultCellEditor {
        private JButton btn;
        private int     clickedRow = -1;

            PdfButtonEditor(JCheckBox checkBox) {
                super(checkBox);

                btn = new JButton() {
                    {
                        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
                        setOpaque(true);
                        setFocusPainted(false);
                        setBorderPainted(false);
                        setBackground(new Color(37, 99, 235));
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                        setBorder(BorderFactory.createEmptyBorder(10, 6, 4, 10));

                        IconLabel icon = new IconLabel("download", new Color(37, 99, 235), Color.WHITE) {
                            { setPreferredSize(new Dimension(16, 16)); }
                        };
                        JLabel lbl = new JLabel("Télécharger");
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        lbl.setForeground(Color.WHITE);
                        add(icon);
                        add(lbl);
                    }
                };

                btn.addActionListener(e -> {
                    fireEditingStopped();
                    if (clickedRow >= 0) {
                        int modelRow = table.convertRowIndexToModel(clickedRow);
                        telechargerPdf(modelRow);
                    }
                });



        }

        @Override
        public Component getTableCellEditorComponent(
                JTable t, Object value, boolean isSelected, int row, int col) {
            clickedRow = row;
            return btn;
        }

        @Override public Object getCellEditorValue() { return "Télécharger"; }
        @Override public boolean isCellEditable(java.util.EventObject e) { return true; }
    }

    // ===================== GÉNÉRER =====================
    private void dialogGenerer() {
        JDialog d = makeDialog("Générer une facture", 420, 220);

        JComboBox<ComboItem> fMembre = new JComboBox<>();
        fMembre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fMembre.setBackground(ui.getBg());
        fMembre.setForeground(ui.getText());
        for (Membre m : membreDao.getAll())
            fMembre.addItem(new ComboItem(m.getId(), m.getNom() + " " + m.getPrenom()));

        JComboBox<ComboItem> fRes = new JComboBox<>();
        fRes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fRes.setBackground(ui.getBg());
        fRes.setForeground(ui.getText());

        chargerReservationsDuMembre(fRes,
                fMembre.getItemCount() > 0 ? ((ComboItem) fMembre.getItemAt(0)).id : -1);

        fMembre.addActionListener(e -> {
            ComboItem sel = (ComboItem) fMembre.getSelectedItem();
            if (sel != null) chargerReservationsDuMembre(fRes, sel.id);
        });

        d.add(makeForm(
                new String[]{"Membre", "Réservation"},
                new JComponent[]{fMembre, fRes}
        ), BorderLayout.CENTER);

        d.add(makeDialogActions(d, () -> {
            ComboItem membre = (ComboItem) fMembre.getSelectedItem();
            ComboItem res    = (ComboItem) fRes.getSelectedItem();

            if (membre == null || res == null) {
                alert(d, "Sélectionnez un membre et une réservation !");
                return;
            }

            Facture f = dao.genererFacture(res.id);
            if (f == null) { alert(d, "Erreur lors de la génération de la facture."); return; }

            load();
            ui.setStatus(String.format("  Facture générée : %.2f DT", f.getMontant()));
            d.dispose();

            JOptionPane.showMessageDialog(ui,
                    "✅ Facture générée avec succès !\n\n"
                            + "Membre      : " + membre.label + "\n"
                            + "Réservation : #" + res.id + "\n"
                            + "Montant     : " + String.format("%.2f DT", f.getMontant()) + "\n"
                            + "Statut      : EN_ATTENTE",
                    "Facture créée", JOptionPane.INFORMATION_MESSAGE);
        }), BorderLayout.SOUTH);

        d.setVisible(true);
    }

    // ===================== CHANGER STATUT =====================
    private void dialogChangerStatut() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { alert(null, "Sélectionnez une facture !"); return; }
        int row = table.convertRowIndexToModel(viewRow);
        int id  = Integer.parseInt(model.getValueAt(row, 0).toString());

        JDialog d = makeDialog("Changer le statut", 360, 180);

        JComboBox<String> fStatut = new JComboBox<>(new String[]{"EN_ATTENTE", "PAYÉE", "ANNULÉE"});
        fStatut.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fStatut.setBackground(ui.getBg());
        fStatut.setForeground(ui.getText());
        fStatut.setSelectedItem(model.getValueAt(row, 5).toString());

        d.add(makeForm(new String[]{"Nouveau statut"}, new JComponent[]{fStatut}), BorderLayout.CENTER);
        d.add(makeDialogActions(d, () -> {
            dao.modifierStatut(id, fStatut.getSelectedItem().toString());
            load();
            ui.setStatus("  Statut mis à jour !");
            d.dispose();
        }), BorderLayout.SOUTH);

        d.setVisible(true);
    }

    // ===================== SUPPRIMER =====================
    private void supprimer() {
        int r = table.getSelectedRow();
        if (r < 0) { alert(null, "Sélectionnez une facture !"); return; }
        int row = table.convertRowIndexToModel(r);
        int id  = Integer.parseInt(model.getValueAt(row, 0).toString());

        if (JOptionPane.showConfirmDialog(ui,
                "Supprimer la facture #" + id + " ?", "Confirmation",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dao.supprimerFacture(id);
            load();
            ui.setStatus("  Facture #" + id + " supprimée !");
        }
    }

    // ===================== COMBO HELPERS =====================
    private static class ComboItem {
        final int id; final String label;
        ComboItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private void chargerReservationsDuMembre(JComboBox<ComboItem> combo, int idMembre) {
        combo.removeAllItems();
        for (Reservation r : resDao.getToutesReservations()) {
            if (r.getIdMembre() == idMembre) {
                String label = "Rés. #" + r.getId()
                        + "  (" + r.getType() + ")"
                        + "  " + r.getDateDebut() + " → " + r.getDateFin();
                combo.addItem(new ComboItem(r.getId(), label));
            }
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
        };        JLabel label = new JLabel(text);
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

    private void alert(Component parent, String msg) {
        JOptionPane.showMessageDialog(
                parent != null ? parent : ui, msg, "Attention", JOptionPane.WARNING_MESSAGE);
    }
}