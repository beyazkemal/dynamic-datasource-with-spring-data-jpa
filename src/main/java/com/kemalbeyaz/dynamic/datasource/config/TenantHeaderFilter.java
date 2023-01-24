package com.kemalbeyaz.dynamic.datasource.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(-101)
public class TenantHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantKey = httpRequest.getHeader(ConfigurationConstants.TENANT_HEADER_KEY);
        TenantContextHolder.setTenantKey(tenantKey);
        chain.doFilter(request, response);
    }
}
