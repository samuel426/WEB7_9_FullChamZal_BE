package back.fcz.domain.capsule.controller;

import back.fcz.domain.capsule.service.CapsuleCreateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/capsule")
public class CapsuleCreateController {

    private final CapsuleCreateService capsuleCreateService;

    // 캡슐 생성

    // 캡슐 수정

    // 캡슐 삭제

}