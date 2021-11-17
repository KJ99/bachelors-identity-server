package pl.kj.bachelors.identity.integration.application.controller;

import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kj.bachelors.identity.infrastructure.repository.UploadedFileRepository;
import pl.kj.bachelors.identity.integration.BaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResourceApiControllerTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UploadedFileRepository repository;

    @Autowired
    private Storage storage;

    private MockMultipartFile file;
    @BeforeEach
    public void setUp() {
        this.file = new MockMultipartFile(
                "some_pdf_file",
                "some important pdf",
                "application/pdf",
                getPdfMagicNumbers()
        );
    }

    @Test
    public void testPost() throws Exception {
        String auth = String.format("%s %s", this.jwtConfig.getType(), this.generateValidAccessToken("uid-active-1"));

        MvcResult mvcResult = mockMvc.perform(
                    multipart("/v1/resources").file("file", this.file.getBytes())
                            .header("Authorization", auth)
                )
                .andExpect(status().isCreated())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString()).contains("file_name");
        assertThat(mvcResult.getResponse().getContentAsString()).contains("media_type");
    }

    @Test
    public void testGetParticular() throws Exception {
        var uploadedFile = this.repository.findById(1).orElseThrow();

        MvcResult mvcResult = mockMvc.perform(get("/v1/resources/1"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
                .contains("1")
                .contains("or-filename.jpg")
                .contains("image/jpg");
    }

    @Test
    public void testDownload() throws Exception {
        var uploadedFile = this.repository.findById(1).orElseThrow();

        mockMvc.perform(get("/v1/resources/1/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpg"));
    }

    @Test
    public void testDownload_FileNotExists() throws Exception {
        mockMvc.perform(get("/v1/resources/-5/download"))
                .andExpect(status().isNotFound());
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
