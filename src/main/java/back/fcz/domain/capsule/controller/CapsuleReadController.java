package back.fcz.domain.capsule.controller;

import back.fcz.domain.capsule.dto.request.CapsuleReadRequestDto;
import back.fcz.domain.capsule.dto.response.CapsuleReadResponseDto;
import back.fcz.domain.capsule.entity.Capsule;
import back.fcz.domain.capsule.entity.CapsuleRecipient;
import back.fcz.domain.capsule.repository.CapsuleRecipientRepository;
import back.fcz.domain.capsule.service.CapsuleReadService;
import back.fcz.global.exception.BusinessException;
import back.fcz.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("/api/v1/capsule")
public class CapsuleReadController {
    private final CapsuleReadService capsuleReadService;
    private final CapsuleRecipientRepository capsuleRecipientRepository;

    // 개인 캡슐 조건 검증 + 읽기
    @GetMapping("/{capsuleId}")
    public ResponseEntity<CapsuleReadResponseDto> readCapsule(
            @RequestBody CapsuleReadRequestDto capsuleReadRequestDto
    ) {
        Capsule resultCapsule = capsuleReadService.getCapsule(capsuleReadRequestDto.capsuleId());
        //isProtected확인(0이면 수신자가 비회원  /  1이면 수신자가 회원)
        if(resultCapsule.isProtected()){  //isProtected=1 -> 회원

            if(capsuleReadService.phoneNumberVerification(resultCapsule, capsuleReadRequestDto.phoneNumber(),
                    capsuleReadRequestDto.unlockAt(), capsuleReadRequestDto.locationLat(), capsuleReadRequestDto.locationLng()))
            {
                CapsuleRecipient capsuleRecipient = capsuleRecipientRepository.findByCapsuleId_CapsuleId(resultCapsule.getCapsuleId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.CAPSULE_NOT_FOUND));

                //응답 Dto생성
                CapsuleReadResponseDto capsuleReadResponseDto = new CapsuleReadResponseDto(
                        resultCapsule.getCapsuleId(),
                        resultCapsule.getCapsuleColor(),
                        resultCapsule.getCapsulePackingColor(),
                        capsuleRecipient.getRecipientName(),
                        resultCapsule.getNickname(),
                        resultCapsule.getTitle(),
                        resultCapsule.getContent(),
                        resultCapsule.getCreatedAt(),
                        resultCapsule.getCurrentViewCount() > 0,
                        resultCapsule.getUnlockType(),
                        resultCapsule.getUpdatedAt(),
                        resultCapsule.getLocationName(),
                        resultCapsule.getLocationLat(),
                        resultCapsule.getLocationLng()
                );
                return ResponseEntity.ok(capsuleReadResponseDto);
            }
        } else{ //isProtected()가 0인경우 -> 비회원

            if(capsuleReadService.urlAndPasswordVerification(resultCapsule, capsuleReadRequestDto.password(), resultCapsule.getCapPassword(),
                    capsuleReadRequestDto.unlockAt(), capsuleReadRequestDto.locationLat(), capsuleReadRequestDto.locationLng()))
            {
                CapsuleRecipient capsuleRecipient = capsuleRecipientRepository.findByCapsuleId_CapsuleId(resultCapsule.getCapsuleId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.CAPSULE_NOT_FOUND));

                //응답 Dto생성
                CapsuleReadResponseDto capsuleReadResponseDto = new CapsuleReadResponseDto(
                        resultCapsule.getCapsuleId(),
                        resultCapsule.getCapsuleColor(),
                        resultCapsule.getCapsulePackingColor(),
                        capsuleRecipient.getRecipientName(),
                        resultCapsule.getNickname(),
                        resultCapsule.getTitle(),
                        resultCapsule.getContent(),
                        resultCapsule.getCreatedAt(),
                        resultCapsule.getCurrentViewCount() > 0,
                        resultCapsule.getUnlockType(),
                        resultCapsule.getUpdatedAt(),
                        resultCapsule.getLocationName(),
                        resultCapsule.getLocationLat(),
                        resultCapsule.getLocationLng());
                return ResponseEntity.ok(capsuleReadResponseDto);
            }
            
        }

        throw new BusinessException(ErrorCode.CAPSULE_CONDITION_ERROR);
    }

    //사용자가 열람한 적이 있는 캡슐이라면 조건없이 읽기(이건 그냥 읽기)


    //회원의 대시보드
}
