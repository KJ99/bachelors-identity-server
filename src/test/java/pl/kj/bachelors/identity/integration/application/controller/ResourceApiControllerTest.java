package pl.kj.bachelors.identity.integration.application.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kj.bachelors.identity.application.Application;
import pl.kj.bachelors.identity.application.controller.ResourceApiController;
import pl.kj.bachelors.identity.application.exception.BadRequestHttpException;
import pl.kj.bachelors.identity.domain.model.UploadedFile;
import pl.kj.bachelors.identity.domain.service.file.FileUploader;
import pl.kj.bachelors.identity.infrastructure.repository.UploadedFileRepository;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = { Application.class })
@ComponentScan(basePackages = "pl.kj.bachelors.identity")
public class ResourceApiControllerTest {
    @MockBean private FileUploader fileUploader;
    @MockBean private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private MockMvc mockMvc;

    MockMultipartFile file;
    String[] mediaTypes;
    int maxBytes;

    @BeforeEach
    public void setUp() throws BadRequestHttpException, IOException {
        this.file = getFile();
        this.mediaTypes = new String[] { "application/pdf" };
        this.maxBytes = 5 * 1024;

        UploadedFile result = new UploadedFile();
        result.setFileName("randomname");
        result.setMediaType("application/pdf");
        result.setDirectory("somedirectory");
        result.setOriginalFileName(this.file.getName());

        given(this.fileUploader.processUpload(any(), any(), anyInt())).willReturn(result);
    }

    @Test
    public void testPost() throws BadRequestHttpException, Exception {
        MvcResult mvcResult = mockMvc.perform(multipart("/v1/resources").file("file", this.file.getBytes()))
                .andExpect(status().isCreated())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).contains("created_id");
    }

    @Test
    public void testGetParticular() throws Exception {
        var uploadedFile = new UploadedFile();
        uploadedFile.setFileName("World");
        uploadedFile.setOriginalFileName("Hello");
        uploadedFile.setId(10);
        uploadedFile.setMediaType("application/json");

        given(this.uploadedFileRepository.findById(1)).willReturn(Optional.of(uploadedFile));


        MvcResult mvcResult = mockMvc.perform(get("/v1/resources/1"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
                .contains("10")
                .contains("Hello")
                .contains("application/json");
    }

    @Test
    public void testDownload() throws Exception {
        File tempFile = File.createTempFile("temp-", "-testing.png");
        var uploadedFile = new UploadedFile();
        uploadedFile.setFileName(tempFile.getName());
        uploadedFile.setDirectory(tempFile.getParent());
        uploadedFile.setOriginalFileName("original_text");
        uploadedFile.setMediaType("image/png");
        uploadedFile.setId(1);
        given(this.uploadedFileRepository.findById(1)).willReturn(Optional.of(uploadedFile));

        mockMvc.perform(get("/v1/resources/1/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));

        tempFile.deleteOnExit();
    }

    @Test
    public void testDownload_FileNotExists() throws Exception {
        var uploadedFile = new UploadedFile();
        uploadedFile.setFileName("not-exists");
        uploadedFile.setDirectory("/thisfile");
        uploadedFile.setOriginalFileName("original_text");
        uploadedFile.setMediaType("image/png");
        uploadedFile.setId(1);
        given(this.uploadedFileRepository.findById(1)).willReturn(Optional.of(uploadedFile));

        mockMvc.perform(get("/v1/resources/1/download"))
                .andExpect(status().isNotFound());
    }

    private MockMultipartFile getFile() {
        return new MockMultipartFile(
                "some_pdf_file",
                "some important pdf",
                "application/pdf",
                getPdfMagicNumbers()
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

}
