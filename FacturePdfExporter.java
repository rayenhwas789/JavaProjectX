import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Génère un reçu PDF professionnel pour une facture.
 * Dépendance : itext-5.x.x.jar (itextpdf)
 */
public class FacturePdfExporter {

    // Palette
    private static final BaseColor BLUE_DARK  = new BaseColor(30,  64, 175);
    private static final BaseColor BLUE_MED   = new BaseColor(59,  130, 246);
    private static final BaseColor BLUE_LIGHT = new BaseColor(219, 234, 254);
    private static final BaseColor GREEN      = new BaseColor(22,  163, 74);
    private static final BaseColor ORANGE     = new BaseColor(234, 88,  12);
    private static final BaseColor RED        = new BaseColor(220, 38,  38);
    private static final BaseColor GRAY_DARK  = new BaseColor(31,  41,  55);
    private static final BaseColor GRAY_MID   = new BaseColor(107, 114, 128);
    private static final BaseColor GRAY_LIGHT = new BaseColor(243, 244, 246);
    private static final BaseColor WHITE      = BaseColor.WHITE;

    // Polices
    private static Font fontTitle()    { return new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, WHITE); }
    private static Font fontSubtitle() { return new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(191,219,254)); }
    private static Font fontSectionH() { return new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BLUE_DARK); }
    private static Font fontLabel()    { return new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, GRAY_MID); }
    private static Font fontValue()    { return new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, GRAY_DARK); }
    private static Font fontAmount()   { return new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BLUE_DARK); }
    private static Font fontSmall()    { return new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, GRAY_MID); }
    private static Font fontStatus(String statut) {
        BaseColor c = switch (statut) {
            case "PAYÉE"      -> GREEN;
            case "EN_ATTENTE" -> ORANGE;
            case "ANNULÉE"    -> RED;
            default           -> GRAY_MID;
        };
        return new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, c);
    }

    /**
     * @param outputPath chemin absolu du fichier PDF à créer
     * @param factureId  identifiant de la facture
     * @param membre     nom complet du membre
     * @param resId      id de la réservation
     * @param typeRes    type de réservation (ex. "SALLE", "COURS")
     * @param dateDebut  date début réservation
     * @param dateFin    date fin réservation
     * @param dateFacture date d'émission
     * @param montant    montant en DT
     * @param statut     EN_ATTENTE | PAYÉE | ANNULÉE
     * @param clubName   nom du club (personnalisable)
     */
    public static void generate(
            String outputPath,
            int    factureId,
            String membre,
            int    resId,
            String typeRes,
            String dateDebut,
            String dateFin,
            String dateFacture,
            double montant,
            String statut,
            String clubName
    ) throws Exception {

        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outputPath));
        doc.open();

        // ── HEADER ──────────────────────────────────────────────────────────
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{3f, 1.5f});
        header.setSpacingAfter(20);

        // Cellule gauche : nom du club + titre
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBackgroundColor(BLUE_DARK);
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPadding(20);

        Paragraph clubP = new Paragraph(clubName, fontTitle());
        clubP.setSpacingAfter(4);
        leftCell.addElement(clubP);
        leftCell.addElement(new Paragraph("Reçu de Paiement", fontSubtitle()));

        // Cellule droite : numéro de facture
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBackgroundColor(BLUE_MED);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPadding(20);
        rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Font fNum = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, new BaseColor(191,219,254));
        Font fNumVal = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, WHITE);
        rightCell.addElement(new Paragraph("FACTURE N°", fNum));
        Paragraph numP = new Paragraph(String.format("%06d", factureId), fNumVal);
        numP.setAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(numP);

        header.addCell(leftCell);
        header.addCell(rightCell);
        doc.add(header);

        // ── STATUT BADGE ────────────────────────────────────────────────────
        PdfPTable statusRow = new PdfPTable(1);
        statusRow.setWidthPercentage(100);
        statusRow.setSpacingAfter(16);

        BaseColor statusBg = switch (statut) {
            case "PAYÉE"      -> new BaseColor(220, 252, 231);
            case "EN_ATTENTE" -> new BaseColor(255, 237, 213);
            case "ANNULÉE"    -> new BaseColor(254, 226, 226);
            default           -> GRAY_LIGHT;
        };
        String statusLabel = switch (statut) {
            case "PAYÉE"      -> "✔  FACTURE PAYÉE";
            case "EN_ATTENTE" -> "⏳  EN ATTENTE DE PAIEMENT";
            case "ANNULÉE"    -> "✕  FACTURE ANNULÉE";
            default           -> statut;
        };

        PdfPCell statusCell = new PdfPCell(new Phrase(statusLabel, fontStatus(statut)));
        statusCell.setBackgroundColor(statusBg);
        statusCell.setBorder(Rectangle.NO_BORDER);
        statusCell.setPadding(10);
        statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        statusRow.addCell(statusCell);
        doc.add(statusRow);

        // ── INFOS FACTURE + MEMBRE ───────────────────────────────────────────
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1f, 1f});
        infoTable.setSpacingAfter(16);

        // Bloc gauche : infos facture
        PdfPCell leftInfo = infoSection("Informations Facture",
                new String[]{"Numéro de facture", "Date d'émission", "Date d'impression"},
                new String[]{
                        String.format("#%06d", factureId),
                        dateFacture,
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                });
        // Bloc droit : infos membre
        PdfPCell rightInfo = infoSection("Informations Membre",
                new String[]{"Nom complet", "Référence membre"},
                new String[]{membre, "MBR-" + String.format("%04d", resId)});

        infoTable.addCell(leftInfo);
        infoTable.addCell(rightInfo);
        doc.add(infoTable);

        // ── DÉTAIL RÉSERVATION ───────────────────────────────────────────────
        doc.add(sectionTitle("Détail de la Réservation"));

        PdfPTable detailTable = new PdfPTable(4);
        detailTable.setWidthPercentage(100);
        detailTable.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1.5f});
        detailTable.setSpacingAfter(20);

        // En-tête du tableau
        String[] detailHeaders = {"Réservation", "Type", "Date début", "Date fin"};
        for (String h : detailHeaders) {
            PdfPCell hCell = new PdfPCell(new Phrase(h, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, WHITE)));
            hCell.setBackgroundColor(BLUE_DARK);
            hCell.setBorder(Rectangle.NO_BORDER);
            hCell.setPadding(10);
            hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            detailTable.addCell(hCell);
        }
        // Ligne de données
        String[] detailVals = {"Rés. #" + resId, typeRes, dateDebut, dateFin};
        for (int i = 0; i < detailVals.length; i++) {
            PdfPCell vCell = new PdfPCell(new Phrase(detailVals[i], fontValue()));
            vCell.setBackgroundColor(i % 2 == 0 ? GRAY_LIGHT : WHITE);
            vCell.setBorder(Rectangle.NO_BORDER);
            vCell.setPadding(10);
            vCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            detailTable.addCell(vCell);
        }
        doc.add(detailTable);

        // ── MONTANT TOTAL ────────────────────────────────────────────────────
        doc.add(sectionTitle("Récapitulatif"));

        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setWidths(new float[]{2f, 1f});
        totalTable.setSpacingAfter(24);

        addTotalRow(totalTable, "Sous-total HT",   String.format("%.2f DT", montant * 0.81), false);
        addTotalRow(totalTable, "TVA (19%)",        String.format("%.2f DT", montant * 0.19), false);
        addTotalRowHighlight(totalTable, "MONTANT TOTAL TTC", String.format("%.2f DT", montant));
        doc.add(totalTable);

        // ── GRAND TOTAL VISUEL ───────────────────────────────────────────────
        PdfPTable bigTotal = new PdfPTable(1);
        bigTotal.setWidthPercentage(60);
        bigTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        bigTotal.setSpacingAfter(24);

        PdfPCell bigCell = new PdfPCell();
        bigCell.setBackgroundColor(BLUE_LIGHT);
        bigCell.setBorder(Rectangle.NO_BORDER);
        bigCell.setPadding(16);
        bigCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph amtLabel = new Paragraph("Montant à régler", new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BLUE_DARK));
        amtLabel.setAlignment(Element.ALIGN_CENTER);
        Paragraph amtVal = new Paragraph(String.format("%.2f DT", montant), fontAmount());
        amtVal.setAlignment(Element.ALIGN_CENTER);
        bigCell.addElement(amtLabel);
        bigCell.addElement(amtVal);
        bigTotal.addCell(bigCell);
        doc.add(bigTotal);

        // ── PIED DE PAGE ─────────────────────────────────────────────────────
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        footer.setSpacingBefore(10);

        PdfPCell footCell = new PdfPCell();
        footCell.setBackgroundColor(GRAY_LIGHT);
        footCell.setBorder(Rectangle.TOP);
        footCell.setBorderColor(new BaseColor(209, 213, 219));
        footCell.setPadding(14);

        Paragraph footP = new Paragraph(
                clubName + " — Merci pour votre confiance.\n"
                        + "Ce document est généré automatiquement et fait office de reçu officiel.",
                fontSmall());
        footP.setAlignment(Element.ALIGN_CENTER);
        footCell.addElement(footP);
        footer.addCell(footCell);
        doc.add(footer);

        doc.close();
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private static PdfPCell infoSection(String title, String[] labels, String[] values) {
        PdfPCell outer = new PdfPCell();
        outer.setBorder(Rectangle.NO_BORDER);
        outer.setPaddingRight(10);

        Paragraph t = new Paragraph(title, fontSectionH());
        t.setSpacingAfter(8);
        outer.addElement(t);

        PdfPTable inner = new PdfPTable(2);
        inner.setWidthPercentage(100);
        try { inner.setWidths(new float[]{1.2f, 1.8f}); } catch (Exception ignored) {}

        for (int i = 0; i < labels.length; i++) {
            PdfPCell lbl = new PdfPCell(new Phrase(labels[i], fontLabel()));
            lbl.setBorder(Rectangle.NO_BORDER);
            lbl.setPaddingBottom(6);
            PdfPCell val = new PdfPCell(new Phrase(values[i], fontValue()));
            val.setBorder(Rectangle.NO_BORDER);
            val.setPaddingBottom(6);
            inner.addCell(lbl);
            inner.addCell(val);
        }
        outer.addElement(inner);
        return outer;
    }

    private static Paragraph sectionTitle(String text) {
        Paragraph p = new Paragraph(text, fontSectionH());
        p.setSpacingBefore(4);
        p.setSpacingAfter(8);
        return p;
    }

    private static void addTotalRow(PdfPTable t, String label, String value, boolean bold) {
        Font lf = bold ? new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, GRAY_DARK) : fontLabel();
        Font vf = bold ? new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, GRAY_DARK) : fontValue();
        PdfPCell lc = new PdfPCell(new Phrase(label, lf));
        lc.setBorder(Rectangle.BOTTOM); lc.setBorderColor(new BaseColor(229,231,235));
        lc.setPadding(8); lc.setHorizontalAlignment(Element.ALIGN_LEFT);
        PdfPCell vc = new PdfPCell(new Phrase(value, vf));
        vc.setBorder(Rectangle.BOTTOM); vc.setBorderColor(new BaseColor(229,231,235));
        vc.setPadding(8); vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(lc); t.addCell(vc);
    }

    private static void addTotalRowHighlight(PdfPTable t, String label, String value) {
        Font lf = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, WHITE);
        Font vf = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, WHITE);
        PdfPCell lc = new PdfPCell(new Phrase(label, lf));
        lc.setBackgroundColor(BLUE_DARK); lc.setBorder(Rectangle.NO_BORDER);
        lc.setPadding(10); lc.setHorizontalAlignment(Element.ALIGN_LEFT);
        PdfPCell vc = new PdfPCell(new Phrase(value, vf));
        vc.setBackgroundColor(BLUE_DARK); vc.setBorder(Rectangle.NO_BORDER);
        vc.setPadding(10); vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(lc); t.addCell(vc);
    }
}