# 커뮤니티 게시판

사용자의 니즈(Needs)에 맞게 관리자가 게시판을 제공하고 이용할 수 있는 게시판입니다.

## 1️⃣Tech Stack

<img src="https://img.shields.io/badge/Java-000000?style=flat-square&logo=OpenJDK&logoColor=white"/>
<img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=Gradle&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=flat-square&logo=SpringBoot&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring Security-6DB33F?style=flat-square&logo=SpringSecurity&logoColor=white"/>
<br>
<img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white"/>
<img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=Redis&logoColor=white"/>
<img src="https://img.shields.io/badge/Amazon S3-569A31?style=flat-square&logo=Amazon S3&logoColor=white"/>
<img src="https://img.shields.io/badge/elasticsearch-005571?style=flat-square&logo=elasticsearch&logoColor=white"/>

## 2️⃣ERD

## 3️⃣프로젝트 기능 및 설계

### ✅ Member API

#### 1.회원가입

- 모든 사용자는 회원가입을 할 수 있으며 일반 회원으로 가입됩니다.
- 일반회원, 관리자 2개의 권한이 있으며, 별도의 관리자 회원가입 API는 제공하지 않습니다.
- 이메일, 닉네임, 패스워드, 휴대폰번호를 입력 받습니다.
- 회원 가입시 이메일 인증을 수행합니다.

<details>
<summary>[POST] /member/register</summary>

Parameter:

~~~
{
  "memberName": "관리자",
  "email": "admin@gmail.com",
  "password": "1234",
  "mobile": "010-1111-2222"
}
~~~

Result:

~~~
{
  "memberName": "관리자",
  "email": "admin@gmail.com",
  "password": "$2a$10$dHQ02betpizOR/BXgGew5ea2Z4Dqgz76dWklUyKOxtG5CMb4AWmcm",
  "mobile": "010-1111-2222"
}
~~~

</details>

#### 2.로그인

- 회원정보와 일치할 경우 로그인을 할 수 있습니다.

<details>
<summary>[POST] /member/login</summary>

</details>

#### 3.회원정보 조회

- 이메일, 닉네임, 휴대폰번호를 조회할 수 있습니다.

#### 4.회원정보 수정

- 닉네임,패스워드,휴대폰 번호를 수정할 수 있습니다.

#### 5.회원 탈퇴

- 회원정보를 삭제합니다.

### ✅ Board API

#### 1. 게시판 글 작성

- 게시판 작성은 로그인한 사용자만 가능합니다.
- 게시판 작성시 카테고리를 선택해야 작성이 가능합니다.
- 사용자는 제목,내용,사진을 이용하여 게시할 수 있습니다.
- 사진은 AWS S3을 이용하여 저장 및 관리 합니다.

#### 2.게시판 글 수정
- 작성자는 본인이 작성한 글을 수정할 수 있습니다.

#### 3.게시판 글 삭제
- 작성자는 본인이 작성한 글을 삭제할 수 있습니다.
- 관리자는 모든 게시판의 글을 삭제할 수 있습니다.

#### 4.게시판 목록 조회
- 모든 사용자는 게시글을 조회할 수 있습니다.
- 게시글은 최신순으로 정렬되고, 조회수 및 좋아요 개수로 정렬 가능합니다.
- 게시글은 paging 처리를 하여 보여줍니다.

#### 5.게시글 좋아요
- 로그인한 사용자는 권한에 관계 없이 좋아요를 누를 수 있습니다.
- 좋아요 기능은 게시글당 1회만 가능합니다.

### ✅ Comment API
#### 1.댓글 작성 기능
- 로그인한 사용자는 권한에 관계 없이 댓글을 작성할 수 있습니다.

#### 2.댓글 수정 기능
- 작성자는 본인이 작성한 댓글을 삭제할 수 있습니다.

#### 3.댓글 삭제 기능
- 작성자는 본인이 작성한 댓글을 삭제할 수 있습니다.
- 관리자는 모든 게시판의 댓글을 삭제할 수 있습니다.

#### 4.댓글 목록 조회 기능
- 모든 사용자는 댓글을 조회할 수 있습니다.
- 댓글은 최신 순으로만 정렬되며 Paging처리를 합니다.

#### 5.댓글 좋아요 기능
- 로그인한 사용자는 권한에 관계 없이 좋아요를 누를 수 있습니다.
- 좋아요 기능은 게시글당 1회만 가능합니다.


### ✅TO-DO
- [ ] 관리자 게시판 CRUD 구현
- [ ] 관리자 카테고리 관리 구현
- [ ] Oauth를 이용한 로그인 구현
- [ ] 검색기능 : Elastic Search를 활용한 검색
- [ ] SSE를 활용한 댓글 알림 기능


## Trouble Shooting