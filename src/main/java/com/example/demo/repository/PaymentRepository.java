package com.example.demo.repository;

import com.example.demo.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentRecord, Long> {
    Optional<PaymentRecord> findByOrderId(String orderId);
    Optional<PaymentRecord> findByRazorpayOrderId(String razorpayOrderId);
}
