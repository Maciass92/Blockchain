package com.example.blockchaininfo.controllers;

import com.example.blockchaininfo.model.NetworkHashrate;
import com.example.blockchaininfo.model.PoolHashrate;
import com.example.blockchaininfo.services.NetworkHashrateService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.anyLong;

public class indexControllerTest {

    indexController indexController;
    MockMvc mockMvc;

    @Mock
    NetworkHashrateService networkHashrateService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        indexController = new indexController(networkHashrateService);
        mockMvc = MockMvcBuilders.standaloneSetup(indexController).build();
    }

    @Test
    public void getMainPage() {
    }

    @Test
    public void getNetworkInfoTest() throws Exception {

        //given
        PoolHashrate poolHashrate = new PoolHashrate();

        //when
        when(networkHashrateService.getPoolHashrate(anyLong())).thenReturn(poolHashrate);

        //then
        mockMvc.perform(get("/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("poolHashrate"));

        verify(networkHashrateService, times(1)).getPoolHashrate(anyLong());
    }
}