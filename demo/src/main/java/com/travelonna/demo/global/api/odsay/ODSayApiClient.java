package com.travelonna.demo.global.api.odsay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ODSayApiClient {

    private final RestTemplate restTemplate;
    private final Environment environment;
    private final ObjectMapper objectMapper;
    
    @Value("${odsay.api.key.server}")
    private String serverApiKey;
    
    // @Value("${odsay.api.key.service}")
    // private String serviceApiKey;
    
    private static final String BASE_URL = "https://api.odsay.com/v1/api";
    private static final String PUBLIC_IP_CHECK_URL = "https://checkip.amazonaws.com";
    private static final String EC2_METADATA_URL = "http://169.254.169.254/latest/meta-data/public-ipv4";
    
    /**
     * RestTemplate 인터셉터를 설정하여 요청과 응답 로깅
     */
    @PostConstruct
    public void init() {
        // BufferingClientHttpRequestFactory 사용하여 응답 본문을 여러 번 읽을 수 있게 함
        restTemplate.setRequestFactory(
            new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory())
        );
        
        // 요청/응답 로깅 인터셉터 추가
        List<ClientHttpRequestInterceptor> interceptors = Collections.singletonList(
            (request, body, execution) -> {
                // 요청 정보 로깅
                log.info("===== ODSay API 요청 정보 =====");
                log.info("요청 URL: {}", request.getURI());
                log.info("요청 메소드: {}", request.getMethod());
                log.info("요청 헤더:");
                request.getHeaders().forEach((key, value) -> log.info("  - {}: {}", key, value));
                
                if (body.length > 0) {
                    log.info("요청 본문: {}", new String(body));
                }
                
                // 실제 요청 실행
                org.springframework.http.client.ClientHttpResponse response = execution.execute(request, body);
                
                // 응답 정보 로깅
                log.info("===== ODSay API 응답 정보 =====");
                log.info("응답 상태 코드: {}", response.getStatusCode());
                log.info("응답 헤더:");
                response.getHeaders().forEach((key, value) -> log.info("  - {}: {}", key, value));
                
                return response;
            }
        );
        
        restTemplate.setInterceptors(interceptors);
        log.info("ODSay API 클라이언트 요청/응답 로깅 인터셉터 설정 완료");
    }
    
    /**
     * EC2 메타데이터에서 퍼블릭 IP 주소를 가져옵니다.
     * 
     * @return EC2 퍼블릭 IP 주소 또는 에러 발생 시 외부 서비스로 확인
     */
    private String getEC2PublicIp() {
        try {
            URL url = new URL(EC2_METADATA_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000); // 짧은 타임아웃 (EC2 아닌 환경에서는 빨리 실패하도록)
            connection.setReadTimeout(1000);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String publicIp = reader.readLine().trim();
            reader.close();
            
            log.info("EC2 메타데이터에서 퍼블릭 IP 가져옴: {}", publicIp);
            return publicIp;
        } catch (Exception e) {
            log.info("EC2 메타데이터에서 IP 가져오기 실패, 외부 서비스 사용");
            return getPublicIpFromExternalService();
        }
    }
    
    /**
     * 외부 서비스를 통해 퍼블릭 IP 주소를 가져옵니다.
     * 
     * @return 퍼블릭 IP 주소 또는 에러 발생 시 "unknown"
     */
    private String getPublicIpFromExternalService() {
        try {
            URL url = new URL(PUBLIC_IP_CHECK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String publicIp = reader.readLine().trim();
            reader.close();
            
            log.info("외부 서비스에서 퍼블릭 IP 가져옴: {}", publicIp);
            return publicIp;
        } catch (Exception e) {
            log.error("퍼블릭 IP 주소 조회 중 오류 발생", e);
            return "unknown";
        }
    }
    
    /**
     * 현재 환경에 맞는 API 키 반환
     * 개발 환경: server API key
     * 운영 환경: service API key
     * 
     * @return 현재 환경에 적합한 API 키
     */
    private String getApiKey() {
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isProduction = false;
        
        for (String profile : activeProfiles) {
            if (profile.equals("prod") || profile.equals("production")) {
                isProduction = true;
                break;
            }
        }
        
        // 항상 server API key 사용
        String apiKey = serverApiKey;
        log.info("현재 환경: {}, 서버 API Key 사용: {}", isProduction ? "운영" : "개발", apiKey.substring(0, 5) + "...");
        return apiKey;
    }
    
    /**
     * 직접 HttpURLConnection을 사용하여 API 호출
     * 
     * @param urlStr API URL
     * @return API 응답 (JSON)
     */
    private JsonNode callApiWithUrlConnection(String urlStr) {
        try {
            log.info("API 호출: {}", urlStr);
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            // 헤더 설정
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.61 Safari/537.36");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Origin", "http://travelonna.shop");
            connection.setRequestProperty("Referer", "http://travelonna.shop/");
            
            log.info("===== API 요청 정보 =====");
            log.info("요청 URL: {}", urlStr);
            log.info("요청 메소드: GET");
            log.info("요청 헤더:");
            connection.getRequestProperties().forEach((key, value) -> log.info("  - {}: {}", key, value));
            
            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            log.info("응답 코드: {}", responseCode);
            
            // 응답 읽기
            StringBuilder response = new StringBuilder();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                String responseBody = response.toString();
                log.info("API 응답(원본): {}", responseBody);
                
                // JSON 파싱
                if (responseBody != null && !responseBody.isEmpty()) {
                    JsonNode jsonResponse = objectMapper.readTree(responseBody);
                    
                    // 에러 응답인지 확인
                    if (jsonResponse.has("error")) {
                        log.error("API 에러 응답: {}", jsonResponse.get("error"));
                    }
                    
                    return jsonResponse;
                } else {
                    log.error("응답 본문이 비어 있습니다.");
                    return null;
                }
            } else {
                // 에러 응답 처리
                BufferedReader err = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String errLine;
                
                while ((errLine = err.readLine()) != null) {
                    response.append(errLine);
                }
                err.close();
                
                log.error("API 응답 코드 오류: {}, 응답: {}", responseCode, response.toString());
                return null;
            }
        } catch (Exception e) {
            log.error("API 호출 중 오류 발생", e);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 출발지와 도착지로 대중교통 정보 조회
     * 
     * @param srcName 출발지 이름
     * @param dstName 도착지 이름
     * @param date 출발 날짜
     * @param transportType 이동 수단 타입(car, bus, train, etc)
     * @return API 응답 (JSON)
     */
    public JsonNode searchTransportation(String srcName, String dstName, LocalDate date, String transportType) {
        log.info("ODSay API 호출: 출발지 {}, 도착지 {}, 날짜 {}, 이동수단 {}", srcName, dstName, date, transportType);
        
        String apiKey = getApiKey();
        log.info("API Key(원본): {}", apiKey);
        
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        try {
            // 퍼블릭 및 프라이빗 IP 정보 가져오기
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            String hostName = localHost.getHostName();
            String privateIp = localHost.getHostAddress();
            String publicIp = getEC2PublicIp();
            
            log.info("호출 호스트 정보:");
            log.info("- 호스트명: {}", hostName);
            log.info("- 프라이빗 IP: {}", privateIp);
            log.info("- 퍼블릭 IP: {}", publicIp);
            log.info("⚠️ 중요: ODSay API 인증을 위해서는 이 퍼블릭 IP({})를 ODSay API 관리자 페이지에 등록해야 합니다!", publicIp);
            
            // URL 직접 구성 (수동 인코딩)
            String encodedSrcName = encode(srcName);
            String encodedDstName = encode(dstName);
            
            String urlStr = BASE_URL + "/searchPubTransPath"
                    + "?apiKey=" + apiKey
                    + "&SX="
                    + "&SY="
                    + "&EX="
                    + "&EY="
                    + "&OPT=" + getOptByTransportType(transportType)
                    + "&SearchPathType=0"
                    + "&SearchDate=" + formattedDate;
            
            return callApiWithUrlConnection(urlStr);
        } catch (Exception e) {
            log.error("대중교통 정보 조회 API 호출 중 오류 발생", e);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 터미널/역 ID 조회
     * 
     * @param terminalName 터미널/역 이름 (예: "대구", "서울", "동대구" 등)
     * @param lang 언어 코드 (0: 국문)
     * @return API 응답 (JSON)
     */
    public JsonNode getTrainTerminals(String terminalName, String lang) {
        log.info("ODSay API 호출: 터미널/역 검색 {}, 언어 {}", terminalName, lang);
        
        String apiKey = getApiKey();
        log.info("API Key(원본): {}", apiKey);
        
        try {
            // 퍼블릭 및 프라이빗 IP 정보 가져오기
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            String hostName = localHost.getHostName();
            String privateIp = localHost.getHostAddress();
            String publicIp = getEC2PublicIp();
            
            log.info("호출 호스트 정보:");
            log.info("- 호스트명: {}", hostName);
            log.info("- 프라이빗 IP: {}", privateIp);
            log.info("- 퍼블릭 IP: {}", publicIp);
            log.info("⚠️ 중요: ODSay API 인증을 위해서는 이 퍼블릭 IP({})를 ODSay API 관리자 페이지에 등록해야 합니다!", publicIp);
            
            // URL 구성 - 한글 파라미터 인코딩
            String encodedTerminalName = encode(terminalName);
            log.info("인코딩 전 역 이름: {}, 인코딩 후: {}", terminalName, encodedTerminalName);
            
            String urlStr = BASE_URL + "/trainTerminals" 
                    + "?apiKey=" + apiKey
                    + "&lang=" + lang
                    + "&terminalName=" + encodedTerminalName;
            
            return callApiWithUrlConnection(urlStr);
        } catch (Exception e) {
            log.error("터미널/역 검색 API 호출 중 오류 발생", e);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 기차 시간표 조회
     * 
     * @param startStationID 출발역 ID
     * @param endStationID 도착역 ID
     * @param lang 언어 코드 (0: 국문)
     * @return API 응답 (JSON)
     */
    public JsonNode getTrainServiceTime(String startStationID, String endStationID, String lang) {
        log.info("ODSay API 호출: 기차 시간표 조회 출발역ID {}, 도착역ID {}, 언어 {}", startStationID, endStationID, lang);
        
        String apiKey = getApiKey();
        log.info("API Key(원본): {}", apiKey);
        
        try {
            // 퍼블릭 및 프라이빗 IP 정보 가져오기
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            String hostName = localHost.getHostName();
            String privateIp = localHost.getHostAddress();
            String publicIp = getEC2PublicIp();
            
            log.info("호출 호스트 정보:");
            log.info("- 호스트명: {}", hostName);
            log.info("- 프라이빗 IP: {}", privateIp);
            log.info("- 퍼블릭 IP: {}", publicIp);
            log.info("⚠️ 중요: ODSay API 인증을 위해서는 이 퍼블릭 IP({})를 ODSay API 관리자 페이지에 등록해야 합니다!", publicIp);
            
            // URL 직접 구성
            String urlStr = BASE_URL + "/trainServiceTime"
                    + "?apiKey=" + apiKey
                    + "&lang=" + lang
                    + "&startStationID=" + startStationID
                    + "&endStationID=" + endStationID;
            
            log.info("기차 시간표 조회 API URL: {}", urlStr);
            return callApiWithUrlConnection(urlStr);
        } catch (Exception e) {
            log.error("기차 시간표 조회 API 호출 중 오류 발생", e);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 교통 수단 타입에 따른 ODSay API 옵션값 반환
     */
    private String getOptByTransportType(String transportType) {
        if (transportType == null) {
            return "0"; // 전체
        }
        
        return switch (transportType) {
            case "bus" -> "1"; // 버스
            case "train" -> "2"; // 기차
            case "car" -> "4"; // 자동차
            default -> "0"; // 기타(전체)
        };
    }
    
    /**
     * 문자열을 URL 인코딩합니다.
     * 
     * @param value 인코딩할 문자열
     * @return 인코딩된 문자열
     */
    private String encode(String value) {
        try {
            if (value == null) {
                return "";
            }
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("URL 인코딩 중 오류 발생", e);
            return value;
        }
    }
    
    /**
     * 고속버스 터미널 ID 조회
     * 
     * @param terminalName 터미널 이름 (예: "동서울", "서울", "대구" 등)
     * @param lang 언어 코드 (0: 국문)
     * @return API 응답 (JSON)
     */
    public JsonNode getExpressBusTerminals(String terminalName, String lang) {
        log.info("ODSay API 호출: 고속버스 터미널 검색 {}, 언어 {}", terminalName, lang);
        
        String apiKey = getApiKey();
        log.info("API Key(원본): {}", apiKey);
        
        try {
            // 퍼블릭 및 프라이빗 IP 정보 가져오기
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            String hostName = localHost.getHostName();
            String privateIp = localHost.getHostAddress();
            String publicIp = getEC2PublicIp();
            
            log.info("호출 호스트 정보:");
            log.info("- 호스트명: {}", hostName);
            log.info("- 프라이빗 IP: {}", privateIp);
            log.info("- 퍼블릭 IP: {}", publicIp);
            log.info("⚠️ 중요: ODSay API 인증을 위해서는 이 퍼블릭 IP({})를 ODSay API 관리자 페이지에 등록해야 합니다!", publicIp);
            
            // URL 구성 - 한글 파라미터 인코딩
            String encodedTerminalName = encode(terminalName);
            log.info("인코딩 전 터미널 이름: {}, 인코딩 후: {}", terminalName, encodedTerminalName);
            
            String urlStr = BASE_URL + "/expressBusTerminals" 
                    + "?apiKey=" + apiKey
                    + "&lang=" + lang
                    + "&terminalName=" + encodedTerminalName;
            
            return callApiWithUrlConnection(urlStr);
        } catch (Exception e) {
            log.error("고속버스 터미널 검색 API 호출 중 오류 발생", e);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 고속버스 시간표 조회
     * 
     * @param startStationID 출발 터미널 ID
     * @param endStationID 도착 터미널 ID
     * @param searchDate 검색 날짜 (yyyyMMdd 형식)
     * @param lang 언어 코드 (0: 국문)
     * @return API 응답 (JSON)
     */
    public JsonNode getExpressBusServiceTime(String startStationID, String endStationID, String searchDate, String lang) {
        log.info("ODSay API 호출: 고속버스 시간표 조회 출발역ID {}, 도착역ID {}, 날짜 {}, 언어 {}", 
                startStationID, endStationID, searchDate, lang);
        
        String apiKey = getApiKey();
        
        try {
            // 퍼블릭 및 프라이빗 IP 정보 가져오기
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            String hostName = localHost.getHostName();
            String privateIp = localHost.getHostAddress();
            String publicIp = getEC2PublicIp();
            
            log.info("호출 호스트 정보:");
            log.info("- 호스트명: {}", hostName);
            log.info("- 프라이빗 IP: {}", privateIp);
            log.info("- 퍼블릭 IP: {}", publicIp);
            
            // URL 직접 구성 - 실제 API 요구사항에 맞게 파라미터 이름 구성
            String urlStr = BASE_URL + "/expressServiceTime"
                    + "?apiKey=" + apiKey
                    + "&lang=" + lang
                    + "&startStationID=" + startStationID
                    + "&endStationID=" + endStationID;
            
            // 날짜가 있으면 추가
            if (searchDate != null && !searchDate.isEmpty()) {
                urlStr += "&searchDate=" + searchDate;
            }
            
            log.info("고속버스 시간표 조회 API URL: {}", urlStr.replaceAll(apiKey, "API_KEY_MASKED"));
            JsonNode response = callApiWithUrlConnection(urlStr);
            
            // 응답 디버깅용 로깅
            if (response != null) {
                if (response.has("result") && response.get("result").has("station")) {
                    JsonNode stations = response.get("result").get("station");
                    log.info("응답 정보: station 필드 있음, 개수: {}", stations.size());
                    
                    if (stations.isArray() && stations.size() > 0) {
                        JsonNode firstStation = stations.get(0);
                        if (firstStation.has("schedule")) {
                            String scheduleStr = firstStation.get("schedule").asText();
                            log.info("첫 시간표 샘플: {}", scheduleStr.length() > 50 ? scheduleStr.substring(0, 50) + "..." : scheduleStr);
                        }
                    }
                } else {
                    log.warn("응답에 station 필드가 없거나 유효하지 않습니다.");
                }
            }
            
            return response;
        } catch (Exception e) {
            log.error("고속버스 시간표 조회 API 호출 중 오류 발생", e);
            e.printStackTrace();
            return null;
        }
    }
} 