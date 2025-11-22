package com.example.demo.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

	@Mock
	private PostRepository postRepository;

	@Mock
	private OperatorUserRepository operatorUserRepository;

	@Mock
	private OperatorRepository operatorRepository;

	@Mock
	private JdbcTemplate jdbcTemplate;

	@InjectMocks
	private PostService postService;

	@Test
	void createPostPersistsEntity() {
		OperatorUser author = new OperatorUser(null, "홍길동", "hong@example.local", "pw");
		given(operatorUserRepository.findByUsername("홍길동")).willReturn(Optional.of(author));

		UUID id = UUID.randomUUID();
		Post persisted = new Post("첫 게시글", author, "내용입니다");
		setField(persisted, "id", id);
		setTimestamps(persisted);
		given(postRepository.save(any(Post.class))).willReturn(persisted);

		Post saved = postService.create(new PostForm("첫 게시글", "홍길동", "내용입니다"));

		assertThat(saved.getId()).isEqualTo(id);
		assertThat(saved.getAuthor()).isEqualTo("홍길동");
		verify(postRepository).save(any(Post.class));
	}

	@Test
	void findAllReturnsLatestFirst() {
		Post newest = samplePost("new");
		Post older = samplePost("old");
		Sort expectedSort = Sort.by(Sort.Direction.DESC, "createdAt");
		given(postRepository.findAll(expectedSort)).willReturn(List.of(newest, older));
		given(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).willReturn(1);

		List<Post> posts = postService.findAll();

		assertThat(posts).hasSize(2);
		assertThat(posts.get(0).getTitle()).isEqualTo("new");
		verify(postRepository).findAll(expectedSort);
		verify(jdbcTemplate).queryForObject("SELECT 1", Integer.class);
	}

	@Test
	void updateChangesFields() {
		Post existing = samplePost("제목");
		UUID id = existing.getId();
		given(postRepository.findById(id)).willReturn(Optional.of(existing));
		OperatorUser newAuthor = new OperatorUser(null, "다른 작성자", "other@example.local", "pw");
		given(operatorUserRepository.findByUsername("다른 작성자")).willReturn(Optional.of(newAuthor));

		postService.update(id, new PostForm("새 제목", "다른 작성자", "새 내용"));

		assertThat(existing.getTitle()).isEqualTo("새 제목");
		assertThat(existing.getAuthor()).isEqualTo("다른 작성자");
		assertThat(existing.getContent()).isEqualTo("새 내용");
	}

	@Test
	void deleteRemovesPost() {
		Post existing = samplePost("삭제");
		given(postRepository.findById(existing.getId())).willReturn(Optional.of(existing));

		postService.delete(existing.getId());

		verify(postRepository).delete(existing);
	}

	@Test
	void findByIdThrowsWhenMissing() {
		UUID missing = UUID.randomUUID();
		given(postRepository.findById(missing)).willReturn(Optional.empty());

		assertThatThrownBy(() -> postService.findById(missing))
			.isInstanceOf(ResponseStatusException.class);
	}

	private Post samplePost(String title) {
		OperatorUser author = new OperatorUser(null, "작성자", "author@example.local", "pw");
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
		try {
			var field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(target, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
