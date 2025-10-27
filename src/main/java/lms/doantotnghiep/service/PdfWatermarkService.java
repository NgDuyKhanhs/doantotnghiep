package lms.doantotnghiep.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PdfWatermarkService {

    public File addWatermark(InputStream pdfInput, Integer userId) throws IOException {
        File tempFile = File.createTempFile("watermarked_", ".pdf");
        try (
                PdfReader reader = new PdfReader(pdfInput);
                PdfWriter writer = new PdfWriter(tempFile);
                PdfDocument pdfDoc = new PdfDocument(reader, writer)
        ) {
            String watermarkText = "Giang vien: " + userId;
            int n = pdfDoc.getNumberOfPages();
            for (int i = 1; i <= n; i++) {
                PdfPage page = pdfDoc.getPage(i);
                Rectangle ps = page.getPageSize();
                PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);
                Canvas canvas = new Canvas(pdfCanvas, ps);
                Paragraph p = new Paragraph(watermarkText)
                        .setFontSize(20f)
                        .setFontColor(ColorConstants.GRAY)
                        .setOpacity(0.3f);

                canvas.showTextAligned(p,
                        ps.getWidth() / 2,
                        ps.getHeight() / 2,
                        i,
                        TextAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        (float) Math.toRadians(45));
                canvas.close();
            }
        }
        return tempFile;
    }
}