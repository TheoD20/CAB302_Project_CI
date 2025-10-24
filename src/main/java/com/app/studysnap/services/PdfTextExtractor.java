package com.app.studysnap.services;

import com.app.studysnap.exceptions.DataAccessException;
import com.app.studysnap.exceptions.ValidationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ThreadLocalRandom;

import static com.app.studysnap.services.TextParser.*;

/**
 * Extracts UTF-8 text from PDF and TXT files.
 * <p>
 * For large PDFs (&gt;30 pages), a random middle window is sampled to avoid
 * front/back matter like TOCs and dedications. All output is normalized to a
 * consistent line/whitespace format to behave like pasted text.
 * </p>
 */
public final class PdfTextExtractor {

    /**
     * Default constructor:
     * Creates a new {@code PdfTextExtractor}.
     */
    public PdfTextExtractor() {}

    /**
     * Extracts and normalizes raw text from a supported file type.
     * <ul>
     *   <li><b>PDF</b>: full extract if ≤30 pages, otherwise a random middle window.</li>
     *   <li><b>TXT</b>: read as UTF-8.</li>
     * </ul>
     * @param file the input file (.pdf or .txt)
     * @return normalized text
     */
    public String extract(File file) {
        try {
            String name = file.getName().toLowerCase();
            String raw;
            if (name.endsWith(".pdf")) {
                raw = (countPages(file) > 30) ? extractPdfMidSample(file) : extractPdf(file);
                if (isBlank(raw)) {
                    throw new ValidationException("No extractable text found in the file (it may be a scanned PDF).");
                }
            } else if (name.endsWith(".txt")) {
                raw = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            } else {
                throw new ValidationException("Unsupported file type: " + file.getName());
            }

            return normalize(raw);
        } catch (ValidationException ve) {
            throw ve;
        } catch (IOException ioe) {
            throw new DataAccessException("Failed to read file: " + file, ioe);
        } catch (Exception e) {
            throw new DataAccessException("Unexpected error while extracting text: " + file, e);
        }
    }

    /**
     * Extracts text from all pages of a PDF.
     * @param f PDF file
     * @return extracted text
     */
    private String extractPdf(File f) {
        try (PDDocument doc = PDDocument.load(f)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setSuppressDuplicateOverlappingText(true);
            stripper.setWordSeparator(" ");
            stripper.setLineSeparator("\n");
            return stripper.getText(doc);
        } catch (IOException ioe) {
            throw new DataAccessException("Failed to read PDF: " + f, ioe);
        }
    }

    /**
     * Counts the number of pages in a PDF.
     * @param f PDF file
     * @return page count
     * @throws DataAccessException if the PDF cannot be read
     */
    private int countPages(File f) throws IOException {
        try (PDDocument doc = PDDocument.load(f)) {
            return doc.getNumberOfPages();
        } catch (IOException ioe) {
            throw new DataAccessException("Failed to open PDF for page count: " + f, ioe);
        }
    }

    /**
     * Extracts a random middle window of pages from a large PDF to avoid front/back matter.
     * The window size is between 12 and 40 pages or pages/6, whichever fits.
     * @param f PDF file
     * @return extracted text from the window
     */
    public String extractPdfMidSample(File f) {
        try (PDDocument doc = PDDocument.load(f)) {
            int pages = doc.getNumberOfPages();

            int window = Math.min(40, Math.max(12, pages / 6));
            int maxStart = pages - window + 1;
            int start = ThreadLocalRandom.current().nextInt(1, Math.max(2, maxStart + 1));
            int end = Math.min(pages, start + window - 1);

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            stripper.setSuppressDuplicateOverlappingText(true);
            stripper.setWordSeparator(" ");
            stripper.setLineSeparator("\n");
            stripper.setStartPage(start);
            stripper.setEndPage(end);

            return stripper.getText(doc);
        } catch (IOException ioe) {
            throw new DataAccessException("Failed to read PDF sample: " + f, ioe);
        }
    }

    /**
     * Normalizes raw text to reduce layout artifacts:
     * <ul>
     *   <li>CRLF/CR → LF</li>
     *   <li>NBSP/ZWSP/soft hyphen cleanup</li>
     *   <li>Preserves words across hyphen line breaks</li>
     *   <li>Collapses horizontal whitespace and tall gaps</li>
     *   <li>Trims leading/trailing whitespace</li>
     * </ul>
     * @param raw original text
     * @return normalized text (never {@code null})
     */
    private static String normalize(String raw) {
        if (raw == null) return "";
        String s = raw;

        // New lines to LF
        s = s.replace("\r\n", "\n").replace("\r", "\n");

        // Invisible and layout artifacts
        s = s.replace('\u00A0', ' ');
        s = s.replace("\u200B", "");
        s = s.replace("\u00AD", "");

        // Handle hyphen wrappers: break-hyphen at line end + lowercase continuation
        s = s.replaceAll("(?<=\\p{L})-\\n(?=\\p{Ll})", "");

        // Horizontal whitespace compaction
        s = s.replaceAll("[ \\t\\x0B\\f]+", " ");

        // Normalize tall gaps to a single blank line
        s = s.replaceAll("\\n{3,}", "\n\n");

        // Trim edges via TextParser helper
        return trim(s);
    }
}