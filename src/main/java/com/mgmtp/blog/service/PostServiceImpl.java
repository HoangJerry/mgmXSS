package com.mgmtp.blog.service;

import com.mgmtp.blog.dao.PostRepository;
import com.mgmtp.blog.model.Post;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PostServiceImpl implements PostService {

	@Autowired
	private PostRepository postRepository;

	@Override
	public List<Post> findByTitle(String title) {
		List<Post> result = new ArrayList<>();
			for (Post item : postRepository.findByTitle(title)) {
				result.add(item);
			}


		return result;
	}

	@Override
	public List<Post> findAll() {
		List<Post> result = new ArrayList<>();
		for (Post item : postRepository.findAll()) {
			result.add(item);
		}
		return result;
	}

	@Override
	public boolean addPost(Post post) {
		boolean result = false;
		try {
			result = postRepository.addPost(post);
		} catch (Exception e) {
		}
		return result;
	}

	@Override
	public Post findById(String id) {
		List<Post> posts = postRepository.findById(id);
		if(posts.size()==0) {
			return null;
		}
		return posts.get(0);
	}
	
	

}