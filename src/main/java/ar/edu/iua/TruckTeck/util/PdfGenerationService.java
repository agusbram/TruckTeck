package ar.edu.iua.TruckTeck.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
// import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import ar.edu.iua.TruckTeck.model.Conciliation;
import ar.edu.iua.TruckTeck.model.Order;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio encargado de la generación de documentos PDF profesionales.
 * <p>
 * Genera reportes de conciliación con formato corporativo, incluyendo:
 * - Encabezado con datos de la empresa
 * - Información detallada de la orden
 * - Datos de pesaje y carga
 * - Análisis de conciliación
 * - Footer con fecha de generación
 * </p>
 */
@Service
@Slf4j
public class PdfGenerationService {

    @Value("${pdf.temp.directory:./temp/pdfs}")
    private String tempDirectory;

    @Value("${company.name}")
    private String companyName;

    @Value("${company.address}")
    private String companyAddress;

    @Value("${company.city}")
    private String companyCity;

    // @Value("${company.phone}")
    // private String companyPhone;

    @Value("${company.email}")
    private String companyEmail;

    // @Value("${company.web}")
    // private String companyWeb;

    @Value("${pdf.color.primary.r}")
    private int primaryR;

    @Value("${pdf.color.primary.g}")
    private int primaryG;

    @Value("${pdf.color.primary.b}")
    private int primaryB;

    @Value("${pdf.color.secondary.r}")
    private int secondaryR;

    @Value("${pdf.color.secondary.g}")
    private int secondaryG;

    @Value("${pdf.color.secondary.b}")
    private int secondaryB;

    // Colores corporativos
    private Color getPrimaryColor() {
        return new DeviceRgb(primaryR, primaryG, primaryB);
    }

    private Color getSecondaryColor() {
        return new DeviceRgb(secondaryR, secondaryG, secondaryB);
    }

    private static final Color GRAY_LIGHT = new DeviceRgb(240, 240, 240);
    private static final Color GRAY_MEDIUM = new DeviceRgb(200, 200, 200);
    private static final Color WHITE = new DeviceRgb(255, 255, 255);
    private static final Color BLACK = new DeviceRgb(0, 0, 0);
    private static final Color GREEN = new DeviceRgb(76, 175, 80);
    private static final Color RED = new DeviceRgb(244, 67, 54);
    private static final Color ORANGE = new DeviceRgb(255, 152, 0);

    /**
     * Genera un PDF de conciliación y lo retorna como array de bytes.
     *
     * @param order Orden finalizada.
     * @param conciliation Datos de conciliación calculados.
     * @return Array de bytes del PDF generado.
     * @throws Exception Si ocurre un error durante la generación.
     */
    public byte[] generateConciliationPdf(Order order, Conciliation conciliation) throws Exception {
        log.info("Iniciando generación de PDF para orden: {}", order.getNumber());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // Inicializar documento PDF
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(40, 40, 40, 40);

            // Fuentes
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // ==================== ENCABEZADO ====================
            addHeader(document, fontBold, fontRegular);

            // ==================== TÍTULO ====================
            addTitle(document, fontBold, order);

            // ==================== INFORMACIÓN DE LA ORDEN ====================
            addOrderInfo(document, fontBold, fontRegular, order);

            // ==================== DATOS DE PESAJE ====================
            addWeighingData(document, fontBold, fontRegular, conciliation);

            // ==================== DATOS DE CARGA ====================
            addLoadingData(document, fontBold, fontRegular, order, conciliation);

            // ==================== ANÁLISIS DE CONCILIACIÓN ====================
            addConciliationAnalysis(document, fontBold, fontRegular, conciliation);

            // ==================== RESUMEN EJECUTIVO ====================
            addExecutiveSummary(document, fontBold, fontRegular, conciliation);

            // ==================== FOOTER ====================
            addFooter(document, fontRegular);

            // Cerrar documento
            document.close();
            log.info("PDF generado exitosamente para orden: {}", order.getNumber());

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF para orden: {}", order.getNumber(), e);
            throw new Exception("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Genera un PDF y lo guarda en un archivo.
     *
     * @param order Orden finalizada.
     * @param conciliation Datos de conciliación.
     * @return Ruta del archivo generado.
     * @throws Exception Si ocurre un error durante la generación.
     */
    public String generateConciliationPdfFile(Order order, Conciliation conciliation) throws Exception {
        byte[] pdfBytes = generateConciliationPdf(order, conciliation);

        // Crear directorio temporal si no existe
        File directory = new File(tempDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generar nombre único para el archivo
        String fileName = String.format("Conciliacion_%s_%s.pdf",
                order.getNumber(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        String filePath = tempDirectory + File.separator + fileName;

        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdfBytes);
            log.info("PDF guardado en: {}", filePath);
        }

        return filePath;
    }

    // ==================== MÉTODOS AUXILIARES DE SECCIONES ====================

    private void addHeader(Document document, PdfFont fontBold, PdfFont fontRegular) throws Exception {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .useAllAvailableWidth();

        // Información de la empresa
        Cell companyCell = new Cell()
                .add(new Paragraph(companyName)
                        .setFont(fontBold)
                        .setFontSize(18)
                        .setFontColor(getPrimaryColor()))
                .add(new Paragraph(companyAddress)
                        .setFont(fontRegular)
                        .setFontSize(9)
                        .setMarginTop(3))
                .add(new Paragraph(companyCity)
                        .setFont(fontRegular)
                        .setFontSize(9))
                .add(new Paragraph("Email: " + companyEmail)
                        .setFont(fontRegular)
                        .setFontSize(8)
                        .setMarginTop(2))
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(10);

        // Logo o espacio para logo
        Cell logoCell = new Cell()
            // Imagen a la derecha
        //     .add(new Image(ImageDataFactory.create("src/main/resources/static/TruckTeck.png"))
        //     .add(new Image(ImageDataFactory.create("../../../../../../resources/static/TruckTeck.png"))
        //             .setWidth(80)  // Ancho en puntos
        //             .setHeight(80) // Alto en puntos
        //             .setHorizontalAlignment(HorizontalAlignment.RIGHT))
                    // .setMarginLeft(20))

            // Texto
        //     .add(new Paragraph("Terminal Management")
        //             .setFont(fontRegular)
        //             .setFontSize(8)
        //             .setTextAlignment(TextAlignment.RIGHT)
        //             .setMarginTop(5))

            .setBorder(Border.NO_BORDER)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setTextAlignment(TextAlignment.RIGHT)
            .setHorizontalAlignment(HorizontalAlignment.RIGHT)
            .setPaddingBottom(10)
            .setPaddingRight(10);


        // Cell logoCell = new Cell()
        //         .add(new Paragraph("SISTEMA de CARGA de GAS LIQUIDO")
        //                 .setFont(fontBold)
        //                 .setFontSize(12)
        //                 .setFontColor(getSecondaryColor())
        //                 .setTextAlignment(TextAlignment.RIGHT))
        //         .add(new Paragraph("Terminal Management")
        //                 .setFont(fontRegular)
        //                 .setFontSize(8)
        //                 .setTextAlignment(TextAlignment.RIGHT))
        //         .setBorder(Border.NO_BORDER)
        //         .setVerticalAlignment(VerticalAlignment.TOP)
        //         .setPaddingBottom(10);

        headerTable.addCell(companyCell);
        headerTable.addCell(logoCell);

        document.add(headerTable);

        // Línea separadora
        document.add(new Paragraph()
                .setBorderBottom(new SolidBorder(getPrimaryColor(), 2))
                .setMarginBottom(20));
    }

    private void addTitle(Document document, PdfFont fontBold, Order order) {
        document.add(new Paragraph("REPORTE DE CONCILIACIÓN")
                .setFont(fontBold)
                .setFontSize(20)
                .setFontColor(getSecondaryColor())
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5));

        document.add(new Paragraph("Orden N° " + order.getNumber())
                .setFont(fontBold)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
    }

    private void addOrderInfo(Document document, PdfFont fontBold, PdfFont fontRegular, Order order) {
        document.add(new Paragraph("INFORMACIÓN DE LA ORDEN")
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(getPrimaryColor())
                .setMarginBottom(10));

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .useAllAvailableWidth();

        // Fila 1
        addInfoCell(infoTable, "Cliente:", order.getClient().getCompanyName(), fontBold, fontRegular);
        addInfoCell(infoTable, "Producto:", order.getProduct().getName(), fontBold, fontRegular);

        // Fila 2
        addInfoCell(infoTable, "Camión:", order.getTruck().getDomain(), fontBold, fontRegular);
        addInfoCell(infoTable, "Chofer:", order.getDriver().getName() + " " + order.getDriver().getSurname(), fontBold, fontRegular);

        // Fila 3
        addInfoCell(infoTable, "Fecha Inicio:", formatDateTime(order.getStartLoading()), fontBold, fontRegular);
        addInfoCell(infoTable, "Fecha Fin:", formatDateTime(order.getEndLoading()), fontBold, fontRegular);

        document.add(infoTable);
        document.add(new Paragraph().setMarginBottom(15));
    }

    private void addWeighingData(Document document, PdfFont fontBold, PdfFont fontRegular, Conciliation conciliation) {
        document.add(new Paragraph("DATOS DE PESAJE")
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(getPrimaryColor())
                .setMarginBottom(10));

        Table weighingTable = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .useAllAvailableWidth();

        // Encabezado
        weighingTable.addHeaderCell(createHeaderCell("Concepto", fontBold));
        weighingTable.addHeaderCell(createHeaderCell("Valor (kg)", fontBold));

        // Datos
        addDataRow(weighingTable, "Peso Inicial (Tara)", 
                   String.format("%.2f", conciliation.getInitialWeight()), 
                   fontRegular);
        
        addDataRow(weighingTable, "Peso Final (Bruto)", 
                   String.format("%.2f", conciliation.getFinalWeight()), 
                   fontRegular);
        
        addDataRow(weighingTable, "Peso Neto (Balanza)", 
                   String.format("%.2f", conciliation.getNetWeight()), 
                   fontRegular);
        
        addDataRow(weighingTable, "Producto Cargado / Masa Acumulada", 
                   String.format("%.2f", conciliation.getAccumulatedMass()), 
                   fontRegular);

        document.add(weighingTable);
        document.add(new Paragraph().setMarginBottom(15));
    }

    private void addLoadingData(Document document, PdfFont fontBold, PdfFont fontRegular, 
                                Order order, Conciliation conciliation) {
        document.add(new Paragraph("PARÁMETROS PROMEDIO DE CARGA")
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(getPrimaryColor())
                .setMarginBottom(10));

        Table loadingTable = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                .useAllAvailableWidth();

        // Encabezado
        loadingTable.addHeaderCell(createHeaderCell("Parámetro", fontBold));
        loadingTable.addHeaderCell(createHeaderCell("Valor", fontBold));
        loadingTable.addHeaderCell(createHeaderCell("Unidad", fontBold));

        // Datos
        addDataRow(loadingTable, "Temperatura Promedio", 
                   String.format("%.2f", conciliation.getAverageTemperature()), 
                   "°C", fontRegular, BLACK);
        
        addDataRow(loadingTable, "Densidad Promedio", 
                   String.format("%.4f", conciliation.getAverageDensity()), 
                   "kg/L", fontRegular, BLACK);
        
        addDataRow(loadingTable, "Caudal Promedio", 
                   String.format("%.2f", conciliation.getAverageCaudal()), 
                   "kg/h", fontRegular, BLACK);
        
        addDataRow(loadingTable, "Preset (Objetivo)", 
                   String.format("%.2f", order.getPreset()), 
                   "kg", fontRegular, BLACK);

        document.add(loadingTable);
        document.add(new Paragraph().setMarginBottom(15));
    }

    private void addConciliationAnalysis(Document document, PdfFont fontBold, PdfFont fontRegular, 
                                         Conciliation conciliation) {
        document.add(new Paragraph("ANÁLISIS DE CONCILIACIÓN")
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(getPrimaryColor())
                .setMarginBottom(10));

        Double difference = conciliation.getDifferenceWeight();
        Double percentDiff = (difference / conciliation.getNetWeight()) * 100;

        // Tabla de diferencias
        Table analysisTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .useAllAvailableWidth();

        Cell diffCell = new Cell()
                .add(new Paragraph("DIFERENCIA ABSOLUTA")
                        .setFont(fontBold)
                        .setFontSize(10)
                        .setMarginBottom(5))
                .add(new Paragraph(String.format("%.2f kg", Math.abs(difference)))
                        .setFont(fontBold)
                        .setFontSize(18)
                        .setFontColor(getColorForDifference(difference)))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(GRAY_LIGHT)
                .setPadding(15);

        Cell percentCell = new Cell()
                .add(new Paragraph("DIFERENCIA PORCENTUAL")
                        .setFont(fontBold)
                        .setFontSize(10)
                        .setMarginBottom(5))
                .add(new Paragraph(String.format("%.2f%%", Math.abs(percentDiff)))
                        .setFont(fontBold)
                        .setFontSize(18)
                        .setFontColor(getColorForDifference(difference)))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(GRAY_LIGHT)
                .setPadding(15);

        analysisTable.addCell(diffCell);
        analysisTable.addCell(percentCell);

        document.add(analysisTable);
        document.add(new Paragraph().setMarginBottom(15));
    }

    private void addExecutiveSummary(Document document, PdfFont fontBold, PdfFont fontRegular, 
                                     Conciliation conciliation) {
        document.add(new Paragraph("RESUMEN EJECUTIVO")
                .setFont(fontBold)
                .setFontSize(12)
                .setFontColor(getPrimaryColor())
                .setMarginBottom(10));

        Double difference = conciliation.getDifferenceWeight();
        Double percentDiff = (difference / conciliation.getNetWeight()) * 100;

        String status = getStatusText(difference, percentDiff);
        Color statusColor = getColorForDifference(difference);
        String icon = getIconForDifference(difference);

        Table summaryTable = new Table(1).useAllAvailableWidth();
        
        Cell summaryCell = new Cell()
                .add(new Paragraph(icon + " " + status)
                        .setFont(fontBold)
                        .setFontSize(14)
                        .setFontColor(statusColor)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph(getRecommendation(difference, percentDiff))
                        .setFont(fontRegular)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(10))
                .setBackgroundColor(GRAY_LIGHT)
                .setPadding(20)
                .setBorder(new SolidBorder(statusColor, 2));

        summaryTable.addCell(summaryCell);
        document.add(summaryTable);
    }

    private void addFooter(Document document, PdfFont fontRegular) {
        document.add(new Paragraph()
                .setBorderTop(new SolidBorder(GRAY_MEDIUM, 1))
                .setMarginTop(30)
                .setMarginBottom(10));

        String generatedDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        document.add(new Paragraph("Documento generado automáticamente por " + companyName)
                .setFont(fontRegular)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(GRAY_MEDIUM));

        document.add(new Paragraph("Fecha de generación: " + generatedDate)
                .setFont(fontRegular)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(GRAY_MEDIUM));

        document.add(new Paragraph("Este documento tiene validez legal y se encuentra firmado digitalmente")
                .setFont(fontRegular)
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(GRAY_MEDIUM)
                .setMarginTop(5));
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private void addInfoCell(Table table, String label, String value, PdfFont fontBold, PdfFont fontRegular) {
        Cell cell = new Cell()
                .add(new Paragraph(label)
                        .setFont(fontBold)
                        .setFontSize(9)
                        .setMarginBottom(2))
                .add(new Paragraph(value)
                        .setFont(fontRegular)
                        .setFontSize(10))
                .setBackgroundColor(GRAY_LIGHT)
                .setPadding(8)
                .setBorder(new SolidBorder(WHITE, 2));
        table.addCell(cell);
    }

    private Cell createHeaderCell(String text, PdfFont fontBold) {
        return new Cell()
                .add(new Paragraph(text)
                        .setFont(fontBold)
                        .setFontSize(10)
                        .setFontColor(WHITE))
                .setBackgroundColor(getSecondaryColor())
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    private void addDataRow(Table table, String label, String value, PdfFont fontRegular) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(fontRegular).setFontSize(9))
                .setPadding(6));
        
        table.addCell(new Cell()
                .add(new Paragraph(value).setFont(fontRegular).setFontSize(9))
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(6));
    }

    private void addDataRow(Table table, String label, String value, String unit, 
                           PdfFont fontRegular, Color unitColor) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(fontRegular).setFontSize(9))
                .setPadding(6));
        
        table.addCell(new Cell()
                .add(new Paragraph(value).setFont(fontRegular).setFontSize(9))
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(6));
        
        table.addCell(new Cell()
                .add(new Paragraph(unit).setFont(fontRegular).setFontSize(9).setFontColor(unitColor))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6));
    }

    private Color getColorForDifference(Double difference) {
        double absDiff = Math.abs(difference);
        if (absDiff < 10) return GREEN;
        if (absDiff < 50) return ORANGE;
        return RED;
    }

    private String getIconForDifference(Double difference) {
        double absDiff = Math.abs(difference);
        if (absDiff < 10) return "✓";
        if (absDiff < 50) return "⚠";
        return "✗";
    }

    private String getStatusText(Double difference, Double percentDiff) {
        double absDiff = Math.abs(difference);
        if (absDiff < 10) return "CONCILIACIÓN EXCELENTE";
        if (absDiff < 50) return "CONCILIACIÓN ACEPTABLE";
        return "REQUIERE REVISIÓN";
    }

    private String getRecommendation(Double difference, Double percentDiff) {
        double absDiff = Math.abs(difference);
        if (absDiff < 10) {
            return "La diferencia entre balanza y caudalímetro está dentro de los parámetros normales. " +
                   "El proceso de carga se completó exitosamente.";
        }
        if (absDiff < 50) {
            return "Se detectó una diferencia moderada. Se recomienda verificar la calibración " +
                   "del caudalímetro en el próximo mantenimiento preventivo.";
        }
        return "ATENCIÓN: La diferencia supera los límites aceptables. " +
               "Se requiere inspección inmediata del sistema de medición y calibración de equipos.";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}