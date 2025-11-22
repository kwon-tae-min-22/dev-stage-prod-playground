package com.example.demo.board;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PostService {

	private final PostRepository postRepository;
	private final OperatorUserRepository operatorUserRepository;
	private final OperatorRepository operatorRepository;
	private final JdbcTemplate jdbcTemplate;
	private static final Pattern EMAIL_SAFE = Pattern.compile("[^a-zA-Z0-9]");

	public PostService(PostRepository postRepository,
		OperatorUserRepository operatorUserRepository,
		OperatorRepository operatorRepository,
		JdbcTemplate jdbcTemplate) {
		this.postRepository = postRepository;
		this.operatorUserRepository = operatorUserRepository;
		this.operatorRepository = operatorRepository;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional(readOnly = true)
	public List<Post> findAll() {
		verifyDbConnection();
		return postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
	}

	@Transactional(readOnly = true)
	public Post findById(UUID id) {
		return postRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
	}

	public Post create(PostForm form) {
		OperatorUser operatorUser = resolveAuthor(form.getAuthor());
		Post post = new Post(form.getTitle(), operatorUser, form.getContent());
		return postRepository.save(post);
	}

	public Post update(UUID id, PostForm form) {
		Post post = findById(id);
		OperatorUser operatorUser = resolveAuthor(form.getAuthor());
		post.update(form.getTitle(), operatorUser, form.getContent());
		return post;
	}

	public void delete(UUID id) {
		Post post = findById(id);
		postRepository.delete(post);
	}

	private void verifyDbConnection() {
		try {
			jdbcTemplate.queryForObject("SELECT 1", Integer.class);
		} catch (DataAccessException ex) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "데이터베이스 연결에 실패했습니다.", ex);
		}
	}

	private OperatorUser resolveAuthor(String author) {
		return operatorUserRepository.findByUsername(author)
			.orElseGet(() -> operatorUserRepository.save(buildOperatorUser(author)));
	}

	private OperatorUser buildOperatorUser(String author) {
		String normalized = EMAIL_SAFE.matcher(author.trim().toLowerCase()).replaceAll(".");
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		String localPart = (normalized.isEmpty() ? "user" : normalized);
		if (localPart.length() > 40) {
			localPart = localPart.substring(0, 40);
		}
		String email = localPart + "+board-" + suffix + "@example.local";

		Operator operator = operatorRepository.findTopByOrderByCreatedAtAsc().orElse(null);
		return new OperatorUser(operator, author, email, "generated-by-board-app");
	}
}
