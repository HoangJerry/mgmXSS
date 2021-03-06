package com.mgmtp.blog.controller;

import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.mgmtp.blog.model.Comment;
import com.mgmtp.blog.model.Post;
import com.mgmtp.blog.model.Session;
import com.mgmtp.blog.model.User;
import com.mgmtp.blog.service.CommentService;
import com.mgmtp.blog.service.PostService;
import com.mgmtp.blog.service.SessionService;
import com.mgmtp.blog.service.UserService;

@Controller
public class PostController {


	@Autowired
	PostService postService;
	
	@Autowired
	CommentService commentService;

	@Autowired
	UserService userService;
	
	@Autowired
	SessionService sessionService;

	@RequestMapping(value = "/post", method = RequestMethod.GET)
	public String getPostDetail(Model model, HttpServletRequest request) {
		String id = request.getParameter("id");

		// redirect to index with empty query
		if (id.length() == 0)
			return "redirect:/";

		Post post;

		post = postService.findById(id);

		model.addAttribute("post", post);

		List<Comment> comments = commentService.findAllByPostId(id);
		model.addAttribute("comments", comments);

		// check session
		Cookie loginCookie = sessionService.checkLoginCookie(request);
		if (loginCookie != null)
			sessionService.checkSessionId(loginCookie.getValue());
		return "blog-post";
	}
	
	@RequestMapping(value = "/post", method = RequestMethod.POST)
	public String showHomePage(HttpServletRequest request, 
			HttpServletResponse response, Model model) {
		String postTitle = request.getParameter("post-title"); 
		String postContent = request.getParameter("post-content"); 
		Cookie loginCookie = sessionService.checkLoginCookie(request);
		List<Session> sessions;
		if (loginCookie != null) {
			sessions = sessionService.checkSessionId(loginCookie.getValue());
    			if (!sessions.isEmpty()) {
    				List<User> users = userService.findByUsername(sessions.get(0).getUsername());
    				Post post = new Post(postTitle, postContent, users.get(0));
    				if(postService.addPost(post)) {
						model.addAttribute("isSuccess", true);
						return "redirect:/";
    				}
    				else {
						model.addAttribute("isSuccess", false);
						return "redirect:/home";
    				}
	    	        
    			}
		}
		return "redirect:/";
	}
	
	@RequestMapping(value = "/post", params = "id", method = RequestMethod.POST)
	public String getPostDetail(HttpServletRequest request, 
			HttpServletResponse response, Model model) {
		String id = request.getParameter("id"); 
		String comment = request.getParameter("comment"); 
		String author = request.getParameter("author"); 
		if(comment.isEmpty() || author.isEmpty()) 
			return "redirect:/post?id="+id;
		Post post = postService.findById(id);
		Comment c = new Comment(author, comment, post);
		commentService.addComment(c);
		return "redirect:/post?id="+id;
	}
}
