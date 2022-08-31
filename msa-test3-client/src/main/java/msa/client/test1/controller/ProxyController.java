package msa.client.test1.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import msa.client.test1.vo.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
public class ProxyController {

    // http://localhost:8081/sso-resource-server/api/foos/
    @Value("${resourceserver.api.url}")
    private String fooApiUrl;

    @Autowired
    private WebClient webClient;

    @ResponseBody
    @GetMapping("/api2/members")
    public List<Member> getApiMembers(HttpServletRequest request) {

        WebClient webClient = WebClient
                .builder()
                .baseUrl(fooApiUrl)
//                .defaultCookie("쿠키","쿠키값")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Flux<Member> response  = webClient.get()
                .uri("/users")
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization","bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NjEyNDY0ODksInVzZXJfbmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiVVNFUiIsIkFETUlOIl0sImp0aSI6ImNiYWVlZmQwLThkYzctNDIwYi1hOGFkLWUyZDk3NDkyNTVlYSIsImNsaWVudF9pZCI6ImN1c3RvbTEiLCJzY29wZSI6WyJyZWFkIl19.VOZh8HrNTBq1XRcvcjAL9hoKO87d6vgvnUY9oIuiK6c")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Member.class)
                ;

        List<Member> members = response.collect(Collectors.toList()).block();

        return members;
    }

    @ResponseBody
    @GetMapping("/api3/{apiName}")
    public List<Object> getApiObjects(@PathVariable(value="apiName") String apiName) {

        WebClient webClient = WebClient
                .builder()
                .baseUrl(fooApiUrl)
//                .defaultCookie("쿠키","쿠키값")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Flux<Object> response  = webClient.get()
                .uri("/" + apiName)
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization","bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NjEyNDY0ODksInVzZXJfbmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiVVNFUiIsIkFETUlOIl0sImp0aSI6ImNiYWVlZmQwLThkYzctNDIwYi1hOGFkLWUyZDk3NDkyNTVlYSIsImNsaWVudF9pZCI6ImN1c3RvbTEiLCJzY29wZSI6WyJyZWFkIl19.VOZh8HrNTBq1XRcvcjAL9hoKO87d6vgvnUY9oIuiK6c")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Object.class)
                ;

        List<Object> members = response.collect(Collectors.toList()).block();

        return members;
    }

    @ResponseBody
    @PostMapping("/api3/{apiName}")
    public List<Object> postApiObjects(@PathVariable(value="apiName") String apiName, @RequestBody Flux<Object> request) {

        Flux<Object> reqFlux = (Flux<Object>) request;

        WebClient webClient = WebClient
                .builder()
                .baseUrl(fooApiUrl)
//                .defaultCookie("쿠키","쿠키값")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Flux<Object> response  = webClient.post()
                .uri("/" + apiName)
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization","bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NjEyNDY0ODksInVzZXJfbmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiVVNFUiIsIkFETUlOIl0sImp0aSI6ImNiYWVlZmQwLThkYzctNDIwYi1hOGFkLWUyZDk3NDkyNTVlYSIsImNsaWVudF9pZCI6ImN1c3RvbTEiLCJzY29wZSI6WyJyZWFkIl19.VOZh8HrNTBq1XRcvcjAL9hoKO87d6vgvnUY9oIuiK6c")
                .accept(MediaType.APPLICATION_JSON)
                .body(request, Object.class)
//                .body(BodyInserter.class.cast(request))
                .retrieve()
                .bodyToFlux(Object.class)
                ;

        List<Object> members = response.collect(Collectors.toList()).block();

        return members;
    }

    @GetMapping("/api/**")
    public ResponseEntity<byte[]> getproxy(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) byte[] body) throws IOException, URISyntaxException {

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(60000); //1m
        httpRequestFactory.setConnectTimeout(60000); //1m
        httpRequestFactory.setReadTimeout(60000); //1m

        // restTempate tobe bean
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);

        // url
        String originReqURL = request.getRequestURI().replaceAll("^/api", "");
        String originQueryString = request.getQueryString();
        String urlStr = fooApiUrl + originReqURL + (StringUtils.isEmpty(originQueryString) ? "" : "?"+originQueryString);

        URI url = new URI(urlStr);

        log.error(request.getRequestURI());
        log.error(url.toString());

        // method
//        String originMethod = request.getHeader("x-origin-method");
//        HttpMethod method = HttpMethod.valueOf(originMethod.toUpperCase());


        // header
        Enumeration<String> headerNames = request.getHeaderNames();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            headers.add(headerName, headerValue);
        }

        // http entity (body, header)
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, HttpMethod.GET, httpEntity, byte[].class);
    }

    @PostMapping("/api/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) byte[] body) throws IOException, URISyntaxException {

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(60000); //1m
        httpRequestFactory.setConnectTimeout(60000); //1m
        httpRequestFactory.setReadTimeout(60000); //1m

        // restTempate tobe bean
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);

        // url
        String originReqURL = request.getRequestURI().replaceAll("^/api", "");
        String originQueryString = request.getQueryString();
        String urlStr = fooApiUrl + originReqURL + (StringUtils.isEmpty(originQueryString) ? "" : "?"+originQueryString);

        URI url = new URI(urlStr);

        log.error(request.getRequestURI());
        log.error(url.toString());

        // method
//        String originMethod = request.getHeader("x-origin-method");
//        HttpMethod method = HttpMethod.valueOf(originMethod.toUpperCase());


        // header
        Enumeration<String> headerNames = request.getHeaderNames();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            headers.add(headerName, headerValue);
        }

        // http entity (body, header)
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, HttpMethod.POST, httpEntity, byte[].class);
    }

    @PutMapping("/api/**")
    public ResponseEntity<byte[]> putproxy(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) byte[] body) throws IOException, URISyntaxException {

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(60000); //1m
        httpRequestFactory.setConnectTimeout(60000); //1m
        httpRequestFactory.setReadTimeout(60000); //1m

        // restTempate tobe bean
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);

        // url
        String originReqURL = request.getRequestURI().replaceAll("^/api", "");
        String originQueryString = request.getQueryString();
        String urlStr = fooApiUrl + originReqURL + (StringUtils.isEmpty(originQueryString) ? "" : "?"+originQueryString);

        URI url = new URI(urlStr);

        log.error(request.getRequestURI());
        log.error(url.toString());

        // method
//        String originMethod = request.getHeader("x-origin-method");
//        HttpMethod method = HttpMethod.valueOf(originMethod.toUpperCase());


        // header
        Enumeration<String> headerNames = request.getHeaderNames();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            headers.add(headerName, headerValue);
        }

        // http entity (body, header)
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, HttpMethod.PUT, httpEntity, byte[].class);
    }

    @DeleteMapping("/api/**")
    public ResponseEntity<byte[]> deleteproxy(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) byte[] body) throws IOException, URISyntaxException {

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(60000); //1m
        httpRequestFactory.setConnectTimeout(60000); //1m
        httpRequestFactory.setReadTimeout(60000); //1m

        // restTempate tobe bean
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);

        // url
        String originReqURL = request.getRequestURI().replaceAll("^/api", "");
        String originQueryString = request.getQueryString();
        String urlStr = fooApiUrl + originReqURL + (StringUtils.isEmpty(originQueryString) ? "" : "?"+originQueryString);

        URI url = new URI(urlStr);

        log.error(request.getRequestURI());
        log.error(url.toString());

        // method
//        String originMethod = request.getHeader("x-origin-method");
//        HttpMethod method = HttpMethod.valueOf(originMethod.toUpperCase());


        // header
        Enumeration<String> headerNames = request.getHeaderNames();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            headers.add(headerName, headerValue);
        }

        // http entity (body, header)
        HttpEntity<byte[]> httpEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, byte[].class);
    }

    @RequestMapping("/members")
    public String getMembers(Model model) {

        WebClient webClient = WebClient
                .builder()
                .baseUrl(fooApiUrl)
//                .defaultCookie("쿠키","쿠키값")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Flux<Member> response  = webClient.get()
                .uri("/users")
//                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization","bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2NjEyNDY0ODksInVzZXJfbmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiVVNFUiIsIkFETUlOIl0sImp0aSI6ImNiYWVlZmQwLThkYzctNDIwYi1hOGFkLWUyZDk3NDkyNTVlYSIsImNsaWVudF9pZCI6ImN1c3RvbTEiLCJzY29wZSI6WyJyZWFkIl19.VOZh8HrNTBq1XRcvcjAL9hoKO87d6vgvnUY9oIuiK6c")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Member.class)
                ;

        List<Member> members = response.collect(Collectors.toList()).block();

        model.addAttribute("members", members);
        return "members";
    }

}
