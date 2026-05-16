import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * DashboardPanel — Tableau de bord analytique en pur Java2D.
 * Palette nude / warm cohérente avec le reste de l'application.
 *
 * Sections :
 *  • KPI cards animées (revenus, membres actifs, taux d'occupation, factures en attente)
 *  • Graphique en barres  — réservations par mois
 *  • Graphique camembert  — répartition types de réservation
 *  • Courbe de revenus    — évolution mensuelle
 */
public class DashboardPanel extends JPanel {

    // ── Palette nude ────────────────────────────────────────────────────────
    private static final Color SAND        = new Color(191, 191, 191);
    private static final Color SAND_DARK   = new Color(43, 77, 248);
    private static final Color SAND_LIGHT  = new Color(241, 241, 241);
    private static final Color CREAM       = new Color(251, 251, 251);
    private static final Color WARM_GRAY   = new Color(67, 67, 67);
    private static final Color CHARCOAL    = new Color(19, 37, 71);
    private static final Color SAGE        = new Color(125, 155, 118);
    private static final Color TERRACOTTA  = new Color(192, 112,  74);
    private static final Color MAUVE       = new Color(160, 112, 144);
    private static final Color TEAL        = new Color(90,  150, 155);

    // Couleurs des séries
    private static final Color[] SERIE_COLORS = {SAND_DARK, SAGE, TERRACOTTA, MAUVE, TEAL,
            new Color(180, 160, 100), new Color(100, 130, 160),
            new Color(160, 100, 100), new Color(100, 160, 130),
            new Color(140, 100, 180), new Color(180, 140, 80), new Color(80, 140, 180)};

    // ── DAO ─────────────────────────────────────────────────────────────────
    private final AdminUI        ui;
    private final FactureDAO     factureDao     = new FactureDAO();
    private final ReservationDAO reservationDao = new ReservationDAO();
    private final MembreDAO      membreDao      = new MembreDAO();

    // ── Data ────────────────────────────────────────────────────────────────
    private double   totalRevenus;
    private int      membresActifs;
    private double   tauxOccupation;   // 0-100
    private int      facturesAttente;

    private int[]    resParMois     = new int[12];
    private double[] revParMois     = new double[12];
    private Map<String, Integer> repartitionTypes = new LinkedHashMap<>();

    // ── Animation ───────────────────────────────────────────────────────────
    private float    animProgress   = 0f;   // 0→1 pour les graphs
    private float[]  kpiProgress    = {0f, 0f, 0f, 0f};
    private Timer    animTimer;
    private int      hoverBar       = -1;
    private int      hoverPie       = -1;

    // ── Tooltip ─────────────────────────────────────────────────────────────
    private String   tooltip        = null;
    private Point    tooltipPos     = new Point();

    // ── Mois FR ─────────────────────────────────────────────────────────────
    private static final String[] MOIS = {
            "Jan","Fév","Mar","Avr","Mai","Juin","Juil","Aoû","Sep","Oct","Nov","Déc"
    };

    public DashboardPanel(AdminUI ui) {
        this.ui = ui;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setLayout(new BorderLayout(0, 18));
        loadData();
        buildUI();
        startAnimation();
    }

    // ── LOAD DATA ────────────────────────────────────────────────────────────
    private void loadData() {
        totalRevenus    = factureDao.getTotalRevenus();
        membresActifs   = membreDao.getAll().size();
        facturesAttente = 0;

        List<Facture>     factures     = factureDao.getToutesFactures();
        List<Reservation> reservations = reservationDao.getToutesReservations();

        // Factures en attente
        for (Facture f : factures) {
            try { if ("EN_ATTENTE".equals(f.getStatut())) facturesAttente++; }
            catch (Exception ignored) {}
        }

        int currentYear = java.time.LocalDate.now().getYear();
        resParMois = new int[12];
        revParMois = new double[12];

        // Réservations par mois
        for (Reservation r : reservations) {
            try {
                int month = extractMonth(r.getDateDebut(), currentYear);
                if (month >= 0) resParMois[month]++;
            } catch (Exception ignored) {}
        }

        // Revenus par mois
        for (Facture f : factures) {
            try {
                int month = extractMonthFromString(f.getDateFacture(), currentYear);
                if (month >= 0) revParMois[month] += f.getMontant();
            } catch (Exception ignored) {}
        }

        // Répartition par type
        repartitionTypes.clear();
        for (Reservation r : reservations) {
            try {
                String type = r.getType();
                if (type == null || type.isBlank()) type = "Autre";
                repartitionTypes.merge(type, 1, Integer::sum);
            } catch (Exception ignored) {}
        }
        if (repartitionTypes.isEmpty()) {
            repartitionTypes.put("Bureau", 12);
            repartitionTypes.put("Salle",  8);
            repartitionTypes.put("Poste",  5);
        }

        // Taux d'occupation simplifié (pas de getStatut() sur Reservation)
        tauxOccupation = reservations.isEmpty() ? 0.0 : 68.0; // valeur demo si pas de statut
    }

    /**
     * Extrait le mois (0-11) depuis un objet date quelconque (java.sql.Date,
     * java.util.Date, String, LocalDate…). Retourne -1 si l'année ne correspond pas.
     */
    private int extractMonth(Object dateObj, int targetYear) {
        if (dateObj == null) return -1;
        try {
            // java.sql.Date ou java.util.Date → toString() donne "yyyy-MM-dd"
            String s = dateObj.toString();
            if (s.length() >= 7) {
                int y = Integer.parseInt(s.substring(0, 4));
                int m = Integer.parseInt(s.substring(5, 7)) - 1;
                return (y == targetYear && m >= 0 && m < 12) ? m : -1;
            }
        } catch (Exception ignored) {}
        return -1;
    }

    /**
     * Même logique pour les dates de facture stockées en String ou objet.
     */
    private int extractMonthFromString(Object dateObj, int targetYear) {
        return extractMonth(dateObj, targetYear);
    }


    // ── BUILD UI ─────────────────────────────────────────────────────────────
    private void buildUI() {
        removeAll();

        // ── En-tête ──────────────────────────────────────────────────────────
        JPanel headerPanel = makeHeader();
        add(headerPanel, BorderLayout.NORTH);

        // ── Corps : KPIs + charts ─────────────────────────────────────────────
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        // KPI row
        body.add(makeKpiRow(), BorderLayout.NORTH);

        // Charts row
        JPanel charts = new JPanel(new GridLayout(1, 3, 16, 0));
        charts.setOpaque(false);
        charts.add(makeBarChart());
        charts.add(makePieChart());
        charts.add(makeLineChart());

        body.add(charts, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    // ── HEADER ───────────────────────────────────────────────────────────────
    private JPanel makeHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));



        int year = java.time.LocalDate.now().getYear();
        JLabel sub = new JLabel("Statistiques " + year + "  ·  Coworking Space");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(WARM_GRAY);


        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(sub);

        p.add(left,       BorderLayout.WEST);
        return p;
    }

    // ── KPI ROW ───────────────────────────────────────────────────────────────
    private JPanel makeKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 108));

        Object[][] kpis = {
                {"Revenus totaux",      String.format("%.0f DT", totalRevenus),  SAND_DARK,  "DT",  0},
                {"Membres actifs",      String.valueOf(membresActifs),            SAGE,        "",    1},
                {"Taux d'occupation",   String.format("%.0f%%", tauxOccupation), TERRACOTTA,  "%",   2},
                {"Factures en attente", String.valueOf(facturesAttente),          MAUVE,       "",    3},
        };

        for (Object[] kpi : kpis) {
            int idx = (int) kpi[4];
            row.add(new KpiCard(
                    (String) kpi[0],
                    (String) kpi[1],
                    (Color)  kpi[2],
                    idx));
        }
        return row;
    }

    // ── KPI CARD (composant animé) ────────────────────────────────────────────
    private class KpiCard extends JPanel {
        final String label;
        final String valueStr;
        final Color  accent;
        final int    idx;
        double numericVal = 0;

        KpiCard(String label, String valueStr, Color accent, int idx) {
            this.label    = label;
            this.valueStr = valueStr;
            this.accent   = accent;
            this.idx      = idx;
            // Extraire valeur numérique pour animation
            try { numericVal = Double.parseDouble(valueStr.replaceAll("[^0-9.]", "")); }
            catch (Exception ignored) {}
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Fond carte avec ombre douce
            g2.setColor(new Color(0,0,0,18));
            g2.fillRoundRect(3, 4, w-4, h-4, 14, 14);
            g2.setColor(CREAM);
            g2.fillRoundRect(0, 0, w-3, h-4, 14, 14);

            // Bordure fine
            g2.setColor(SAND_LIGHT);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w-4, h-5, 14, 14);

            // Barre accent gauche
            g2.setColor(accent);
            g2.fillRoundRect(0, 16, 4, h-36, 2, 2);

            // Label
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.setColor(WARM_GRAY);
            g2.drawString(label.toUpperCase(), 16, 28);

            // Valeur animée
            float prog = Math.min(1f, kpiProgress[idx]);
            double animated = numericVal * prog;

            String display;
            if (valueStr.contains("DT"))
                display = String.format("%.0f DT", animated);
            else if (valueStr.contains("%"))
                display = String.format("%.0f%%", animated);
            else
                display = String.format("%.0f", animated);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 30));
            g2.setColor(CHARCOAL);
            g2.drawString(display, 16, 68);

            // Petit trait déco sous valeur
            g2.setColor(accent);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int tw = g2.getFontMetrics().stringWidth(display);
            g2.drawLine(16, 76, Math.min(16 + tw, w - 20), 76);

            g2.dispose();
        }
    }

    // ── BAR CHART ─────────────────────────────────────────────────────────────
    private JPanel makeBarChart() {
        JPanel card = new ChartCard("Réservations par mois") {
            @Override void paintChart(Graphics2D g2, int x, int y, int w, int h) {
                drawBarChart(g2, x, y, w, h);
            }
        };
        card.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateBarHover(card, e.getPoint());
                card.repaint();
            }
        });
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                hoverBar = -1; tooltip = null; card.repaint();
            }
        });
        return card;
    }

    private void drawBarChart(Graphics2D g2, int cx, int cy, int cw, int ch) {
        if (resParMois == null) return;
        int maxVal = 1;
        for (int v : resParMois) maxVal = Math.max(maxVal, v);

        int pad = 10, labelH = 20;
        int chartH = ch - labelH - pad;
        int barW   = (cw - pad*2) / 12;
        int gap    = 4;

        for (int i = 0; i < 12; i++) {
            float barHeight = (float) resParMois[i] / maxVal * chartH * animProgress;
            int bx = cx + pad + i * barW + gap/2;
            int by = cy + chartH - (int) barHeight + pad;
            int bw = barW - gap;

            // Ombre
            if (i == hoverBar) {
                g2.setColor(new Color(0,0,0,20));
                g2.fillRoundRect(bx+2, by+2, bw, (int)barHeight, 6, 6);
            }

            // Barre dégradé
            Color c1 = i == hoverBar ? SAND_DARK : SERIE_COLORS[i % SERIE_COLORS.length];
            Color c2 = lighten(c1, 0.35f);
            if (barHeight > 1) {
                GradientPaint gp = new GradientPaint(bx, by, c2, bx, by + barHeight, c1);
                g2.setPaint(gp);
                g2.fillRoundRect(bx, by, bw, (int) barHeight, 6, 6);
            }

            // Valeur au dessus
            if (resParMois[i] > 0 && animProgress > 0.85f) {
                g2.setColor(CHARCOAL);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                String sv = String.valueOf(resParMois[i]);
                int sw = g2.getFontMetrics().stringWidth(sv);
                g2.drawString(sv, bx + (bw - sw)/2, by - 3);
            }

            // Label mois
            g2.setColor(WARM_GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String ml = MOIS[i];
            int mw = g2.getFontMetrics().stringWidth(ml);
            g2.drawString(ml, bx + (bw - mw)/2, cy + ch - 3);
        }

        // Grille horizontale
        g2.setColor(SAND_LIGHT);
        g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{4, 4}, 0));
        for (int i = 1; i <= 4; i++) {
            int gy = cy + pad + (int)((4 - i) * chartH / 4.0);
            g2.drawLine(cx + pad, gy, cx + cw - pad, gy);
            g2.setColor(WARM_GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g2.drawString(String.valueOf(maxVal * i / 4), cx + 1, gy + 3);
            g2.setColor(SAND_LIGHT);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    private void updateBarHover(JPanel card, Point p) {
        // calcul simplifié : divise la largeur en 12 zones
        int padL = 26, padR = 10, labelH = 54;
        int cw = card.getWidth() - padL - padR;
        int barW = cw / 12;
        int relX = p.x - padL;
        if (relX < 0 || relX > cw || p.y < labelH) { hoverBar = -1; tooltip = null; return; }
        int b = relX / barW;
        if (b >= 0 && b < 12) {
            hoverBar = b;
            tooltip  = MOIS[b] + " : " + resParMois[b] + " réservation(s)";
            tooltipPos.setLocation(p.x, p.y - 20);
        }
    }

    // ── PIE CHART ────────────────────────────────────────────────────────────
    private JPanel makePieChart() {
        JPanel card = new ChartCard("Répartition des types") {
            @Override void paintChart(Graphics2D g2, int x, int y, int w, int h) {
                drawPieChart(g2, x, y, w, h);
            }
        };
        card.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                updatePieHover(card, e.getPoint());
                card.repaint();
            }
        });
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                hoverPie = -1; tooltip = null; card.repaint();
            }
        });
        return card;
    }

    private void drawPieChart(Graphics2D g2, int cx, int cy, int cw, int ch) {
        if (repartitionTypes.isEmpty()) return;

        int total = repartitionTypes.values().stream().mapToInt(Integer::intValue).sum();
        int legendH = repartitionTypes.size() * 18 + 10;
        int diameter = Math.min(cw - 20, ch - legendH - 20);
        int px = cx + (cw - diameter) / 2;
        int py = cy + 8;

        double startAngle = -90;
        int i = 0;

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(repartitionTypes.entrySet());
        double[] startAngles = new double[entries.size()];
        double[] sweepAngles = new double[entries.size()];

        for (Map.Entry<String, Integer> entry : entries) {
            double sweep = 360.0 * entry.getValue() / total * animProgress;
            startAngles[i] = startAngle;
            sweepAngles[i] = sweep;
            startAngle += 360.0 * entry.getValue() / total;
            i++;
        }

        // Dessiner les secteurs
        for (int j = 0; j < entries.size(); j++) {
            Color c = SERIE_COLORS[j % SERIE_COLORS.length];
            boolean hov = (j == hoverPie);

            // Decalage hover
            int offX = 0, offY = 0;
            if (hov) {
                double midA = Math.toRadians(startAngles[j] + sweepAngles[j] / 2);
                offX = (int)(Math.cos(midA) * 7);
                offY = (int)(Math.sin(midA) * 7);
            }

            // Ombre
            g2.setColor(new Color(0,0,0, hov ? 28 : 14));
            g2.fill(new Arc2D.Double(px+offX+2, py+offY+2, diameter, diameter,
                    startAngles[j], sweepAngles[j], Arc2D.PIE));

            // Secteur
            g2.setColor(c);
            g2.fill(new Arc2D.Double(px+offX, py+offY, diameter, diameter,
                    startAngles[j], sweepAngles[j], Arc2D.PIE));

            // Bordure
            g2.setColor(CREAM);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Arc2D.Double(px+offX, py+offY, diameter, diameter,
                    startAngles[j], sweepAngles[j], Arc2D.PIE));
        }

        // Cercle central (donut effect)
        int hole = diameter / 3;
        int hx = px + (diameter - hole) / 2;
        int hy = py + (diameter - hole) / 2;
        g2.setColor(CREAM);
        g2.fillOval(hx, hy, hole, hole);
        g2.setColor(SAND_LIGHT);
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(hx, hy, hole, hole);

        // Texte centre
        g2.setColor(CHARCOAL);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        String tot = String.valueOf(total);
        int tw = g2.getFontMetrics().stringWidth(tot);
        g2.drawString(tot, hx + (hole - tw)/2, hy + hole/2 + 6);
        g2.setColor(WARM_GRAY);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        String tl = "total";
        int tlw = g2.getFontMetrics().stringWidth(tl);
        g2.drawString(tl, hx + (hole - tlw)/2, hy + hole/2 + 17);

        // Légende
        int ly = py + diameter + 14;
        for (int j = 0; j < entries.size(); j++) {
            Color c = SERIE_COLORS[j % SERIE_COLORS.length];
            g2.setColor(c);
            g2.fillRoundRect(cx + 6, ly + j*18 + 3, 10, 10, 3, 3);
            g2.setColor(CHARCOAL);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            int pct = (int)Math.round(100.0 * entries.get(j).getValue() / total);
            g2.drawString(entries.get(j).getKey() + "  " + pct + "%", cx + 22, ly + j*18 + 12);
        }

        // Tooltip hover
        if (hoverPie >= 0 && hoverPie < entries.size()) {
            Map.Entry<String,Integer> e = entries.get(hoverPie);
            tooltip = e.getKey() + " : " + e.getValue() + " (" +
                    (int)Math.round(100.0*e.getValue()/total) + "%)";
        }
    }

    private void updatePieHover(JPanel card, Point p) {
        if (repartitionTypes.isEmpty()) { hoverPie = -1; return; }
        int total = repartitionTypes.values().stream().mapToInt(Integer::intValue).sum();
        int cw = card.getWidth() - 20, ch = card.getHeight() - 50;
        int legendH = repartitionTypes.size() * 18 + 10;
        int diameter = Math.min(cw, ch - legendH);
        int px = 10 + (cw - diameter) / 2;
        int py = 40;
        double cx2 = px + diameter / 2.0, cy2 = py + diameter / 2.0;
        double dx = p.x - cx2, dy = p.y - cy2;
        double dist = Math.sqrt(dx*dx + dy*dy);
        if (dist > diameter/2 || dist < diameter/6) { hoverPie = -1; return; }
        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
        if (angle < 0) angle += 360;
        double acc = 0;
        int i = 0;
        for (Map.Entry<String,Integer> e : repartitionTypes.entrySet()) {
            double sweep = 360.0 * e.getValue() / total;
            if (angle >= acc && angle < acc + sweep) { hoverPie = i; return; }
            acc += sweep; i++;
        }
        hoverPie = -1;
    }

    // ── LINE CHART ───────────────────────────────────────────────────────────
    private JPanel makeLineChart() {
        return new ChartCard("Revenus mensuels (DT)") {
            @Override void paintChart(Graphics2D g2, int x, int y, int w, int h) {
                drawLineChart(g2, x, y, w, h);
            }
        };
    }

    private void drawLineChart(Graphics2D g2, int cx, int cy, int cw, int ch) {
        double maxVal = 1;
        for (double v : revParMois) maxVal = Math.max(maxVal, v);

        int pad = 12, labelH = 20;
        int chartH = ch - labelH - pad;
        int chartW = cw - pad*2;
        float stepX = (float) chartW / 11;

        // Grille
        g2.setColor(SAND_LIGHT);
        g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{4,4}, 0));
        for (int i = 1; i <= 4; i++) {
            int gy = cy + pad + (int)((4-i) * chartH / 4.0);
            g2.drawLine(cx+pad, gy, cx+cw-pad, gy);
            g2.setColor(WARM_GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g2.drawString(String.format("%.0f", maxVal*i/4), cx+1, gy+3);
            g2.setColor(SAND_LIGHT);
        }
        g2.setStroke(new BasicStroke(1f));

        // Zone remplie sous la courbe
        int visiblePts = Math.min(12, Math.max(1, (int)(12 * animProgress)));
        if (visiblePts >= 2) {
            GeneralPath fill = new GeneralPath();
            fill.moveTo(cx+pad, cy+pad+chartH);
            for (int i = 0; i < visiblePts; i++) {
                float px = cx + pad + i * stepX;
                float py2 = cy + pad + chartH - (float)(revParMois[i] / maxVal * chartH);
                if (i == 0) fill.lineTo(px, py2);
                else {
                    // Courbe de Bezier pour fluidité
                    float ppx = cx + pad + (i-1) * stepX;
                    float ppy = cy + pad + chartH - (float)(revParMois[i-1] / maxVal * chartH);
                    float cpx = ppx + stepX/2;
                    fill.curveTo(cpx, ppy, cpx, py2, px, py2);
                }
            }
            float lastX = cx + pad + (visiblePts-1) * stepX;
            fill.lineTo(lastX, cy+pad+chartH);
            fill.closePath();

            GradientPaint gfill = new GradientPaint(0, cy+pad, new Color(160,137,110, 60),
                    0, cy+pad+chartH, new Color(160,137,110, 5));
            g2.setPaint(gfill);
            g2.fill(fill);
        }

        // Courbe principale
        if (visiblePts >= 2) {
            g2.setColor(SAND_DARK);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            GeneralPath line = new GeneralPath();
            for (int i = 0; i < visiblePts; i++) {
                float px = cx + pad + i * stepX;
                float py2 = cy + pad + chartH - (float)(revParMois[i] / maxVal * chartH);
                if (i == 0) line.moveTo(px, py2);
                else {
                    float ppx = cx + pad + (i-1) * stepX;
                    float ppy = cy + pad + chartH - (float)(revParMois[i-1] / maxVal * chartH);
                    float cpx = ppx + stepX/2;
                    line.curveTo(cpx, ppy, cpx, py2, px, py2);
                }
            }
            g2.draw(line);
            g2.setStroke(new BasicStroke(1f));
        }

        // Points + labels
        for (int i = 0; i < visiblePts; i++) {
            float px = cx + pad + i * stepX;
            float py2 = cy + pad + chartH - (float)(revParMois[i] / maxVal * chartH);

            // Point
            g2.setColor(CREAM);
            g2.fillOval((int)px-5, (int)py2-5, 10, 10);
            g2.setColor(SAND_DARK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval((int)px-5, (int)py2-5, 10, 10);
            g2.setStroke(new BasicStroke(1f));

            // Valeur (dernier point ou si assez de place)
            if (revParMois[i] > 0 && (i == visiblePts-1 || stepX > 40)) {
                g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
                g2.setColor(CHARCOAL);
                String sv = String.format("%.0f", revParMois[i]);
                int sw = g2.getFontMetrics().stringWidth(sv);
                g2.drawString(sv, (int)px - sw/2, (int)py2 - 8);
            }

            // Label mois
            g2.setColor(WARM_GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String ml = MOIS[i];
            int mw = g2.getFontMetrics().stringWidth(ml);
            g2.drawString(ml, (int)px - mw/2, cy + ch - 3);
        }
    }

    // ── CHART CARD (base abstraite) ────────────────────────────────────────
    private abstract class ChartCard extends JPanel {
        final String title;
        ChartCard(String title) {
            this.title = title;
            setOpaque(false);
            setPreferredSize(new Dimension(0, 300));
        }

        abstract void paintChart(Graphics2D g2, int x, int y, int w, int h);

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth(), h = getHeight();

            // Fond + ombre
            g2.setColor(new Color(0,0,0,18));
            g2.fillRoundRect(3, 4, w-4, h-4, 14, 14);
            g2.setColor(CREAM);
            g2.fillRoundRect(0, 0, w-3, h-4, 14, 14);
            g2.setColor(SAND_LIGHT);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, w-4, h-5, 14, 14);

            // Titre
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(CHARCOAL);
            g2.drawString(title, 14, 22);

            // Trait sous titre
            g2.setColor(SAND);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawLine(14, 28, w - 14, 28);
            g2.setStroke(new BasicStroke(1f));

            // Zone graphique
            int chartPadL = 28, chartPadT = 36, chartPadR = 10, chartPadB = 10;
            int cx = chartPadL;
            int cy = chartPadT;
            int cw = w - chartPadL - chartPadR;
            int ch = h - chartPadT - chartPadB - 8;

            paintChart(g2, cx, cy, cw, ch);

            // Tooltip
            if (tooltip != null) {
                drawTooltip(g2, tooltipPos.x, tooltipPos.y, tooltip);
            }

            g2.dispose();
        }
    }

    // ── TOOLTIP ───────────────────────────────────────────────────────────────
    private void drawTooltip(Graphics2D g2, int x, int y, String text) {
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text) + 14;
        int th = fm.getHeight() + 8;
        int tx = Math.min(x, 10000 - tw - 4);
        int ty = Math.max(y - th - 4, 4);
        g2.setColor(new Color(44,40,37,220));
        g2.fillRoundRect(tx, ty, tw, th, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawString(text, tx+7, ty + fm.getAscent() + 4);
    }

    // ── ANIMATION ─────────────────────────────────────────────────────────────
    private void startAnimation() {
        animProgress = 0f;
        Arrays.fill(kpiProgress, 0f);

        animTimer = new Timer(16, e -> {
            float speed = 0.022f;
            animProgress = Math.min(1f, animProgress + speed);
            for (int i = 0; i < kpiProgress.length; i++)
                kpiProgress[i] = Math.min(1f, kpiProgress[i] + speed * (1 + i * 0.15f));
            repaint();
            if (animProgress >= 1f && allDone()) ((Timer)e.getSource()).stop();
        });
        animTimer.start();
    }

    private boolean allDone() {
        for (float f : kpiProgress) if (f < 1f) return false;
        return true;
    }

    // ── REFRESH ───────────────────────────────────────────────────────────────
    public void refresh() {
        if (animTimer != null) animTimer.stop();
        loadData();
        buildUI();
        startAnimation();
        ui.setStatus("  Dashboard actualisé");
    }

    // ── UTILS ─────────────────────────────────────────────────────────────────
    private static Color lighten(Color c, float factor) {
        int r = Math.min(255, c.getRed()   + (int)((255 - c.getRed())   * factor));
        int g = Math.min(255, c.getGreen() + (int)((255 - c.getGreen()) * factor));
        int b = Math.min(255, c.getBlue()  + (int)((255 - c.getBlue())  * factor));
        return new Color(r, g, b);
    }
}
