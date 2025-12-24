package back.fcz.global.aop;

import back.fcz.domain.member.service.CurrentUserContext;
import back.fcz.global.dto.InServerMemberResponse;
import back.fcz.global.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class DuplicateRequestAspect {
    private final CurrentUserContext currentUserContext;

    private Set<String> requestSet = Collections.synchronizedSet(new HashSet());

    @Pointcut("within(*..*Controller)")
    public void onRequest() {}

    public Optional<InServerMemberResponse> tryGetCurrentUser() {
        try {
            return Optional.of(currentUserContext.getCurrentUser());
        } catch (BusinessException e) {
            return Optional.empty();
        }
    }

    @Around("onRequest()")
    public Object duplicateRequestCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String httpMethod = request.getMethod();

        // GET 메소드인 경우 중복 체크를 하지 않음
        if ("GET".equalsIgnoreCase(httpMethod)) {
            return joinPoint.proceed();
        }

        // 회원 여부에 따라 memebrId / null로 값이 다름
        Optional<InServerMemberResponse> optionalUser = tryGetCurrentUser();

        String requestId;

        if(optionalUser.isPresent()){ // 로그인 상태
            Long memberId = optionalUser.get().memberId(); // memberId를 식별자로 사용

            requestId = memberId + ":" +
                    request.getMethod() + ":" +
                    request.getRequestURI();
        }else{ // 비로그인 상태일 때
            String clientKey = request.getRemoteAddr() + ":" +
                    request.getHeader("User-Agent"); // 요청을 보낸 HTTP IP / user Agent 를 식별자로 사용
            requestId =
                    clientKey + ":" +
                            request.getMethod() + ":" +
                            request.getRequestURI();
        }

        if (requestSet.contains(requestId)) {
            // 중복 요청인 경우
            return handleDuplicateRequest();
        }
        requestSet.add(requestId);
        try {
            // 핵심 로직 실행
            return joinPoint.proceed();
        } finally {
            // 실행 완료 후 삭제
            requestSet.remove(requestId);
        }
    }

    private ResponseEntity<Object> handleDuplicateRequest() {
        // 중복 요청에 대한 응답 처리
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("중복된 요청 입니다");
    }
}
