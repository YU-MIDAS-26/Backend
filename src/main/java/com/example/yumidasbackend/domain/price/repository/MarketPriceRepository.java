package com.example.yumidasbackend.domain.price.repository;

import com.example.yumidasbackend.domain.price.entity.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

    Optional<MarketPrice> findTopByItemNameOrderByCollectedDateDescCreatedAtDesc(String itemName);

    Optional<MarketPrice> findTopByItemNameOrderByMinPriceAscCollectedDateDesc(String itemName);

    List<MarketPrice> findTop100ByOrderByCollectedDateDescCreatedAtDesc();

    // 부류코드로 필터링
    List<MarketPrice> findTop100ByCategoryCodeOrderByCollectedDateDescCreatedAtDesc(String categoryCode);

    // 품목명 검색 (부분 일치)
    List<MarketPrice> findTop100ByItemNameContainingOrderByCollectedDateDescCreatedAtDesc(String itemName);

    // 부류코드 + 품목명 검색
    List<MarketPrice> findTop100ByCategoryCodeAndItemNameContainingOrderByCollectedDateDescCreatedAtDesc(
            String categoryCode, String itemName);
}
