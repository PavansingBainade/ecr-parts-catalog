package com.ecrtracker.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        httpResponse.setHeader(
                "Access-Control-Allow-Origin",
                "http://localhost:5173"
        );

        httpResponse.setHeader(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );

        httpResponse.setHeader(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization"
        );

        // Handle browser preflight request
        if ("OPTIONS".equalsIgnoreCase(
                ((javax.servlet.http.HttpServletRequest) request)
                        .getMethod())) {

            httpResponse.setStatus(
                    HttpServletResponse.SC_OK
            );

            return;
        }

        chain.doFilter(request, response);
    }
}