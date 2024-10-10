package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.*;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {

    ResponseEntity<?> loginAdmin(AuthAdminDTO request);
    ResponseEntity<?> registerClient(RegisterClientDTO request);
    ResponseEntity<?> loginClient(LoginClientDTO request);
    ResponseEntity<?> sendRecoverPasswordCode(EmailDTO email);
    ResponseEntity<?> recoverPassword(RecoverPassDTO dto);
}
