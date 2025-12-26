package back.fcz.domain.capsule.service;

import back.fcz.domain.capsule.entity.CapsuleAttachment;
import back.fcz.domain.capsule.entity.CapsuleAttachmentStatus;
import back.fcz.domain.capsule.repository.CapsuleAttachmentRepository;
import back.fcz.infra.storage.FileStorage;
import back.fcz.infra.storage.PresignedUrlProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final CapsuleAttachmentRepository capsuleAttachmentRepository;
    private final FileStorage fileStorage;
    private final PresignedUrlProvider presignedUrlProvider;

    private static final Duration GET_EXPIRES = Duration.ofMinutes(15); // 이미지 다운로드용 presign URL 만료 시간


    @Transactional(readOnly = true)
    public String presignedDownload(Long memberId, Long attachmentId){
        CapsuleAttachment attachment = capsuleAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));

        return presignedUrlProvider.presignGet(
                attachment.getS3Key(),
                GET_EXPIRES
        );
    }

    @Transactional
    public void deleteTemp(Long memberId, Long attachmentId){
        CapsuleAttachment attachment = capsuleAttachmentRepository.findByIdAndUploaderId(attachmentId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));

        if (attachment.getStatus() != CapsuleAttachmentStatus.TEMP){
            throw new IllegalStateException("임시 첨부파일만 삭제할 수 있습니다.");
        }
        fileStorage.delete(attachment.getS3Key());
        attachment.markDeleted();
        capsuleAttachmentRepository.save(attachment);
    }
}
