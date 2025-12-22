package back.fcz.domain.capsule.DTO.response;

public record CapsuleLikeResponse(
        int likeCount,
        String message
) {
    public static CapsuleLikeResponse from(int likeCount, String message) {
        return new CapsuleLikeResponse(
                likeCount,
                message
        );
    }
}
