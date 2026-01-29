package com.example.demo.board;

import com.example.demo.board.mapper.OperatorUserMapper;
import com.example.demo.board.mapper.PostMapper;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PostService {

	private final PostMapper postMapper;
	private final OperatorUserMapper operatorUserMapper;
	private final JdbcTemplate jdbcTemplate;
	private static final Pattern EMAIL_SAFE = Pattern.compile("[^a-zA-Z0-9]");

	public PostService(PostMapper postMapper,
		OperatorUserMapper operatorUserMapper,
		JdbcTemplate jdbcTemplate) {
		this.postMapper = postMapper;
		this.operatorUserMapper = operatorUserMapper;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional(readOnly = true)
	public List<Post> findAll() {
		verifyDbConnection();
		return postMapper.selectAll();
	}

	@Transactional(readOnly = true)
	public Post findById(UUID id) {
		Post post = postMapper.selectById(id);
		if (post == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
		}
		return post;
	}

	public Post create(PostForm form) {
		OperatorUser operatorUser = resolveAuthor(form.getAuthor(), form.getEmail());
		Post post = new Post(form.getTitle(), operatorUser, form.getContent());
		return postMapper.insert(post);
	}

	public Post update(UUID id, PostForm form) {
		OperatorUser operatorUser = resolveAuthor(form.getAuthor(), form.getEmail());
		Post toUpdate = new Post(form.getTitle(), operatorUser, form.getContent());
		toUpdate.setId(id);
		Post updated = postMapper.update(toUpdate);
		if (updated == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
		}
		return updated;
	}

	public void delete(UUID id) {
		int deleted = postMapper.delete(id);
		if (deleted == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
		}
	}

	private void verifyDbConnection() {
		try {
			jdbcTemplate.queryForObject("SELECT 1", Integer.class);
		} catch (DataAccessException ex) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "데이터베이스 연결에 실패했습니다.", ex);
		}
	}

	private OperatorUser resolveAuthor(String author) {
		OperatorUser existing = operatorUserMapper.selectByUsername(author);
		if (existing != null) {
			return existing;
		}
		return operatorUserMapper.insert(buildOperatorUser(author));
	}

	private OperatorUser resolveAuthor(String author, String email) {
		String normalizedEmail = normalizeEmail(email);
		OperatorUser existing = operatorUserMapper.selectByUsername(author);
		if (existing != null) {
			if (shouldUpdateEmail(existing, normalizedEmail)) {
				operatorUserMapper.updateEmail(existing.getId(), normalizedEmail);
				existing.setEmail(normalizedEmail);
			}
			return existing;
		}
		return operatorUserMapper.insert(buildOperatorUser(author, normalizedEmail));
	}

	private OperatorUser buildOperatorUser(String author) {
		String resolvedEmail = generateEmail(author);
		return new OperatorUser(author, resolvedEmail, "generated-by-board-app");
	}

	private OperatorUser buildOperatorUser(String author, String email) {
		String resolvedEmail = (email == null || email.isBlank()) ? generateEmail(author) : email;
		return new OperatorUser(author, resolvedEmail, "generated-by-board-app");
	}

	private String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}
		String trimmed = email.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private boolean shouldUpdateEmail(OperatorUser existing, String email) {
		if (email == null || existing == null) {
			return false;
		}
		if (existing.getId() == null) {
			return false;
		}
		String current = existing.getEmail();
		return current == null || !current.equals(email);
	}

	private String generateEmail(String author) {
		String normalized = EMAIL_SAFE.matcher(author.trim().toLowerCase()).replaceAll(".");
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		String localPart = (normalized.isEmpty() ? "user" : normalized);
		if (localPart.length() > 40) {
			localPart = localPart.substring(0, 40);
		}
		return localPart + "+board-" + suffix + "@example.local";
	}
}
