package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.*;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.model.Event;
import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.ApiTokenResponse;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminService {

    ApiTokenResponse<Object> updateAdmin(String adminId, UpdateAdminDTO dto);
    ApiResponse<UpdateAdminDTO> getAccountInformation(String adminId);
    ApiResponse<String> deleteAdminAccount(String adminId);
    ApiResponse<Coupon> createCoupon(CreateCouponDTO dto);
    ApiResponse<Coupon> updateCoupon(String couponId, UpdateCouponDTO dto);
    ApiResponse<String> deleteCoupon(String couponId);
    ApiResponse<String> deleteAllCoupons();
    ApiResponse<Event> addEvent(HandleEventDTO dto);
    ApiResponse<Map<String, Object>> getAllEventsPaginated(int page, int size);
    ApiResponse<Event> updateEvent(String id, HandleEventDTO dto);
    ApiResponse<String> deleteEvent(String id);
    ApiResponse<String> deleteAllEvents();
    ApiResponse<Map<String, Object>> getAllCouponsPaginated(int page, int size);
    ApiTokenResponse<Map<String, Object>> getAllAdmins() throws Exception;
    ByteArrayInputStream generateMonthlyEventReportPdf(int month, int year);
    List<EventReportDTO> generateMonthlyEventReport(int month, int year);
}


