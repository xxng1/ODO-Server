package odo.server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Table(name = "oauth_member",
        uniqueConstraints = {
                // uniqueConstraints를 통해 OauthId의 유일함을 보장
                @UniqueConstraint(
                        name = "oauth_id_unique",
                        columnNames = {
                                "oauth_server_id",
                                "oauth_server"
                        }
                ),
        }
)
public class OauthMember {

    // 자동 증가되는 id 입니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자의 별명과 프로필 사진 URL에 대한 정보를 제공받기로 했으므로, 이를 저장하기 위해 필드
    @Embedded
    private OauthId oauthId;
    private String email;
    private String profileImageUrl;
    private String nickname;

    // 리액트에서 받은 추가정보(블로그 이름, 블로그 주소, 닉네임) 등을 저장하기 위한 필드
    private String blogName;
    private String blogAddress;
    private String blogNickname;

    public Long id() {
        return id;
    }

    public OauthId oauthId() {
        return oauthId;
    }

    public String email() { return email; }

    public String nickname() { return nickname; }

    public String profileImageUrl() {
        return profileImageUrl;
    }

    public String blogName() { return blogName; }

    public String blogAddress() { return blogAddress; }

    public String blogNickname() { return blogNickname; }

    public void setBlogName(String blogName) {
        this.blogName = blogName;
    }

    public void setBlogAddress(String blogAddress) {
        this.blogAddress = blogAddress;
    }

    public void setBlogNickname(String blogNickname) {
        this.blogNickname = blogNickname;
    }
}