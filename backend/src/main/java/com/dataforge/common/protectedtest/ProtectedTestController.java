package com.dataforge.common.protectedtest;

import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protected")
public class ProtectedTestController {

    @GetMapping
    public ResponseEntity<ProtectedTestResponse> protectedTest(Principal principal) {
        return ResponseEntity.ok(new ProtectedTestResponse("AUTHENTICATED", principal.getName()));
    }
}
