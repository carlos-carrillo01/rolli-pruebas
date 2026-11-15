package com.diego.rolli;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SalesManager implements OrderRepository {
    private static final String PREF_NAME = "rolli_sales_prefs";
    private static final String KEY_ORDERS = "key_orders_list";
    private static final String KEY_CLIENT_UID = "key_client_uid";
    private static final String KEY_LAST_NAME = "key_last_customer_name";
    private static final String KEY_LAST_CAREER = "key_last_career";
    private static final String KEY_LAST_CONTROL = "key_last_control_number";
    private static final String KEY_LAST_NOT_STUDENT = "key_last_not_student";
    private static final String KEY_LAST_STAFF_ROLE = "key_last_staff_role";
    private static final String KEY_LAST_CLASSROOM = "key_last_classroom";
    private static final String KEY_LAST_PHONE = "key_last_phone_number";
    private static final double UNIT_PRICE_MXN = 20.0;

    private final SharedPreferences preferences;

    public SalesManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public String getOrCreateClientUid() {
        String uid = preferences.getString(KEY_CLIENT_UID, "");
        if (TextUtils.isEmpty(uid)) {
            uid = "local_" + UUID.randomUUID().toString();
            preferences.edit().putString(KEY_CLIENT_UID, uid).apply();
        }
        return uid;
    }

    public void saveCustomerProfile(String name, String career, String controlNumber, boolean isNotStudent, String staffRole, String classroom, String phoneNumber) {
        preferences.edit()
                .putString(KEY_LAST_NAME, SecurityUtils.sanitizeText(name, SecurityUtils.MAX_NAME_LENGTH))
                .putString(KEY_LAST_CAREER, SecurityUtils.sanitizeText(career, SecurityUtils.MAX_ROLE_OR_CAREER_LENGTH))
                .putString(KEY_LAST_CONTROL, SecurityUtils.sanitizeText(controlNumber, SecurityUtils.MAX_CONTROL_LENGTH))
                .putBoolean(KEY_LAST_NOT_STUDENT, isNotStudent)
                .putString(KEY_LAST_STAFF_ROLE, SecurityUtils.sanitizeText(staffRole, SecurityUtils.MAX_ROLE_OR_CAREER_LENGTH))
                .putString(KEY_LAST_CLASSROOM, SecurityUtils.sanitizeText(classroom, SecurityUtils.MAX_CLASSROOM_LENGTH))
                .putString(KEY_LAST_PHONE, SecurityUtils.sanitizeDigitsOnly(phoneNumber, SecurityUtils.PHONE_LENGTH))
                .apply();
    }

    public String getLastCustomerName() {
        return preferences.getString(KEY_LAST_NAME, "");
    }

    public String getLastCareer() {
        return preferences.getString(KEY_LAST_CAREER, "");
    }

    public String getLastControlNumber() {
        return preferences.getString(KEY_LAST_CONTROL, "");
    }

    public boolean getLastIsNotStudent() {
        return preferences.getBoolean(KEY_LAST_NOT_STUDENT, false);
    }

    public String getLastStaffRole() {
        return preferences.getString(KEY_LAST_STAFF_ROLE, "");
    }

    public String getLastClassroom() {
        return preferences.getString(KEY_LAST_CLASSROOM, "");
    }

    public String getLastPhoneNumber() {
        return preferences.getString(KEY_LAST_PHONE, "");
    }

    public int getActiveOrdersCount() {
        int count = 0;
        for (Order order : getOrders()) {
            if (!order.isDelivered()) {
                count++;
            }
        }
        return count;
    }

    public int getActiveRollsCount() {
        int total = 0;
        for (Order order : getOrders()) {
            if (!order.isDelivered()) {
                for (OrderItem item : order.getItems()) {
                    total += item.getQuantity();
                }
            }
        }
        return total;
    }

    @Override
    public void fetchOrders(Callback<List<Order>> callback) {
        try {
            callback.onSuccess(getOrders());
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void createOrder(Order order, Callback<Order> callback) {
        try {
            saveOrder(order);
            callback.onSuccess(order);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void markOrderDelivered(String orderId, Callback<Void> callback) {
        try {
            List<Order> orders = getOrders();
            for (Order o : orders) {
                if (o.getId().equals(orderId)) {
                    o.setDelivered(true);
                    break;
                }
            }
            saveOrdersList(orders);
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void deleteOrder(String orderId, Callback<Void> callback) {
        try {
            deleteOrder(orderId);
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public void clearHistory(Callback<Void> callback) {
        try {
            clearHistory();
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    public void saveOrder(Order order) {
        Order sanitized = sanitizeOrderForPersistence(order);
        List<Order> orders = getOrders();
        orders.add(0, sanitized);
        saveOrdersList(orders);
    }

    public void updateOrder(Order updatedOrder) {
        Order sanitized = sanitizeOrderForPersistence(updatedOrder);
        List<Order> orders = getOrders();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId().equals(sanitized.getId())) {
                orders.set(i, sanitized);
                break;
            }
        }
        saveOrdersList(orders);
    }

    public void deleteOrder(String orderId) {
        if (TextUtils.isEmpty(orderId)) return;
        List<Order> orders = getOrders();
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getId().equals(orderId)) {
                orders.remove(i);
                break;
            }
        }
        saveOrdersList(orders);
    }

    public List<Order> getOrders() {
        List<Order> list = new ArrayList<>();
        String jsonString = preferences.getString(KEY_ORDERS, "[]");
        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String id = obj.optString("id");
                String ownerUid = obj.optString("ownerUid", getOrCreateClientUid());
                String customerName = SecurityUtils.sanitizeText(obj.optString("customerName"), SecurityUtils.MAX_NAME_LENGTH);
                String career = SecurityUtils.sanitizeText(obj.optString("career"), SecurityUtils.MAX_ROLE_OR_CAREER_LENGTH);
                String controlNumber = SecurityUtils.sanitizeText(obj.optString("controlNumber"), SecurityUtils.MAX_CONTROL_LENGTH);
                boolean isNotStudent = obj.optBoolean("notStudent", obj.optBoolean("isNotStudent", false));
                String staffRole = SecurityUtils.sanitizeText(obj.optString("staffRole"), SecurityUtils.MAX_ROLE_OR_CAREER_LENGTH);
                String classroom = SecurityUtils.sanitizeText(obj.optString("classroom"), SecurityUtils.MAX_CLASSROOM_LENGTH);
                String phoneNumber = SecurityUtils.sanitizeDigitsOnly(obj.optString("phoneNumber"), SecurityUtils.PHONE_LENGTH);
                String paymentMethod = SecurityUtils.sanitizeText(obj.optString("paymentMethod"), 40);
                String timestamp = obj.optString("timestamp");
                String pickupCode = obj.optString("pickupCode", "0000");
                boolean delivered = obj.optBoolean("delivered", false);
                String status = obj.optString("status", delivered ? Order.STATUS_DELIVERED : Order.STATUS_PENDING);
                long createdAt = obj.optLong("createdAtEpochMs", System.currentTimeMillis());
                long updatedAt = obj.optLong("updatedAtEpochMs", createdAt);

                List<OrderItem> items = new ArrayList<>();
                double recomputedTotal = 0.0;
                JSONArray itemsArray = obj.optJSONArray("items");
                if (itemsArray != null) {
                    for (int j = 0; j < itemsArray.length(); j++) {
                        JSONObject itemObj = itemsArray.getJSONObject(j);
                        String pName = SecurityUtils.sanitizeText(itemObj.optString("productName"), 40);
                        int qty = Math.max(0, itemObj.optInt("quantity"));
                        if (qty > 0) {
                            items.add(new OrderItem(pName, qty, UNIT_PRICE_MXN));
                            recomputedTotal += qty * UNIT_PRICE_MXN;
                        }
                    }
                }
                double storedTotal = obj.optDouble("totalAmount", recomputedTotal);
                double safeTotal = recomputedTotal > 0 ? recomputedTotal : Math.max(0.0, storedTotal);

                list.add(new Order(
                        id,
                        ownerUid,
                        customerName,
                        career,
                        controlNumber,
                        isNotStudent,
                        staffRole,
                        classroom,
                        phoneNumber,
                        paymentMethod,
                        items,
                        safeTotal,
                        timestamp,
                        pickupCode,
                        delivered,
                        status,
                        createdAt,
                        updatedAt
                ));
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    private Order sanitizeOrderForPersistence(Order raw) {
        List<OrderItem> safeItems = new ArrayList<>();
        double recomputedTotal = 0.0;
        for (OrderItem item : raw.getItems()) {
            int qty = Math.max(0, item.getQuantity());
            if (qty > 0) {
                safeItems.add(new OrderItem(
                        SecurityUtils.sanitizeText(item.getProductName(), 40),
                        qty,
                        UNIT_PRICE_MXN
                ));
                recomputedTotal += qty * UNIT_PRICE_MXN;
            }
        }
        String safeUid = TextUtils.isEmpty(raw.getOwnerUid()) ? getOrCreateClientUid() : raw.getOwnerUid();
        String safeId = TextUtils.isEmpty(raw.getId()) ? SecurityUtils.generateOrderId() : raw.getId();
        return new Order(
                safeId,
                safeUid,
                SecurityUtils.sanitizeText(raw.getCustomerName(), SecurityUtils.MAX_NAME_LENGTH),
                SecurityUtils.sanitizeText(raw.getCareer(), SecurityUtils.MAX_ROLE_OR_CAREER_LENGTH),
                SecurityUtils.sanitizeText(raw.getControlNumber(), SecurityUtils.MAX_CONTROL_LENGTH),
                raw.isNotStudent(),
                SecurityUtils.sanitizeText(raw.getStaffRole(), SecurityUtils.MAX_ROLE_OR_CAREER_LENGTH),
                SecurityUtils.sanitizeText(raw.getClassroom(), SecurityUtils.MAX_CLASSROOM_LENGTH),
                SecurityUtils.sanitizeDigitsOnly(raw.getPhoneNumber(), SecurityUtils.PHONE_LENGTH),
                SecurityUtils.sanitizeText(raw.getPaymentMethod(), 40),
                safeItems,
                recomputedTotal,
                raw.getTimestamp(),
                SecurityUtils.isValidPickupCode(raw.getPickupCode()) ? raw.getPickupCode() : SecurityUtils.generatePickupCode(),
                raw.isDelivered(),
                raw.getStatus(),
                raw.getCreatedAtEpochMs(),
                System.currentTimeMillis()
        );
    }

    private void saveOrdersList(List<Order> orders) {
        try {
            JSONArray array = new JSONArray();
            for (Order order : orders) {
                JSONObject obj = new JSONObject();
                obj.put("id", order.getId());
                obj.put("ownerUid", order.getOwnerUid());
                obj.put("customerName", order.getCustomerName());
                obj.put("career", order.getCareer());
                obj.put("controlNumber", order.getControlNumber());
                obj.put("notStudent", order.isNotStudent());
                obj.put("staffRole", order.getStaffRole());
                obj.put("classroom", order.getClassroom());
                obj.put("phoneNumber", order.getPhoneNumber());
                obj.put("paymentMethod", order.getPaymentMethod());
                obj.put("totalAmount", order.getTotalAmount());
                obj.put("timestamp", order.getTimestamp());
                obj.put("pickupCode", order.getPickupCode());
                obj.put("delivered", order.isDelivered());
                obj.put("status", order.getStatus());
                obj.put("createdAtEpochMs", order.getCreatedAtEpochMs());
                obj.put("updatedAtEpochMs", order.getUpdatedAtEpochMs());

                JSONArray itemsArray = new JSONArray();
                for (OrderItem item : order.getItems()) {
                    JSONObject itemObj = new JSONObject();
                    itemObj.put("productName", item.getProductName());
                    itemObj.put("quantity", item.getQuantity());
                    itemObj.put("pricePerUnit", item.getPricePerUnit());
                    itemsArray.put(itemObj);
                }
                obj.put("items", itemsArray);
                array.put(obj);
            }
            preferences.edit().putString(KEY_ORDERS, array.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    public double getTotalRevenue() {
        double total = 0;
        for (Order order : getOrders()) {
            total += order.getTotalAmount();
        }
        return total;
    }

    public double getDeliveredRevenue() {
        double total = 0;
        for (Order order : getOrders()) {
            if (order.isDelivered()) {
                total += order.getTotalAmount();
            }
        }
        return total;
    }

    public double getPendingRevenue() {
        double total = 0;
        for (Order order : getOrders()) {
            if (!order.isDelivered()) {
                total += order.getTotalAmount();
            }
        }
        return total;
    }

    public int getTotalRollsSold() {
        int total = 0;
        for (Order order : getOrders()) {
            for (OrderItem item : order.getItems()) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    public void clearHistory() {
        preferences.edit().remove(KEY_ORDERS).apply();
    }
}
