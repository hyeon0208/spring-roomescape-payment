package roomescape.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentMethod;
import roomescape.reservation.domain.Reservation;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(String paymentKey, String orderId, String status, String method, int totalAmount) {

    public Payment from(Reservation reservation) {
        return new Payment(reservation, paymentKey, orderId, status, PaymentMethod.from(method), totalAmount);
    }
}
