# 채널별 매출 비중(도넛) API 가이드

> **차트 종류**: 도넛 / 파이 차트
> **데이터 소스**: `payments` 테이블
> **목적**: 매장 vs 배달 매출 비중 한눈에 보기

---

## 1. 엔드포인트

```
GET /api/payments/stats/channel-breakdown?from=2026-05-01&to=2026-05-31
```

### 쿼리 파라미터

| 이름 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `from` | `yyyy-MM-dd` | ❌ | 30일 전 | 시작일 |
| `to` | `yyyy-MM-dd` | ❌ | 오늘 | 종료일 |

---

## 2. 응답 형식

```typescript
interface ChannelBreakdown {
  channel: "OFFLINE" | "DELIVERY"; // 채널 코드
  label: string;                    // "매장" | "배달" (한글 표시용)
  amount: number;                   // 해당 채널 매출 합계
  count: number;                    // 해당 채널 거래 건수
  ratio: number;                    // 전체 매출 대비 비율 (0.0 ~ 1.0)
}

// 응답: ApiResponse<ChannelBreakdown[]>
```

### 예시 응답

```json
{
  "status": "SUCCESS",
  "message": "요청 성공",
  "data": [
    {"channel": "DELIVERY", "label": "배달", "amount": 2580000, "count": 136, "ratio": 0.3805},
    {"channel": "OFFLINE",  "label": "매장", "amount": 4200000, "count": 219, "ratio": 0.6195}
  ]
}
```

→ ratio 합이 1.0 (0.3805 + 0.6195 = 1.0). 차트에 그대로 사용 가능.

---

## 3. 차트 구현 방법

### ⭐ 옵션 1: Recharts (가장 간단)

```bash
npm install recharts
```

```jsx
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const COLORS = {
  OFFLINE: '#3b82f6',   // 매장 - 파랑
  DELIVERY: '#f59e0b',  // 배달 - 주황
};

function ChannelDonut({ data }) {
  return (
    <ResponsiveContainer width="100%" height={300}>
      <PieChart>
        <Pie
          data={data}
          dataKey="amount"
          nameKey="label"
          cx="50%"
          cy="50%"
          innerRadius={60}    // 도넛 (속 비움)
          outerRadius={100}
          label={(entry) => `${entry.label} ${(entry.ratio * 100).toFixed(1)}%`}
        >
          {data.map((entry) => (
            <Cell key={entry.channel} fill={COLORS[entry.channel]} />
          ))}
        </Pie>
        <Tooltip
          formatter={(v, name, props) =>
            [`${v.toLocaleString()}원 (${props.payload.count}건)`, name]
          }
        />
        <Legend />
      </PieChart>
    </ResponsiveContainer>
  );
}

// 사용
function ChannelPage() {
  const [data, setData] = useState([]);
  useEffect(() => {
    fetch('http://localhost:8080/api/payments/stats/channel-breakdown?from=2026-05-01&to=2026-05-31')
      .then(r => r.json())
      .then(j => setData(j.data));
  }, []);
  return <ChannelDonut data={data} />;
}
```

---

### 옵션 2: Chart.js

```bash
npm install chart.js react-chartjs-2
```

```jsx
import { Doughnut } from 'react-chartjs-2';
import { Chart, ArcElement, Tooltip, Legend } from 'chart.js';
Chart.register(ArcElement, Tooltip, Legend);

function ChannelDonut({ data }) {
  const chartData = {
    labels: data.map(d => d.label),
    datasets: [{
      data: data.map(d => d.amount),
      backgroundColor: data.map(d =>
        d.channel === 'OFFLINE' ? '#3b82f6' : '#f59e0b'
      ),
    }],
  };

  const options = {
    plugins: {
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const d = data[ctx.dataIndex];
            return `${d.label}: ${d.amount.toLocaleString()}원 (${(d.ratio * 100).toFixed(1)}%, ${d.count}건)`;
          },
        },
      },
    },
  };

  return <Doughnut data={chartData} options={options} />;
}
```

---

### 옵션 3: ECharts

```jsx
import ReactECharts from 'echarts-for-react';

function ChannelDonut({ data }) {
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (p) =>
        `${p.name}<br/>${p.value.toLocaleString()}원 (${p.percent}%)`,
    },
    legend: { bottom: 0 },
    series: [{
      name: '채널별 매출',
      type: 'pie',
      radius: ['40%', '70%'],    // 도넛
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%' },
      data: data.map(d => ({
        name: d.label,
        value: d.amount,
        itemStyle: {
          color: d.channel === 'OFFLINE' ? '#3b82f6' : '#f59e0b',
        },
      })),
    }],
  };

  return <ReactECharts option={option} style={{ height: 350 }} />;
}
```

---

## 4. 라이브러리 비교

| 라이브러리 | 설치 크기 | 코드 양 | 디자인 | 추천 |
|---|---|---|---|---|
| **Recharts** | ~500KB | 짧음 | 깔끔 | ⭐⭐⭐ |
| **Chart.js** | ~250KB | 중간 | 좋음 | ⭐⭐ |
| **ECharts** | ~5MB | 중간 | 매우 좋음 | ⭐⭐ |

**MVP는 Recharts 추천** — 가장 간단하고, 다른 차트(일별 라인)도 Recharts로 만들면 일관성.

---

## 5. UI/UX 권장사항

### 색상
- **매장 (OFFLINE)**: 차분한 파랑 (`#3b82f6`)
- **배달 (DELIVERY)**: 따뜻한 주황 (`#f59e0b`)
- 또는 브랜드 컬러 활용

### 가운데에 합계 표시 (도넛 중심)

```jsx
<div className="relative">
  <ChannelDonut data={data} />
  <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
    <div className="text-2xl font-bold">
      {data.reduce((sum, d) => sum + d.amount, 0).toLocaleString()}원
    </div>
    <div className="text-sm text-gray-500">총 매출</div>
  </div>
</div>
```

### 비교 텍스트 인사이트
- "이번 달 매장 매출이 배달의 **약 1.6배**"
- "배달 비중 38.1%"

### 데이터 없을 때
- 한 채널이 0건이면 한 가지 색만 그려짐. 예외 처리 필요 시:
  ```jsx
  if (data.length === 0) return <div>거래 데이터가 없습니다.</div>;
  ```

### 기간 필터
- 일별 추이 + 히트맵과 **같은 기간 필터** 공유하는 게 자연스러움

---

## 6. 테스트

```bash
curl "http://localhost:8080/api/payments/stats/channel-breakdown?from=2026-05-01&to=2026-05-31" \
  | python3 -m json.tool
```

**예상**: 2개 항목, ratio 합 = 1.0.

---

## 7. 백엔드 구현 메모 (참고)

- Native SQL: `GROUP BY channel`
- ratio는 서비스 레이어에서 계산 (`amount / SUM(amount)`)
- 소수점 4자리까지 반올림 (`Math.round(ratio * 10000) / 10000`)

---

## 문의

- 한 채널만 응답 → 해당 기간에 다른 채널 거래가 없는 것 (정상)
- ratio 합이 0.9999 등 → 부동소수점 오차, 차트엔 영향 없음
