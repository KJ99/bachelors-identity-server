package pl.kj.bachelors.identity.unit.infrastructure.service.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import pl.kj.bachelors.identity.BaseTest;
import pl.kj.bachelors.identity.domain.exception.AggregatedApiError;
import pl.kj.bachelors.identity.infrastructure.service.file.FileUploadService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

public class FileUploadServiceTests extends BaseTest {

    @Autowired
    private FileUploadService service;

    private String[] allowedMediaTypes;
    private int maxFileSize;

    @BeforeEach
    public void setUp() {
        this.allowedMediaTypes = new String[] { "application/pdf", "image/gif" };
        this.maxFileSize = 5 * 1024 * 1024;
    }

    @Test
    public void testProcessUpload_CorrectResult() throws IOException, AggregatedApiError {
        MultipartFile file = getCorrectPdf();
        var uploadedFile = service.processUpload(file, this.allowedMediaTypes, this.maxFileSize);

        assertThat(uploadedFile).isNotNull();
        assertThat(uploadedFile.getFileName())
                .isNotNull()
                .isNotEmpty();
        assertThat(uploadedFile.getDirectory())
                .isNotNull()
                .isNotEmpty();
        assertThat(uploadedFile.getMediaType())
                .isEqualTo("application/pdf");
        assertThat(uploadedFile.getOriginalFileName())
                .isEqualTo(file.getOriginalFilename());

    }

    @Test
    public void testProcessUpload_BadMediaType() {
        Throwable thrown = catchThrowable(() -> {
            MultipartFile file = getPngInPdf();
            var uploadedFile = service.processUpload(file, this.allowedMediaTypes, this.maxFileSize);
        });
        assertThat(thrown).isInstanceOf(AggregatedApiError.class);
        AggregatedApiError ex = (AggregatedApiError) thrown;
        assertThat(ex.getErrors()).isNotEmpty();
        assertThat(ex.getErrors().stream().anyMatch(err -> err.getCode().equals("FILE.01"))).isTrue();
    }

    @Test
    public void testProcessUpload_FileTooLarge() {
        Throwable thrown = catchThrowable(() -> {
            MultipartFile file = getLargePdf();
            service.processUpload(file, this.allowedMediaTypes, this.maxFileSize);
        });
        assertThat(thrown).isInstanceOf(AggregatedApiError.class);
        AggregatedApiError ex = (AggregatedApiError) thrown;
        assertThat(ex.getErrors()).isNotEmpty();
        assertThat(ex.getErrors().stream().anyMatch(err -> err.getCode().equals("FILE.02"))).isTrue();
    }


    private MultipartFile getCorrectPdf() {
        return new MockMultipartFile(
                "some_pdf_file",
                "some important pdf",
                "application/pdf",
                getPdfMagicNumbers()
        );
    }

    private MultipartFile getPngInPdf() {
        return new MockMultipartFile(
                "some_pdf_file",
                "some important pdf",
                "application/pdf",
                getPngMagicNumbers()
        );
    }

    private MultipartFile getLargePdf() {
        byte[] magicNumbers = getPdfMagicNumbers();
        byte[] allBytes = new byte[10 * 1024 * 1024];

        System.arraycopy(magicNumbers, 0, allBytes, 0, magicNumbers.length);

        return new MockMultipartFile(
                "some_pdf_file",
                "some important pdf",
                "application/pdf",
                allBytes
        );
    }

    private byte[] getPdfMagicNumbers() {
        return new byte[] {
                (byte) 0x25,
                (byte) 0x50,
                (byte) 0x44,
                (byte) 0x46,
                (byte) 0x2D
        };
    }

    private byte[] getPngMagicNumbers() {
        return new byte[] {
                (byte) 0x89,
                (byte) 0x50,
                (byte) 0x4E,
                (byte) 0x47,
                (byte) 0x0D,
                (byte) 0x0A,
                (byte) 0x1A,
                (byte) 0x0A,
        };
    }
}
