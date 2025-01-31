package odo.server.image;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
 
 
@RestController
@RequestMapping("/api")

public class ImageController {

    @Autowired
    private ImageService imageService;

    // create post
    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file,
            @RequestParam("postKey") String postKey) {
        try {
            // 파일 저장 경로 지정
            String directory = "./image/"; // 실제 서버의 파일 디렉터리 경로로 변경해야 합니다.
            String fileOriName = file.getOriginalFilename();
            String fileNewName = (new Date().getTime()) + "" + (new Random().ints(1000, 9999).findAny().getAsInt())
                    + ".png"; // 현재 날짜와 랜덤 정수값으로 새로운 파일명 만들기

            // 파일 저장
            Path path = Paths.get(directory + fileNewName);
            Files.write(path, file.getBytes());

            // 파일 정보 DB에 저장
            Image image = new Image();
            image.setPostKey(Integer.parseInt(postKey));
            image.setFileOriName(fileOriName);
            image.setFileNewName(fileNewName);
            imageService.saveImage(image);

            // 저장된 이미지 URL 반환
            return ResponseEntity.ok(fileNewName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패");
        }
    }





    //----이미지 수정/삭제
    // ... (이전 코드 부분)

    @PostMapping("/image/update")
    public ResponseEntity<String> updateImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam("postKey") String postKey) {
        try {
            // 기존 이미지 삭제
            String existingFileNewName = imageService.getImageByPostKey(Integer.parseInt(postKey));
            if (existingFileNewName != null) {
                Path existingFilePath = Paths.get("./image/" + existingFileNewName);
                Files.deleteIfExists(existingFilePath);
            }

            // 새로운 이미지 저장
            String directory = "./image/";
            String fileOriName = file.getOriginalFilename();
            String fileNewName = (new Date().getTime()) + "" + (new Random().ints(1000, 9999).findAny().getAsInt())
                    + ".png";

            Path path = Paths.get(directory + fileNewName);
            Files.write(path, file.getBytes());

            // 파일 정보 DB에 저장
            Image image = new Image();
            image.setPostKey(Integer.parseInt(postKey));
            image.setFileOriName(fileOriName);
            image.setFileNewName(fileNewName);
            imageService.saveImage(image);

            // 새로 저장된 이미지 URL 반환
            return ResponseEntity.ok(fileNewName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패");
        }
    }


    // ... (이전 코드 부분)

    @PostMapping("/image/delete")
    public ResponseEntity<String> deleteImage(@RequestParam("postKey") String postKey) {
        try {
            // DB에서 이미지 정보 가져오기
            String existingFileNewName = imageService.getImageByPostKey(Integer.parseInt(postKey));

            if (existingFileNewName != null) {
                // 파일시스템에서 이미지 파일 삭제
                Path existingFilePath = Paths.get("./image/" + existingFileNewName);
                Files.deleteIfExists(existingFilePath);

                // DB에서 이미지 정보 삭제
                imageService.deleteImage(Integer.parseInt(postKey));

                return ResponseEntity.ok("이미지가 삭제되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("이미지를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 삭제 실패");
        }
    }

// ... (이후 코드 부분)



    //----이미지 수정/삭제

    @GetMapping("/image/{fileNewName}")
    public ResponseEntity<Resource> getImageByName(@PathVariable String fileNewName) {
        try {
            String directory = "./image/";
            Path filePath = Paths.get(directory + fileNewName);
            Resource resource = new UrlResource(filePath.toUri());
            // 파일이 존재하는지 확인 + 없으면 error.png
            if (!resource.exists()) {
                Path errorFilePath = Paths.get( "./image/error.png");
                Resource errorResource = new UrlResource(errorFilePath.toUri());

                if (errorResource.exists()) {
                    return ResponseEntity.ok(errorResource);
                } else {
                    throw new FileNotFoundException("Error file not found: error.png");
                }
            }
            return ResponseEntity.ok(resource);
            // .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
            // resource.getFilename() + "\"")
            // .body(resource);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/image/thumbnail/{postKey}")
    public ResponseEntity<String> getImageByPostKey(@PathVariable Integer postKey) {
        String fileNewName = imageService.getImageByPostKey(postKey);
        return ResponseEntity.ok(fileNewName);
    }
}