package com.pkg.balance.mgmt.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.util.Enumeration;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 使用 CachingRequestWrapper 包装原始请求
        HttpServletRequest wrappedRequest = new CachingRequestWrapper(request);

        // 打印请求头信息
        System.out.println("Request URL: " + wrappedRequest.getRequestURL());
        System.out.println("Request Method: " + wrappedRequest.getMethod());
        Enumeration<String> headerNames = wrappedRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println("Header Name: " + headerName + ", Value: " + wrappedRequest.getHeader(headerName));
        }

        // 打印请求体信息
        StringBuilder requestBody = new StringBuilder();
        BufferedReader reader = wrappedRequest.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        System.out.println("Request Body: " + requestBody.toString());

        // 返回包装后的请求对象
        return true; // 继续处理请求
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 处理完成后的方法
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后的处理
    }
}
