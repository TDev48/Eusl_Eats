package com.eusleats.eusleats.service;

import com.eusleats.eusleats.event.OrderEvent;
import com.eusleats.eusleats.repository.FoodStoreRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Creates the notifications and the store SMS for an order event.
 * It runs only after the order change is safely saved, and a failure here
 * is logged but never affects the order itself.
 */
@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final NotificationService notificationService;
    private final FoodStoreRepository foodStoreRepository;
    private final SmsService smsService;

    public OrderEventListener(
            NotificationService notificationService,
            FoodStoreRepository foodStoreRepository,
            SmsService smsService) {

        this.notificationService = notificationService;
        this.foodStoreRepository = foodStoreRepository;
        this.smsService = smsService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true)
    public void handle(OrderEvent event) {

        try {
            switch (event.type()) {

                case PLACED -> {
    String total = money(event);
    Integer displayNumber = event.displayOrderNumber();

    safely(() -> notificationService.create(
            NotificationService.STORE, event.storeId(),
            "ORDER_PLACED", event.orderId(),
            "New order #" + displayNumber + " placed by "
                    + event.studentName() + " (" + event.itemCount()
                    + " item" + (event.itemCount() == 1 ? "" : "s")
                    + ", " + total + ")."));

    safely(() -> notificationService.create(
            NotificationService.STUDENT, event.studentId(),
            "ORDER_PLACED", event.orderId(),
            "Order #" + displayNumber + " was placed at "
                    + event.storeName() + " (" + total + ")."));

    textTheStore(event,
            "EUSL Eats: New order #" + displayNumber + " from "
                    + event.studentName() + " - " + total
                    + ". Please check your orders.");
}

                case CANCELLED_BY_STUDENT -> {
                    safely(() -> notificationService.create(
                            NotificationService.STUDENT, event.studentId(),
                            "ORDER_CANCELLED", event.orderId(),
                            "Order cancelled"));

                    safely(() -> notificationService.create(
                            NotificationService.STORE, event.storeId(),
                            "ORDER_CANCELLED", event.orderId(),
                            "Order #" + event.orderId() + " was cancelled by "
                                    + event.studentName() + "."));

                    textTheStore(event,
                            "EUSL Eats: Order #" + event.orderId()
                                    + " was cancelled by " + event.studentName() + ".");
                }

                
case STATUS_CHANGED -> {
    String status = event.status();

    safely(() -> notificationService.create(
            NotificationService.STUDENT, event.studentId(),
            "STATUS_" + status.toUpperCase().replace(" ", "_"),
            event.orderId(),
            "Cancelled".equalsIgnoreCase(status)
                    ? "Order #" + event.orderId()
                    + " was cancelled by the store."
                    : "Your order #" + event.orderId()
                    + " is now " + status + "."));

    if ("Cancelled".equalsIgnoreCase(status)) {
        safely(() -> notificationService.create(
                NotificationService.STORE, event.storeId(),
                "ORDER_CANCELLED", event.orderId(),
                "Order #" + event.orderId()
                + " was cancelled by the store."));
    } else if ("Completed".equalsIgnoreCase(status)) {
        safely(() -> notificationService.create(
                NotificationService.STORE, event.storeId(),
                "ORDER_COMPLETED", event.orderId(),
                "Order #" + event.orderId()
                + " has been completed."));
    }
}

            }

        } catch (Exception e) {
            log.error("Could not process order event {}: {}", event, e.getMessage());
        }
    }

    private void textTheStore(OrderEvent event, String text) {

        String phone = foodStoreRepository.findById(event.storeId())
                .map(store -> store.getUser() == null ? null : store.getUser().getPhone())
                .orElse(null);

        smsService.send(phone, text);
    }

    private String money(OrderEvent event) {
        return String.format("LKR %,.2f", event.totalAmount());
    }

    private void safely(Runnable action) {

        try {
            action.run();
        } catch (Exception e) {
            log.error("Notification could not be saved: {}", e.getMessage());
        }
    }
}
