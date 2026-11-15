package com.diego.rolli;

import java.util.ArrayList;
import java.util.List;

public class Order {
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_DELIVERED = "delivered";

    private String id;
    private String ownerUid;
    private String customerName;
    private String career;
    private String controlNumber;
    private boolean notStudent;
    private String staffRole;
    private String classroom;
    private String phoneNumber;
    private String paymentMethod;
    private List<OrderItem> items;
    private double totalAmount;
    private String timestamp;
    private String pickupCode;
    private boolean delivered;
    private String status;
    private long createdAtEpochMs;
    private long updatedAtEpochMs;

    public Order() {
        this.items = new ArrayList<>();
        this.status = STATUS_PENDING;
    }

    public Order(
            String id,
            String customerName,
            String career,
            String controlNumber,
            boolean isNotStudent,
            String staffRole,
            String classroom,
            String phoneNumber,
            String paymentMethod,
            List<OrderItem> items,
            double totalAmount,
            String timestamp,
            String pickupCode,
            boolean delivered
    ) {
        this(
                id,
                "",
                customerName,
                career,
                controlNumber,
                isNotStudent,
                staffRole,
                classroom,
                phoneNumber,
                paymentMethod,
                items,
                totalAmount,
                timestamp,
                pickupCode,
                delivered,
                delivered ? STATUS_DELIVERED : STATUS_PENDING,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );
    }

    public Order(
            String id,
            String ownerUid,
            String customerName,
            String career,
            String controlNumber,
            boolean notStudent,
            String staffRole,
            String classroom,
            String phoneNumber,
            String paymentMethod,
            List<OrderItem> items,
            double totalAmount,
            String timestamp,
            String pickupCode,
            boolean delivered,
            String status,
            long createdAtEpochMs,
            long updatedAtEpochMs
    ) {
        this.id = id;
        this.ownerUid = ownerUid != null ? ownerUid : "";
        this.customerName = customerName != null ? customerName : "";
        this.career = career != null ? career : "";
        this.controlNumber = controlNumber != null ? controlNumber : "";
        this.notStudent = notStudent;
        this.staffRole = staffRole != null ? staffRole : "";
        this.classroom = classroom != null ? classroom : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
        this.paymentMethod = paymentMethod != null ? paymentMethod : "";
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.totalAmount = Math.max(0.0, totalAmount);
        this.timestamp = timestamp != null ? timestamp : "";
        this.pickupCode = pickupCode != null ? pickupCode : "0000";
        this.delivered = delivered;
        this.status = status != null ? status : (delivered ? STATUS_DELIVERED : STATUS_PENDING);
        this.createdAtEpochMs = createdAtEpochMs;
        this.updatedAtEpochMs = updatedAtEpochMs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid != null ? ownerUid : "";
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName != null ? customerName : "";
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career != null ? career : "";
    }

    public String getControlNumber() {
        return controlNumber;
    }

    public void setControlNumber(String controlNumber) {
        this.controlNumber = controlNumber != null ? controlNumber : "";
    }

    public boolean isNotStudent() {
        return notStudent;
    }

    public void setNotStudent(boolean notStudent) {
        this.notStudent = notStudent;
    }

    public String getStaffRole() {
        return staffRole;
    }

    public void setStaffRole(String staffRole) {
        this.staffRole = staffRole != null ? staffRole : "";
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom != null ? classroom : "";
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod != null ? paymentMethod : "";
    }

    public List<OrderItem> getItems() {
        return items != null ? items : new ArrayList<>();
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = Math.max(0.0, totalAmount);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp != null ? timestamp : "";
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode != null ? pickupCode : "0000";
    }

    public boolean isDelivered() {
        return delivered || STATUS_DELIVERED.equals(status);
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
        this.status = delivered ? STATUS_DELIVERED : STATUS_PENDING;
        this.updatedAtEpochMs = System.currentTimeMillis();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status : STATUS_PENDING;
        this.delivered = STATUS_DELIVERED.equals(this.status);
    }

    public long getCreatedAtEpochMs() {
        return createdAtEpochMs;
    }

    public void setCreatedAtEpochMs(long createdAtEpochMs) {
        this.createdAtEpochMs = createdAtEpochMs;
    }

    public long getUpdatedAtEpochMs() {
        return updatedAtEpochMs;
    }

    public void setUpdatedAtEpochMs(long updatedAtEpochMs) {
        this.updatedAtEpochMs = updatedAtEpochMs;
    }
}
