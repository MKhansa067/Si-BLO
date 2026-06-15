package com.siblo.rent.service;

import com.siblo.rent.entity.Booking;
import com.siblo.rent.entity.Payment;
import com.siblo.rent.entity.Payment.PaymentStatus;
import com.siblo.rent.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment completePayment(Booking booking) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setMethod("SIMULATED");
        payment.setPaidAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    @Transactional
    public void refundPayment(Booking booking) {
        paymentRepository.findByBookingId(booking.getId()).ifPresent(p -> {
            p.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(p);
        });
    }
}
