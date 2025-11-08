package com.example.demo.board;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BoardController {

	private final PostService postService;

	public BoardController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping("/")
	public String index() {
		return "redirect:/posts";
	}

	@GetMapping("/posts")
	public String list(Model model) {
		model.addAttribute("posts", postService.findAll());
		return "posts/list";
	}

	@GetMapping("/posts/new")
	public String createForm(Model model) {
		model.addAttribute("postForm", new PostForm());
		model.addAttribute("mode", "create");
		return "posts/form";
	}

	@PostMapping("/posts")
	public String create(@Valid @ModelAttribute("postForm") PostForm form,
		BindingResult bindingResult,
		RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "posts/form";
		}

		postService.create(form);
		redirectAttributes.addFlashAttribute("message", "새 게시글이 등록되었습니다.");
		return "redirect:/posts";
	}

	@GetMapping("/posts/{id}")
	public String detail(@PathVariable Long id, Model model) {
		model.addAttribute("post", postService.findById(id));
		return "posts/detail";
	}

	@GetMapping("/posts/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Post post = postService.findById(id);
		model.addAttribute("postForm", new PostForm(post.getTitle(), post.getAuthor(), post.getContent()));
		model.addAttribute("mode", "edit");
		model.addAttribute("postId", id);
		return "posts/form";
	}

	@PostMapping("/posts/{id}")
	public String update(@PathVariable Long id,
		@Valid @ModelAttribute("postForm") PostForm form,
		BindingResult bindingResult,
		RedirectAttributes redirectAttributes,
		Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("mode", "edit");
			model.addAttribute("postId", id);
			return "posts/form";
		}

		postService.update(id, form);
		redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
		return "redirect:/posts/" + id;
	}

	@PostMapping("/posts/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		postService.delete(id);
		redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
		return "redirect:/posts";
	}
}
