package com.example.demo.board;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.demo.board.mapper.OperatorUserMapper;
import com.example.demo.board.mapper.PostMapper;
import com.example.demo.config.SecurityConfig;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ReflectionUtils;

@WebMvcTest(BoardController.class)
@Import(SecurityConfig.class)
class BoardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PostService postService;

	@MockitoBean
	private PostMapper postMapper;

	@MockitoBean
	private OperatorUserMapper operatorUserMapper;

	@Test
	void listRendersPostsTemplate() throws Exception {
		given(postService.findAll()).willReturn(List.of(samplePost(UUID.randomUUID())));

		mockMvc.perform(get("/posts"))
			.andExpect(status().isOk())
			.andExpect(view().name("posts/list"))
			.andExpect(model().attributeExists("posts"));
	}

	@Test
	void showDetailDisplaysPost() throws Exception {
		UUID id = UUID.randomUUID();
		given(postService.findById(id)).willReturn(samplePost(id));

		mockMvc.perform(get("/posts/{id}", id))
			.andExpect(status().isOk())
			.andExpect(view().name("posts/detail"))
			.andExpect(model().attributeExists("post"));
	}

	@Test
	void createPostValidDataRedirectsToList() throws Exception {
		given(postService.create(any(PostForm.class)))
			.willReturn(samplePost(UUID.randomUUID()));

		mockMvc.perform(post("/posts")
				.param("title", "제목")
				.param("author", "작성자")
				.param("email", "author@example.local")
				.param("content", "내용")
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/posts"))
			.andExpect(flash().attribute("message", containsString("등록")));

		then(postService).should().create(argThat(form ->
			"제목".equals(form.getTitle()) &&
				"작성자".equals(form.getAuthor()) &&
				"author@example.local".equals(form.getEmail()) &&
				"내용".equals(form.getContent())));
	}

	@Test
	void createPostValidationErrorReturnsForm() throws Exception {
		mockMvc.perform(post("/posts").with(csrf()))
			.andExpect(status().isOk())
			.andExpect(view().name("posts/form"))
			.andExpect(model().attributeHasFieldErrors("postForm", "title", "author", "email", "content"));
	}

	@Test
	void deletePostRedirectsToList() throws Exception {
		UUID id = UUID.randomUUID();

		mockMvc.perform(post("/posts/{id}/delete", id).with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/posts"));

		then(postService).should().delete(id);
	}

	private Post samplePost(UUID id) {
		OperatorUser author = new OperatorUser("사용자", "user@example.local", "pw");
		Post post = new Post("테스트", author, "본문");
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		setField("id", post, id);
		setField("createdAt", post, now);
		setField("updatedAt", post, now);
		return post;
	}

	private void setField(String fieldName, Post target, Object value) {
		var field = ReflectionUtils.findField(Post.class, fieldName);
		ReflectionUtils.makeAccessible(field);
		ReflectionUtils.setField(field, target, value);
	}
}
