# AI 챗봇 (RAG) API 가이드

> **개요**: 사장님이 메시지를 보내면 BSight의 실제 가게 데이터(매출/히트맵/채널)를 참고해 답변합니다.
> **모델**: OpenAI gpt-4o-mini
> **데이터 소스**: payments 테이블 + payment 차트 API 3개

---

## 1. 엔드포인트

```
POST /api/chat
Content-Type: application/json
```

### Request Body
```typescript
interface ChatRequest {
  message: string;  // 사용자 질문 (필수)
}
```

### Response
```typescript
interface ChatResponse {
  answer: string;             // GPT 응답 (마크다운 포맷)
  usedContext: string[];      // 참조한 백엔드 데이터 소스
}

// 전체: ApiResponse<ChatResponse>
```

---

## 2. 예시

### Request
```json
{ "message": "이번 달 매출 어땠어?" }
```

### Response
```json
{
  "status": "SUCCESS",
  "message": "요청 성공",
  "data": {
    "answer": "## 이번 달 매출 요약\n\n총 매출은 **6,500,000원** (340건)입니다.\n\n| 지표 | 값 |\n|---|---|\n| 평균 일매출 | 250,000원 |\n| 거래 건수 | 340건 |\n\n특히 **토요일 19시**가 가장 매출이 높았어요!",
    "usedContext": ["payments/stats/daily"]
  }
}
```

---

## 3. 키워드 감지 (자동)

사용자 메시지에 다음 키워드가 들어있으면 자동으로 해당 데이터를 GPT에 컨텍스트로 전달합니다:

| 키워드 | 자동 조회 데이터 |
|---|---|
| 매출, 수익, 오늘, 이번달, 어제, 지난주 | 일별 매출 |
| 시간대, 피크, 언제, 바쁜, 한가, 요일 | 요일×시간대 히트맵 (TOP 10) |
| 매장, 배달, 비율, 비중, 채널 | 채널별 매출 비중 |

→ 어떤 데이터가 사용됐는지는 응답의 `usedContext`로 확인 가능.

---

## 4. 프론트 코드 예시 (React)

### 호출 함수
```typescript
async function askChatbot(message: string): Promise<ChatResponse> {
  const res = await fetch('http://localhost:8080/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message }),
  });
  const json = await res.json();
  return json.data;
}
```

### 마크다운 렌더링 (react-markdown 추천)

```bash
npm install react-markdown remark-gfm
```

```jsx
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

function ChatMessage({ answer }) {
  return (
    <div className="prose prose-sm max-w-none">
      <ReactMarkdown remarkPlugins={[remarkGfm]}>
        {answer}
      </ReactMarkdown>
    </div>
  );
}
```

→ 표/리스트/굵게 등 자동 렌더링.

### 전체 챗봇 컴포넌트
```jsx
function ChatBot() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);

  async function send() {
    if (!input.trim()) return;
    const userMsg = { role: 'user', content: input };
    setMessages(m => [...m, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const res = await askChatbot(input);
      setMessages(m => [...m, { role: 'bot', content: res.answer }]);
    } catch (e) {
      setMessages(m => [...m, { role: 'bot', content: '오류가 발생했어요.' }]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <div className="messages">
        {messages.map((m, i) => (
          <div key={i} className={m.role}>
            {m.role === 'bot' ? <ChatMessage answer={m.content} /> : m.content}
          </div>
        ))}
        {loading && <div>...</div>}
      </div>
      <div>
        <input
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && send()}
          placeholder="궁금한 걸 물어보세요"
        />
        <button onClick={send} disabled={loading}>보내기</button>
      </div>
    </div>
  );
}
```

---

## 5. 추천 질문 (UI 버튼으로 노출하면 좋음)

- "이번 달 매출 어땠어?"
- "언제가 가장 바빠?"
- "매장이랑 배달 비율 알려줘"
- "이번주 매출 추이 보여줘"
- "어제 매출 얼마야?"

→ 사장님이 처음 챗봇 사용할 때 클릭 한 번으로 체험 가능.

---

## 6. 주의사항

### 응답 시간
- 일반적으로 1~3초
- 첫 응답은 cold start로 5초 정도 걸릴 수 있음
- **로딩 인디케이터 필수**

### 에러 처리
백엔드 OpenAI 호출이 실패해도 에러 안 던지고 친절한 안내 메시지 응답:
```json
{ "answer": "죄송해요, 지금은 답변을 드리기 어려워요. 잠시 후 다시 시도해주세요." }
```

### 비용
- gpt-4o-mini: 1회 약 0.5원
- 1,000회 = 약 750원
- 무료는 아니지만 무리 없는 수준

### 보안 (현재 임시 permit)
- 지금: `/api/chat/**` 인증 없이 호출 가능
- 향후: JWT 필수로 변경 예정
- 그때는 `Authorization: Bearer <token>` 헤더 추가 필요

---

## 7. 백엔드 환경 설정 (참고)

`application-local.yml`에 OpenAI API 키가 있어야 동작:
```yaml
openai:
  api-key: sk-proj-xxx
```

→ K님 로컬에는 설정되어 있음. 프론트 개발자가 로컬 백엔드 띄울 때만 신경 쓰면 됨.

---

## 8. 향후 확장 (PR 거리)

| 기능 | 난이도 |
|---|---|
| 대화 이력 저장 (DB) | 중간 |
| Function Calling으로 KAMIS/네이버 시세 연동 | 높음 |
| 메뉴 추천 / 가격 결정 비서 | 높음 |
| 음성 입력 (Whisper) | 높음 |

---

## 문의

- 응답 이상 → `usedContext` 확인. 키워드 인식이 잘 됐는지 체크.
- 에러 응답만 옴 → 백엔드 로그에 OpenAI API 키/할당량 확인.
- Swagger UI: `http://localhost:8080/swagger-ui.html` (Chat 태그)
