package odo.server.post;

import odo.server.image.ImageService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {
 
	@Autowired
	private PostService postService;
	private final ImageService imageService;

	// get all post
	@GetMapping("/post")
	public List<Object> getAllPost() {
		List<Post> posts = postService.getAllPost();
		List<Object> result = new ArrayList<>();

		for (Post post : posts) {
			Integer postKey = post.getPostKey();
			System.out.println(postKey);
			String imageName = imageService.getImageByPostKey(postKey);

			// 원하는 구조의 새로운 맵 생성
			Map<String, Object> postWithImage = new HashMap<>();
			postWithImage.put("postId", post.getPostId());
			postWithImage.put("postKey", post.getPostKey());
			postWithImage.put("title", post.getTitle());
			postWithImage.put("tag", post.getTag());
			postWithImage.put("tagList", post.getTagList());
			postWithImage.put("summary", post.getSummary());
			postWithImage.put("contents", post.getContents());
			postWithImage.put("createdTime", post.getCreatedTime());
			postWithImage.put("updatedTime", post.getUpdatedTime());
			postWithImage.put("userId", post.getUserId());
			postWithImage.put("likes", post.getLikes());
			postWithImage.put("counts", post.getCounts());
			postWithImage.put("fileNewName", imageName);
//
			result.add(postWithImage);
		}
		return result;
	}

	// @GetMapping("/posts/{userId}")
	// public List<Post> getPostByUserId(
	// @PathVariable Integer userId) {

	// return postService.getPostByUserId(userId);
	// }
	@GetMapping("/posts/{userId}")
	public List<Object> getPostsAndImagesByUserId(@PathVariable Integer userId) {
		List<Post> posts = postService.getPostByUserId(userId);
		List<Object> result = new ArrayList<>();

		for (Post post : posts) {
			Integer postKey = post.getPostKey();
			System.out.println(postKey);
			String imageName = imageService.getImageByPostKey(postKey);

			// 원하는 구조의 새로운 맵 생성
			Map<String, Object> postWithImage = new HashMap<>();
			postWithImage.put("postId", post.getPostId());
			postWithImage.put("postKey", post.getPostKey());
			postWithImage.put("title", post.getTitle());
			postWithImage.put("tag", post.getTag());
			postWithImage.put("tagList", post.getTagList());
			postWithImage.put("summary", post.getSummary());
			postWithImage.put("contents", post.getContents());
			postWithImage.put("createdTime", post.getCreatedTime());
			postWithImage.put("updatedTime", post.getUpdatedTime());
			postWithImage.put("userId", post.getUserId());
			postWithImage.put("likes", post.getLikes());
			postWithImage.put("counts", post.getCounts());
			postWithImage.put("fileNewName", imageName);

			result.add(postWithImage);
		}
		return result;
	}

	// create post
	@PostMapping("/post")
	public String createPost(@RequestBody Post post) {
		String result = postService.createPost(post);
		postService.saveTags(post, post.getUserId(), post.getTagList());
		return result;
	}

	@GetMapping("/post/{postId}")
	public ResponseEntity<Post> getPostById(
			@PathVariable Integer postId) {

		return postService.getPost(postId);
	}

	@PutMapping("/post/{postId}")
	public ResponseEntity<Post> updatePost(@PathVariable Integer postId, @RequestBody Post updatedPost) {
		return postService.updatePost(postId, updatedPost);
	}

	@DeleteMapping("/post/{postId}")
	public ResponseEntity<?> deletePost(@PathVariable Integer postId) {
		return postService.deletePost(postId);
	}



	// get posts created today
	@GetMapping("/posts/today")
	public List<Post> getPostsCreatedToday() {
		return postService.getPostsCreatedToday();
	}

	// get post count by date
	@GetMapping("/post/countByDate")
	public List<Map<String, Object>> getPostCountByDate() {
		return postService.getPostCountByDate();
	}

	@PostMapping("/post/addNewValue")
	public ResponseEntity<String> addNewValue(@RequestBody Map<String, Object> newEntry) {
		try {
			postService.addNewValue(newEntry);
			return ResponseEntity.ok("New entry added successfully(spring)");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error adding new entry: " + e.getMessage());
		}
	}


	@GetMapping("/post/countFreeze")
	public List<Map<String, Object>> getCountFreeze() {
		return postService.getCountFreeze();
	}






}