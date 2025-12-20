package back.fcz.domain.openai.moderation.entity;

public enum ModerationDecision {
    ALLOW,          // flagged여도 일단 저장 허용(기록만 남김)
    BLOCK,          // flagged면 저장 차단
    FAIL_OPEN,      // OpenAI 장애/에러 시 통과시키고 기록만
    SKIP_NO_CLIENT  // api-key 미설정 등으로 검증 스킵
}
