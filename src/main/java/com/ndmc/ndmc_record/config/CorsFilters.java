//package com.ndmc.ndmc_record.config;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Component
//public class CorsFilters implements Filter {
//
//	private static final Logger LOGGER = LoggerFactory.getLogger(CorsFilters.class);
//	@Override
//	public void init(FilterConfig filterConfig) throws ServletException {
//		LOGGER.info("Initializing CORSFilter");
//	}
//
//	@Override
//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//			throws IOException, ServletException {
//		// TODO Auto-generated method stub
//		//LOGGER.info("inside do filter");
//		//System.out.println("inside do filter");
//		HttpServletRequest requestToUse = (HttpServletRequest) request;
//		HttpServletResponse responseToUse = (HttpServletResponse) response;
//		//LOGGER.info("resp to use "+responseToUse);
//		responseToUse.setHeader("Access-Control-Allow-Origin",requestToUse.getHeader("*"));
//		responseToUse.setHeader("Access-Control-Allow-Credentials", "true");
//		responseToUse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE,PUT");
//		responseToUse.setHeader("Access-Control-Max-Age", "3600");
//		responseToUse.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");
//		//LOGGER.info("res to use : "+ responseToUse);
//		chain.doFilter(requestToUse, responseToUse);
//		//LOGGER.info("exiting the do filter");
//	}
//
//	@Override
//	public void destroy() {
//		// close open resources here
//	}
//
//
//}
