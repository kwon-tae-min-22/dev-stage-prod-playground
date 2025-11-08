package com.example.demo.board;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PostService {

	private final PostRepository postRepository;

	public PostService(PostRepository postRepository) {
		this.postRepository = postRepository;
	}

	@Transactional(readOnly = true)
	public List<Post> findAll() {
		return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
	}

	@Transactional(readOnly = true)
	public Post findById(Long id) {
		return postRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
	}

	public Post create(PostForm form) {
		Post post = new Post(form.getTitle(), form.getAuthor(), form.getContent());
		return postRepository.save(post);
	}

	public Post update(Long id, PostForm form) {
		Post post = findById(id);
		post.update(form.getTitle(), form.getAuthor(), form.getContent());
		return post;
	}

	public void delete(Long id) {
		Post post = findById(id);
		postRepository.delete(post);
	}
}
