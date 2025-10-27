package dev.kreaker.kinvex.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import dev.kreaker.kinvex.dto.report.InventoryMovementReportDto;
import dev.kreaker.kinvex.dto.report.ReportExportRequest;
import dev.kreaker.kinvex.dto.report.StockLevelReportDto;
import dev.kreaker.kinvex.dto.report.SupplierPerformanceReportDto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for exporting reports to PDF and Excel formats Requirement 4.5: Export reports in PDF and
 * Excel formats
 */
@Service
public class ReportExportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FILE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    private final ReportService reportService;

    public ReportExportService(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Export report based on request parameters Requirement 4.5: Export reports in PDF and Excel
     * formats
     */
    public byte[] exportReport(ReportExportRequest request) throws IOException {
        logger.info("Exporting report: {}", request);

        switch (request.getFormat()) {
            case PDF:
                return exportToPdf(request);
            case EXCEL:
                return exportToExcel(request);
            default:
                throw new IllegalArgumentException(
                        "Unsupported export format: " + request.getFormat());
        }
    }

    /** Export report to PDF format using iText */
    private byte[] exportToPdf(ReportExportRequest request) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // Add title
            String title =
                    request.getTitle() != null
                            ? request.getTitle()
                            : getDefaultTitle(request.getReportType());
            document.add(
                    new Paragraph(title)
                            .setFontSize(18)
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginBottom(20));

            // Add generation info
            document.add(
                    new Paragraph("Generado el: " + LocalDateTime.now().format(DATE_FORMATTER))
                            .setFontSize(10)
                            .setTextAlignment(TextAlignment.RIGHT)
                            .setMarginBottom(10));

            if (request.getStartDate() != null && request.getEndDate() != null) {
                document.add(
                        new Paragraph(
                                        "Período: "
                                                + request.getStartDate().format(DATE_FORMATTER)
                                                + " - "
                                                + request.getEndDate().format(DATE_FORMATTER))
                                .setFontSize(10)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setMarginBottom(20));
            }

            // Add report content based on type
            switch (request.getReportType()) {
                case INVENTORY_MOVEMENTS:
                    addInventoryMovementsToPdf(document, request);
                    break;
                case STOCK_LEVELS:
                    addStockLevelsToPdf(document, request);
                    break;
                case SUPPLIER_PERFORMANCE:
                    addSupplierPerformanceToPdf(document, request);
                    break;
            }

        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    /** Export report to Excel format using Apache POI */
    private byte[] exportToExcel(ReportExportRequest request) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            String sheetName = getSheetName(request.getReportType());
            Sheet sheet = workbook.createSheet(sheetName);

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Add report content based on type
            switch (request.getReportType()) {
                case INVENTORY_MOVEMENTS:
                    addInventoryMovementsToExcel(sheet, headerStyle, dataStyle, request);
                    break;
                case STOCK_LEVELS:
                    addStockLevelsToExcel(sheet, headerStyle, dataStyle, request);
                    break;
                case SUPPLIER_PERFORMANCE:
                    addSupplierPerformanceToExcel(sheet, headerStyle, dataStyle, request);
                    break;
            }

            // Auto-size columns
            for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
        } finally {
            workbook.close();
        }

        return baos.toByteArray();
    }

    // PDF helper methods
    private void addInventoryMovementsToPdf(Document document, ReportExportRequest request) {
        List<InventoryMovementReportDto> movements;
        if (Boolean.TRUE.equals(request.getDetailed())) {
            movements = reportService.getDetailedInventoryMovementReport(request.toReportFilter());
        } else {
            movements = reportService.getInventoryMovementReport(request.toReportFilter());
        }

        if (movements.isEmpty()) {
            document.add(
                    new Paragraph("No se encontraron movimientos para el período especificado.")
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginTop(20));
            return;
        }

        // Create table
        float[] columnWidths =
                Boolean.TRUE.equals(request.getDetailed())
                        ? new float[] {1, 2, 2, 1, 1, 2, 1, 2, 2}
                        : new float[] {1, 2, 2, 1, 1, 2};

        Table table =
                new Table(UnitValue.createPercentArray(columnWidths))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setMarginTop(10);

        // Add headers
        if (Boolean.TRUE.equals(request.getDetailed())) {
            table.addHeaderCell(createHeaderCell("ID"));
            table.addHeaderCell(createHeaderCell("Código"));
            table.addHeaderCell(createHeaderCell("Producto"));
            table.addHeaderCell(createHeaderCell("Tipo"));
            table.addHeaderCell(createHeaderCell("Cantidad"));
            table.addHeaderCell(createHeaderCell("Referencia"));
            table.addHeaderCell(createHeaderCell("Sistema"));
            table.addHeaderCell(createHeaderCell("Usuario"));
            table.addHeaderCell(createHeaderCell("Fecha"));
        } else {
            table.addHeaderCell(createHeaderCell("ID"));
            table.addHeaderCell(createHeaderCell("Código"));
            table.addHeaderCell(createHeaderCell("Producto"));
            table.addHeaderCell(createHeaderCell("Tipo"));
            table.addHeaderCell(createHeaderCell("Cantidad"));
            table.addHeaderCell(createHeaderCell("Fecha"));
        }

        // Add data rows
        for (InventoryMovementReportDto movement : movements) {
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(movement.getMovementId().toString())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(movement.getProductCode())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(movement.getProductName())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(movement.getMovementType().toString())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(movement.getQuantity().toString())));

            if (Boolean.TRUE.equals(request.getDetailed())) {
                table.addCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(
                                        new Paragraph(
                                                movement.getReferenceType() != null
                                                        ? movement.getReferenceType().toString()
                                                        : "")));
                table.addCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(
                                        new Paragraph(
                                                movement.getSourceSystem() != null
                                                        ? movement.getSourceSystem()
                                                        : "")));
                table.addCell(
                        new com.itextpdf.layout.element.Cell()
                                .add(
                                        new Paragraph(
                                                movement.getCreatedByUsername() != null
                                                        ? movement.getCreatedByUsername()
                                                        : "")));
            }

            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(movement.getCreatedAt().format(DATE_FORMATTER))));
        }

        document.add(table);
    }

    private void addStockLevelsToPdf(Document document, ReportExportRequest request) {
        List<StockLevelReportDto> stockLevels =
                reportService.getStockLevelReport(request.toReportFilter());

        if (stockLevels.isEmpty()) {
            document.add(
                    new Paragraph("No se encontraron productos para los filtros especificados.")
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginTop(20));
            return;
        }

        // Create table
        float[] columnWidths = {1, 2, 2, 1, 1, 1, 1, 2};
        Table table =
                new Table(UnitValue.createPercentArray(columnWidths))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setMarginTop(10);

        // Add headers
        table.addHeaderCell(createHeaderCell("Código"));
        table.addHeaderCell(createHeaderCell("Producto"));
        table.addHeaderCell(createHeaderCell("Categoría"));
        table.addHeaderCell(createHeaderCell("Stock Actual"));
        table.addHeaderCell(createHeaderCell("Stock Mín."));
        table.addHeaderCell(createHeaderCell("Stock Máx."));
        table.addHeaderCell(createHeaderCell("Precio"));
        table.addHeaderCell(createHeaderCell("Último Movimiento"));

        // Add data rows
        for (StockLevelReportDto stock : stockLevels) {
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(stock.getProductCode())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(stock.getProductName())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(
                                    new Paragraph(
                                            stock.getCategoryName() != null
                                                    ? stock.getCategoryName()
                                                    : "")));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(stock.getCurrentStock().toString())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(stock.getMinStock().toString())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(
                                    new Paragraph(
                                            stock.getMaxStock() != null
                                                    ? stock.getMaxStock().toString()
                                                    : "")));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph("$" + stock.getUnitPrice().toString())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(
                                    new Paragraph(
                                            stock.getLastMovementDate() != null
                                                    ? stock.getLastMovementDate()
                                                            .format(DATE_FORMATTER)
                                                    : "")));
        }

        document.add(table);
    }

    private void addSupplierPerformanceToPdf(Document document, ReportExportRequest request) {
        List<SupplierPerformanceReportDto> suppliers =
                reportService.getSupplierPerformanceReport(request.toReportFilter());

        if (suppliers.isEmpty()) {
            document.add(
                    new Paragraph("No se encontraron proveedores para el período especificado.")
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginTop(20));
            return;
        }

        // Create table
        float[] columnWidths = {2, 2, 1, 1, 1, 2};
        Table table =
                new Table(UnitValue.createPercentArray(columnWidths))
                        .setWidth(UnitValue.createPercentValue(100))
                        .setMarginTop(10);

        // Add headers
        table.addHeaderCell(createHeaderCell("Proveedor"));
        table.addHeaderCell(createHeaderCell("Contacto"));
        table.addHeaderCell(createHeaderCell("Total Órdenes"));
        table.addHeaderCell(createHeaderCell("Completadas"));
        table.addHeaderCell(createHeaderCell("Canceladas"));
        table.addHeaderCell(createHeaderCell("Valor Promedio"));

        // Add data rows
        for (SupplierPerformanceReportDto supplier : suppliers) {
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(supplier.getSupplierName())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(
                                    new Paragraph(
                                            supplier.getContactPerson() != null
                                                    ? supplier.getContactPerson()
                                                    : "")));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(supplier.getTotalOrders().toString())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(supplier.getCompletedOrders().toString())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph(supplier.getCancelledOrders().toString())));
            table.addCell(
                    new com.itextpdf.layout.element.Cell()
                            .add(new Paragraph("$" + supplier.getAverageOrderValue().toString())));
        }

        document.add(table);
    }

    private com.itextpdf.layout.element.Cell createHeaderCell(String text) {
        return new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
    }

    // Excel helper methods
    private void addInventoryMovementsToExcel(
            Sheet sheet, CellStyle headerStyle, CellStyle dataStyle, ReportExportRequest request) {
        List<InventoryMovementReportDto> movements;
        if (Boolean.TRUE.equals(request.getDetailed())) {
            movements = reportService.getDetailedInventoryMovementReport(request.toReportFilter());
        } else {
            movements = reportService.getInventoryMovementReport(request.toReportFilter());
        }

        int rowNum = 0;

        // Create header row
        Row headerRow = sheet.createRow(rowNum++);
        int colNum = 0;

        createCell(headerRow, colNum++, "ID", headerStyle);
        createCell(headerRow, colNum++, "Código", headerStyle);
        createCell(headerRow, colNum++, "Producto", headerStyle);
        createCell(headerRow, colNum++, "Tipo", headerStyle);
        createCell(headerRow, colNum++, "Cantidad", headerStyle);

        if (Boolean.TRUE.equals(request.getDetailed())) {
            createCell(headerRow, colNum++, "Tipo Referencia", headerStyle);
            createCell(headerRow, colNum++, "Sistema Origen", headerStyle);
            createCell(headerRow, colNum++, "Usuario", headerStyle);
            createCell(headerRow, colNum++, "Notas", headerStyle);
        }

        createCell(headerRow, colNum++, "Fecha", headerStyle);
        createCell(headerRow, colNum++, "Precio Unitario", headerStyle);

        // Create data rows
        for (InventoryMovementReportDto movement : movements) {
            Row row = sheet.createRow(rowNum++);
            colNum = 0;

            createCell(row, colNum++, movement.getMovementId().toString(), dataStyle);
            createCell(row, colNum++, movement.getProductCode(), dataStyle);
            createCell(row, colNum++, movement.getProductName(), dataStyle);
            createCell(row, colNum++, movement.getMovementType().toString(), dataStyle);
            createCell(row, colNum++, movement.getQuantity().toString(), dataStyle);

            if (Boolean.TRUE.equals(request.getDetailed())) {
                createCell(
                        row,
                        colNum++,
                        movement.getReferenceType() != null
                                ? movement.getReferenceType().toString()
                                : "",
                        dataStyle);
                createCell(
                        row,
                        colNum++,
                        movement.getSourceSystem() != null ? movement.getSourceSystem() : "",
                        dataStyle);
                createCell(
                        row,
                        colNum++,
                        movement.getCreatedByUsername() != null
                                ? movement.getCreatedByUsername()
                                : "",
                        dataStyle);
                createCell(
                        row,
                        colNum++,
                        movement.getNotes() != null ? movement.getNotes() : "",
                        dataStyle);
            }

            createCell(row, colNum++, movement.getCreatedAt().format(DATE_FORMATTER), dataStyle);
            createCell(row, colNum++, "$" + movement.getUnitPrice().toString(), dataStyle);
        }
    }

    private void addStockLevelsToExcel(
            Sheet sheet, CellStyle headerStyle, CellStyle dataStyle, ReportExportRequest request) {
        List<StockLevelReportDto> stockLevels =
                reportService.getStockLevelReport(request.toReportFilter());

        int rowNum = 0;

        // Create header row
        Row headerRow = sheet.createRow(rowNum++);
        int colNum = 0;

        createCell(headerRow, colNum++, "Código", headerStyle);
        createCell(headerRow, colNum++, "Producto", headerStyle);
        createCell(headerRow, colNum++, "Categoría", headerStyle);
        createCell(headerRow, colNum++, "Stock Actual", headerStyle);
        createCell(headerRow, colNum++, "Stock Mínimo", headerStyle);
        createCell(headerRow, colNum++, "Stock Máximo", headerStyle);
        createCell(headerRow, colNum++, "Precio Unitario", headerStyle);
        createCell(headerRow, colNum++, "Entradas Período", headerStyle);
        createCell(headerRow, colNum++, "Salidas Período", headerStyle);
        createCell(headerRow, colNum++, "Último Movimiento", headerStyle);

        // Create data rows
        for (StockLevelReportDto stock : stockLevels) {
            Row row = sheet.createRow(rowNum++);
            colNum = 0;

            createCell(row, colNum++, stock.getProductCode(), dataStyle);
            createCell(row, colNum++, stock.getProductName(), dataStyle);
            createCell(
                    row,
                    colNum++,
                    stock.getCategoryName() != null ? stock.getCategoryName() : "",
                    dataStyle);
            createCell(row, colNum++, stock.getCurrentStock().toString(), dataStyle);
            createCell(row, colNum++, stock.getMinStock().toString(), dataStyle);
            createCell(
                    row,
                    colNum++,
                    stock.getMaxStock() != null ? stock.getMaxStock().toString() : "",
                    dataStyle);
            createCell(row, colNum++, "$" + stock.getUnitPrice().toString(), dataStyle);
            createCell(row, colNum++, stock.getInboundMovements().toString(), dataStyle);
            createCell(row, colNum++, stock.getOutboundMovements().toString(), dataStyle);
            createCell(
                    row,
                    colNum++,
                    stock.getLastMovementDate() != null
                            ? stock.getLastMovementDate().format(DATE_FORMATTER)
                            : "",
                    dataStyle);
        }
    }

    private void addSupplierPerformanceToExcel(
            Sheet sheet, CellStyle headerStyle, CellStyle dataStyle, ReportExportRequest request) {
        List<SupplierPerformanceReportDto> suppliers =
                reportService.getSupplierPerformanceReport(request.toReportFilter());

        int rowNum = 0;

        // Create header row
        Row headerRow = sheet.createRow(rowNum++);
        int colNum = 0;

        createCell(headerRow, colNum++, "Proveedor", headerStyle);
        createCell(headerRow, colNum++, "Persona de Contacto", headerStyle);
        createCell(headerRow, colNum++, "Email", headerStyle);
        createCell(headerRow, colNum++, "Teléfono", headerStyle);
        createCell(headerRow, colNum++, "Total Órdenes", headerStyle);
        createCell(headerRow, colNum++, "Órdenes Completadas", headerStyle);
        createCell(headerRow, colNum++, "Órdenes Pendientes", headerStyle);
        createCell(headerRow, colNum++, "Órdenes Canceladas", headerStyle);
        createCell(headerRow, colNum++, "Valor Total Órdenes", headerStyle);
        createCell(headerRow, colNum++, "Valor Promedio Orden", headerStyle);

        // Create data rows
        for (SupplierPerformanceReportDto supplier : suppliers) {
            Row row = sheet.createRow(rowNum++);
            colNum = 0;

            createCell(row, colNum++, supplier.getSupplierName(), dataStyle);
            createCell(
                    row,
                    colNum++,
                    supplier.getContactPerson() != null ? supplier.getContactPerson() : "",
                    dataStyle);
            createCell(
                    row,
                    colNum++,
                    supplier.getEmail() != null ? supplier.getEmail() : "",
                    dataStyle);
            createCell(
                    row,
                    colNum++,
                    supplier.getPhone() != null ? supplier.getPhone() : "",
                    dataStyle);
            createCell(row, colNum++, supplier.getTotalOrders().toString(), dataStyle);
            createCell(row, colNum++, supplier.getCompletedOrders().toString(), dataStyle);
            createCell(row, colNum++, supplier.getPendingOrders().toString(), dataStyle);
            createCell(row, colNum++, supplier.getCancelledOrders().toString(), dataStyle);
            createCell(row, colNum++, "$" + supplier.getTotalOrderValue().toString(), dataStyle);
            createCell(row, colNum++, "$" + supplier.getAverageOrderValue().toString(), dataStyle);
        }
    }

    // Utility methods
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private void createCell(Row row, int columnIndex, String value, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private String getDefaultTitle(ReportExportRequest.ReportType reportType) {
        switch (reportType) {
            case INVENTORY_MOVEMENTS:
                return "Reporte de Movimientos de Inventario";
            case STOCK_LEVELS:
                return "Reporte de Niveles de Stock";
            case SUPPLIER_PERFORMANCE:
                return "Reporte de Desempeño de Proveedores";
            default:
                return "Reporte del Sistema";
        }
    }

    private String getSheetName(ReportExportRequest.ReportType reportType) {
        switch (reportType) {
            case INVENTORY_MOVEMENTS:
                return "Movimientos Inventario";
            case STOCK_LEVELS:
                return "Niveles Stock";
            case SUPPLIER_PERFORMANCE:
                return "Desempeño Proveedores";
            default:
                return "Reporte";
        }
    }

    /** Generate filename for export */
    public String generateFilename(ReportExportRequest request) {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMATTER);
        String reportName = getSheetName(request.getReportType()).replaceAll(" ", "_");
        String extension =
                request.getFormat() == ReportExportRequest.ExportFormat.PDF ? ".pdf" : ".xlsx";
        return reportName + "_" + timestamp + extension;
    }
}
