package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.UserService;

@Controller
public class ViewController {
    private final UserService users;
    public ViewController(UserService users) { this.users = users; }
    @GetMapping("/admin") public String admin(Authentication authentication, Model model) {
        model.addAttribute("authUser", users.findByEmail(authentication.getName())); return "admin";
    }
    @GetMapping("/user") public String user(Authentication authentication, Model model) {
        User user = users.findByEmail(authentication.getName()); model.addAttribute("authUser", user); return "user";
    }
}
