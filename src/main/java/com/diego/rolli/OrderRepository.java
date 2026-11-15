package com.diego.rolli;

import java.util.List;

public interface OrderRepository {

    interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }

    String getOrCreateClientUid();

    void fetchOrders(Callback<List<Order>> callback);

    void createOrder(Order order, Callback<Order> callback);

    void markOrderDelivered(String orderId, Callback<Void> callback);

    void deleteOrder(String orderId, Callback<Void> callback);

    void clearHistory(Callback<Void> callback);
}
