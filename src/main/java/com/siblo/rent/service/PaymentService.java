package com.siblo.rent.service;

import com.siblo.rent.entity.Booking;
import com.siblo.rent.entity.Payment;
import com.siblo.rent.entity.Payment.PaymentStatus;
import com.siblo.rent.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import com.midtrans.httpclient.error.MidtransError;
import com.siblo.rent.entity.User;
import com.siblo.rent.repository.BookingRepository;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
private final BookingRepository bookingRepository;
private final MidtransService midtransService;

public PaymentService(PaymentRepository paymentRepository,
                       BookingRepository bookingRepository,
                       MidtransService midtransService) {
    this.paymentRepository = paymentRepository;
    this.bookingRepository = bookingRepository;
    this.midtransService = midtransService;
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

    /**
     * Membuat transaksi Snap baru untuk sebuah booking.
     * Harga & data customer diambil server-side dari Booking + User.
     */
    @Transactional
    public Map<String, String> createPaymentForBooking(Long bookingId) throws MidtransError {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking dengan id " + bookingId + " tidak ditemukan"));

        Payment payment = paymentRepository.findByBookingId(bookingId).orElse(null);
        if (payment != null && payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Booking ini sudah lunas, tidak bisa dibuatkan transaksi baru");
        }

        User user = booking.getUser();
        String orderId = "SIBLO-" + bookingId + "-" + UUID.randomUUID().toString().substring(0, 8);

        JSONObject result = midtransService.createSnapTransaction(
                orderId,
                booking.getTotalPrice().longValue(),
                user.getName(),
                user.getEmail()
        );

        if (payment == null) {
            payment = new Payment();
            payment.setBooking(booking);
        }
        payment.setOrderId(orderId);
        payment.setAmount(booking.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        Map<String, String> response = new HashMap<>();
        response.put("token", result.getString("token"));
        response.put("redirect_url", result.optString("redirect_url", ""));
        response.put("order_id", orderId);
        return response;
    }

    /**
     * Dipanggil PaymentNotificationController setelah signature_key
     * terverifikasi valid.
     */
    @Transactional
    public void handleNotification(Map<String, Object> payload) {
        String orderId = String.valueOf(payload.get("order_id"));
        String transactionStatus = String.valueOf(payload.get("transaction_status"));
        String fraudStatus = String.valueOf(payload.get("fraud_status"));
        String paymentType = String.valueOf(payload.get("payment_type"));
        String transactionId = String.valueOf(payload.get("transaction_id"));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Payment dengan order_id " + orderId + " tidak ditemukan"));

        payment.setMethod(paymentType);
        payment.setTransactionId(transactionId);

        switch (transactionStatus) {
            case "capture" ->
                    payment.setStatus("accept".equals(fraudStatus) ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
            case "settlement" -> payment.setStatus(PaymentStatus.COMPLETED);
            case "cancel", "deny", "expire" -> payment.setStatus(PaymentStatus.FAILED);
            case "pending" -> payment.setStatus(PaymentStatus.PENDING);
            default -> {
                // status lain dari Midtrans, biarkan apa adanya
            }
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            payment.setPaidAt(LocalDateTime.now());
            Booking booking = payment.getBooking();
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }

        paymentRepository.save(payment);
    }
}
