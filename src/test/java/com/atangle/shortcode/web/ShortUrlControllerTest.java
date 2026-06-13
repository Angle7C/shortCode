package com.atangle.shortcode.web;

import com.atangle.shortcode.service.ShortUrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShortUrlController.class)
class ShortUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShortUrlService shortUrlService;

    @Test
    void shouldCreateShortUrl() throws Exception {
        when(shortUrlService.createShortUrl(eq("https://example.com"), eq(7), eq("tester")))
                .thenReturn(new ShortUrlService.CreateShortUrlResult(
                        "Abc12345",
                        "https://example.com",
                        "code_01",
                        "short_url_mapping_01"
                ));

        mockMvc.perform(post("/short-urls")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "originUrl": "https://example.com",
                                  "expireDays": 7,
                                  "creator": "tester"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.shortCode").value("Abc12345"))
                .andExpect(jsonPath("$.data.originUrl").value("https://example.com"));
    }

    @Test
    void shouldRedirectToOriginUrl() throws Exception {
        when(shortUrlService.getOriginUrlAndIncreaseAccessCount("Abc12345"))
                .thenReturn("https://example.com");

        mockMvc.perform(get("/short-urls/Abc12345"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }
}
