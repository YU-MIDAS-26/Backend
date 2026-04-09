# 시세 조회 기능 - 프론트엔드 구현 가이드

## 개요

KAMIS 농산물 시세 데이터를 부류코드 및 품목명으로 필터링하여 조회하는 기능입니다.
백엔드 API는 이미 완성되어 있으며, 프론트에서 UI와 API 연동만 구현하면 됩니다.

---

## API 명세

### 기본 URL
```
GET /api/prices/list
```

### 쿼리 파라미터

| 파라미터 | 필수 여부 | 설명 | 예시 |
|---|---|---|---|
| `categoryCode` | 선택 | 부류코드 (아래 코드표 참고) | `200` |
| `itemName` | 선택 | 품목명 부분 검색 (LIKE 검색) | `양파` |

> 두 파라미터 모두 생략하면 최근 100건 전체 반환

### 부류코드 목록

| 코드 | 분류 |
|---|---|
| `100` | 식량작물 |
| `200` | 채소류 |
| `300` | 특용작물 |
| `400` | 과일류 |
| `500` | 축산물 |
| `600` | 수산물 |

### 호출 예시

```
# 채소류 전체 조회
GET /api/prices/list?categoryCode=200

# 품목명 검색 (부류 무관)
GET /api/prices/list?itemName=양파

# 채소류 중 양파 검색
GET /api/prices/list?categoryCode=200&itemName=양파

# 축산물 중 소 검색
GET /api/prices/list?categoryCode=500&itemName=소
```

### 응답 형식

```json
{
  "status": "success",
  "message": null,
  "data": [
    {
      "id": 1,
      "itemName": "양파",
      "avgPrice": 1200,
      "minPrice": 1100,
      "maxPrice": 1350,
      "unit": "1kg",
      "marketName": "상품 특",
      "collectedDate": "2026-04-09",
      "source": "kamis",
      "categoryCode": "200"
    }
  ]
}
```

---

## 구현 방향

### 1. 부류코드 상수 정의

코드가 변경될 가능성이 낮으므로 하드코딩으로 관리합니다.

```js
const CATEGORIES = [
  { code: null,  label: "전체" },
  { code: "100", label: "식량작물" },
  { code: "200", label: "채소류" },
  { code: "300", label: "특용작물" },
  { code: "400", label: "과일류" },
  { code: "500", label: "축산물" },
  { code: "600", label: "수산물" },
];
```

### 2. 상태 관리

```js
const [selectedCategory, setSelectedCategory] = useState(null); // 선택된 부류코드
const [itemName, setItemName] = useState("");                    // 검색어
const [priceList, setPriceList] = useState([]);                  // 조회 결과
```

### 3. API 호출 함수

```js
const fetchPrices = async (categoryCode, itemName) => {
  const params = new URLSearchParams();
  if (categoryCode) params.append("categoryCode", categoryCode);
  if (itemName)     params.append("itemName", itemName);

  const res = await fetch(`/api/prices/list?${params.toString()}`);
  const json = await res.json();
  setPriceList(json.data);
};
```

### 4. 카테고리 탭 클릭 시 호출

```js
const handleCategoryClick = (code) => {
  setSelectedCategory(code);
  fetchPrices(code, itemName);
};
```

### 5. 검색어 입력 후 조회

검색 버튼 클릭 또는 엔터 입력 시 호출을 권장합니다.
(입력 중 실시간 호출은 API 부하 유발 가능)

```js
const handleSearch = () => {
  fetchPrices(selectedCategory, itemName);
};
```

---

## 주의사항

**itemName 검색은 부분 검색(LIKE)** 이므로 `소` 입력 시 "소고기", "소갈비" 등이 함께 조회됩니다.
사용성 향상을 위해 자동완성 또는 품목 선택 드롭다운 추가를 검토하세요.

**결과는 최대 100건**으로 제한되어 있으며, `collectedDate` 기준 최신순으로 정렬됩니다.

**데이터가 없는 경우** 빈 배열(`[]`)이 반환됩니다. 매일 오전 9시 수집되므로 당일 수집 전에는 전날 데이터가 최신입니다.

---

## 화면 구성 예시

```
[ 전체 ] [ 식량작물 ] [ 채소류 ] [ 특용작물 ] [ 과일류 ] [ 축산물 ] [ 수산물 ]

🔍 [품목명 검색창          ] [검색]

┌──────────┬────────┬────────┬────────┬──────────┬────────────┐
│ 품목명   │ 현재가 │ 최저가 │ 최고가 │ 단위     │ 수집일자   │
├──────────┼────────┼────────┼────────┼──────────┼────────────┤
│ 양파     │ 1,200  │ 1,100  │ 1,350  │ 1kg      │ 2026-04-09 │
│ 배추     │ 2,500  │ 2,300  │ 2,800  │ 1포기    │ 2026-04-09 │
└──────────┴────────┴────────┴────────┴──────────┴────────────┘
```
