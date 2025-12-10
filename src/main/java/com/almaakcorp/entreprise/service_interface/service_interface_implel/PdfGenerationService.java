package com.almaakcorp.entreprise.service_interface.service_interface_implel;

import com.almaakcorp.entreprise.models.QuotationItem;
import com.almaakcorp.entreprise.models.Quotations;
import com.almaakcorp.entreprise.controllers.QuotationResources;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PdfGenerationService {

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(26, 95, 122);
    private static final DeviceRgb TEXT_COLOR = new DeviceRgb(55, 65, 81);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(244, 244, 245);
    private static final DeviceRgb ROW_SEPARATOR = new DeviceRgb(230, 230, 230);

    /**
     * Generate a professional quotation PDF
     * @param quotation The quotation data
     * @return PDF as byte array
     */
    public byte[] generateQuotationPdf(Quotations quotation) throws IOException {
        return generateQuotationPdf(quotation, null);
    }

    /**
     * Generate a professional quotation PDF with column options
     *
     * @param quotation The quotation data
     * @param columnOptions Column options for PDF generation
     * @return PDF as byte array
     */
    public byte[] generateQuotationPdf(Quotations quotation, QuotationResources.ColumnOptions columnOptions) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(36, 36, 36, 36); // 0.5 inch margins

        try {
            // Load fonts
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Add header
            addHeader(document, quotation, boldFont, regularFont);

            // Add company and customer info
            addCompanyAndCustomerInfo(document, quotation, boldFont, regularFont);

            // Add reference and attention
            addReferenceAndAttention(document, quotation, boldFont, regularFont);

            // Add items table
            addItemsTable(document, quotation, boldFont, regularFont, columnOptions);

            // Add totals
            addTotals(document, quotation, boldFont, regularFont);

            // Add notes (if any)
            addNotes(document, quotation, boldFont, regularFont);

            // Add terms and conditions (if any)
            addTermsAndConditions(document, quotation, boldFont, regularFont);

            // Add signature section
            addSignatureSection(document, boldFont, regularFont, columnOptions);

            // Add footer
            addFooter(document, regularFont);

        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage(), e);
            throw new IOException("Failed to generate PDF", e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private void addHeader(Document document, Quotations quotation, PdfFont boldFont, PdfFont regularFont) throws IOException {
        // Header: logo left, quote meta right
        Table headerTable = new Table(new float[]{1.5f, 1f}).useAllAvailableWidth();
        headerTable.setMarginBottom(8);

        // Left: logo or fallback text
        Cell leftCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0).setPaddingLeft(0);
        try {
            ClassPathResource logoResource = new ClassPathResource("public/logo.jpeg");
            if (logoResource.exists()) {
                Image logo = new Image(ImageDataFactory.create(logoResource.getURL()));
                logo.setAutoScale(true);
                // keep it compact but prominent
                logo.setMaxWidth(140);
                logo.setMaxHeight(50);
                leftCell.add(logo);
            } else {
                leftCell.add(new Paragraph("ALMAAKCORP").setFont(boldFont).setFontSize(16).setFontColor(HEADER_COLOR));
            }
        } catch (Exception e) {
            log.warn("Could not load logo: {}", e.getMessage());
            leftCell.add(new Paragraph("ALMAAKCORP").setFont(boldFont).setFontSize(16).setFontColor(HEADER_COLOR));
        }

        // Right: quotation metadata aligned right
        Cell rightCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        String quoteId = quotation.getQuotationId() != null ? quotation.getQuotationId() : "NEW QUOTE";
        rightCell.add(new Paragraph("Quotation #: " + quoteId).setFont(regularFont).setFontSize(10).setFontColor(TEXT_COLOR));
        rightCell.add(new Paragraph("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).setFont(regularFont).setFontSize(10).setFontColor(TEXT_COLOR));
        if (quotation.getValidUntil() != null) {
            rightCell.add(new Paragraph("Valid until: " + quotation.getValidUntil().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(boldFont).setFontSize(10).setFontColor(HEADER_COLOR));
        }

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);

        // Title with separator
        Paragraph title = new Paragraph("QUOTATION")
                .setFont(boldFont)
                .setFontSize(20)
                .setFontColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setCharacterSpacing(1.2f)
                .setMarginTop(6)
                .setMarginBottom(6);
        document.add(title);

        LineSeparator line = new LineSeparator(new SolidLine(1f));
        line.setStrokeColor(HEADER_COLOR);
        document.add(line);

        document.add(new Paragraph("\n"));
    }

    private void addCompanyAndCustomerInfo(Document document, Quotations quotation, PdfFont boldFont, PdfFont regularFont) {
        Table infoTable = new Table(new float[]{1f, 1f}).useAllAvailableWidth();
        infoTable.setMarginTop(8);
        infoTable.setMarginBottom(10);

        // Company info block (left) with light shaded background
        Cell companyCell = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(new DeviceRgb(250, 251, 252))
                .setPadding(8);
        companyCell.add(new Paragraph("Almaakcorp sarl").setFont(boldFont).setFontSize(12).setFontColor(HEADER_COLOR));
        companyCell.add(new Paragraph("ADDRESS: TERRITOIRE DE WATSA, DURBA/ DUEMBE").setFont(regularFont).setFontSize(9).setFontColor(TEXT_COLOR));
        companyCell.add(new Paragraph("GALLERIE MAHANAIM, ROOM 07, ID NAT: 19-F4300-N58465L").setFont(regularFont).setFontSize(9).setFontColor(TEXT_COLOR));
        companyCell.add(new Paragraph("N° IMPOT: A2408855C CNSS: 1020017400, ARSP: 4151855306").setFont(regularFont).setFontSize(9).setFontColor(TEXT_COLOR));
        companyCell.add(new Paragraph("RCCM: CD/GOM/RCCM/24-B-01525, VENDOR: 1075430").setFont(regularFont).setFontSize(9).setFontColor(TEXT_COLOR));
        companyCell.add(new Paragraph("Website: www.almaakcorp.com | Email: wilsonmuhasa@almaakcorp.com").setFont(regularFont).setFontSize(9).setFontColor(TEXT_COLOR));
        companyCell.add(new Paragraph("Tel: +243 816 833 285").setFont(regularFont).setFontSize(9).setFontColor(TEXT_COLOR));

        // Customer info block (right) with left border accent
        Cell customerCell = new Cell().setBorder(Border.NO_BORDER)
                .setPadding(8)
                .setPaddingLeft(12)
                .setBorderLeft(new SolidBorder(HEADER_COLOR, 2));
        customerCell.add(new Paragraph("To:").setFont(boldFont).setFontSize(12).setFontColor(HEADER_COLOR));
        customerCell.add(new Paragraph(quotation.getCustomerName() != null ? quotation.getCustomerName() : "N/A").setFont(regularFont).setFontSize(10).setFontColor(TEXT_COLOR));
        if (quotation.getCustomerAddress() != null) {
            customerCell.add(new Paragraph(quotation.getCustomerAddress()).setFont(regularFont).setFontSize(10).setFontColor(TEXT_COLOR));
        }
        if (quotation.getCustomerEmail() != null) {
            customerCell.add(new Paragraph(quotation.getCustomerEmail()).setFont(regularFont).setFontSize(10).setFontColor(TEXT_COLOR));
        }

        infoTable.addCell(companyCell);
        infoTable.addCell(customerCell);
        document.add(infoTable);
    }

    private void addReferenceAndAttention(Document document, Quotations quotation, PdfFont boldFont, PdfFont regularFont) {
        Table refTable = new Table(new float[]{1f, 1f}).useAllAvailableWidth();
        refTable.setBorder(Border.NO_BORDER);
        refTable.setBackgroundColor(new DeviceRgb(250, 250, 250));
        refTable.setPadding(6);
        refTable.setMarginBottom(8);

        Cell refCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        refCell.add(new Paragraph("Reference:").setFont(boldFont).setFontSize(10).setFontColor(HEADER_COLOR));
        refCell.add(new Paragraph(quotation.getReference() != null ? quotation.getReference() : "N/A").setFont(regularFont).setFontSize(10).setFontColor(TEXT_COLOR));

        Cell attCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
        attCell.add(new Paragraph("Attention:").setFont(boldFont).setFontSize(10).setFontColor(HEADER_COLOR));
        attCell.add(new Paragraph(quotation.getAttention() != null ? quotation.getAttention() : "N/A").setFont(regularFont).setFontSize(10).setFontColor(TEXT_COLOR));

        refTable.addCell(refCell);
        refTable.addCell(attCell);
        document.add(refTable);
        document.add(new Paragraph("\n"));
    }

    private void addItemsTable(Document document, Quotations quotation, PdfFont boldFont, PdfFont regularFont, QuotationResources.ColumnOptions columnOptions) {
        // Determine which columns to include
        boolean includeDescription = columnOptions == null || columnOptions.isIncludeDescription();
        boolean includePartNumber = columnOptions == null || columnOptions.isIncludePartNumber();
        boolean includeManufacturer = columnOptions == null || columnOptions.isIncludeManufacturer();

        // Build column widths list
        List<Float> columnWidths = new ArrayList<>();
        columnWidths.add(0.7f); // No.
        columnWidths.add(2.2f); // Item name
        if (includeDescription) columnWidths.add(3f);
        if (includePartNumber) columnWidths.add(1.2f);
        if (includeManufacturer) columnWidths.add(1.2f);
        columnWidths.add(0.8f); // Qty
        columnWidths.add(1.5f); // Unit Price
        columnWidths.add(1.5f); // Total

        float[] widths = new float[columnWidths.size()];
        for (int i = 0; i < columnWidths.size(); i++) widths[i] = columnWidths.get(i);

        Table table = new Table(widths).useAllAvailableWidth();
        table.setMarginTop(6);
        table.setMarginBottom(6);

        // Header cells with consistent look
        table.addHeaderCell(new Cell().add(new Paragraph("No.").setFont(boldFont).setFontSize(9).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(HEADER_COLOR).setTextAlignment(TextAlignment.CENTER).setPadding(6));
        table.addHeaderCell(new Cell().add(new Paragraph("Item").setFont(boldFont).setFontSize(9).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(HEADER_COLOR).setPadding(6));

        if (includeDescription) {
            table.addHeaderCell(new Cell().add(new Paragraph("Description").setFont(boldFont).setFontSize(9).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(HEADER_COLOR).setPadding(6));
        }
        if (includePartNumber) {
            table.addHeaderCell(new Cell().add(new Paragraph("Part N.").setFont(boldFont).setFontSize(9).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(HEADER_COLOR).setPadding(6));
        }
        if (includeManufacturer) {
            table.addHeaderCell(new Cell().add(new Paragraph("Manufacturer").setFont(boldFont).setFontSize(9).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(HEADER_COLOR).setPadding(6));
        }

        table.addHeaderCell(new Cell().add(new Paragraph("Qty").setFont(boldFont).setFontSize(9).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(HEADER_COLOR).setTextAlignment(TextAlignment.CENTER).setPadding(6));
        table.addHeaderCell(new Cell().add(new Paragraph("Unit Price").setFont(boldFont).setFontSize(9).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(HEADER_COLOR).setTextAlignment(TextAlignment.RIGHT).setPadding(6));
        table.addHeaderCell(new Cell().add(new Paragraph("Total").setFont(boldFont).setFontSize(9).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(HEADER_COLOR).setTextAlignment(TextAlignment.RIGHT).setPadding(6));

        // Data rows
        List<QuotationItem> items = quotation.getQuotationItems();
        if (items == null) items = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            QuotationItem item = items.get(i);
            boolean isEvenRow = i % 2 == 0;
            DeviceRgb rowBg = isEvenRow ? new DeviceRgb(255, 255, 255) : LIGHT_GRAY;

            // No.
            table.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1)).setFont(regularFont).setFontSize(8))
                    .setBackgroundColor(rowBg).setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(new SolidBorder(ROW_SEPARATOR, 0.5f)).setPadding(6));

            // Item name
            String itemName = item.getProduct() != null ? item.getProduct().getProductName() : "N/A";
            table.addCell(new Cell().add(new Paragraph(itemName).setFont(regularFont).setFontSize(8))
                    .setBackgroundColor(rowBg).setBorderBottom(new SolidBorder(ROW_SEPARATOR, 0.5f)).setPadding(6));

            // Description
            if (includeDescription) {
                String description = (item.getProduct() != null && item.getProduct().getProductDescription() != null)
                        ? item.getProduct().getProductDescription()
                        : "N/A";
                table.addCell(new Cell().add(new Paragraph(description).setFont(regularFont).setFontSize(8))
                        .setBackgroundColor(rowBg).setBorderBottom(new SolidBorder(ROW_SEPARATOR, 0.5f)).setPadding(6));
            }

            // Part number
            if (includePartNumber) {
                String partNumber = (item.getProduct() != null && item.getProduct().getProductPartNumber() != null)
                        ? item.getProduct().getProductPartNumber()
                        : "N/A";
                table.addCell(new Cell().add(new Paragraph(partNumber).setFont(regularFont).setFontSize(8))
                        .setBackgroundColor(rowBg).setBorderBottom(new SolidBorder(ROW_SEPARATOR, 0.5f)).setPadding(6));
            }

            // Manufacturer
            if (includeManufacturer) {
                String manufacturer = (item.getProduct() != null && item.getProduct().getProductManufacturer() != null)
                        ? item.getProduct().getProductManufacturer()
                        : "N/A";
                table.addCell(new Cell().add(new Paragraph(manufacturer).setFont(regularFont).setFontSize(8))
                        .setBackgroundColor(rowBg).setBorderBottom(new SolidBorder(ROW_SEPARATOR, 0.5f)).setPadding(6));
            }

            // Qty
            table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity())).setFont(regularFont).setFontSize(8))
                    .setBackgroundColor(rowBg).setTextAlignment(TextAlignment.CENTER)
                    .setBorderBottom(new SolidBorder(ROW_SEPARATOR, 0.5f)).setPadding(6));

            // Unit price (with thousands separator)
            double unitPriceDouble = item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : 0.0;
            table.addCell(new Cell().add(new Paragraph(String.format("$%,.2f", unitPriceDouble)).setFont(regularFont).setFontSize(8))
                    .setBackgroundColor(rowBg).setTextAlignment(TextAlignment.RIGHT)
                    .setBorderBottom(new SolidBorder(ROW_SEPARATOR, 0.5f)).setPadding(6));

            // Total (qty * unit)
            double total = unitPriceDouble * (item.getQuantity());
            table.addCell(new Cell().add(new Paragraph(String.format("$%,.2f", total)).setFont(regularFont).setFontSize(8))
                    .setBackgroundColor(rowBg).setTextAlignment(TextAlignment.RIGHT)
                    .setBorderBottom(new SolidBorder(ROW_SEPARATOR, 0.5f)).setPadding(6));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addTotals(Document document, Quotations quotation, PdfFont boldFont, PdfFont regularFont) {
        // Two-column layout: left empty / notes space, right totals box
        Table outer = new Table(new float[]{3f, 2f}).useAllAvailableWidth();
        outer.setMarginTop(6);
        outer.setMarginBottom(8);

        // Left: spacer
        outer.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph("")).setPadding(0));

        // Right: totals box
        Table totals = new Table(new float[]{2f, 1f}).useAllAvailableWidth();
        totals.setBorder(new SolidBorder(HEADER_COLOR, 1));
        totals.setPadding(0);

        // Header row
        totals.addCell(new Cell(1, 2)
                .add(new Paragraph("QUOTATION SUMMARY").setFont(boldFont).setFontSize(10).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6));

        // Total Quantity
        int totalQuantity = quotation.getQuotationItems() != null
                ? quotation.getQuotationItems().stream().mapToInt(item -> Math.toIntExact(item.getQuantity())).sum()
                : 0;
        totals.addCell(new Cell().add(new Paragraph("Total Quantity:").setFont(regularFont).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6));
        totals.addCell(new Cell().add(new Paragraph(String.valueOf(totalQuantity)).setFont(regularFont).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6).setTextAlignment(TextAlignment.RIGHT));

        // ETA
        String eta = quotation.getEta() != null && !quotation.getEta().trim().isEmpty()
                ? quotation.getEta()
                : "1 week (business days)";
        totals.addCell(new Cell().add(new Paragraph("ETA:").setFont(regularFont).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6));
        totals.addCell(new Cell().add(new Paragraph(eta).setFont(regularFont).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6).setTextAlignment(TextAlignment.RIGHT));

        // Subtotal
        double subtotal = quotation.getSubtotal() != null ? quotation.getSubtotal() : 0.0;
        totals.addCell(new Cell().add(new Paragraph("Subtotal:").setFont(regularFont).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6));
        totals.addCell(new Cell().add(new Paragraph(String.format("$%,.2f", subtotal)).setFont(regularFont).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6).setTextAlignment(TextAlignment.RIGHT));

        // Discount if present
        if (quotation.getDiscount() != null && quotation.getDiscount() > 0) {
            totals.addCell(new Cell().add(new Paragraph("Discount:").setFont(regularFont).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6));
            totals.addCell(new Cell().add(new Paragraph(String.format("-$%,.2f", quotation.getDiscount())).setFont(regularFont).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6).setTextAlignment(TextAlignment.RIGHT));
        }

        // Tax (rate or amount)
        if (quotation.getTaxRate() != null && quotation.getTaxRate() > 0) {
            totals.addCell(new Cell().add(new Paragraph(String.format("Tax (%.1f%%):", quotation.getTaxRate())).setFont(regularFont).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6));
            totals.addCell(new Cell().add(new Paragraph(String.format("$%,.2f", quotation.getTax() != null ? quotation.getTax() : 0.0)).setFont(regularFont).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6).setTextAlignment(TextAlignment.RIGHT));
        } else if (quotation.getTax() != null && quotation.getTax() > 0) {
            totals.addCell(new Cell().add(new Paragraph("Tax:").setFont(regularFont).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6));
            totals.addCell(new Cell().add(new Paragraph(String.format("$%,.2f", quotation.getTax())).setFont(regularFont).setFontSize(9)).setBorder(Border.NO_BORDER).setPadding(6).setTextAlignment(TextAlignment.RIGHT));
        }

        // Separator
        totals.addCell(new Cell(1, 2).add(new Paragraph("")).setBorder(new SolidBorder(LIGHT_GRAY, 0.8f)).setPadding(2));

        // Total emphasized
        totals.addCell(new Cell().add(new Paragraph("TOTAL:").setFont(boldFont).setFontSize(11).setFontColor(HEADER_COLOR)).setBorder(Border.NO_BORDER).setBackgroundColor(LIGHT_GRAY).setPadding(8));
        totals.addCell(new Cell().add(new Paragraph(String.format("$%,.2f", quotation.getTotalAmount() != null ? quotation.getTotalAmount() : 0.0)).setFont(boldFont).setFontSize(11)).setBorder(Border.NO_BORDER).setBackgroundColor(LIGHT_GRAY).setTextAlignment(TextAlignment.RIGHT).setPadding(8));

        outer.addCell(new Cell().setBorder(Border.NO_BORDER).add(totals).setPadding(0));
        document.add(outer);
        document.add(new Paragraph("\n"));
    }

    private void addTermsAndConditions(Document document, Quotations quotation, PdfFont boldFont, PdfFont regularFont) {
        if (quotation.getTerms() != null && !quotation.getTerms().trim().isEmpty()) {
            document.add(new Paragraph("Terms and Conditions:").setFont(boldFont).setFontSize(12).setFontColor(HEADER_COLOR));
            document.add(new Paragraph(quotation.getTerms()).setFont(regularFont).setFontSize(10).setFontColor(TEXT_COLOR).setMarginTop(4));
            document.add(new Paragraph("\n"));
        }
    }

    private void addNotes(Document document, Quotations quotation, PdfFont boldFont, PdfFont regularFont) {
        if (quotation.getNotes() != null && !quotation.getNotes().trim().isEmpty()) {
            document.add(new Paragraph("Notes:").setFont(boldFont).setFontSize(12).setFontColor(HEADER_COLOR));
            document.add(new Paragraph(quotation.getNotes()).setFont(regularFont).setFontSize(10).setFontColor(TEXT_COLOR).setMarginTop(4));
            document.add(new Paragraph("\n"));
        }
    }

    private void addSignatureSection(Document document, PdfFont boldFont, PdfFont regularFont, QuotationResources.ColumnOptions columnOptions) throws IOException {
        boolean includeManagerStamp = columnOptions == null || columnOptions.isIncludeManagerStamp();
        boolean includeCompanyStamp = columnOptions == null || columnOptions.isIncludeCompanyStamp();

        if (!includeManagerStamp && !includeCompanyStamp) {
            return;
        }

        // subtle separator line
        LineSeparator sep = new LineSeparator(new SolidLine(0.5f));
        sep.setStrokeColor(ROW_SEPARATOR);
        document.add(sep);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Authorized Signature:").setFont(boldFont).setFontSize(12).setFontColor(HEADER_COLOR));
        document.add(new Paragraph("\n"));

        // Create signature table with dynamic columns
        List<Float> widths = new ArrayList<>();
        if (includeManagerStamp) widths.add(1f);
        if (includeCompanyStamp) widths.add(1f);
        widths.add(2f); // spacer

        float[] w = new float[widths.size()];
        for (int i = 0; i < widths.size(); i++) w[i] = widths.get(i);

        Table sigTable = new Table(w).useAllAvailableWidth();
        sigTable.setMarginTop(6);

        if (includeManagerStamp) {
            Cell managerCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
            try {
                ClassPathResource managerStampResource = new ClassPathResource("public/managerStamp.png");
                if (managerStampResource.exists()) {
                    Image managerStamp = new Image(ImageDataFactory.create(managerStampResource.getURL()));
                    managerStamp.setMaxWidth(100);
                    managerStamp.setMaxHeight(60);
                    managerCell.add(managerStamp);
                } else {
                    managerCell.add(new Paragraph("Manager Stamp").setFont(regularFont).setFontSize(9).setFontColor(TEXT_COLOR));
                }
            } catch (Exception e) {
                log.warn("Could not load manager stamp: {}", e.getMessage());
                managerCell.add(new Paragraph("Manager Stamp").setFont(regularFont).setFontSize(9).setFontColor(TEXT_COLOR));
            }
            managerCell.add(new Paragraph("Manager").setFont(regularFont).setFontSize(9).setTextAlignment(TextAlignment.CENTER).setMarginTop(4));
            sigTable.addCell(managerCell);
        }

        if (includeCompanyStamp) {
            Cell compCell = new Cell().setBorder(Border.NO_BORDER).setPadding(6);
            try {
                ClassPathResource stampResource = new ClassPathResource("public/stamp.png");
                if (stampResource.exists()) {
                    Image stamp = new Image(ImageDataFactory.create(stampResource.getURL()));
                    stamp.setMaxWidth(100);
                    stamp.setMaxHeight(60);
                    compCell.add(stamp);
                } else {
                    compCell.add(new Paragraph("Company Stamp").setFont(regularFont).setFontSize(9).setFontColor(TEXT_COLOR));
                }
            } catch (Exception e) {
                log.warn("Could not load company stamp: {}", e.getMessage());
                compCell.add(new Paragraph("Company Stamp").setFont(regularFont).setFontSize(9).setFontColor(TEXT_COLOR));
            }
            compCell.add(new Paragraph("Company Seal").setFont(regularFont).setFontSize(9).setTextAlignment(TextAlignment.CENTER).setMarginTop(4));
            sigTable.addCell(compCell);
        }

        // spacing cell
        sigTable.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph("")).setPadding(6));

        document.add(sigTable);
        document.add(new Paragraph("\n"));
    }

    private void addFooter(Document document, PdfFont regularFont) {
        // Footer as shaded bar
        Paragraph bank = new Paragraph("Bank account details: Bank name: Equity BCDC | Bank Account name: Almaak Corporation Sarl | bank account number: 288200123855435 USD")
                .setFont(regularFont).setFontSize(9).setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6);
        // Add a full-width table to simulate a shaded footer bar (to control padding)
        Table footerTable = new Table(new float[]{1f}).useAllAvailableWidth();
        Cell footerCell = new Cell().setBorder(Border.NO_BORDER).setBackgroundColor(HEADER_COLOR).add(bank);
        footerTable.addCell(footerCell);
        document.add(footerTable);

        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Generated on " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFont(regularFont).setFontSize(9).setFontColor(TEXT_COLOR).setTextAlignment(TextAlignment.RIGHT));
    }
}
