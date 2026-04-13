## 📝 커밋 메시지 작성 규칙

- 작성 방법 : `[카테고리]` 작업 내용 요약

| 카테고리 | 설명 |
|--|--|
| 추가 | 새로운 기능 추가 |
| 수정 | 기존 기능 수정, 버그 수정 |
| 삭제 | 불필요한 내용 삭제, 기능 삭제 |
| 문서 | 코드 수정 없이 문서 관련 작업 시 |
| 테스트 | 테스트 코드 작업 시 |
| 환경 | 빌드, 설정 파일, DB 연결 등 환경 관련 작업 시 |

### 예시
- `[추가] 회원가입 API 구현`
- `[수정] 로그인 예외 처리 로직 보완`
- `[환경] MySQL 연결 설정 추가`
- `[문서] README 작성`

---

## ⚙️ 개발 환경 정보 (Environment)

| 항목 | 버전 | 비고 |
|------|------|------|
| **Java** | 17 | 프로젝트 소스 컴파일 대상(Java 17 언어 수준) |
| **JDK** | 22.0.2 (Temurin) | 실제 로컬 실행 환경 |
| **Spring Boot** | 3.5.6 | `build.gradle.kts` 기준 |
| **Gradle** | 8.14.3 | Wrapper 사용 (`./gradlew`) |
| **MySQL** | 8.4.6 | 로컬 DB |
| **Python** | 3.x | 크롤링 모듈 실행 환경 |
| **FastAPI** | 사용 | Python 서비스 연동 시 사용 |
| **Beautiful Soup** | 사용 | 외부 데이터 파싱 |
| **Selenium** | 사용 | 동적 페이지 데이터 수집 |

---

## 💻 실행 방법

### 1. 레포지토리 복사

```bash
git clone https://github.com/YU-MIDAS-26/Backend.git
```

### 2. Spring Boot 서버 실행

```bash
# 1. Spring Boot 서버 디렉토리로 이동
cd spring-server

# 2. 프로젝트 의존성 다운로드 및 전체 빌드 수행
./gradlew clean build

# 3. Spring Boot 서버 실행
./gradlew bootRun
```

### 3. Python 크롤링 모듈 실행

```bash
# 1. Python 크롤링 모듈 디렉토리로 이동
cd python-crawler

# 2. 가상환경 생성
python -m venv venv

# 3. 가상환경 활성화
# macOS / Linux 환경
source venv/bin/activate

# Windows 환경
venv\Scripts\activate

# 4. 필요한 패키지 설치
pip install -r requirements.txt

# 5. KAMIS 크롤링 스크립트 실행
python scripts/run_kamis.py
```

---

## 📂 디렉토리 구조

```bash
backend/
├── spring-server/      # Spring Boot 기반 API 서버 디렉토리
├── python-crawler/     # Python 기반 크롤링 및 데이터 수집 디렉토리
├── docs/               # 프로젝트 관련 문서 저장 디렉토리
├── .gitignore          # Git 추적에서 제외할 파일 목록
├── .gitattributes      # 줄바꿈 및 파일 속성 관리 설정
├── .editorconfig       # 팀 공통 코드 스타일 및 편집기 설정
└── README.md           # 프로젝트 설명 및 실행 방법 문서
```