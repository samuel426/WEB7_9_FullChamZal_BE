package back.fcz.infra.sms;

import back.fcz.global.exception.BusinessException;
import back.fcz.global.exception.ErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsSender {

    private final CoolSmsClient coolSmsClient;

    @RateLimiter(name = "coolsmsSend")
    @CircuitBreaker(name = "coolsmsSend", fallbackMethod = "fallbackSend")
    public void send(String phoneNumber, String message) {
        coolSmsClient.sendSms(phoneNumber, message);
    }

    // CircuitBreaker OPEN or 연쇄 장애 시 여기로 떨어짐
    private void fallbackSend(String phoneNumber, String message, Throwable t) {
        if (t instanceof BusinessException businessException) {
            throw businessException;
        }

        log.error(
                "SMS provider call unavailable: exceptionType={}, message={}",
                t.getClass().getSimpleName(),
                t.getMessage(),
                t
        );
        BusinessException businessException = new BusinessException(ErrorCode.SMS_PROVIDER_UNAVAILABLE);
        businessException.initCause(t);
        throw businessException;
    }
}
