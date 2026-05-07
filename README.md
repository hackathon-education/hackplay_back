# HackPlay Backend

HackPlay 백엔드 리포지토리입니다.

---

## ⚙️ 로컬 실행 방법

### 사전 준비
- Java 21
- PostgreSQL, Redis, Docker 실행 중
- `.env` 파일 필요 → 팀원에게 요청

### 실행
```bash
./gradlew bootRun
```

### API 문서
서버 실행 후 http://localhost:8080/swagger-ui/index.html

---

## 📁 폴더 구조
```
src
├─main
│  ├─java
│  │  └─com
│  │      └─hackplay
│  │          └─hackplay
│  │              ├─common      // 공통 모듈 (유틸, 예외 처리, 상수 등)
│  │              ├─config      // 환경설정 및 외부 모듈 연동 설정
│  │              ├─controller  // API 요청 처리
│  │              ├─domain      // 엔티티, 도메인 모델
│  │              ├─dto         // 요청·응답 데이터 전송 객체
│  │              ├─repository  // DB 접근 계층
│  │              └─service     // 비즈니스 로직
│  └─resources
│      └─application.properties
└─test
```

---

## 🌿 브랜치 전략
- `main`: 운영 배포용
- `dev`: 개발용
- `type/#이슈번호/설명`: 작업용 브랜치

---

## 📝 커밋 | 이슈 | PR 컨벤션

### 이슈 & PR
- 이슈는 이슈 템플릿에 맞춰 작성합니다.
- PR은 `dev` 브랜치로 요청합니다.

### 커밋 메시지 구조
```
type: 제목

body (선택)

footer (선택, 이슈 번호 등)
```

### 타입

| Gitmoji | 타입         | 설명                        |
| ---     | ----------   | ------------------------- |
| ✨      | `feat`       | 새로운 기능 추가              |
| 🐛      | `fix`        | 버그 수정                    |
| ♻️      | `refactor`   | 코드 리팩토링                 |
| ⚡️     | `perf`       | 성능 개선                    |
| 📝      | `docs`       | 문서/주석 추가·수정            |
| ✅      | `test`       | 테스트 코드 추가/수정          |
| 🔒      | `security`   | 보안 이슈 수정                |
| 🗃      | `db`         | 데이터베이스 관련 수정         |
| 🔊      | `log`        | 로그 추가/수정                |
| 🔖      | `release`    | 릴리즈/버전 태그              |
| 📦      | `build`      | 빌드/배포 파일 추가/수정       |
| 💚      | `ci`         | CI/CD 관련 수정              |
| 🔧      | `chore`      | 기타 자잘한 작업              |
| ⏪      | `revert`     | 변경 내용 되돌리기            |
