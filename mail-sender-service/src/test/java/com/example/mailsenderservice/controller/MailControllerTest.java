package com.example.mailsenderservice.controller;

import com.example.mailsenderservice.exc.SecureCodeException;
import com.example.mailsenderservice.gen.MessageGenerator;
import com.example.mailsenderservice.model.SecureCode;
import com.example.mailsenderservice.service.MailSender;
import com.example.mailsenderservice.service.SecureCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MailController.class)
@ExtendWith(SpringExtension.class)
class MailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MailSender mailSender;

    @MockBean
    private SecureCodeService service;

    @MockBean
    private MessageGenerator messageGenerator;

    @Test
    void sendToEmail() throws Exception {
        String email = "fursov@gmail.com";
        SecureCode secureCode = new SecureCode();
        secureCode.setReceiverEmail(email);
        Mockito.when(messageGenerator.generate(email)).thenReturn(secureCode);
        mockMvc.perform(post("/security-code/send/to/" + email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiverEmail").value(email));
    }

    @Test
    void getCodeByEmail() throws Exception {
        String email = "fursov@gmail.com";
        SecureCode secureCode = new SecureCode();
        secureCode.setReceiverEmail(email);
        Mockito.when(service.findByReceiverEmail(email)).thenReturn(secureCode);

        String request = "/security-code/" + email;

        mockMvc.perform(get(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiverEmail").value(email));

        Mockito.when(service.findByReceiverEmail(email)).thenThrow(SecureCodeException.class);

            mockMvc.perform(get(request))
                    .andExpect(status().isNotFound());
    }

}