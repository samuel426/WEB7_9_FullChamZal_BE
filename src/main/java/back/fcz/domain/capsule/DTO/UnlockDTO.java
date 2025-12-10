package back.fcz.domain.capsule.DTO;

import java.time.LocalDateTime;

public record UnlockDTO (
        LocalDateTime unlockAt,
        String location,
        GPSDTO gps,
        int viewingRadius
){}
