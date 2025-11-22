package com.example.demo.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PostForm {

	@NotBlank(message = "제목을 입력해주세요.")
	@Size(max = 200, message = "제목은 최대 200자까지 가능합니다.")
	private String title;

	@NotBlank(message = "작성자를 입력해주세요.")
	@Size(max = 50, message = "작성자 이름은 최대 50자까지 가능합니다.")
	private String author;

	@NotBlank(message = "내용을 입력해주세요.")
	private String content;

	public PostForm() {
	}

	public PostForm(String title, String author, String content) {
		this.title = title;
		this.author = author;
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
