package com.example.demo.board;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "게시판", description = "게시글 목록 및 CRUD 화면")
public class BoardController {

	private final PostService postService;

	public BoardController(PostService postService) {
		this.postService = postService;
	}

	@Operation(
		summary = "루트 접근 시 게시글 목록으로 이동",
		description = "/ 요청을 게시글 목록 화면으로 리다이렉트합니다."
	)
	@GetMapping("/")
	public String index() {
		return "redirect:/posts";
	}

	@Operation(
		summary = "게시글 목록 조회",
		description = "전체 게시글을 최신순으로 조회해 목록 화면에 전달합니다."
	)
	@GetMapping("/posts")
	public String list(Model model) {
		model.addAttribute("posts", postService.findAll());
		return "posts/list";
	}

	@Operation(
		summary = "게시글 작성 폼 조회",
		description = "새 게시글을 작성할 수 있는 입력 폼을 제공합니다."
	)
	@GetMapping("/posts/new")
	public String createForm(Model model) {
		model.addAttribute("postForm", new PostForm());
		model.addAttribute("mode", "create");
		return "posts/form";
	}

	@Operation(
		summary = "게시글 등록",
		description = "입력받은 내용으로 게시글을 생성하고 목록 페이지로 이동합니다."
	)
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

	@Operation(
		summary = "게시글 상세 조회",
		description = "ID로 게시글을 조회해 상세 화면에 노출합니다."
	)
	@GetMapping("/posts/{id}")
	public String detail(@PathVariable Long id, Model model) {
		model.addAttribute("post", postService.findById(id));
		return "posts/detail";
	}

	@Operation(
		summary = "게시글 수정 폼 조회",
		description = "기존 게시글 데이터를 포함한 수정 폼을 제공합니다."
	)
	@GetMapping("/posts/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		Post post = postService.findById(id);
		model.addAttribute("postForm", new PostForm(post.getTitle(), post.getAuthor(), post.getContent()));
		model.addAttribute("mode", "edit");
		model.addAttribute("postId", id);
		return "posts/form";
	}

	@Operation(
		summary = "게시글 수정",
		description = "입력한 내용으로 게시글을 업데이트하고 상세 페이지로 이동합니다."
	)
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

	@Operation(
		summary = "게시글 삭제",
		description = "게시글을 삭제하고 목록 페이지로 이동합니다."
	)
	@PostMapping("/posts/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		postService.delete(id);
		redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
		return "redirect:/posts";
	}
}
