package com.test.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.test.service.UserService;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);
	
	@Autowired JwtProvider authProvider;
	
	@Autowired UserService userService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try{
			String jwtToken=getJwt(request);
			System.out.println(jwtToken);
			if(jwtToken!=null && authProvider.ValidateJwtToken(jwtToken)){
				String username = authProvider.getUserNameFromJwtToken(jwtToken);

				UserDetails userDetails = userService.loadUserByUsername(username);
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(authentication);

			}
			
		}catch(Exception e){
			logger.error("Can NOT set user authentication -> Message: {}", e);
			
		}
		filterChain.doFilter(request, response);
		
	}

	private String getJwt(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		System.out.println(authHeader);
		if (authHeader != null && authHeader.startsWith("Bearer")) {
			return authHeader.replace("Bearer ", "");
			
		}

		return null;

	}

}
