package com.uq.jokievents.service.interfaces;

import com.google.firebase.auth.FirebaseToken;
import com.uq.jokievents.dtos.*;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AuthenticationService {

    Map<Admin, String> loginAdmin(AuthAdminDTO request);
    Map<Client, String> registerClient(RegisterClientDTO request);
    Map<Client, String> loginClient(LoginClientDTO request);
    String sendRecoverPasswordCode(EmailDTO email);
    void recoverPassword(RecoverPassDTO dto);
    Map<String, String> registerUserIfNotExists(FirebaseToken decodedToken);
    Map<String, String> googleLogin(String idToken);
}