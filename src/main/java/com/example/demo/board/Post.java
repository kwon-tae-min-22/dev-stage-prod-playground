package com.example.demo.board;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Post {

	private UUID id;

	private OperatorUser operatorUser;

	private String title;

	private String content;

	private OffsetDateTime createdAt;

	private OffsetDateTime updatedAt;

	public Post() {
	}

	public Post(String title, OperatorUser operatorUser, String content) {
		this.title = title;
		this.operatorUser = operatorUser;
		this.content = content;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public OperatorUser getOperatorUser() {
		return operatorUser;
	}

	public void setOperatorUser(OperatorUser operatorUser) {
		this.operatorUser = operatorUser;
	}

	public String getAuthor() {
		return operatorUser != null ? operatorUser.getUsername() : null;
	}

	public void update(String title, OperatorUser operatorUser, String content) {
		this.title = title;
		this.operatorUser = operatorUser;
		this.content = content;
	}
}
