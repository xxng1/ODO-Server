package odo.server.image;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

	@Autowired
	private ImageRepository imageRepository;

	// create post
	public String saveImage(Image image) {
		imageRepository.save(image);
		return image.getFileNewName();
	}

	public ResponseEntity<Image> getImage(Integer imageId) {
		Image image = imageRepository.findById(imageId)
				.orElseThrow(() -> new ResourceNotFoundException("Not exist Post Data by id : [" + imageId + "]"));
		return ResponseEntity.ok(image);
	}

	public String getImageByPostKey(Integer postKey) {
		List<Image> imageList = imageRepository.findAllByPostKey(postKey);
		if (imageList.isEmpty()) {
			return "error.png";
		}
		return imageList.get(0).getFileNewName();
	}

}