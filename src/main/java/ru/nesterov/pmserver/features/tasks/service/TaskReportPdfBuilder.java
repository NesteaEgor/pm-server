package ru.nesterov.pmserver.features.tasks.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ru.nesterov.pmserver.features.tasks.dto.TaskReportRowDto;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TaskReportPdfBuilder {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    .withZone(ZoneId.systemDefault());

    public static byte[] build(
            String projectName,
            List<TaskReportRowDto> rows,
            int total,
            int todo,
            int inProgress,
            int done
    ) {
        try {
            Document doc = new Document(PageSize.A4.rotate(), 24, 24, 18, 18);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(doc, out);

            doc.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font subFont = new Font(Font.HELVETICA, 12);
            Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font cellFont = new Font(Font.HELVETICA, 10);

            Font redFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(180, 0, 0));
            Font orangeFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(255, 140, 0));

            doc.add(new Paragraph("Отчёт по задачам проекта", titleFont));
            doc.add(new Paragraph("Проект: " + projectName, subFont));
            doc.add(new Paragraph(
                    String.format("Всего: %d   TODO: %d   IN_PROGRESS: %d   DONE: %d",
                            total, todo, inProgress, done),
                    subFont
            ));
            doc.add(Chunk.NEWLINE);

            // 6 колонок: задача, статус, дедлайн, дней, постановщик, исполнитель
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3.6f, 1.5f, 1.6f, 1.0f, 2.2f, 2.2f});
            table.setHeaderRows(1);

            addHeader(table, headerFont,
                    "Задача",
                    "Статус",
                    "Дедлайн",
                    "Дней",
                    "Постановщик",
                    "Исполнитель"
            );

            for (TaskReportRowDto r : rows) {
                table.addCell(cell(r.getTitle(), cellFont));
                table.addCell(cell(r.getStatus(), cellFont));

                String dl = (r.getDeadline() == null) ? "-" : DATE_FMT.format(r.getDeadline());
                table.addCell(centerCell(dl, cellFont));

                PdfPCell daysCell;
                if (r.getDaysToDeadline() == null) {
                    daysCell = centerCell("-", cellFont);
                } else {
                    long days = r.getDaysToDeadline();
                    Font f = cellFont;
                    if (days < 0) f = redFont;
                    else if (days <= 3) f = orangeFont;
                    daysCell = centerCell(Long.toString(days), f);
                }
                table.addCell(daysCell);

                table.addCell(cell(r.getCreatorName(), cellFont));
                table.addCell(cell(r.getAssigneeName(), cellFont));
            }

            doc.add(table);
            doc.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private static void addHeader(PdfPTable table, Font font, String... titles) {
        for (String t : titles) {
            PdfPCell cell = new PdfPCell(new Phrase(t, font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(new Color(235, 235, 235));
            cell.setPaddingTop(8);
            cell.setPaddingBottom(8);
            table.addCell(cell);
        }
    }

    private static PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
        c.setVerticalAlignment(Element.ALIGN_TOP);
        c.setPaddingTop(6);
        c.setPaddingBottom(6);
        c.setPaddingLeft(8);
        c.setPaddingRight(8);
        return c;
    }

    private static PdfPCell centerCell(String text, Font font) {
        PdfPCell c = cell(text, font);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }
}
