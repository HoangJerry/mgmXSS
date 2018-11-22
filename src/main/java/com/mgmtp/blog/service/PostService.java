package com.mgmtp.blog.service;

import com.mgmtp.blog.model.Post;
import java.util.List;

public interface PostService {
	
	List<Post> findAll();
	
	List<Post> findByTitle(String title);
	
	Post findById(String id);

	boolean addPost(Post post);
	
}