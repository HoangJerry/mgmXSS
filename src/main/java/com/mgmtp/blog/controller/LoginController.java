package com.mgmtp.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.mgmtp.blog.model.*;
import com.mgmtp.blog.service.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class LoginController {
	
	@Autowired
	SessionService sessionService;
	
	@Autowired
	CaptchaService captchaService;
	
	@Autowired
	PasswordService passwordService;
	
	@RequestMapping(value = "/login", method = RequestMethod.GET)
    public String showLoginPage(Model model, HttpServletRequest request) {
		Cookie loginCookie = sessionService.checkLoginCookie(request);
		
        if (loginCookie != null)
        		if (!sessionService.checkSessionId(loginCookie.getValue()).isEmpty()) 
        			return "redirect:/home";
        return "login";
    }
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String showHomePage(HttpServletRequest request, 
			HttpServletResponse response, Model model) {
		String username = request.getParameter("username");  
		String password = request.getParameter("password"); 
		String g_recaptcha_response = request.getParameter("g-recaptcha-response");
		

		if(!captchaService.verifyResponse(g_recaptcha_response)) {
			model.addAttribute("errorMessage", "You're going too fast");
			return "login";
				
		}
		
		boolean isValidUser =  userService.validateUser(username, password);
		
		if (!isValidUser) {
			model.addAttribute("errorMessage", "Invalid Credentials");
            return "login";
        }
		
		setSessionCookie(request, response, username);
		return "redirect:/home";
    }
	
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String LogoutPage(HttpServletRequest request, HttpServletResponse response) {
		Cookie loginCookie =  sessionService.checkLoginCookie(request);
		if (loginCookie != null) {
            loginCookie.setMaxAge(0);
            response.addCookie(loginCookie);
            sessionService.delBySessionId(loginCookie.getValue());
        }	
		return "redirect:/";
	}
	
	@Autowired
    UserService userService;
	@RequestMapping("/home")
    public String home(Model model, HttpServletRequest request) {
		Cookie loginCookie = sessionService.checkLoginCookie(request);
		List<Session> sessions;
		if (loginCookie != null) {
			sessions = sessionService.checkSessionId(loginCookie.getValue());
    			if (!sessions.isEmpty()) {
    				model.addAttribute("username", sessions.get(0).getUsername());	
	    			List<User> users = (List<User>) userService.findAll();
	    			model.addAttribute("users", users);
	    	        return "home";
    			}
		}
		return "redirect:/";
    }
	
	@RequestMapping(value = "/signup", method = RequestMethod.GET)
    public String showRegisterPage(Model model, HttpServletRequest request) {
		Cookie loginCookie = sessionService.checkLoginCookie(request);
		
        if (loginCookie != null)
        		if (!sessionService.checkSessionId(loginCookie.getValue()).isEmpty()) 
        			return "redirect:/home";
        return "signup";
    }
	
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public String register(HttpServletRequest request, 
			HttpServletResponse response, Model model) {
		String username = request.getParameter("username");  
		String password = request.getParameter("password"); 
		String firstname = request.getParameter("firstname"); 
		String lastname = request.getParameter("lastname"); 
		String g_recaptcha_response = request.getParameter("g-recaptcha-response");

		if(!captchaService.verifyResponse(g_recaptcha_response)) {
			model.addAttribute("errorMessage", "You're going too fast");
			return "signup";
				
		}

		if (!userService.findByUsername(username).isEmpty()) {
			model.addAttribute("errorMessage", "User is existed");
            return "signup";
        }
		
		String salt = passwordService.getRandomString(8);
		User user = new User (username, password, firstname, lastname);
		
		user.setPassword(passwordService.pbkdf2(password, salt));
		user.setSalt(salt);
			
		if (!userService.addUser(user, salt)) {
			model.addAttribute("errorMessage", "Cannot register");
            return "signup";
		}
		
		setSessionCookie(request, response, username);
		return "redirect:/home";
	}
	
	private void setSessionCookie(HttpServletRequest request, 
			HttpServletResponse response, String username) {

		String sessionid = "";
		
		sessionid = sessionService.getRandomSessionId();			
		
		Cookie loginCookie = new Cookie("SESSIONID", sessionid);
		response.addCookie(loginCookie);
		sessionService.addSession(username, sessionid);
	}
	
}
