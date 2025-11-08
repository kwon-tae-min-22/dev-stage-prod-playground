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

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.example.demo.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ReflectionUtils;

@WebMvcTest(BoardController.class)
@Import(SecurityConfig.class)
class BoardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PostService postService;

	@Test
	void listRendersPostsTemplate() throws Exception {
		given(postService.findAll()).willReturn(List.of(samplePost(1L)));

		mockMvc.perform(get("/posts"))
			.andExpect(status().isOk())
			.andExpect(view().name("posts/list"))
			.andExpect(model().attributeExists("posts"));
	}

	@Test
	void showDetailDisplaysPost() throws Exception {
		given(postService.findById(1L)).willReturn(samplePost(1L));

		mockMvc.perform(get("/posts/1"))
			.andExpect(status().isOk())
			.andExpect(view().name("posts/detail"))
			.andExpect(model().attributeExists("post"));
	}

	@Test
	void createPostValidDataRedirectsToList() throws Exception {
		given(postService.create(any(PostForm.class)))
			.willReturn(samplePost(1L));

		mockMvc.perform(post("/posts")
				.param("title", "제목")
				.param("author", "작성자")
				.param("content", "내용")
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/posts"))
			.andExpect(flash().attribute("message", containsString("등록")));

		then(postService).should().create(argThat(form ->
			"제목".equals(form.getTitle()) &&
				"작성자".equals(form.getAuthor()) &&
				"내용".equals(form.getContent())));
	}

	@Test
	void createPostValidationErrorReturnsForm() throws Exception {
		mockMvc.perform(post("/posts").with(csrf()))
			.andExpect(status().isOk())
			.andExpect(view().name("posts/form"))
			.andExpect(model().attributeHasFieldErrors("postForm", "title", "author", "content"));
	}

	@Test
	void deletePostRedirectsToList() throws Exception {
		mockMvc.perform(post("/posts/1/delete").with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/posts"));

		then(postService).should().delete(1L);
	}

	private Post samplePost(Long id) {
		Post post = new Post("테스트", "사용자", "본문");
		LocalDateTime now = LocalDateTime.now();
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
