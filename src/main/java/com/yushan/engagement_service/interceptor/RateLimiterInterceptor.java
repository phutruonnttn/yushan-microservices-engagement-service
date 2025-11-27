package com.yushan.engagement_service.interceptor;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * Interceptor to handle @RateLimiter annotation using RateLimiterRegistry
 * Similar to how API Gateway implements rate limiting
 */
@Component
@Slf4j
public class RateLimiterInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // Check if method has @RateLimiter annotation
        io.github.resilience4j.ratelimiter.annotation.RateLimiter rateLimiterAnnotation = 
            method.getAnnotation(io.github.resilience4j.ratelimiter.annotation.RateLimiter.class);

        if (rateLimiterAnnotation == null) {
            return true; // No rate limiting for this method
        }

        String rateLimiterName = rateLimiterAnnotation.name();
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterName);

        if (rateLimiter == null) {
            log.warn("Rate limiter '{}' not found in registry, proceeding without rate limiting", rateLimiterName);
            return true;
        }

        // Try to acquire permission
        boolean permitAcquired = rateLimiter.acquirePermission();
        
        if (!permitAcquired) {
            log.warn("Rate limit exceeded for rate limiter: {} on method: {}", 
                    rateLimiterName, method.getName());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Please try again later.\", \"status\": 429}");
            return false; // Stop request processing
        }

        log.debug("Rate limiter permit acquired for: {} on method: {}", 
                 rateLimiterName, method.getName());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // No-op
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // No-op
    }
}

