package back.fcz.domain.capsule.service;

import back.fcz.domain.capsule.DTO.request.CapsuleCreateRequestDTO;
import back.fcz.domain.capsule.DTO.request.SecretCapsuleCreateRequestDTO;
import back.fcz.domain.capsule.DTO.response.CapsuleCreateResponseDTO;
import back.fcz.domain.capsule.DTO.response.SecretCapsuleCreateResponseDTO;
import back.fcz.domain.capsule.entity.Capsule;
import back.fcz.domain.capsule.repository.CapsuleRepository;
import back.fcz.domain.member.entity.Member;
import back.fcz.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CapsuleCreateService {

    private final CapsuleRepository capsuleRepository;
    private final MemberRepository memberRepository;


    // UUID 생성
    public String setUUID(){
        return UUID.randomUUID().toString();
    }

    // 공개 캡슐 생성
    public CapsuleCreateResponseDTO publicCapsuleCreate(CapsuleCreateRequestDTO capsuleCreate){
        Capsule capsule = capsuleCreate.toEntity();
        capsule.setUuid(setUUID());
        Capsule saved = capsuleRepository.save(capsule);

        return CapsuleCreateResponseDTO.from(saved);
    }

    // 비공개 캡슐 생성 - URL + 비밀번호 조회
    public SecretCapsuleCreateResponseDTO selfCreateCapsule(SecretCapsuleCreateRequestDTO capsuleCreate, String password){

        Member member = memberRepository.findById(capsuleCreate.memberId())
                .orElseThrow(() -> new RuntimeException("Member not found")); // 에러코드 작성

        Capsule secretCapsule = capsuleCreate.toEntity();

        secretCapsule.setUuid(setUUID());
        secretCapsule.setCapPassword(password);
        secretCapsule.setMemberId(member);

        Capsule saved = capsuleRepository.save(secretCapsule);

        String url  = /*도메인*/ "http://localhost:8080/api/v1/capsule/" + secretCapsule.getUuid();

        return SecretCapsuleCreateResponseDTO.from(saved, url);
    }

    // 비공개 캡슐 생성 - 전화 번호 조회
    public SecretCapsuleCreateResponseDTO creatCapsule(SecretCapsuleCreateRequestDTO capsuleCreate, String receiveTel){

    }

    // 캡슐 수정

    // 캡슐 삭제
}
