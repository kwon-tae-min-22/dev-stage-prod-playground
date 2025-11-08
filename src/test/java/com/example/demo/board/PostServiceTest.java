package com.example.demo.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;

@DataJpaTest
@Import(PostService.class)
class PostServiceTest {

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private PostService postService;

	@Test
	void createPostPersistsEntity() {
		PostForm form = new PostForm("첫 게시글", "홍길동", "내용입니다");

		Post saved = postService.create(form);

		assertThat(saved.getId()).isNotNull();
		assertThat(postRepository.count()).isEqualTo(1);
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getUpdatedAt()).isNotNull();
	}

	@Test
	void findAllReturnsLatestFirst() {
		postService.create(new PostForm("old", "user", "old content"));
		postService.create(new PostForm("new", "user", "new content"));

		List<Post> posts = postService.findAll();

		assertThat(posts).hasSize(2);
		assertThat(posts.get(0).getTitle()).isEqualTo("new");
	}

	@Test
	void updateChangesFields() {
		Post saved = postService.create(new PostForm("제목", "작성자", "내용"));

		Post updated = postService.update(saved.getId(), new PostForm("새 제목", "다른 작성자", "새 내용"));

		assertThat(updated.getTitle()).isEqualTo("새 제목");
		assertThat(updated.getAuthor()).isEqualTo("다른 작성자");
		assertThat(updated.getContent()).isEqualTo("새 내용");
	}

	@Test
	void deleteRemovesPost() {
		Post saved = postService.create(new PostForm("삭제", "사용자", "내용"));

		postService.delete(saved.getId());

		assertThat(postRepository.count()).isZero();
	}

	@Test
	void findByIdThrowsWhenMissing() {
		assertThatThrownBy(() -> postService.findById(999L))
			.isInstanceOf(ResponseStatusException.class);
	}
}
