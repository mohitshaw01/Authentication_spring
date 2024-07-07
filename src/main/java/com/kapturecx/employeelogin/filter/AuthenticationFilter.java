package com.kapturecx.employeelogin.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code, if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Example: Check if the request has a valid session or token
        String authToken = httpRequest.getHeader("Authorization");
        if (isValidSessionOrToken(authToken)) {
            // Proceed with the request
            chain.doFilter(request, response);
        } else {
            // If authentication fails, respond with an error
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Unauthorized access");
        }
    }

    private boolean isValidSessionOrToken(String authToken) {
        // Implement your logic to validate the session or token
        return authToken != null && authToken.equals("valid-token"); // Example validation
    }

    @Override
    public void destroy() {
        // Cleanup code, if needed
    }
}
