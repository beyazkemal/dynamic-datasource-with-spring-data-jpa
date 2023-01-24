package com.kemalbeyaz.dynamic.datasource.config;

import org.slf4j.MDC;
import java.util.Objects;

public class TenantContextHolder {

    private static final InheritableThreadLocal<String> TENANT_KEY = new InheritableThreadLocal<>();

    public static void setTenantKey(String tenantKey) {
        if (Objects.isNull(tenantKey)) {
            tenantKey = ConfigurationConstants.DEFAULT_TENANT_KEY;
        }

        TENANT_KEY.set(tenantKey);
        MDC.put("tenantKey", tenantKey);
    }

    public static String getTenantKey() {
        return TENANT_KEY.get();
    }

    public static void clear() {
        TENANT_KEY.remove();
    }

}
