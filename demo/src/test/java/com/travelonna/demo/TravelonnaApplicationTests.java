package com.travelonna.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;

import com.travelonna.demo.global.security.jwt.JwtTokenProvider;

@SpringBootTest
@ActiveProfiles("test")
class TravelonnaApplicationTests {

	@MockBean
	private JwtTokenProvider jwtTokenProvider;

	@MockBean
	private UserDetailsService userDetailsService;

	@Test
	void contextLoads() {
	}

}
