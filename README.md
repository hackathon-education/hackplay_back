# HackPlay Backend

HackPlay는 수강생이 브라우저에서 코드를 작성·실행·제출하고, 관리자가 채점하는 **웹 기반 코딩 교육 플랫폼**입니다.  
별도의 개발 환경 설치 없이 브라우저만으로 프로젝트 생성부터 과제 제출까지 전 과정을 진행할 수 있습니다.

---

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.4 |
| Database | PostgreSQL |
| Cache | Redis |
| Auth | JWT (httpOnly Cookie) |
| Realtime | WebSocket |
| Container | Docker |
| Storage | Cloudflare R2 (S3 호환) |
| Build | Gradle |

---

## ⚙️ 로컬 실행 방법

### 사전 준비
- Java 21
- PostgreSQL 실행 중
- Redis 실행 중
- Docker 실행 중 (프로젝트 실행 기능 사용 시)

### 환경 변수 설정
프로젝트 루트에 `.env` 파일을 생성합니다.

```env
DB_URL=jdbc:postgresql://localhost:5432/hackplay
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

JWT_SECRET=your_jwt_secret_key
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
EMAIL_AUTH_CODE_EXPIRATION=1800000

PROJECTS_BASE_PATH=/your/projects/path
SCRIPTS_BASE_PATH=/your/scripts/path

R2_ACCESS_KEY=your_r2_access_key
R2_SECRET_KEY=your_r2_secret_key
R2_ENDPOINT=https://{accountId}.r2.cloudflarestorage.com
R2_BUCKET=hackplay
```

### 실행
```bash
./gradlew bootRun
```

### API 문서
서버 실행 후 http://localhost:8080/swagger-ui/index.html 에서 확인

---

## 📁 폴더 구조
```
src
├─main
│  ├─java
│  │  └─com
│  │      └─hackplay
│  │          └─hackplay
│  │              ├─common      // 공통 모듈 (유틸, 예외 처리, 상수 등) 파일의 모음입니다.
│  │              ├─config      // 환경설정 및 외부 모듈 연동 설정 파일의 모음입니다.
│  │              ├─controller  // 프론트에게 API 요청을 받아 서비스 호출 및 응답 처리를 하는 파일들의 모음입니다. 
│  │              ├─domain      // 엔티티, 도메인 모델 및 규칙을 정의하는 파일의 모음입니다. 
│  │              ├─dto         // 요청·응답 데이터 전송 객체를 정의하는 파일의 모음입니다.
│  │              ├─repository  // DB 접근 계층 - 데이터의 CRUD를 처리하는 파일의 모음입니다.
│  │              └─service     // 핵심 비즈니스 로직을 구현하는 파일들의 모음입니다.
│  └─resources
│      └─application.properties
└─test
    └─java
        └─com
            └─hackplay
                └─hackplay
```

---

## 🌿 브랜치 전략
- `main`: 운영 배포용 브랜치
- `dev`: 개발용 브랜치
- `type/#이슈번호/설명`: 작업용 브랜치

---

## 📝 커밋 | 이슈 | PR 컨벤션

### 📑 이슈 & PR 작성 가이드
- 이슈는 이슈 템플릿을 따라 작성합니다.
- PR은 커밋 메시지를 따라 작성됩니다. (커밋 메시지의 제목이 PR의 제목, 본문이 PR의 본문이 됩니다.)

### 📑 커밋 메시지 구조
커밋 메시지는 3개의 파트로 구성됩니다. (공백 줄로 구분)
```
type: 제목

body

footer
```
- title(제목) → "type: 제목"  
- body(본문) → 선택 (변경 이유, 맥락 설명)  
- footer(꼬리말) → 선택 (이슈 트래커 ID, 참조 링크)

### 🔖 타입

| Gitmoji | 타입         | 설명                        |
| --- | ---------- | ------------------------- |
| ✨   | `feat`     | 새로운 기능 추가                 |
| 🐛  | `fix`      | 버그 수정                     |
| ♻️  | `refactor` | 코드 리팩토링                   |
| ⚡️  | `perf`     | 성능 개선                     |
| 📝  | `docs`     | 문서/주석 추가·수정               |
| ✅   | `test`     | 테스트 코드 추가/수정              |
| 🔒  | `security` | 보안 이슈 수정                  |
| 🗃  | `db`       | 데이터베이스 관련 수정              |
| 🔊  | `log`      | 로그 추가/수정                  |
| 🔖  | `release`  | 릴리즈/버전 태그                 |
| 📦  | `build`    | 빌드/배포 파일 추가/수정            |
| 💚  | `ci`       | CI/CD 관련 수정               |
| 🔧  | `chore`    | 기타 자잘한 작업 (의존성, 설정, 이동 등) |
| ⏪   | `revert`   | 변경 내용 되돌리기                |

---

## ER 다이어그램

추후 업데이트 예정

---

## 시스템 아키텍처

추후 업데이트 예정

---

## 🔗 링크

| 이름 | URL |
|------|-----|
| API 문서 (Swagger) | http://localhost:8080/swagger-ui/index.html |
| 이슈 트래커 | https://github.com/hackathon-education/hackplay_back/issues |
