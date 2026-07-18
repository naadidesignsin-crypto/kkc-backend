package com.kkc.kundali.service;

import com.kkc.kundali.dto.DashaPeriodResponse;
import com.kkc.kundali.dto.KundaliDashaResponse;
import com.kkc.kundali.dto.KundaliDoshaResponse;
import com.kkc.kundali.dto.KundaliPlanetsResponse;
import com.kkc.kundali.dto.KundaliSummaryResponse;
import com.kkc.kundali.dto.PlanetPositionResponse;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class KundaliPdfService {

    private final KundaliGenerationService kundaliGenerationService;
    private final KundaliDisplayService kundaliDisplayService;

    public KundaliPdfService(
            KundaliGenerationService kundaliGenerationService,
            KundaliDisplayService kundaliDisplayService
    ) {
        this.kundaliGenerationService = kundaliGenerationService;
        this.kundaliDisplayService = kundaliDisplayService;
    }

    public byte[] generateReportPdf(Long reportId) {
        KundaliSummaryResponse summary = kundaliGenerationService.findSummaryById(reportId);
        KundaliPlanetsResponse planets = kundaliDisplayService.getPlanets(reportId);
        KundaliDashaResponse dasha = kundaliDisplayService.getDasha(reportId);
        KundaliDoshaResponse dosha = kundaliDisplayService.getDosha(reportId);

        String html = buildHtml(summary, planets, dasha, dosha);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate Kundali PDF", ex);
        }
    }

   /* private String buildHtml(
            KundaliSummaryResponse summary,
            KundaliPlanetsResponse planets,
            KundaliDashaResponse dasha,
            KundaliDoshaResponse dosha
    ) {
        String generatedAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8" />
                    <title>Kundali Report</title>
                    <style>
                        @page {
                            size: A4;
                            margin: 22mm 16mm;
                        }

                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            font-family: Arial, Helvetica, sans-serif;
                            color: #1c160d;
                            background: #ffffff;
                            font-size: 12px;
                            line-height: 1.45;
                        }

                        .header {
                            border-bottom: 3px solid #b77a21;
                            padding-bottom: 14px;
                            margin-bottom: 18px;
                        }

                        .brand {
                            font-size: 12px;
                            color: #8a5a16;
                            font-weight: 700;
                            letter-spacing: 2px;
                            text-transform: uppercase;
                        }

                        h1 {
                            margin: 5px 0 4px;
                            font-size: 30px;
                            color: #1c160d;
                        }

                        .sub {
                            margin: 0;
                            color: #655540;
                        }

                        .meta-row {
                            display: table;
                            width: 100%;
                            margin-top: 14px;
                        }

                        .meta-cell {
                            display: table-cell;
                            width: 50%;
                            vertical-align: top;
                        }

                        .section {
                            margin-top: 18px;
                            page-break-inside: avoid;
                        }

                        h2 {
                            margin: 0 0 10px;
                            padding-bottom: 5px;
                            border-bottom: 1px solid #e1c68a;
                            color: #8a5a16;
                            font-size: 17px;
                        }

                        .grid {
                            display: table;
                            width: 100%;
                            border-collapse: separate;
                            border-spacing: 8px;
                            margin-left: -8px;
                            margin-right: -8px;
                        }

                        .row {
                            display: table-row;
                        }

                        .box {
                            display: table-cell;
                            width: 50%;
                            border: 1px solid #ead8ad;
                            border-radius: 8px;
                            padding: 9px;
                            background: #fffaf0;
                        }

                        .label {
                            display: block;
                            margin-bottom: 4px;
                            color: #856327;
                            font-size: 10px;
                            font-weight: 700;
                            text-transform: uppercase;
                            letter-spacing: 0.6px;
                        }

                        .value {
                            font-size: 13px;
                            font-weight: 700;
                            color: #1d160c;
                        }

                        table {
                            width: 100%;
                            border-collapse: collapse;
                            margin-top: 8px;
                        }

                        th {
                            background: #f2e2bd;
                            color: #5d3b0d;
                            font-size: 10px;
                            text-transform: uppercase;
                            letter-spacing: 0.5px;
                        }

                        th, td {
                            border: 1px solid #ead8ad;
                            padding: 7px;
                            text-align: left;
                            vertical-align: top;
                        }

                        td {
                            font-size: 11px;
                        }

                        .dasha-item {
                            border: 1px solid #ead8ad;
                            border-radius: 8px;
                            padding: 8px;
                            margin-bottom: 7px;
                            background: #fffaf0;
                        }

                        .active {
                            border-color: #b77a21;
                            background: #f7e8c2;
                        }

                        .dosha-card {
                            border: 1px solid #ead8ad;
                            border-radius: 10px;
                            padding: 12px;
                            background: #fffaf0;
                        }

                        .footer {
                            margin-top: 22px;
                            padding-top: 10px;
                            border-top: 1px solid #ead8ad;
                            color: #786955;
                            font-size: 10px;
                        }

                        .page-break {
                            page-break-before: always;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <div class="brand">KKC Astrology</div>
                        <h1>Kundali Report</h1>
                        <p class="sub">Generated from birth details and planetary calculations.</p>

                        <div class="meta-row">
                            <div class="meta-cell">
                                <strong>Name:</strong> %s<br/>
                                <strong>Birth Place:</strong> %s<br/>
                                <strong>Date of Birth:</strong> %s<br/>
                                <strong>Time of Birth:</strong> %s
                            </div>
                            <div class="meta-cell">
                                <strong>Report ID:</strong> %s<br/>
                                <strong>Provider:</strong> %s<br/>
                                <strong>Status:</strong> %s<br/>
                                <strong>Generated At:</strong> %s
                            </div>
                        </div>
                    </div>

                    %s

                    %s

                    %s

                    %s

                    <div class="footer">
                        This report is generated by KKC Astrology for informational and consultation purposes.
                    </div>
                </body>
                </html>
                """.formatted(
                esc(summary.getFullName()),
                esc(summary.getBirthPlace()),
                safe(summary.getDateOfBirth()),
                safe(summary.getTimeOfBirth()),
                safe(summary.getId()),
                esc(summary.getProvider()),
                safe(summary.getStatus()),
                esc(generatedAt),
                buildSummarySection(summary),
                buildPlanetsSection(planets),
                buildDashaSection(dasha),
                buildDoshaSection(dosha)
        );
    }*/

    private String buildHtml(
            KundaliSummaryResponse summary,
            KundaliPlanetsResponse planets,
            KundaliDashaResponse dasha,
            KundaliDoshaResponse dosha
    ) {
        String generatedAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8" />
                <title>Kundali Report</title>
                <style>
                    @page {
                        size: A4;
                        margin: 22mm 16mm;
                    }

                    * {
                        box-sizing: border-box;
                    }

                    body {
                        margin: 0;
                        font-family: Arial, Helvetica, sans-serif;
                        color: #1c160d;
                        background: #ffffff;
                        font-size: 12px;
                        line-height: 1.45;
                    }

                    .header {
                        border-bottom: 3px solid #b77a21;
                        padding-bottom: 14px;
                        margin-bottom: 18px;
                    }

                    .brand {
                        font-size: 12px;
                        color: #8a5a16;
                        font-weight: 700;
                        letter-spacing: 2px;
                        text-transform: uppercase;
                    }

                    h1 {
                        margin: 5px 0 4px;
                        font-size: 30px;
                        color: #1c160d;
                    }

                    .sub {
                        margin: 0;
                        color: #655540;
                    }

                    .meta-row {
                        display: table;
                        width: 100%;
                        margin-top: 14px;
                    }

                    .meta-cell {
                        display: table-cell;
                        width: 50%;
                        vertical-align: top;
                    }

                    .section {
                        margin-top: 18px;
                        page-break-inside: avoid;
                    }

                    h2 {
                        margin: 0 0 10px;
                        padding-bottom: 5px;
                        border-bottom: 1px solid #e1c68a;
                        color: #8a5a16;
                        font-size: 17px;
                    }

                    .grid {
                        display: table;
                        width: 100%;
                        border-collapse: separate;
                        border-spacing: 8px;
                        margin-left: -8px;
                        margin-right: -8px;
                    }

                    .row {
                        display: table-row;
                    }

                    .box {
                        display: table-cell;
                        width: 50%;
                        border: 1px solid #ead8ad;
                        border-radius: 8px;
                        padding: 9px;
                        background: #fffaf0;
                    }

                    .label {
                        display: block;
                        margin-bottom: 4px;
                        color: #856327;
                        font-size: 10px;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 0.6px;
                    }

                    .value {
                        font-size: 13px;
                        font-weight: 700;
                        color: #1d160c;
                    }

                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-top: 8px;
                    }

                    th {
                        background: #f2e2bd;
                        color: #5d3b0d;
                        font-size: 10px;
                        text-transform: uppercase;
                        letter-spacing: 0.5px;
                    }

                    th, td {
                        border: 1px solid #ead8ad;
                        padding: 7px;
                        text-align: left;
                        vertical-align: top;
                    }

                    td {
                        font-size: 11px;
                    }

                    .dasha-item {
                        border: 1px solid #ead8ad;
                        border-radius: 8px;
                        padding: 8px;
                        margin-bottom: 7px;
                        background: #fffaf0;
                    }

                    .active {
                        border-color: #b77a21;
                        background: #f7e8c2;
                    }

                    .dosha-card {
                        border: 1px solid #ead8ad;
                        border-radius: 10px;
                        padding: 12px;
                        background: #fffaf0;
                    }

                    .footer {
                        margin-top: 22px;
                        padding-top: 10px;
                        border-top: 1px solid #ead8ad;
                        color: #786955;
                        font-size: 10px;
                    }

                    .page-break {
                        page-break-before: always;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="brand">KKC Astrology</div>
                    <h1>Kundali Report</h1>
                    <p class="sub">Generated from birth details and planetary calculations.</p>

                    <div class="meta-row">
                        <div class="meta-cell">
                            <strong>Name:</strong> {{FULL_NAME}}<br/>
                            <strong>Birth Place:</strong> {{BIRTH_PLACE}}<br/>
                            <strong>Date of Birth:</strong> {{DATE_OF_BIRTH}}<br/>
                            <strong>Time of Birth:</strong> {{TIME_OF_BIRTH}}
                        </div>
                        <div class="meta-cell">
                            <strong>Report ID:</strong> {{REPORT_ID}}<br/>
                            <strong>Provider:</strong> {{PROVIDER}}<br/>
                            <strong>Status:</strong> {{STATUS}}<br/>
                            <strong>Generated At:</strong> {{GENERATED_AT}}
                        </div>
                    </div>
                </div>

                {{SUMMARY_SECTION}}

                {{PLANETS_SECTION}}

                {{DASHA_SECTION}}

                {{DOSHA_SECTION}}

                <div class="footer">
                    This report is generated by KKC Astrology for informational and consultation purposes.
                </div>
            </body>
            </html>
            """;

        return html
                .replace("{{FULL_NAME}}", esc(summary.getFullName()))
                .replace("{{BIRTH_PLACE}}", esc(summary.getBirthPlace()))
                .replace("{{DATE_OF_BIRTH}}", safe(summary.getDateOfBirth()))
                .replace("{{TIME_OF_BIRTH}}", safe(summary.getTimeOfBirth()))
                .replace("{{REPORT_ID}}", safe(summary.getId()))
                .replace("{{PROVIDER}}", esc(summary.getProvider()))
                .replace("{{STATUS}}", safe(summary.getStatus()))
                .replace("{{GENERATED_AT}}", esc(generatedAt))
                .replace("{{SUMMARY_SECTION}}", buildSummarySection(summary))
                .replace("{{PLANETS_SECTION}}", buildPlanetsSection(planets))
                .replace("{{DASHA_SECTION}}", buildDashaSection(dasha))
                .replace("{{DOSHA_SECTION}}", buildDoshaSection(dosha));
    }

    private String buildSummarySection(KundaliSummaryResponse summary) {
        return """
                <div class="section">
                    <h2>Kundali Summary</h2>

                    <div class="grid">
                        <div class="row">
                            %s
                            %s
                        </div>
                        <div class="row">
                            %s
                            %s
                        </div>
                        <div class="row">
                            %s
                            %s
                        </div>
                        <div class="row">
                            %s
                            %s
                        </div>
                        <div class="row">
                            %s
                            %s
                        </div>
                        <div class="row">
                            %s
                            %s
                        </div>
                    </div>
                </div>
                """.formatted(
                box("Lagna", summary.getAscendant()),
                box("Rashi", summary.getRashi()),
                box("Nakshatra", summary.getNakshatra()),
                box("Pada", summary.getCharan()),
                box("Sign Lord", summary.getSignLord()),
                box("Nakshatra Lord", summary.getNakshatraLord()),
                box("Tithi", summary.getTithi()),
                box("Yoga", summary.getYoga()),
                box("Karan", summary.getKaran()),
                box("Masa", summary.getMasa()),
                box("Sunrise", summary.getSunrise()),
                box("Sunset", summary.getSunset())
        );
    }

    private String buildPlanetsSection(KundaliPlanetsResponse planets) {
        StringBuilder rows = new StringBuilder();

        for (PlanetPositionResponse planet : planets.getPlanets()) {
            rows.append("""
                    <tr>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(
                    esc(planet.getName()),
                    esc(planet.getDegree()),
                    esc(planet.getRashi()),
                    esc(planet.getNakshatra()),
                    safe(planet.getHouse()),
                    yesNo(planet.getRetrograde()),
                    yesNo(planet.getCombust()),
                    esc(planet.getPlanetState())
            ));
        }

        return """
                <div class="section page-break">
                    <h2>Planetary Positions</h2>

                    <table>
                        <thead>
                            <tr>
                                <th>Planet</th>
                                <th>Degree</th>
                                <th>Rashi</th>
                                <th>Nakshatra</th>
                                <th>House</th>
                                <th>Retrograde</th>
                                <th>Combust</th>
                                <th>State</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>
                </div>
                """.formatted(rows);
    }

    private String buildDashaSection(KundaliDashaResponse dasha) {
        StringBuilder items = new StringBuilder();

        for (DashaPeriodResponse period : dasha.getDashaPeriods()) {
            String activeClass = Boolean.TRUE.equals(period.getActive()) ? "dasha-item active" : "dasha-item";

            items.append("""
                    <div class="%s">
                        <strong>%s</strong><br/>
                        %s to %s
                    </div>
                    """.formatted(
                    activeClass,
                    esc(period.getPlanet()),
                    esc(period.getStartDate()),
                    esc(period.getEndDate())
            ));
        }

        String current = dasha.getCurrentDasha() == null
                ? "Not available"
                : esc(dasha.getCurrentDasha().getPlanet()) + " - "
                + esc(dasha.getCurrentDasha().getStartDate()) + " to "
                + esc(dasha.getCurrentDasha().getEndDate());

        return """
                <div class="section">
                    <h2>Vimshottari Mahadasha</h2>
                    <div class="dosha-card">
                        <span class="label">Current Dasha</span>
                        <span class="value">%s</span>
                    </div>
                    <br/>
                    %s
                </div>
                """.formatted(current, items);
    }

    private String buildDoshaSection(KundaliDoshaResponse dosha) {
        String present = Boolean.TRUE.equals(dosha.getMangalDoshaPresent())
                ? "Present"
                : "Not Present";

        return """
                <div class="section">
                    <h2>Mangal Dosha Analysis</h2>

                    <div class="grid">
                        <div class="row">
                            %s
                            %s
                        </div>
                        <div class="row">
                            %s
                            %s
                        </div>
                    </div>

                    <div class="dosha-card">
                        <strong>Reason</strong><br/>
                        %s
                        <br/><br/>
                        <strong>Information</strong><br/>
                        %s
                    </div>
                </div>
                """.formatted(
                box("Mangal Dosha", present),
                box("Type", dosha.getType()),
                box("Intensity", dosha.getIntensity()),
                box("Section Status", dosha.getStatus()),
                esc(dosha.getReason()),
                esc(dosha.getInfo())
        );
    }

    private String box(String label, Object value) {
        return """
                <div class="box">
                    <span class="label">%s</span>
                    <span class="value">%s</span>
                </div>
                """.formatted(esc(label), esc(value));
    }

    private String yesNo(Boolean value) {
        if (value == null) {
            return "-";
        }

        return value ? "Yes" : "No";
    }

    private String safe(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String esc(Object value) {
        if (value == null) {
            return "-";
        }

        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}