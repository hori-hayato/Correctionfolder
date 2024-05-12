package com.example.samuraitravel.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReservationRegisterForm;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class StripeService {
    @Value("${stripe.subscription.price.id}")
    private String stripePriceId;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    public StripeService(ReservationService reservationService, PaymentService paymentService, SubscriptionService subscriptionService) {
        this.reservationService = reservationService;
        this.paymentService = paymentService;
        this.subscriptionService = subscriptionService;
    }

    // サブスクリプションセッションを作成し、Stripeに必要な情報を返す
    public String checkoutSubscription(User user, HttpServletRequest httpServletRequest) {
        Stripe.apiKey = stripeApiKey;
        String requestUrl = new String(httpServletRequest.getRequestURL());
        SessionCreateParams params =
            SessionCreateParams.builder()
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .addLineItem(
                         SessionCreateParams.LineItem.builder()
                         .setPrice(stripePriceId)
                         .setQuantity(1L)
                         .build())
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setSuccessUrl(requestUrl.replaceAll("/user/upgrade", "") + "/user/upgrade?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(requestUrl.replace("/user/upgrade", ""))
            .build();
        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            e.printStackTrace();
            return "";
        }
    }

    // セッションを作成し、Stripeに必要な情報を返す
    public String createStripeSession(String houseName, ReservationRegisterForm reservationRegisterForm, HttpServletRequest httpServletRequest) {
        Stripe.apiKey = stripeApiKey;
        String requestUrl = new String(httpServletRequest.getRequestURL());
        SessionCreateParams params =
            SessionCreateParams.builder()
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .addLineItem(
                         SessionCreateParams.LineItem.builder()
                         .setPriceData(
                                       SessionCreateParams.LineItem.PriceData.builder()
                                       .setProductData(
                                                       SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                       .setName(houseName)
                                                       .build())
                                       .setUnitAmount((long)reservationRegisterForm.getAmount())
                                       .setCurrency("jpy")
                                       .build())
                         .setQuantity(1L)
                         .build())
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(requestUrl.replaceAll("/houses/[0-9]+/reservations/confirm", "") + "/reservations?reserved")
            .setCancelUrl(requestUrl.replace("/reservations/confirm", ""))
            .setPaymentIntentData(
                                  SessionCreateParams.PaymentIntentData.builder()
                                  .putMetadata("houseId", reservationRegisterForm.getHouseId().toString())
                                  .putMetadata("userId", reservationRegisterForm.getUserId().toString())
                                  .putMetadata("checkinDate", reservationRegisterForm.getCheckinDate())
                                  .putMetadata("checkoutDate", reservationRegisterForm.getCheckoutDate())
                                  .putMetadata("numberOfPeople", reservationRegisterForm.getNumberOfPeople().toString())
                                  .putMetadata("amount", reservationRegisterForm.getAmount().toString())
                                  .build())
            .build();
        try {
            Session session = Session.create(params);
            return session.getId();
        } catch (StripeException e) {
            e.printStackTrace();
            return "";
        }
    }

    // セッションから予約情報を取得し、ReservationServiceクラスを介してデータベースに登録する
    public void processSessionCompleted(Event event) {
        Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();
        optionalStripeObject.ifPresent(stripeObject -> {
            Session session = (Session)stripeObject;
            SessionRetrieveParams params = SessionRetrieveParams.builder().addExpand("payment_intent").build();

            try {
                session = Session.retrieve(session.getId(), params, null);
                var intent = session.getPaymentIntentObject();
                if (intent != null) {
                    Map<String, String> paymentIntentObject = intent.getMetadata();
                    reservationService.create(paymentIntentObject);
                    return;
                }
            } catch (StripeException e) {
                e.printStackTrace();
            }
        });
    }

    public void paymentSuccess(Event event) {
        // 支払いが成功した時のイベント
        Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();
        optionalStripeObject.ifPresent(stripeObject -> {
            Invoice invoice = (Invoice) stripeObject;
            paymentService.create(invoice.getCustomer(), invoice.getSubscription(), invoice.getBillingReason(), invoice.getAmountPaid().intValue());
        });
    }

    public void subscriptionSuccess(User user, String sessionId) {
        // サブスクリプションが成功した時にDBへ登録
        try {
            Session session = Session.retrieve(sessionId);
            subscriptionService.create(user, session.getCustomer(), session.getSubscription());
        } catch (Throwable tw) {
            tw.printStackTrace();
        }
    }

    public void deleteCustomer(User user, String customerId) {
        Stripe.apiKey = stripeApiKey;
        try {
            Customer resource = Customer.retrieve(customerId);
            Customer customer = resource.delete();
            subscriptionService.delete(user);
        } catch (Throwable tw) {
            tw.printStackTrace();
        }
    }
}