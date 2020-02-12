package com.how2java.tmall.controller;

import com.github.pagehelper.PageHelper;
import com.how2java.tmall.comparator.*;
import com.how2java.tmall.pojo.*;
import com.how2java.tmall.service.*;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    OrderService orderService;

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
                        @RequestParam("password") String password,
                        Model model, HttpSession session) {
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
    public String product(Model model,
                          @RequestParam("pid") int pid) {
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

    @RequestMapping("/forecheckLogin")
    @ResponseBody
    public String checkLogin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user != null)
            return "success";
        return "fail";
    }

    @RequestMapping("/foreloginAjax")
    @ResponseBody
    public String loginAjax(@RequestParam("name") String name,
                            @RequestParam("password") String password,
                            HttpSession session) {
        name = HtmlUtils.htmlEscape(name);
        User user = userService.get(name, password);
        if(user == null)
            return "fail";
        session.setAttribute("user", user);
        return "success";
    }

    @RequestMapping("/forecategory")
    public String category(@RequestParam("cid") int cid,
                           String sort,
                           Model model) {
        Category c = categoryService.get(cid);
//        List<Product> ps = productService.list(c.getId());
        productService.fill(c);
        productService.setSaleAndReviewNumber(c.getProducts());

        if(sort != null) {
            switch (sort) {
                case "review":
                    Collections.sort(c.getProducts(), new ProductReviewComparator());
                    break;
                case "date":
                    Collections.sort(c.getProducts(), new ProductDateComparator());
                    break;
                case "saleCount":
                    Collections.sort(c.getProducts(), new ProductSaleCountComparator());
                    break;
                case "price":
                    Collections.sort(c.getProducts(), new ProductPriceComparator());
                    break;
                case "all":
                    Collections.sort(c.getProducts(), new ProductAllComparator());
                    break;
            }
        }

        model.addAttribute("c", c);
        return "fore/category";
    }

    @RequestMapping("/foresearch")
    public String search(String keyword, Model model) {
        PageHelper.offsetPage(0, 20);
        List<Product> ps = productService.search(keyword);
        productService.setSaleAndReviewNumber(ps);
        model.addAttribute("ps", ps);
        return "fore/searchResult";
    }

    @RequestMapping("/forebuyone")
    public String buyone(int pid, int num, HttpSession session) {
        Product p = productService.get(pid);
        int oiid = 0; //订单项id

        User user = (User) session.getAttribute("user");
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        boolean found = false;
        //查看现有的订单项中是否有相同的商品
        for(OrderItem oi : ois) {
            if(oi.getProduct().getId().intValue() == p.getId().intValue()) {
                oi.setNumber(oi.getNumber() + num);
                orderItemService.update(oi);
                found = true;
                oiid = oi.getId();
                break;
            }
        }

        //如果是全新的订单项
        if(!found) {
            OrderItem oi = new OrderItem();
            oi.setUid(user.getId());
            oi.setNumber(num);
            oi.setPid(p.getId());
            orderItemService.add(oi);
            oiid = oi.getId();
        }

        return "redirect:forebuy?oiid=" + oiid;
    }

    @RequestMapping("/forebuy")
    public String buy(Model model, String[] oiid, HttpSession session) {
        List<OrderItem> ois = new ArrayList<>();
        double total = 0;

        for(String strid : oiid) {
            OrderItem oi = orderItemService.get(Integer.parseInt(strid));
            ois.add(oi);
            total += oi.getProduct().getPromotePrice() * oi.getNumber();
        }

        session.setAttribute("ois", ois);
        model.addAttribute("total", total);
        return "fore/buy";
    }

    @RequestMapping("/foreaddCart")
    @ResponseBody
    public String addCart(int pid, int num, HttpSession session) {
        Product p = productService.get(pid);

        User user = (User) session.getAttribute("user");
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        boolean found = false;
        //查看现有的订单项中是否有相同的商品
        for(OrderItem oi : ois) {
            if(oi.getProduct().getId().intValue() == p.getId().intValue()) {
                oi.setNumber(oi.getNumber() + num);
                orderItemService.update(oi);
                found = true;
                break;
            }
        }

        //如果是全新的订单项
        if(!found) {
            OrderItem oi = new OrderItem();
            oi.setUid(user.getId());
            oi.setNumber(num);
            oi.setPid(p.getId());
            orderItemService.add(oi);
        }

        return "success";
    }

    @RequestMapping("/forecart")
    public String cart(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        model.addAttribute("ois", ois);
        return "fore/cart";
    }

    @RequestMapping("/forechangeOrderItem")
    @ResponseBody
    public String changeOrderItem(int pid, int number, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null)
            return "fail";
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        for(OrderItem oi : ois) {
            //修改订单项数量
            if(oi.getPid().intValue() == pid) {
                oi.setNumber(number);
                orderItemService.update(oi);
                break;
            }
        }
        return "success";
    }

    @RequestMapping("/foredeleteOrderItem")
    @ResponseBody
    public String deleteOrderItem(int oiid, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null)
            return "fail";
        orderItemService.delete(oiid);
        return "success";
    }

    @RequestMapping("/forecreateOrder")
    public String createOrder(Order order, HttpSession session) {
        User user = (User) session.getAttribute("user");
        //生成订单号
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
        order.setOrderCode(orderCode);
        order.setCreateDate(new Date());
        order.setUid(user.getId());
        order.setStatus(OrderService.waitPay); //待支付状态

        List<OrderItem> ois = (List<OrderItem>) session.getAttribute("ois");
        double total = orderService.add(order, ois);
        return "redirect:forealipay?oid=" + order.getId() + "&total=" + total;
    }

    @RequestMapping("/forepayed")
    public String payed(Model model, int oid, double total) {
        Order order = orderService.get(oid);
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        orderService.update(order);
        model.addAttribute("o", order);
        return "fore/payed";
    }

    @RequestMapping("/forebought")
    public String bought(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        List<Order> os = orderService.list(user.getId(), OrderService.delete);

        orderItemService.fill(os);
        for(Order o : os) {
            List<OrderItem> ois = o.getOrderItems();
            System.out.println("打印订单项:");
            for(OrderItem oi : ois) {
                System.out.println(oi.getPid());
            }
        }
        System.out.println("结束");
        model.addAttribute("os", os);

        return "fore/bought";
    }

    @RequestMapping("/foreconfirmPay")
    public String confirmPay(Model model, int oid) {
        Order o = orderService.get(oid);
        orderItemService.fill(o);
        model.addAttribute("o", o);
        return "fore/confirmPay";
    }

    @RequestMapping("/foreorderConfirmed")
    public String orderConfirmed(Model model, int oid) {
        Order o = orderService.get(oid);
        o.setStatus(OrderService.waitReview);
        o.setConfirmDate(new Date());
        orderService.update(o);
        return "fore/orderConfirmed";
    }

    @RequestMapping("/foredeleteOrder")
    @ResponseBody
    public String deleteOrder(Model model, int oid) {
        Order o = orderService.get(oid);
        o.setStatus(OrderService.delete);
        orderService.update(o);
        return "success";
    }

    @RequestMapping("/forereview")
    public String review( Model model,int oid) {
        Order o = orderService.get(oid);
        orderItemService.fill(o);
        Product p = o.getOrderItems().get(0).getProduct();
        List<Review> reviews = reviewService.list(p.getId());
        productService.setSaleAndReviewNumber(p);
        model.addAttribute("p", p);
        model.addAttribute("o", o);
        model.addAttribute("reviews", reviews);
        return "fore/review";
    }

    @RequestMapping("foredoreview")
    public String doreview(HttpSession session,
                           @RequestParam("oid") int oid,
                           @RequestParam("pid") int pid,
                           String content) {
        Order o = orderService.get(oid);
        o.setStatus(OrderService.finish);

        content = HtmlUtils.htmlEscape(content);

        User user =(User)  session.getAttribute("user");
        Review review = new Review();
        review.setContent(content);
        review.setPid(pid);
        review.setCreateDate(new Date());
        review.setUid(user.getId());
        reviewService.add(review, o);

        return "redirect:forereview?oid="+oid+"&showonly=true";
    }
}
