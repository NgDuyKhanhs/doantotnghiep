package lms.doantotnghiep.security;

import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.action.PdfAction;

import lms.doantotnghiep.enums.AppException;
import lms.doantotnghiep.enums.ErrorConstant;
import org.springframework.stereotype.Component;
import java.io.InputStream;

@Component
public class PDFSecurityScanner {

    public void validate(InputStream inputStream) {
        try (PdfReader reader = new PdfReader(inputStream);
             PdfDocument pdf = new PdfDocument(reader)) {

            checkCatalogForJavascript(pdf);
            checkOpenActionForJavascript(pdf);
            checkEmbeddedFiles(pdf);
            scanForMaliciousObjects(pdf);

        } catch (AppException ex) {
            throw ex;
        } catch (Exception e) {
            throw new AppException(ErrorConstant.FILE_ERROR);
        }
    }

    // ------------------------------------------------------------
    // 1. KIỂM TRA JAVASCRIPT TRONG CATALOG
    // ------------------------------------------------------------
    private void checkCatalogForJavascript(PdfDocument pdf) {
        PdfDictionary catalog = pdf.getCatalog().getPdfObject();

        PdfDictionary names = catalog.getAsDictionary(PdfName.Names);
        if (names != null) {
            PdfDictionary js = names.getAsDictionary(PdfName.JavaScript);
            if (js != null) {
                throw new AppException(
                        ErrorConstant.FILE_JS
                );
            }
        }
    }

    // ------------------------------------------------------------
    // 2. KIỂM TRA OPEN ACTION CÓ CHỨA JS
    // ------------------------------------------------------------
    private void checkOpenActionForJavascript(PdfDocument pdf) {
        PdfDictionary catalog = pdf.getCatalog().getPdfObject();
        PdfObject openActionObj = catalog.get(PdfName.OpenAction);

        if (openActionObj == null) return;

        PdfDictionary actionDict = null;

        if (openActionObj instanceof PdfDictionary) {
            actionDict = (PdfDictionary) openActionObj;
        }

        if (openActionObj instanceof PdfArray) {
            PdfArray array = (PdfArray) openActionObj;
            if (array.size() > 0 && array.get(0) instanceof PdfDictionary) {
                actionDict = array.getAsDictionary(0);
            }
        }

        if (actionDict == null) return;

        PdfName subtype = actionDict.getAsName(PdfName.S);
        if (PdfName.JavaScript.equals(subtype)) {
            throw new AppException(ErrorConstant.FILE_JS);
        }

        PdfString js = actionDict.getAsString(PdfName.JS);
        if (js != null) {
            throw new AppException(ErrorConstant.FILE_JS);
        }
    }



    // ------------------------------------------------------------
    // 3. KIỂM TRA EMBEDDED FILES
    // ------------------------------------------------------------
    private void checkEmbeddedFiles(PdfDocument pdf) {
        PdfDictionary catalog = pdf.getCatalog().getPdfObject();
        PdfDictionary names = catalog.getAsDictionary(PdfName.Names);

        if (names != null) {
            PdfDictionary embeddedFiles = names.getAsDictionary(PdfName.EmbeddedFiles);

            if (embeddedFiles != null) {
                throw new AppException(
                        ErrorConstant.FILE_JS
                );
            }
        }
    }

    // ------------------------------------------------------------
    // 4. QUÉT TẤT CẢ OBJECT TÌM RICHMEDIA, XFA...
    // ------------------------------------------------------------
    private void scanForMaliciousObjects(PdfDocument pdf) {
        for (int i = 1; i <= pdf.getNumberOfPdfObjects(); i++) {
            PdfObject obj = pdf.getPdfObject(i);

            if (obj != null && obj.isDictionary()) {
                PdfDictionary dict = (PdfDictionary) obj;

                if (dict.containsKey(PdfName.RichMedia)) {
                    throw new AppException(ErrorConstant.FILE_JS);
                }

                if (dict.containsKey(PdfName.XFA)) {
                    throw new AppException(ErrorConstant.FILE_JS);
                }

                if (dict.containsKey(PdfName.JS)) {
                    throw new AppException(ErrorConstant.FILE_JS);
                }
                if (PdfName.JavaScript.equals(dict.getAsName(PdfName.S))) {
                    throw new AppException(ErrorConstant.FILE_JS);
                }

                // --- Chặn file attachment ---
                if (dict.containsKey(PdfName.EmbeddedFile)
                        || dict.containsKey(PdfName.Filespec)
                        || dict.containsKey(PdfName.EF)
                        || dict.containsKey(PdfName.AF)) {
                    throw new AppException(ErrorConstant.FILE_JS);
                }

                // --- Chặn action mở file ---
                if (dict.containsKey(PdfName.F) || dict.containsKey(PdfName.FS)) {
                    throw new AppException(ErrorConstant.FILE_JS);
                }
            }
        }
    }

}
