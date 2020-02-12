package com.how2java.tmall.interceptor;

import com.how2java.tmall.pojo.OrderItem;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.service.CategoryService;
import com.how2java.tmall.service.OrderItemService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;

public class LoginInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    CategoryService categoryService;

    //无需登录的页面
    private String[] noNeedAuthPage = {"home", "checkLogin", "register", "loginAjax",
            "login", "product", "category", "search"};

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String contextPath = session.getServletContext().getContextPath();
//        System.out.println(contextPath);
        String uri = request.getRequestURI();
//        System.out.println(uri);
        uri = StringUtils.remove(uri, contextPath);
//        System.out.println(uri);
        if(uri.startsWith("/fore")) {
            String operator = StringUtils.substringAfterLast(uri, "/fore");
            //检查此页面是否需要登录
            if(!Arrays.asList(noNeedAuthPage).contains(operator)) {
                User user = (User) session.getAttribute("user");
                if(user == null) {
                    response.sendRedirect("loginPage");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        super.afterCompletion(request, response, handler, ex);
    }
}
