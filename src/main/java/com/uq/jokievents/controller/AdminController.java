package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.AuthAdminDTO;
import com.uq.jokievents.dtos.RecoverPassAdminDTO;
import com.uq.jokievents.dtos.UpdateAdminDTO;
import com.uq.jokievents.service.interfaces.AdminService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Example JSON:
     * {
     *  "username": "XD",
     *  "email": "mail@mail.com"
     * }
     * @param id String
     * @param dto UpdateAdminDTO
     * @return ResponseEntity
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable String id, @Valid @RequestBody UpdateAdminDTO dto) {
        return adminService.updateAdmin(id, dto);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAdminById(@PathVariable String id) {
        return adminService.deleteAdminById(id);
    }
    /**
     * Example JSON:
     * {
     *  "username": "XD",
     *  "password": "cool-non-encrypted-password"
     * }
     * @param dto AuthAdminDTO
     * @return ResponseEntity
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@Valid @RequestBody AuthAdminDTO dto) {
        return adminService.loginAdmin(dto);
    }

    @PostMapping("/send-recover-code")
    public ResponseEntity<?> sendRecoverCode(@RequestParam String email) {
        return adminService.sendRecoverPasswordCode(email);
    }

    /**
     * Example JSON:
     * {
     *  "email": "mail@mail.com",
     *  "verificationCode": "123456",
     *  "newPassword": "new-non-encrypted-password"
     * }
     * @param dto RecoverPassAdminDTO
     * @return ResponseEntity
     */
    @PostMapping("/recover-password")
    public ResponseEntity<?> recoverPassword(@Valid @RequestBody RecoverPassAdminDTO dto) {
        return adminService.recoverPassword(dto);
    }
}
