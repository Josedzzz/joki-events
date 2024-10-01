package com.uq.jokievents;

import com.uq.jokievents.dtos.UpdateAdminDTO;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.repository.AdminRepository;
import com.uq.jokievents.service.implementation.AdminServiceImpl;
import com.uq.jokievents.service.implementation.JwtServiceImpl;
import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.ApiTokenResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private JwtServiceImpl jwtService;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void testUpdateAdmin_Success() {
        // Mock data
        String adminId = "adminId123";
        UpdateAdminDTO dto = new UpdateAdminDTO("newUsername", "newEmail@example.com");
        Admin existingAdmin = new Admin();
        existingAdmin.setId(adminId);
        existingAdmin.setUsername("oldUsername");
        existingAdmin.setEmail("oldEmail@example.com");

        // Mock repository behavior
        Mockito.when(adminRepository.findById(adminId)).thenReturn(Optional.of(existingAdmin));
        Mockito.when(adminRepository.save(any(existingAdmin))).thenReturn(existingAdmin);
        Mockito.when(jwtService.getAdminToken((UserDetails) Matchers.any(UserDetails.class))).thenReturn("newToken");

        // Run the service method
        ResponseEntity<?> response = adminService.updateAdmin(adminId, dto);

        // Validate response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiTokenResponse<?> apiResponse = (ApiTokenResponse<?>) response.getBody();
        assert apiResponse != null;
        assertEquals("Success", apiResponse.getStatus());
        assertEquals("newUsername", ((Admin) apiResponse.getData()).getUsername());
        assertEquals("newToken", apiResponse.getToken());
    }

    private Admin any(Admin admin) {
        return Mockito.any(Admin.class);
    }



    @Test
    void testUpdateAdmin_AdminNotFound() {
        // Mock the Authentication and SecurityContext
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        // Set the mocked SecurityContext to the SecurityContextHolder
        SecurityContextHolder.setContext(securityContext);

        // Create a mock Admin object to simulate the logged-in admin
        Admin mockAdmin = new Admin();
        mockAdmin.setId("mockAdminId");
        mockAdmin.setUsername("mockUsername");
        mockAdmin.setEmail("mockEmail@example.com");

        // Return the mock Admin when getPrincipal is called
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockAdmin);

        // Create a list of authorities
        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ADMIN"));

        // Set the authorities to simulate the "ADMIN" role
        // Mockito.when(authentication.getAuthorities()).thenReturn(authorities);




        // Now, run the test for updateAdmin method
        // Mock the repository or other dependencies as needed
        Mockito.when(adminRepository.findById("mockAdminId")).thenReturn(Optional.empty());

        // Call the method under test
        ResponseEntity<?> response = adminService.updateAdmin("mockAdminId", new UpdateAdminDTO("newUsername", "newEmail@example.com"));

        // Assert the response (Admin not found case)
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(ApiResponse.class, response.getBody());
    }
    @Test
    void testUpdateAdmin_ValidationFails() {
        // Mock data
        String adminId = "adminId123";
        UpdateAdminDTO dto = new UpdateAdminDTO(null, null);
        Admin existingAdmin = new Admin();

        // Mock repository behavior
        Mockito.when(adminRepository.findById(adminId)).thenReturn(Optional.of(existingAdmin));

        // Run the service method
        ResponseEntity<?> response = adminService.updateAdmin(adminId, dto);

        // Validate response for null username
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertEquals("Error", apiResponse.getStatus());
        assertEquals("Username field is empty", apiResponse.getMessage());
    }

    @Test
    void testUpdateAdmin_InternalServerError() {
        // Mock data
        String adminId = "adminId123";
        UpdateAdminDTO dto = new UpdateAdminDTO("newUsername", "newEmail@example.com");

        // Mock repository behavior to throw exception
        Mockito.when(adminRepository.findById(adminId)).thenThrow(new RuntimeException());

        // Run the service method
        ResponseEntity<?> response = adminService.updateAdmin(adminId, dto);

        // Validate response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertEquals("Error", apiResponse.getStatus());
        assertEquals("Failed to update admin", apiResponse.getMessage());
    }
}


