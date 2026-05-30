package com.bsight.springserver.domain.price.repository;

import com.bsight.springserver.domain.price.entity.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

    // 특정 품목의 가장 최근 시세 1건
    Optional<MarketPrice> findTopByItemNameOrderByCollectedDateDesc(String itemName);

    // 최근 100건 (전체)
    List<MarketPrice> findTop100ByOrderByCollectedDateDesc();

    // 부류코드로 필터링
    List<MarketPrice> findTop100ByCategoryCodeOrderByCollectedDateDesc(String categoryCode);

    // 품목명 부분 검색
    List<MarketPrice> findTop100ByItemNameContainingOrderByCollectedDateDesc(String itemName);

    // 부류코드 + 품목명
    List<MarketPrice> findTop100ByCategoryCodeAndItemNameContainingOrderByCollectedDateDesc(
            String categoryCode, String itemName);

    // 중복 체크 (UNIQUE 제약 충돌 방지)
    Optional<MarketPrice> findByItemCodeAndKindCodeAndRankCodeAndProductClsCodeAndCollectedDate(
            String itemCode, String kindCode, String rankCode, String productClsCode, LocalDate collectedDate);
}
