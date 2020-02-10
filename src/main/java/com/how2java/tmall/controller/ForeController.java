package com.how2java.tmall.controller;

import com.how2java.tmall.pojo.*;
import com.how2java.tmall.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("")
public class ForeController {
    @Autowired
    ProductService productService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    ReviewService reviewService;

    @RequestMapping("/forehome")
    public String home(Model model) {
        List<Category> cs = categoryService.list();
        productService.fill(cs);
        productService.fillByRow(cs);

//        for(Category c : cs) {
//            List<Product> ps = productService.list(c.getId());
//            productService.setFirstProductImage(ps);
//        }

        model.addAttribute("cs", cs);
        return "fore/home";
    }

    @RequestMapping("/foreregister")
    public String register(Model model, User user) {
        String name = user.getName();
        name = HtmlUtils.htmlEscape(name); //把帐号里的特殊符号进行转义，防止恶意注册
        user.setName(name);
        if(userService.isExist(name)) {
            String msg = "用户名已被使用";
            model.addAttribute("msg", msg);
            model.addAttribute("user", null);
            return "fore/register";
        }
        userService.add(user);
        return "redirect:registerSuccessPage";
    }

    @RequestMapping("/forelogin")
    public String login(@RequestParam("name") String name,
                        @RequestParam("password") String password, Model model, HttpSession session) {
        name = HtmlUtils.htmlEscape(name);
        User user = userService.get(name, password);
        if(user == null) {
            model.addAttribute("msg", "帐号或密码错误");
            return "fore/login";
        }
        session.setAttribute("user", user);
        return "redirect:forehome";
    }

    @RequestMapping("/forelogout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:forehome";
    }

    @RequestMapping("/foreproduct")
    public String product(Model model, int pid) {
        Product p = productService.get(pid);

        List<ProductImage> productSingleImages = productImageService.list(p.getId(), ProductImageService.type_single);
        List<ProductImage> productDetailImages = productImageService.list(p.getId(), ProductImageService.type_detail);
        p.setProductSingleImages(productSingleImages);
        p.setProductDetailImages(productDetailImages);
//        p.setFirstProductImage(productSingleImages.get(0));

        List<PropertyValue> pvs = propertyValueService.list(p.getId());
        List<Review> rs = reviewService.list(p.getId());
        productService.setSaleAndReviewNumber(p);

        model.addAttribute("reviews", rs);
        model.addAttribute("p", p);
        model.addAttribute("pvs", pvs);

        return "fore/product";
    }
}
