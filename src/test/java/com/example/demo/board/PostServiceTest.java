package com.example.demo.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.example.demo.board.mapper.OperatorUserMapper;
import com.example.demo.board.mapper.PostMapper;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PostService.class)
class PostServiceTest {

	@MockitoBean
	private PostMapper postMapper;

	@MockitoBean
	private OperatorUserMapper operatorUserMapper;

	@MockitoBean
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PostService postService;

	@Test
	void createPostPersistsEntity() {
		OperatorUser author = new OperatorUser("홍길동", "hong@example.local", "pw");
		given(operatorUserMapper.selectByUsername("홍길동")).willReturn(author);

		UUID id = UUID.randomUUID();
		Post persisted = new Post("첫 게시글", author, "내용입니다");
		setField(persisted, "id", id);
		setTimestamps(persisted);
		given(postMapper.insert(any(Post.class))).willReturn(persisted);

		Post saved = postService.create(new PostForm("첫 게시글", "홍길동", "hong@example.local", "내용입니다"));

		assertThat(saved.getId()).isEqualTo(id);
		assertThat(saved.getAuthor()).isEqualTo("홍길동");
		then(postMapper).should().insert(any(Post.class));
	}

	@Test
	void findAllReturnsLatestFirst() {
		Post newest = samplePost("new");
		Post older = samplePost("old");
		given(postMapper.selectAll()).willReturn(List.of(newest, older));
		given(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).willReturn(1);

		List<Post> posts = postService.findAll();

		assertThat(posts).hasSize(2);
		assertThat(posts.get(0).getTitle()).isEqualTo("new");
		then(postMapper).should().selectAll();
		then(jdbcTemplate).should().queryForObject("SELECT 1", Integer.class);
	}

	@Test
	void updateChangesFields() {
		Post existing = samplePost("제목");
		UUID id = existing.getId();
		OperatorUser newAuthor = new OperatorUser("다른 작성자", "other@example.local", "pw");
		given(operatorUserMapper.selectByUsername("다른 작성자")).willReturn(newAuthor);
		Post updated = new Post("새 제목", newAuthor, "새 내용");
		setField(updated, "id", id);
		setTimestamps(updated);
		given(postMapper.update(any(Post.class))).willReturn(updated);

		Post result = postService.update(id, new PostForm("새 제목", "다른 작성자", "other@example.local", "새 내용"));

		assertThat(result.getTitle()).isEqualTo("새 제목");
		assertThat(result.getAuthor()).isEqualTo("다른 작성자");
		assertThat(result.getContent()).isEqualTo("새 내용");
	}

	@Test
	void deleteRemovesPost() {
		Post existing = samplePost("삭제");
		given(postMapper.delete(existing.getId())).willReturn(1);

		postService.delete(existing.getId());

		then(postMapper).should().delete(existing.getId());
	}

	@Test
	void findByIdThrowsWhenMissing() {
		UUID missing = UUID.randomUUID();
		given(postMapper.selectById(missing)).willReturn(null);

		assertThatThrownBy(() -> postService.findById(missing))
			.isInstanceOf(ResponseStatusException.class);
	}

	private Post samplePost(String title) {
		OperatorUser author = new OperatorUser("작성자", "author@example.local", "pw");
		Post post = new Post(title, author, "내용");
		setField(post, "id", UUID.randomUUID());
		setTimestamps(post);
		return post;
	}

	private void setTimestamps(Post post) {
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		setField(post, "createdAt", now);
		setField(post, "updatedAt", now);
	}

	private void setField(Object target, String fieldName, Object value) {
		var field = ReflectionUtils.findField(target.getClass(), fieldName);
		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, target, value);
	}
}
