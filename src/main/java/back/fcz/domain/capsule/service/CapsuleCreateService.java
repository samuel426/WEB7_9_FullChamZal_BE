package back.fcz.domain.capsule.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CapsuleCreateService {

    // 캡슐 추가
    public long creatCapsule(Long Capsule){
        return Capsule;
    }


    // 캡슐 UUID 생성
    String getUUID(){
        String uuid = UUID.randomUUID().toString();
        System.out.println(uuid);
        return uuid;
    }

    // 캡슐 수정

    // 캡슐 삭제
}
