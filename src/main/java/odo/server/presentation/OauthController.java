package odo.server.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import odo.server.JwtTokenizer;
import odo.server.application.OauthService;
import odo.server.domain.BlogInfoOauthMember;
import odo.server.domain.OauthMember;
import odo.server.domain.OauthServerType;
import odo.server.dto.MemberLoginResponseDto;
import odo.server.post.Post;
import org.apache.tomcat.util.json.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONArray;
 
//import org.springframework.boot.configurationprocessor.json.JSONException;
//import org.springframework.boot.configurationprocessor.json.JSONObject;
//이거로 수정해야 빌드할때 오류 안남
import org.json.JSONException;
import org.json.JSONObject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RequiredArgsConstructor
@RequestMapping("/oauth")
@RestController

public class OauthController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final OauthService oauthService;
    private final JwtTokenizer jwtTokenizer;

    // 사용자가 프론트엔드를 통해 /oauth/kakao로 접속하면 아래 메서드가 실행됩니다.
    // 이때 kakao는 OauthServerType.KAKAO로 변환될 것입니다.
    // OauthService를 통해 KAKAO에서 Auth Code를 받아오기 위한 URL을 생성하고,
    // 여기서 생성된 URL로 사용자를 Redirect 시킵니다.
    @SneakyThrows
    @GetMapping("/{oauthServerType}")
    ResponseEntity<Void> redirectAuthCodeRequestUrl(
            @PathVariable OauthServerType oauthServerType,
            HttpServletResponse response) {
        String redirectUrl = oauthService.getAuthCodeRequestUrl(oauthServerType);
        response.sendRedirect(redirectUrl);
        return ResponseEntity.ok().build();
    }

    // 사용자가 카카오톡으로 로그인 + 정보 제공 동의를 진행하면
    // 프론트엔드로 Redirect되며,
    // 프론트엔드는 이때 code를 받아서 http://ip주소:8080/oauth/login/kakao로 보냅니다.
    @GetMapping("/login/{oauthServerType}")
    ResponseEntity login(
            HttpServletRequest request,
            @PathVariable OauthServerType oauthServerType,
            @RequestParam("code") String code) {
        String[] login = oauthService.login(oauthServerType, code);
        // HttpSession session =request.getSession(true);
        // session.setAttribute("userId", login[2]);
        // System.out.println("id : " + session.getAttribute("userId"));

        String accessToken = jwtTokenizer.createAccessToken(Long.parseLong(login[2]), login[0]);
        String refreshToken = jwtTokenizer.createRefreshToken(Long.parseLong(login[2]), login[0]);

        MemberLoginResponseDto memberloginresponse = MemberLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .memberId(Long.parseLong(login[2]))
                .nickName(login[3])
                .blogName(login[1])
                .email(login[0])
                .build();

        return new ResponseEntity(memberloginresponse, HttpStatus.OK);
    }

    // 프론트엔드로 부터 받아온 user data 입니다.
    @PostMapping("/user/insert")
    public void userInsert(@RequestBody BlogInfoOauthMember userBlogData) throws JSONException {
        log.info("email={}, id={}, userBlogName={}, userBlogNickname={}, userBlogAddress={}",
                userBlogData.getEmail(), userBlogData.getId(), userBlogData.getUserBlogName(),
                userBlogData.getUserBlogNickname(), userBlogData.getUserBlogAddress());

        // h2 에서 일치하는 oauthId 찾아서 blogname, address, nickname 집어넣기
        oauthService.insertBlogInfo(userBlogData.getId(), userBlogData.getUserBlogName(),
                userBlogData.getUserBlogAddress(), userBlogData.getUserBlogNickname());
    }

    @GetMapping("/api/user/{userId}")
    public Map<String, Object> getUserByUserId(
            @PathVariable Long userId) {

        return oauthService.selectByUserId(userId);
    }

    @PostMapping("/api/user/profileImg")
    public void uploadProfileImg(@RequestParam("file") MultipartFile file,
            @RequestParam("userId") Integer userId) {
        try {
            String directory = "./image/";
            String fileNewName = (new Date().getTime()) + "" + (new Random().ints(1000, 9999).findAny().getAsInt())
                    + ".png"; // 현재 날짜와 랜덤 정수값으로 새로운 파일명 만들기

            // 파일 저장
            Path path = Paths.get(directory + fileNewName);
            Files.write(path, file.getBytes());

            oauthService.saveProfileImg(fileNewName, userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}