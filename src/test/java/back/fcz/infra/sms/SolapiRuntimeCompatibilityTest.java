package back.fcz.infra.sms;

import com.solapi.sdk.message.lib.Authenticator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SolapiRuntimeCompatibilityTest {

    @Test
    @DisplayName("SOLAPI SDK가 사용하는 Kotlin 런타임으로 인증 헤더를 생성한다")
    void generatesAuthenticationHeader() {
        String authHeader = new Authenticator("test-api-key", "test-api-secret")
                .generateAuthInfo();

        assertThat(authHeader).isNotBlank();
    }
}
