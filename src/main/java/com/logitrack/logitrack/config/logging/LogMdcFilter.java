package com.logitrack.logitrack.config.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class LogMdcFilter extends OncePerRequestFilter {

    // تعريف الـ Logger
    private static final Logger logger = LoggerFactory.getLogger(LogMdcFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. إنشاء Trace ID لربط جميع الـ Logs الخاصة بنفس الطلب
            MDC.put("trace_id", UUID.randomUUID().toString());

            // 2. إضافة معلومات الطلب الأساسية
            MDC.put("http_method", request.getMethod());
            MDC.put("http_endpoint", request.getRequestURI());

            // 3. جلب معلومات المستخدم (تصحيح المنطق هنا)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // نتحقق أن المستخدم موجود وموثق وأنه ليس "anonymousUser"
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                MDC.put("user_name", auth.getName());
                // تحويل الصلاحيات إلى String بشكل آمن
                MDC.put("user_role", auth.getAuthorities().toString());
            } else {
                MDC.put("user_name", "anonymous");
                MDC.put("user_role", "GUEST");
            }

            // تنفيذ الطلب
            filterChain.doFilter(request, response);

            // 4. إضافة حالة الاستجابة (تتم بعد انتهاء الطلب)
            MDC.put("http_status", String.valueOf(response.getStatus()));

            // (اختياري) تسجيل سطر Log يوضح انتهاء الطلب وحالته
            logger.info("Request completed: {} {} - Status: {}", request.getMethod(), request.getRequestURI(), response.getStatus());

        } finally {
            // 5. تنظيف الـ MDC ضروري جداً لتجنب تسرب البيانات لطلبات أخرى
            MDC.clear();
        }
    }
}