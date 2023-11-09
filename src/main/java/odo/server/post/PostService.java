package odo.server.post;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PostService {

	@Autowired
	private PostRepository postRepository;
	private RestTemplateService restTemplateService;
	// get posts data
	public List<Post> getAllPost() {
		return postRepository.findAll();
	}

	public List<Post> getPostByUserId(Integer UserId) {
		return postRepository.findAllByUserId(UserId);
	}

	// create post
	public String createPost(Post post) {
		post.setSummary(restTemplateService.summary(post.getContents()));
		postRepository.save(post);
		return Integer.toString(post.getPostId());
	}

	public ResponseEntity<Post> getPost(Integer postId) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new ResourceNotFoundException("Not exist Post Data by id : [" + postId + "]"));
		return ResponseEntity.ok(post);
	}

}