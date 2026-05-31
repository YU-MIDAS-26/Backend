-- 시간대 설정
SET time_zone = '+09:00';

-- (선택) 기본 사용자 시드
-- INSERT INTO users (...) VALUES (...);

-- (선택) 테스트용 시세 시드는 KAMIS 수집 API로 처리
-- (선택) 판매전표 시드는 CSV 업로드로 처리

-- 권한 재확인 (이미 docker가 처리하지만 안전장치)
GRANT ALL PRIVILEGES ON bsight.* TO 'midas'@'%';
FLUSH PRIVILEGES;
