package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.AuthAdminDTO;
import com.uq.jokievents.dtos.LoginClientDTO;
import com.uq.jokievents.dtos.RegisterClientDTO;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {

    ResponseEntity<?> loginAdmin(AuthAdminDTO request);
    ResponseEntity<?> registerClient(RegisterClientDTO request);
    ResponseEntity<?> loginClient(LoginClientDTO request);

}
