/**
 * 배치 처리 트리거 (userLimit 지원)
 */
public Map<String, Object> triggerBatch(String batchType, Integer userLimit) {
    try {
        String url = aiServiceUrl + "/api/v1/batch/trigger?batchType=" + batchType;
        if (userLimit != null) {
            url += "&userLimit=" + userLimit;
        }
        
        log.info("🔧 AI 배치 트리거 호출: url={}", url);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
        Map<String, Object> responseBody = response.getBody();
        
        log.info("✅ AI 배치 트리거 응답: {}", responseBody);
        return responseBody;
        
    } catch (Exception e) {
        log.error("❌ AI 배치 트리거 호출 실패: {}", e.getMessage());
        throw new RuntimeException("배치 트리거 호출 실패: " + e.getMessage(), e);
    }
} 