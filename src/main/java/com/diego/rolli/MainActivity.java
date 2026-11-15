package com.diego.rolli;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_ROLLS_PER_FLAVOR = 3;
    private static final int MAX_ACTIVE_ROLLS_PER_USER = 6;

    private int qtyNutella = 0;
    private int qtyQueso = 0;
    private int qtyClasico = 0;
    private int qtyCajeta = 0;

    private TextView tvQtyNutella, tvQtyQueso, tvQtyClasico, tvQtyCajeta;
    private MaterialButton btnMinusNutella, btnMinusQueso, btnMinusClasico, btnMinusCajeta;
    private MaterialButton btnPlusNutella, btnPlusQueso, btnPlusClasico, btnPlusCajeta;
    private TextView tvCartSummary, tvCartTotal, tvMyOrdersBadge;
    private MaterialButton btnCheckout, btnClearCart, btnViewActiveOrders;
    private ImageButton btnAdminAccess, btnMyOrders, btnMoreMenu;

    private View layoutActiveOrders;
    private TextView tvActiveOrdersTitle, tvActiveOrdersSubtitle;

    private SalesManager salesManager;
    private RateLimiter rateLimiter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        salesManager = new SalesManager(this);
        rateLimiter = new RateLimiter(this);

        initViews();
        setupListeners();
        syncQuantityLabels();
        updateCustomerOrdersSection();
    }

    private void initViews() {
        tvQtyNutella = findViewById(R.id.tvQtyNutella);
        tvQtyQueso = findViewById(R.id.tvQtyQueso);
        tvQtyClasico = findViewById(R.id.tvQtyClasico);
        tvQtyCajeta = findViewById(R.id.tvQtyCajeta);
        btnMinusNutella = findViewById(R.id.btnMinusNutella);
        btnMinusQueso = findViewById(R.id.btnMinusQueso);
        btnMinusClasico = findViewById(R.id.btnMinusClasico);
        btnMinusCajeta = findViewById(R.id.btnMinusCajeta);
        btnPlusNutella = findViewById(R.id.btnPlusNutella);
        btnPlusQueso = findViewById(R.id.btnPlusQueso);
        btnPlusClasico = findViewById(R.id.btnPlusClasico);
        btnPlusCajeta = findViewById(R.id.btnPlusCajeta);
        tvCartSummary = findViewById(R.id.tvCartSummary);
        tvCartTotal = findViewById(R.id.tvCartTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnClearCart = findViewById(R.id.btnClearCart);
        btnAdminAccess = findViewById(R.id.btnAdminAccess);
        btnMyOrders = findViewById(R.id.btnMyOrders);
        btnMoreMenu = findViewById(R.id.btnMoreMenu);
        tvMyOrdersBadge = findViewById(R.id.tvMyOrdersBadge);

        layoutActiveOrders = findViewById(R.id.layoutActiveOrders);
        tvActiveOrdersTitle = findViewById(R.id.tvActiveOrdersTitle);
        tvActiveOrdersSubtitle = findViewById(R.id.tvActiveOrdersSubtitle);
        btnViewActiveOrders = findViewById(R.id.btnViewActiveOrders);
    }

    private void setupListeners() {
        btnPlusNutella.setOnClickListener(v -> {
            if (canAddOneMoreRoll(qtyNutella)) {
                qtyNutella++;
                syncQuantityLabels();
            }
        });

        btnMinusNutella.setOnClickListener(v -> {
            if (qtyNutella > 0) {
                qtyNutella--;
                syncQuantityLabels();
            }
        });

        btnPlusQueso.setOnClickListener(v -> {
            if (canAddOneMoreRoll(qtyQueso)) {
                qtyQueso++;
                syncQuantityLabels();
            }
        });

        btnMinusQueso.setOnClickListener(v -> {
            if (qtyQueso > 0) {
                qtyQueso--;
                syncQuantityLabels();
            }
        });

        btnPlusClasico.setOnClickListener(v -> {
            if (canAddOneMoreRoll(qtyClasico)) {
                qtyClasico++;
                syncQuantityLabels();
            }
        });

        btnMinusClasico.setOnClickListener(v -> {
            if (qtyClasico > 0) {
                qtyClasico--;
                syncQuantityLabels();
            }
        });

        btnPlusCajeta.setOnClickListener(v -> {
            if (canAddOneMoreRoll(qtyCajeta)) {
                qtyCajeta++;
                syncQuantityLabels();
            }
        });

        btnMinusCajeta.setOnClickListener(v -> {
            if (qtyCajeta > 0) {
                qtyCajeta--;
                syncQuantityLabels();
            }
        });

        btnClearCart.setOnClickListener(v -> showStyledConfirmDialog(
                getString(R.string.clear_cart),
                getString(R.string.clear_cart_confirm),
                getString(R.string.yes),
                getString(R.string.no),
                this::resetQuantities
        ));

        btnCheckout.setOnClickListener(v -> showCheckoutDialog());

        btnAdminAccess.setOnClickListener(v -> showAdminPinDialog());

        btnMyOrders.setOnClickListener(v -> showMyOrdersDialog());

        btnMoreMenu.setOnClickListener(v -> showHelpDialog());

        layoutActiveOrders.setOnClickListener(v -> showMyOrdersDialog());
        btnViewActiveOrders.setOnClickListener(v -> showMyOrdersDialog());
    }

    private int getRemainingActiveRollsAllowed() {
        return Math.max(0, MAX_ACTIVE_ROLLS_PER_USER - salesManager.getActiveRollsCount());
    }

    private boolean canAddOneMoreRoll(int currentFlavorQty) {
        int activeRolls = salesManager.getActiveRollsCount();
        int remainingAllowed = Math.max(0, MAX_ACTIVE_ROLLS_PER_USER - activeRolls);
        if (calculateTotalItems() >= remainingAllowed) {
            if (activeRolls > 0) {
                Toast.makeText(
                        this,
                        "Tienes " + activeRolls + " roles pendientes. Máximo " + MAX_ACTIVE_ROLLS_PER_USER + " hasta su entrega.",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(this, R.string.max_active_rolls_reached, Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        if (currentFlavorQty >= MAX_ROLLS_PER_FLAVOR) {
            Toast.makeText(this, R.string.max_per_flavor_reached, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private double calculateTotal() {
        return (qtyNutella * 20.0) + (qtyQueso * 20.0) + (qtyClasico * 20.0) + (qtyCajeta * 20.0);
    }

    private int calculateTotalItems() {
        return qtyNutella + qtyQueso + qtyClasico + qtyCajeta;
    }

    private void syncQuantityLabels() {
        tvQtyNutella.setText(String.valueOf(qtyNutella));
        tvQtyQueso.setText(String.valueOf(qtyQueso));
        tvQtyClasico.setText(String.valueOf(qtyClasico));
        tvQtyCajeta.setText(String.valueOf(qtyCajeta));

        updateMinusButtonState(btnMinusNutella, qtyNutella);
        updateMinusButtonState(btnMinusQueso, qtyQueso);
        updateMinusButtonState(btnMinusClasico, qtyClasico);
        updateMinusButtonState(btnMinusCajeta, qtyCajeta);

        int remainingAllowed = getRemainingActiveRollsAllowed();
        boolean totalCapReached = calculateTotalItems() >= remainingAllowed;
        updatePlusButtonState(btnPlusNutella, qtyNutella, totalCapReached);
        updatePlusButtonState(btnPlusQueso, qtyQueso, totalCapReached);
        updatePlusButtonState(btnPlusClasico, qtyClasico, totalCapReached);
        updatePlusButtonState(btnPlusCajeta, qtyCajeta, totalCapReached);

        updateCartBar();
    }

    private void updateMinusButtonState(MaterialButton btn, int qty) {
        boolean enabled = qty > 0;
        btn.setEnabled(enabled);
        btn.setAlpha(enabled ? 1.0f : 0.35f);
    }

    private void updatePlusButtonState(MaterialButton btn, int qty, boolean totalCapReached) {
        btn.setAlpha((qty >= MAX_ROLLS_PER_FLAVOR || totalCapReached) ? 0.35f : 1.0f);
    }

    private void updateCartBar() {
        int totalItems = calculateTotalItems();
        double total = calculateTotal();
        int activeRolls = salesManager.getActiveRollsCount();
        int remainingAllowed = Math.max(0, MAX_ACTIVE_ROLLS_PER_USER - activeRolls);

        if (activeRolls > 0) {
            tvCartSummary.setText(String.format(
                    Locale.getDefault(),
                    "%d roles • %d disp. (%d pendientes)",
                    totalItems,
                    remainingAllowed,
                    activeRolls
            ));
        } else {
            tvCartSummary.setText(String.format(
                    Locale.getDefault(),
                    "%d roles seleccionados (máx. %d)",
                    totalItems,
                    MAX_ACTIVE_ROLLS_PER_USER
            ));
        }
        tvCartTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
        btnCheckout.setEnabled(totalItems > 0);
        btnClearCart.setVisibility(totalItems > 0 ? View.VISIBLE : View.GONE);
    }

    private void updateCustomerOrdersSection() {
        List<Order> allOrders = salesManager.getOrders();
        List<Order> activeOrders = new ArrayList<>();

        for (Order o : allOrders) {
            if (!o.isDelivered()) {
                activeOrders.add(o);
            }
        }

        if (activeOrders.isEmpty()) {
            layoutActiveOrders.setVisibility(View.GONE);
            tvMyOrdersBadge.setVisibility(View.GONE);
            syncQuantityLabels();
            return;
        }

        int count = activeOrders.size();
        tvMyOrdersBadge.setVisibility(View.VISIBLE);
        tvMyOrdersBadge.setText(String.valueOf(count));

        layoutActiveOrders.setVisibility(View.VISIBLE);
        Order latest = activeOrders.get(0);

        if (count == 1) {
            tvActiveOrdersTitle.setText("Tienes 1 pedido pendiente (Código: " + latest.getPickupCode() + ")");
            tvActiveOrdersSubtitle.setText("Toca aquí para ver tu código y detalles");
        } else {
            tvActiveOrdersTitle.setText("Tienes " + count + " pedidos pendientes");
            tvActiveOrdersSubtitle.setText("Último código: " + latest.getPickupCode() + " • Toca para ver detalles");
        }
        syncQuantityLabels();
    }

    private void bindCustomerOrderCard(View itemView, Order order) {
        TextView tvCode = itemView.findViewById(R.id.tvCustomerOrderCode);
        TextView tvStatus = itemView.findViewById(R.id.tvCustomerOrderStatus);
        TextView tvItems = itemView.findViewById(R.id.tvCustomerOrderItems);
        View rowLocation = itemView.findViewById(R.id.rowCustomerOrderLocation);
        TextView tvLocation = itemView.findViewById(R.id.tvCustomerOrderLocation);
        ImageView ivPaymentIcon = itemView.findViewById(R.id.ivCustomerOrderPaymentIcon);
        TextView tvPayment = itemView.findViewById(R.id.tvCustomerOrderPayment);
        TextView tvDate = itemView.findViewById(R.id.tvCustomerOrderDate);
        TextView tvTotal = itemView.findViewById(R.id.tvCustomerOrderTotal);

        tvCode.setText(order.getPickupCode());

        if (order.isDelivered()) {
            tvStatus.setText(R.string.status_delivered);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.green_success));
            ViewCompat.setBackgroundTintList(tvStatus, ColorStateList.valueOf(Color.parseColor("#182E7D32")));
        } else {
            tvStatus.setText(R.string.status_pending);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.burgundy));
            ViewCompat.setBackgroundTintList(tvStatus, ColorStateList.valueOf(Color.parseColor("#16800020")));
        }

        StringBuilder itemsSb = new StringBuilder();
        List<OrderItem> items = order.getItems();
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            itemsSb.append(String.format(
                    Locale.getDefault(),
                    "• %dx %s ($%.2f)",
                    item.getQuantity(),
                    item.getProductName(),
                    item.getQuantity() * item.getPricePerUnit()
            ));
            if (i < items.size() - 1) {
                itemsSb.append("\n");
            }
        }
        tvItems.setText(itemsSb.toString());

        if (!TextUtils.isEmpty(order.getClassroom())) {
            rowLocation.setVisibility(View.VISIBLE);
            tvLocation.setText("Entrega: " + order.getClassroom());
        } else {
            rowLocation.setVisibility(View.GONE);
        }

        boolean isDigital = getString(R.string.pay_digital).equals(order.getPaymentMethod());
        ivPaymentIcon.setImageResource(isDigital ? R.drawable.ic_bank : R.drawable.ic_cash);
        tvPayment.setText("Pago: " + order.getPaymentMethod());

        tvDate.setText(order.getTimestamp());
        tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", order.getTotalAmount()));
    }

    private void showHelpDialog() {
        int[][] sections = {
                {R.drawable.ic_cart, R.string.help_sec1_title, R.string.help_sec1_body},
                {R.drawable.ic_payments, R.string.help_sec2_title, R.string.help_sec2_body},
                {R.drawable.ic_location, R.string.help_sec3_title, R.string.help_sec3_body},
                {R.drawable.ic_close, R.string.help_sec4_title, R.string.help_sec4_body}
        };

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_info_help, null);
        ImageView ivHeroIcon = dialogView.findViewById(R.id.ivInfoHelpHeroIcon);
        TextView tvTitle = dialogView.findViewById(R.id.tvInfoHelpTitle);
        TextView tvSubtitle = dialogView.findViewById(R.id.tvInfoHelpSubtitle);
        ImageButton btnClose = dialogView.findViewById(R.id.btnCloseInfoHelp);
        View layoutHelpContact = dialogView.findViewById(R.id.layoutHelpContact);
        LinearLayout containerHelpPhones = dialogView.findViewById(R.id.containerHelpPhones);
        TextView tvSectionsHeader = dialogView.findViewById(R.id.tvSectionsHeader);
        TextView tvSectionsSubheader = dialogView.findViewById(R.id.tvSectionsSubheader);
        LinearLayout containerSections = dialogView.findViewById(R.id.containerInfoHelpSections);
        View layoutHelpFooterNote = dialogView.findViewById(R.id.layoutHelpFooterNote);
        MaterialButton btnAccept = dialogView.findViewById(R.id.btnAcceptInfoHelp);

        ivHeroIcon.setImageResource(R.drawable.ic_help);
        tvTitle.setText(R.string.help_title);
        tvSubtitle.setText(R.string.help_subtitle);
        tvSectionsHeader.setText(R.string.help_sections_header);
        tvSectionsSubheader.setVisibility(View.GONE);
        layoutHelpFooterNote.setVisibility(View.VISIBLE);
        layoutHelpContact.setVisibility(View.VISIBLE);

        String label = getString(R.string.help_phone_1_label);
        String phone = getString(R.string.help_phone_1_value).trim();
        if (!TextUtils.isEmpty(phone)) {
            View phoneRow = LayoutInflater.from(this).inflate(R.layout.item_help_phone, containerHelpPhones, false);
            TextView tvLabel = phoneRow.findViewById(R.id.tvPhoneLabel);
            TextView tvValue = phoneRow.findViewById(R.id.tvPhoneValue);
            MaterialButton btnCall = phoneRow.findViewById(R.id.btnCallPhone);
            MaterialButton btnCopy = phoneRow.findViewById(R.id.btnCopyPhone);
            View btnWhatsapp = phoneRow.findViewById(R.id.btnWhatsappPhone);

            tvLabel.setText(label);
            tvValue.setText(phone);
            btnCall.setOnClickListener(v -> dialPhoneNumber(phone));
            btnCopy.setOnClickListener(v -> copyToClipboard("Teléfono", phone, getString(R.string.copied_phone)));
            btnWhatsapp.setOnClickListener(v -> openWhatsApp(phone));
            containerHelpPhones.addView(phoneRow);
        }

        for (int i = 0; i < sections.length; i++) {
            int[] section = sections[i];
            View card = LayoutInflater.from(this).inflate(R.layout.item_info_section, containerSections, false);
            View layoutHeader = card.findViewById(R.id.layoutAccordionHeader);
            ImageView ivIcon = card.findViewById(R.id.ivSectionIcon);
            TextView tvSecTitle = card.findViewById(R.id.tvSectionTitle);
            TextView tvSecPreview = card.findViewById(R.id.tvSectionPreview);
            ImageView ivChevron = card.findViewById(R.id.ivSectionChevron);
            View layoutBody = card.findViewById(R.id.layoutAccordionBody);
            TextView tvSecBody = card.findViewById(R.id.tvSectionBody);

            ivIcon.setImageResource(section[0]);
            tvSecTitle.setText(section[1]);
            tvSecBody.setText(section[2]);

            final boolean[] isExpanded = {i == 0};
            layoutBody.setVisibility(isExpanded[0] ? View.VISIBLE : View.GONE);
            ivChevron.setRotation(isExpanded[0] ? 180f : 0f);
            tvSecPreview.setVisibility(View.GONE);

            layoutHeader.setOnClickListener(v -> {
                isExpanded[0] = !isExpanded[0];
                layoutBody.setVisibility(isExpanded[0] ? View.VISIBLE : View.GONE);
                ivChevron.animate()
                        .rotation(isExpanded[0] ? 180f : 0f)
                        .setDuration(180)
                        .start();
            });

            containerSections.addView(card);
        }

        btnAccept.setText(R.string.understood);
        btnAccept.setIcon(null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        applyRoundedDialogWindow(dialog);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnAccept.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openWhatsApp(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + digits));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_whatsapp_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void dialPhoneNumber(String phone) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.replace(" ", "")));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_dialer_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void showMyOrdersDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_my_orders, null);
        ImageButton btnBackMyOrders = dialogView.findViewById(R.id.btnBackMyOrders);
        LinearLayout containerMyOrdersList = dialogView.findViewById(R.id.containerMyOrdersList);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        applyRoundedDialogWindow(dialog);

        List<Order> orders = salesManager.getOrders();

        if (orders.isEmpty()) {
            TextView emptyTv = new TextView(this);
            emptyTv.setText(R.string.no_orders_yet);
            emptyTv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            emptyTv.setPadding(0, 20, 0, 20);
            containerMyOrdersList.addView(emptyTv);
        } else {
            for (Order order : orders) {
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_customer_order, containerMyOrdersList, false);
                bindCustomerOrderCard(itemView, order);
                containerMyOrdersList.addView(itemView);
            }
        }

        btnBackMyOrders.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void populateInteractiveCheckoutCart(LinearLayout container, TextView tvCheckoutSummary, AlertDialog dialog) {
        container.removeAllViews();

        addCheckoutCartRowIfSelected(container, getString(R.string.flavor_nutella), qtyNutella, new CartRowActionListener() {
            @Override
            public void onDelta(int delta) {
                if (delta > 0 && !canAddOneMoreRoll(qtyNutella)) {
                    return;
                }
                qtyNutella = Math.max(1, Math.min(MAX_ROLLS_PER_FLAVOR, qtyNutella + delta));
                syncQuantityLabels();
                populateInteractiveCheckoutCart(container, tvCheckoutSummary, dialog);
            }

            @Override
            public void onDelete() {
                qtyNutella = 0;
                syncQuantityLabels();
                if (calculateTotalItems() == 0) {
                    dialog.dismiss();
                } else {
                    populateInteractiveCheckoutCart(container, tvCheckoutSummary, dialog);
                }
            }
        });

        addCheckoutCartRowIfSelected(container, getString(R.string.flavor_queso_crema), qtyQueso, new CartRowActionListener() {
            @Override
            public void onDelta(int delta) {
                if (delta > 0 && !canAddOneMoreRoll(qtyQueso)) {
                    return;
                }
                qtyQueso = Math.max(1, Math.min(MAX_ROLLS_PER_FLAVOR, qtyQueso + delta));
                syncQuantityLabels();
                populateInteractiveCheckoutCart(container, tvCheckoutSummary, dialog);
            }

            @Override
            public void onDelete() {
                qtyQueso = 0;
                syncQuantityLabels();
                if (calculateTotalItems() == 0) {
                    dialog.dismiss();
                } else {
                    populateInteractiveCheckoutCart(container, tvCheckoutSummary, dialog);
                }
            }
        });

        addCheckoutCartRowIfSelected(container, getString(R.string.flavor_canela), qtyClasico, new CartRowActionListener() {
            @Override
            public void onDelta(int delta) {
                if (delta > 0 && !canAddOneMoreRoll(qtyClasico)) {
                    return;
                }
                qtyClasico = Math.max(1, Math.min(MAX_ROLLS_PER_FLAVOR, qtyClasico + delta));
                syncQuantityLabels();
                populateInteractiveCheckoutCart(container, tvCheckoutSummary, dialog);
            }

            @Override
            public void onDelete() {
                qtyClasico = 0;
                syncQuantityLabels();
                if (calculateTotalItems() == 0) {
                    dialog.dismiss();
                } else {
                    populateInteractiveCheckoutCart(container, tvCheckoutSummary, dialog);
                }
            }
        });

        addCheckoutCartRowIfSelected(container, getString(R.string.flavor_chocolate), qtyCajeta, new CartRowActionListener() {
            @Override
            public void onDelta(int delta) {
                if (delta > 0 && !canAddOneMoreRoll(qtyCajeta)) {
                    return;
                }
                qtyCajeta = Math.max(1, Math.min(MAX_ROLLS_PER_FLAVOR, qtyCajeta + delta));
                syncQuantityLabels();
                populateInteractiveCheckoutCart(container, tvCheckoutSummary, dialog);
            }

            @Override
            public void onDelete() {
                qtyCajeta = 0;
                syncQuantityLabels();
                if (calculateTotalItems() == 0) {
                    dialog.dismiss();
                } else {
                    populateInteractiveCheckoutCart(container, tvCheckoutSummary, dialog);
                }
            }
        });

        tvCheckoutSummary.setText(String.format(Locale.getDefault(), "Total a pagar: $%.2f", calculateTotal()));
    }

    private interface CartRowActionListener {
        void onDelta(int delta);
        void onDelete();
    }

    private void addCheckoutCartRowIfSelected(LinearLayout container, String flavorName, int qty, CartRowActionListener listener) {
        if (qty <= 0) return;

        View row = LayoutInflater.from(this).inflate(R.layout.item_checkout_cart_row, container, false);
        TextView tvName = row.findViewById(R.id.tvCheckoutItemName);
        TextView tvSubtotal = row.findViewById(R.id.tvCheckoutItemSubtotal);
        TextView tvQty = row.findViewById(R.id.tvCheckoutItemQty);
        MaterialButton btnMinus = row.findViewById(R.id.btnCheckoutItemMinus);
        MaterialButton btnPlus = row.findViewById(R.id.btnCheckoutItemPlus);
        ImageButton btnDelete = row.findViewById(R.id.btnCheckoutItemDelete);

        tvName.setText(flavorName);
        tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f ($20 c/u)", qty * 20.0));
        tvQty.setText(String.valueOf(qty));

        if (qty <= 1) {
            btnMinus.setEnabled(false);
            btnMinus.setAlpha(0.35f);
        } else {
            btnMinus.setEnabled(true);
            btnMinus.setAlpha(1.0f);
            btnMinus.setOnClickListener(v -> listener.onDelta(-1));
        }

        boolean totalCapReached = calculateTotalItems() >= getRemainingActiveRollsAllowed();
        btnPlus.setAlpha((qty >= MAX_ROLLS_PER_FLAVOR || totalCapReached) ? 0.35f : 1.0f);
        btnPlus.setOnClickListener(v -> listener.onDelta(1));
        btnDelete.setOnClickListener(v -> listener.onDelete());

        container.addView(row);
    }

    private void showCheckoutDialog() {
        int activeRolls = salesManager.getActiveRollsCount();
        int remainingAllowed = Math.max(0, MAX_ACTIVE_ROLLS_PER_USER - activeRolls);
        if (remainingAllowed == 0) {
            showStyledConfirmDialog(
                    "Límite de roles pendientes",
                    "Ya tienes " + activeRolls + " roles pendientes de entrega (máximo " + MAX_ACTIVE_ROLLS_PER_USER + " por usuario). Espera a recibirlos para pedir más.",
                    getString(R.string.accept),
                    null,
                    null
            );
            return;
        }

        if (calculateTotalItems() > remainingAllowed) {
            showStyledConfirmDialog(
                    "Límite de roles excedido",
                    "Ya tienes " + activeRolls + " roles pendientes. Solo puedes pedir hasta " + remainingAllowed + " roles más hasta su entrega (máximo " + MAX_ACTIVE_ROLLS_PER_USER + " en total).",
                    getString(R.string.accept),
                    null,
                    null
            );
            return;
        }

        int activeCount = salesManager.getActiveOrdersCount();
        if (activeCount >= 3) {
            showStyledConfirmDialog(
                    "Límite de pedidos activos",
                    "Ya tienes 3 pedidos pendientes de entrega. Por favor espera a recibirlos antes de realizar uno nuevo.",
                    getString(R.string.accept),
                    null,
                    null
            );
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_checkout, null);

        ImageButton btnBackCheckout = dialogView.findViewById(R.id.btnBackCheckout);
        TextView tvCheckoutDialogTitle = dialogView.findViewById(R.id.tvCheckoutDialogTitle);
        MaterialButton btnClearCartDialog = dialogView.findViewById(R.id.btnClearCartDialog);

        LinearLayout layoutStepCart = dialogView.findViewById(R.id.layoutStepCart);
        LinearLayout containerCheckoutItems = dialogView.findViewById(R.id.containerCheckoutItems);
        TextView tvCheckoutSummary = dialogView.findViewById(R.id.tvCheckoutSummary);
        MaterialButton btnNextToCustomerInfo = dialogView.findViewById(R.id.btnNextToCustomerInfo);

        LinearLayout layoutStepCustomer = dialogView.findViewById(R.id.layoutStepCustomer);
        TextView tvStep2OrderSummary = dialogView.findViewById(R.id.tvStep2OrderSummary);
        View btnEditCartFromStep2 = dialogView.findViewById(R.id.btnEditCartFromStep2);

        EditText etCustomerName = dialogView.findViewById(R.id.etCustomerName);
        TextInputLayout tilCareer = dialogView.findViewById(R.id.tilCareer);
        EditText etCareer = dialogView.findViewById(R.id.etCareer);
        TextInputLayout tilControlNumber = dialogView.findViewById(R.id.tilControlNumber);
        EditText etControlNumber = dialogView.findViewById(R.id.etControlNumber);
        MaterialCheckBox cbNotStudent = dialogView.findViewById(R.id.cbNotStudent);
        TextInputLayout tilStaffRole = dialogView.findViewById(R.id.tilStaffRole);
        EditText etStaffRole = dialogView.findViewById(R.id.etStaffRole);
        EditText etClassroom = dialogView.findViewById(R.id.etClassroom);
        EditText etPhoneNumber = dialogView.findViewById(R.id.etPhoneNumber);

        View cardPayCash = dialogView.findViewById(R.id.cardPayCash);
        View cardPayDigital = dialogView.findViewById(R.id.cardPayDigital);
        ImageView ivRadioCash = dialogView.findViewById(R.id.ivRadioCash);
        ImageView ivRadioDigital = dialogView.findViewById(R.id.ivRadioDigital);
        ImageView ivIconCash = dialogView.findViewById(R.id.ivIconCash);
        ImageView ivIconDigital = dialogView.findViewById(R.id.ivIconDigital);
        TextView tvPayCashLabel = dialogView.findViewById(R.id.tvPayCashLabel);
        TextView tvPayDigitalLabel = dialogView.findViewById(R.id.tvPayDigitalLabel);
        LinearLayout layoutTransferDetails = dialogView.findViewById(R.id.layoutTransferDetails);
        final boolean[] isDigitalPaymentSelected = {false};

        MaterialButton btnCopyClabe = dialogView.findViewById(R.id.btnCopyClabe);
        MaterialButton btnCopyBeneficiary = dialogView.findViewById(R.id.btnCopyBeneficiary);
        MaterialButton btnCopyAllTransfer = dialogView.findViewById(R.id.btnCopyAllTransfer);
        MaterialButton btnConfirmOrder = dialogView.findViewById(R.id.btnConfirmOrder);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        applyRoundedDialogWindow(dialog);

        tvCheckoutDialogTitle.setText(R.string.cart_step_title);
        btnBackCheckout.setImageResource(R.drawable.ic_close);
        btnBackCheckout.setContentDescription(getString(R.string.close));

        populateInteractiveCheckoutCart(containerCheckoutItems, tvCheckoutSummary, dialog);

        Runnable returnToCartStep = () -> {
            layoutStepCustomer.setVisibility(View.GONE);
            layoutStepCart.setVisibility(View.VISIBLE);
            btnClearCartDialog.setVisibility(View.VISIBLE);
            tvCheckoutDialogTitle.setText(R.string.cart_step_title);
            btnBackCheckout.setImageResource(R.drawable.ic_close);
            btnBackCheckout.setContentDescription(getString(R.string.close));
        };

        btnNextToCustomerInfo.setOnClickListener(v -> {
            layoutStepCart.setVisibility(View.GONE);
            layoutStepCustomer.setVisibility(View.VISIBLE);
            btnClearCartDialog.setVisibility(View.GONE);
            tvCheckoutDialogTitle.setText(R.string.shipping_step_title);
            btnBackCheckout.setImageResource(R.drawable.ic_arrow_back);
            btnBackCheckout.setContentDescription(getString(R.string.back));
            int itemCount = calculateTotalItems();
            String unit = (itemCount == 1) ? "rol" : "roles";
            tvStep2OrderSummary.setText(String.format(
                    Locale.getDefault(),
                    "%d %s • Total $%.2f",
                    itemCount,
                    unit,
                    calculateTotal()
            ));
        });

        btnEditCartFromStep2.setOnClickListener(v -> returnToCartStep.run());

        btnBackCheckout.setOnClickListener(v -> {
            if (layoutStepCustomer.getVisibility() == View.VISIBLE) {
                returnToCartStep.run();
            } else {
                dialog.dismiss();
            }
        });

        btnClearCartDialog.setOnClickListener(v -> showStyledConfirmDialog(
                getString(R.string.clear_cart),
                getString(R.string.clear_cart_confirm),
                getString(R.string.yes),
                getString(R.string.no),
                () -> {
                    resetQuantities();
                    dialog.dismiss();
                }
        ));

        cbNotStudent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tilStaffRole.setVisibility(View.VISIBLE);
                tilCareer.setVisibility(View.GONE);
                tilControlNumber.setVisibility(View.GONE);
                etCareer.setText("");
                etCareer.setError(null);
                etControlNumber.setText("");
            } else {
                tilStaffRole.setVisibility(View.GONE);
                tilCareer.setVisibility(View.VISIBLE);
                tilControlNumber.setVisibility(View.VISIBLE);
                etStaffRole.setText("");
                etStaffRole.setError(null);
            }
        });

        etCustomerName.setText(salesManager.getLastCustomerName());
        etClassroom.setText(salesManager.getLastClassroom());
        etPhoneNumber.setText(salesManager.getLastPhoneNumber());
        boolean savedNotStudent = salesManager.getLastIsNotStudent();
        cbNotStudent.setChecked(savedNotStudent);
        if (savedNotStudent) {
            etStaffRole.setText(salesManager.getLastStaffRole());
        } else {
            etCareer.setText(salesManager.getLastCareer());
            etControlNumber.setText(salesManager.getLastControlNumber());
        }

        Runnable updatePaymentSelectionUi = () -> {
            boolean digital = isDigitalPaymentSelected[0];
            int burgundyColor = ContextCompat.getColor(this, R.color.burgundy);
            int burgundyDarkColor = ContextCompat.getColor(this, R.color.burgundy_dark);
            int warmBrownColor = ContextCompat.getColor(this, R.color.warm_brown);
            int primaryTextColor = ContextCompat.getColor(this, R.color.text_primary);

            if (digital) {
                cardPayCash.setBackgroundResource(R.drawable.bg_payment_card_unselected);
                ivRadioCash.setImageResource(R.drawable.bg_radio_unselected);
                ivIconCash.setImageTintList(ColorStateList.valueOf(warmBrownColor));
                tvPayCashLabel.setTextColor(primaryTextColor);

                cardPayDigital.setBackgroundResource(R.drawable.bg_payment_card_selected);
                ivRadioDigital.setImageResource(R.drawable.bg_radio_selected);
                ivIconDigital.setImageTintList(ColorStateList.valueOf(burgundyColor));
                tvPayDigitalLabel.setTextColor(burgundyDarkColor);

                layoutTransferDetails.setVisibility(View.VISIBLE);
            } else {
                cardPayCash.setBackgroundResource(R.drawable.bg_payment_card_selected);
                ivRadioCash.setImageResource(R.drawable.bg_radio_selected);
                ivIconCash.setImageTintList(ColorStateList.valueOf(burgundyColor));
                tvPayCashLabel.setTextColor(burgundyDarkColor);

                cardPayDigital.setBackgroundResource(R.drawable.bg_payment_card_unselected);
                ivRadioDigital.setImageResource(R.drawable.bg_radio_unselected);
                ivIconDigital.setImageTintList(ColorStateList.valueOf(warmBrownColor));
                tvPayDigitalLabel.setTextColor(primaryTextColor);

                layoutTransferDetails.setVisibility(View.GONE);
            }
        };

        cardPayCash.setOnClickListener(v -> {
            isDigitalPaymentSelected[0] = false;
            updatePaymentSelectionUi.run();
        });
        cardPayDigital.setOnClickListener(v -> {
            isDigitalPaymentSelected[0] = true;
            updatePaymentSelectionUi.run();
        });
        updatePaymentSelectionUi.run();

        btnCopyClabe.setOnClickListener(v -> copyToClipboard("CLABE", getString(R.string.clabe_value), getString(R.string.copied_clabe)));

        btnCopyBeneficiary.setOnClickListener(v -> copyToClipboard("Beneficiario", getString(R.string.beneficiary_value), getString(R.string.copied_beneficiary)));

        btnCopyAllTransfer.setOnClickListener(v -> {
            String allDetails = "CLABE: " + getString(R.string.clabe_value) + "\n" +
                    "Beneficiario: " + getString(R.string.beneficiary_value) + "\n" +
                    "Institución: " + getString(R.string.institution_value) + "\n" +
                    "Concepto: Incluir tu nombre completo";
            copyToClipboard("Datos de Transferencia", allDetails, getString(R.string.copied_all));
        });

        btnConfirmOrder.setOnClickListener(v -> {
            etCustomerName.setError(null);
            etCareer.setError(null);
            etStaffRole.setError(null);
            etClassroom.setError(null);
            etPhoneNumber.setError(null);

            String name = SecurityUtils.sanitizeText(etCustomerName.getText().toString(), SecurityUtils.MAX_NAME_LENGTH);
            String career = SecurityUtils.sanitizeText(etCareer.getText().toString(), SecurityUtils.MAX_ROLE_OR_CAREER_LENGTH);
            String controlNum = SecurityUtils.sanitizeText(etControlNumber.getText().toString(), SecurityUtils.MAX_CONTROL_LENGTH);
            boolean isNotStudent = cbNotStudent.isChecked();
            String staffRole = SecurityUtils.sanitizeText(etStaffRole.getText().toString(), SecurityUtils.MAX_ROLE_OR_CAREER_LENGTH);
            String classroom = SecurityUtils.sanitizeText(etClassroom.getText().toString(), SecurityUtils.MAX_CLASSROOM_LENGTH);
            String phone = SecurityUtils.sanitizeDigitsOnly(etPhoneNumber.getText().toString(), SecurityUtils.PHONE_LENGTH);

            if (!SecurityUtils.isValidHumanName(name)) {
                etCustomerName.setError("Ingresa un nombre válido (sin caracteres especiales)");
                etCustomerName.requestFocus();
                Toast.makeText(MainActivity.this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            if (isNotStudent && !SecurityUtils.isValidCareerOrRole(staffRole)) {
                etStaffRole.setError("Indica un puesto o área válido");
                etStaffRole.requestFocus();
                Toast.makeText(MainActivity.this, "Por favor indica un puesto o área válido.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isNotStudent && !SecurityUtils.isValidCareerOrRole(career)) {
                etCareer.setError("Ingresa una carrera válida");
                etCareer.requestFocus();
                Toast.makeText(MainActivity.this, "Por favor ingresa una carrera válida.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!SecurityUtils.isValidOptionalControlNumber(controlNum)) {
                etControlNumber.setError("Número de control inválido");
                etControlNumber.requestFocus();
                Toast.makeText(MainActivity.this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!SecurityUtils.isValidClassroom(classroom)) {
                etClassroom.setError("Ingresa un lugar de entrega válido");
                etClassroom.requestFocus();
                Toast.makeText(MainActivity.this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!SecurityUtils.isValidPhone10(phone)) {
                etPhoneNumber.setError(getString(R.string.invalid_phone));
                etPhoneNumber.requestFocus();
                Toast.makeText(MainActivity.this, getString(R.string.invalid_phone), Toast.LENGTH_SHORT).show();
                return;
            }

            if (qtyNutella > MAX_ROLLS_PER_FLAVOR || qtyQueso > MAX_ROLLS_PER_FLAVOR
                    || qtyClasico > MAX_ROLLS_PER_FLAVOR || qtyCajeta > MAX_ROLLS_PER_FLAVOR) {
                Toast.makeText(MainActivity.this, R.string.max_per_flavor_reached, Toast.LENGTH_SHORT).show();
                return;
            }

            if (salesManager.getActiveRollsCount() + calculateTotalItems() > MAX_ACTIVE_ROLLS_PER_USER) {
                Toast.makeText(MainActivity.this, R.string.max_active_rolls_reached, Toast.LENGTH_LONG).show();
                return;
            }

            String paymentMethod = isDigitalPaymentSelected[0] ? getString(R.string.pay_digital) : getString(R.string.pay_cash);

            Runnable submitOrderAction = () -> {
                List<OrderItem> items = new ArrayList<>();
                if (qtyNutella > 0) items.add(new OrderItem(getString(R.string.flavor_nutella), qtyNutella, 20.0));
                if (qtyQueso > 0) items.add(new OrderItem(getString(R.string.flavor_queso_crema), qtyQueso, 20.0));
                if (qtyClasico > 0) items.add(new OrderItem(getString(R.string.flavor_canela), qtyClasico, 20.0));
                if (qtyCajeta > 0) items.add(new OrderItem(getString(R.string.flavor_chocolate), qtyCajeta, 20.0));

                salesManager.saveCustomerProfile(name, career, controlNum, isNotStudent, staffRole, classroom, phone);

                String orderId = SecurityUtils.generateOrderId();
                String ownerUid = salesManager.getOrCreateClientUid();
                String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                String pickupCode = SecurityUtils.generatePickupCode();
                long now = System.currentTimeMillis();

                Order order = new Order(
                        orderId,
                        ownerUid,
                        name,
                        career,
                        controlNum,
                        isNotStudent,
                        staffRole,
                        classroom,
                        phone,
                        paymentMethod,
                        items,
                        calculateTotal(),
                        timestamp,
                        pickupCode,
                        false,
                        Order.STATUS_PENDING,
                        now,
                        now
                );
                salesManager.saveOrder(order);

                dialog.dismiss();
                resetQuantities();
                updateCustomerOrdersSection();

                showOrderConfirmationCodeDialog(pickupCode);
            };

            if (activeCount > 0) {
                showStyledConfirmDialog(
                        "Ya tienes un pedido activo",
                        "Actualmente tienes " + activeCount + " pedido(s) pendiente(s) de entrega. ¿Seguro que deseas enviar este pedido adicional?",
                        "Sí, pedir otro",
                        getString(R.string.cancel),
                        submitOrderAction
                );
            } else {
                submitOrderAction.run();
            }
        });

        dialog.show();
    }

    private void applyRoundedDialogWindow(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void showStyledConfirmDialog(String title, String message, String positiveText, String negativeText, Runnable onPositive) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_custom_alert, null);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        MaterialButton btnPositive = view.findViewById(R.id.btnDialogPositive);
        MaterialButton btnNegative = view.findViewById(R.id.btnDialogNegative);

        tvTitle.setText(title);
        tvMessage.setText(message);
        btnPositive.setText(positiveText);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        applyRoundedDialogWindow(dialog);

        btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            if (onPositive != null) {
                onPositive.run();
            }
        });

        if (!TextUtils.isEmpty(negativeText)) {
            btnNegative.setVisibility(View.VISIBLE);
            btnNegative.setText(negativeText);
            btnNegative.setOnClickListener(v -> dialog.dismiss());
        } else {
            btnNegative.setVisibility(View.GONE);
        }

        dialog.show();
    }

    private interface OnInputConfirmedListener {
        void onConfirmed(String input);
    }

    private void showStyledInputDialog(String title, String hint, int inputType, OnInputConfirmedListener listener) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_custom_alert, null);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        TextInputLayout tilInput = view.findViewById(R.id.tilDialogInput);
        EditText etInput = view.findViewById(R.id.etDialogInput);
        MaterialButton btnPositive = view.findViewById(R.id.btnDialogPositive);
        MaterialButton btnNegative = view.findViewById(R.id.btnDialogNegative);

        tvTitle.setText(title);
        tvMessage.setVisibility(View.GONE);
        tilInput.setVisibility(View.VISIBLE);
        tilInput.setHint(hint);
        etInput.setInputType(inputType);

        btnPositive.setText(R.string.accept);
        btnNegative.setVisibility(View.VISIBLE);
        btnNegative.setText(R.string.cancel);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();
        applyRoundedDialogWindow(dialog);

        btnPositive.setOnClickListener(v -> {
            String value = etInput.getText().toString().trim();
            dialog.dismiss();
            listener.onConfirmed(value);
        });

        btnNegative.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showOrderConfirmationCodeDialog(String code) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_custom_alert, null);
        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        View layoutCodeBox = view.findViewById(R.id.layoutDialogCodeBox);
        TextView tvCodeValue = view.findViewById(R.id.tvDialogCodeValue);
        MaterialButton btnPositive = view.findViewById(R.id.btnDialogPositive);

        tvTitle.setText(R.string.order_code_title);
        tvMessage.setText(R.string.order_code_message);
        layoutCodeBox.setVisibility(View.VISIBLE);
        tvCodeValue.setText(code);
        btnPositive.setText(R.string.accept);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();
        applyRoundedDialogWindow(dialog);

        btnPositive.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void copyToClipboard(String label, String text, String toastMessage) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void resetQuantities() {
        qtyNutella = 0;
        qtyQueso = 0;
        qtyClasico = 0;
        qtyCajeta = 0;

        syncQuantityLabels();
    }

    private void showAdminPinDialog() {
        if (rateLimiter.isAdminLocked()) {
            long waitSec = rateLimiter.getAdminLockRemainingSeconds();
            Toast.makeText(this, "Demasiados intentos fallidos. Espera " + waitSec + "s.", Toast.LENGTH_LONG).show();
            return;
        }

        showStyledInputDialog(
                getString(R.string.admin_pin_title),
                getString(R.string.admin_pin_hint),
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD,
                pin -> {
                    if (SecurityUtils.verifyAdminPin(pin)) {
                        rateLimiter.resetAdminFailures();
                        showAdminDialog();
                    } else {
                        rateLimiter.recordAdminFailure();
                        if (rateLimiter.isAdminLocked()) {
                            long waitSec = rateLimiter.getAdminLockRemainingSeconds();
                            Toast.makeText(MainActivity.this, "PIN bloqueado temporalmente (" + waitSec + "s).", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.incorrect_pin, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private static final int ADMIN_FILTER_ALL = 0;
    private static final int ADMIN_FILTER_PENDING = 1;
    private static final int ADMIN_FILTER_DELIVERED = 2;

    private void showAdminDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin, null);

        ImageButton btnBackAdmin = dialogView.findViewById(R.id.btnBackAdmin);
        TextView tvTotalRevenue = dialogView.findViewById(R.id.tvTotalRevenue);
        TextView tvRevenueBreakdown = dialogView.findViewById(R.id.tvRevenueBreakdown);
        TextView tvTotalOrdersCount = dialogView.findViewById(R.id.tvTotalOrdersCount);
        TextView tvPendingOrdersSummary = dialogView.findViewById(R.id.tvPendingOrdersSummary);
        TextView tvAdminCashTotal = dialogView.findViewById(R.id.tvAdminCashTotal);
        TextView tvAdminDigitalTotal = dialogView.findViewById(R.id.tvAdminDigitalTotal);
        View btnOpenSalesSummary = dialogView.findViewById(R.id.btnOpenSalesSummary);
        TextView tvAdminSummaryBtnMeta = dialogView.findViewById(R.id.tvAdminSummaryBtnMeta);
        MaterialButton btnFilterAll = dialogView.findViewById(R.id.btnFilterAll);
        MaterialButton btnFilterPending = dialogView.findViewById(R.id.btnFilterPending);
        MaterialButton btnFilterDelivered = dialogView.findViewById(R.id.btnFilterDelivered);
        LinearLayout containerOrdersList = dialogView.findViewById(R.id.containerOrdersList);
        MaterialButton btnClearHistory = dialogView.findViewById(R.id.btnClearHistory);

        final int[] currentFilter = {salesManager.getActiveOrdersCount() > 0 ? ADMIN_FILTER_PENDING : ADMIN_FILTER_ALL};

        Runnable refreshDashboard = new Runnable() {
            @Override
            public void run() {
                List<Order> allOrders = salesManager.getOrders();
                int pendingCount = 0;
                int deliveredCount = 0;
                int totalPendingRolls = 0;
                double cashRevenue = 0;
                double digitalRevenue = 0;

                String digitalLabel = getString(R.string.pay_digital);

                for (Order o : allOrders) {
                    if (o.isDelivered()) {
                        deliveredCount++;
                    } else {
                        pendingCount++;
                        for (OrderItem item : o.getItems()) {
                            totalPendingRolls += item.getQuantity();
                        }
                    }

                    if (digitalLabel.equals(o.getPaymentMethod())) {
                        digitalRevenue += o.getTotalAmount();
                    } else {
                        cashRevenue += o.getTotalAmount();
                    }
                }

                tvTotalRevenue.setText(String.format(Locale.getDefault(), "$%.2f MXN", salesManager.getTotalRevenue()));
                tvRevenueBreakdown.setText(String.format(
                        Locale.getDefault(),
                        "• Cobrado: $%.2f\n• Por cobrar: $%.2f",
                        salesManager.getDeliveredRevenue(),
                        salesManager.getPendingRevenue()
                ));

                tvTotalOrdersCount.setText(String.valueOf(allOrders.size()));
                tvPendingOrdersSummary.setText(String.format(
                        Locale.getDefault(),
                        "• Pendientes: %d (%d roles)\n• Entregados: %d",
                        pendingCount,
                        totalPendingRolls,
                        deliveredCount
                ));

                tvAdminCashTotal.setText(String.format(Locale.getDefault(), "$%.2f", cashRevenue));
                tvAdminDigitalTotal.setText(String.format(Locale.getDefault(), "$%.2f", digitalRevenue));

                if (allOrders.isEmpty()) {
                    tvAdminSummaryBtnMeta.setText(R.string.admin_summary_subtitle);
                } else {
                    int studentCount = 0;
                    int staffCount = 0;
                    for (Order o : allOrders) {
                        if (o.isNotStudent()) staffCount++;
                        else studentCount++;
                    }
                    tvAdminSummaryBtnMeta.setText(String.format(
                            Locale.getDefault(),
                            "%d ventas • %d alumnos • %d no alumnos",
                            allOrders.size(),
                            studentCount,
                            staffCount
                    ));
                }

                btnFilterAll.setText(String.format(Locale.getDefault(), "%s (%d)", getString(R.string.filter_all), allOrders.size()));
                btnFilterPending.setText(String.format(Locale.getDefault(), "%s (%d)", getString(R.string.filter_pending), pendingCount));
                btnFilterDelivered.setText(String.format(Locale.getDefault(), "%s (%d)", getString(R.string.filter_delivered), deliveredCount));

                styleAdminFilterButton(btnFilterAll, currentFilter[0] == ADMIN_FILTER_ALL);
                styleAdminFilterButton(btnFilterPending, currentFilter[0] == ADMIN_FILTER_PENDING);
                styleAdminFilterButton(btnFilterDelivered, currentFilter[0] == ADMIN_FILTER_DELIVERED);

                populateOrderHistory(containerOrdersList, currentFilter[0], this);
            }
        };

        btnOpenSalesSummary.setOnClickListener(v -> showAdminSummaryDialog(salesManager.getOrders()));

        btnFilterAll.setOnClickListener(v -> {
            currentFilter[0] = ADMIN_FILTER_ALL;
            refreshDashboard.run();
        });
        btnFilterPending.setOnClickListener(v -> {
            currentFilter[0] = ADMIN_FILTER_PENDING;
            refreshDashboard.run();
        });
        btnFilterDelivered.setOnClickListener(v -> {
            currentFilter[0] = ADMIN_FILTER_DELIVERED;
            refreshDashboard.run();
        });

        refreshDashboard.run();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        applyRoundedDialogWindow(dialog);

        btnBackAdmin.setOnClickListener(v -> dialog.dismiss());

        btnClearHistory.setOnClickListener(v -> showStyledConfirmDialog(
                getString(R.string.clear_history),
                getString(R.string.clear_history_confirm),
                getString(R.string.yes),
                getString(R.string.no),
                () -> {
                    salesManager.clearHistory();
                    refreshDashboard.run();
                    updateCustomerOrdersSection();
                }
        ));

        dialog.show();
    }

    private void showAdminSummaryDialog(List<Order> allOrders) {
        View summaryView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_summary, null);

        ImageButton btnCloseSummary = summaryView.findViewById(R.id.btnCloseSummary);
        MaterialButton btnCloseSummaryBottom = summaryView.findViewById(R.id.btnCloseSummaryBottom);
        TextView tvAdminSummaryQuickMeta = summaryView.findViewById(R.id.tvAdminSummaryQuickMeta);
        TextView tvSummaryOverview = summaryView.findViewById(R.id.tvSummaryOverview);
        LinearLayout containerSummaryCareers = summaryView.findViewById(R.id.containerSummaryCareers);
        LinearLayout containerSummaryStaff = summaryView.findViewById(R.id.containerSummaryStaff);
        LinearLayout containerSummaryFlavors = summaryView.findViewById(R.id.containerSummaryFlavors);

        populateAdminSalesSummary(
                allOrders,
                tvAdminSummaryQuickMeta,
                tvSummaryOverview,
                containerSummaryCareers,
                containerSummaryStaff,
                containerSummaryFlavors
        );

        AlertDialog summaryDialog = new AlertDialog.Builder(this)
                .setView(summaryView)
                .create();
        applyRoundedDialogWindow(summaryDialog);

        btnCloseSummary.setOnClickListener(v -> summaryDialog.dismiss());
        btnCloseSummaryBottom.setOnClickListener(v -> summaryDialog.dismiss());
        summaryDialog.show();
    }

    private static class GroupStats {
        String label;
        int orders;
        int rolls;
        double revenue;

        GroupStats(String label) {
            this.label = label;
        }
    }

    private void populateAdminSalesSummary(
            List<Order> allOrders,
            TextView tvQuickMeta,
            TextView tvOverview,
            LinearLayout containerCareers,
            LinearLayout containerStaff,
            LinearLayout containerFlavors
    ) {
        containerCareers.removeAllViews();
        containerStaff.removeAllViews();
        containerFlavors.removeAllViews();

        if (allOrders.isEmpty()) {
            tvQuickMeta.setText(R.string.admin_summary_subtitle);
            tvOverview.setText("Aún no hay ventas registradas para generar el resumen.");
            addSummaryEmptyRow(containerCareers, "Sin ventas de estudiantes");
            addSummaryEmptyRow(containerStaff, "Sin ventas de no estudiantes");
            addSummaryEmptyRow(containerFlavors, "Sin roles vendidos todavía");
            return;
        }

        int studentOrders = 0;
        int studentRolls = 0;
        int staffOrders = 0;
        int staffRolls = 0;
        int cashOrders = 0;
        int digitalOrders = 0;
        int totalRolls = 0;

        Map<String, GroupStats> careerMap = new LinkedHashMap<>();
        Map<String, GroupStats> staffMap = new LinkedHashMap<>();
        Map<String, Integer> flavorMap = new LinkedHashMap<>();

        String digitalLabel = getString(R.string.pay_digital);
        flavorMap.put(getString(R.string.flavor_nutella), 0);
        flavorMap.put(getString(R.string.flavor_queso_crema), 0);
        flavorMap.put(getString(R.string.flavor_canela), 0);
        flavorMap.put(getString(R.string.flavor_chocolate), 0);

        for (Order o : allOrders) {
            int orderRolls = 0;
            for (OrderItem item : o.getItems()) {
                orderRolls += item.getQuantity();
                String fName = item.getProductName();
                Integer prev = flavorMap.get(fName);
                flavorMap.put(fName, (prev != null ? prev : 0) + item.getQuantity());
            }
            totalRolls += orderRolls;

            if (digitalLabel.equals(o.getPaymentMethod())) {
                digitalOrders++;
            } else {
                cashOrders++;
            }

            if (o.isNotStudent()) {
                staffOrders++;
                staffRolls += orderRolls;
                String rawRole = TextUtils.isEmpty(o.getStaffRole()) ? "Personal (sin especificar)" : o.getStaffRole().trim();
                String key = rawRole.toLowerCase(Locale.getDefault());
                GroupStats gs = staffMap.get(key);
                if (gs == null) {
                    gs = new GroupStats(rawRole);
                    staffMap.put(key, gs);
                }
                gs.orders++;
                gs.rolls += orderRolls;
                gs.revenue += o.getTotalAmount();
            } else {
                studentOrders++;
                studentRolls += orderRolls;
                String rawCareer = TextUtils.isEmpty(o.getCareer()) ? "Sin carrera" : o.getCareer().trim();
                String key = rawCareer.toLowerCase(Locale.getDefault());
                GroupStats gs = careerMap.get(key);
                if (gs == null) {
                    gs = new GroupStats(rawCareer);
                    careerMap.put(key, gs);
                }
                gs.orders++;
                gs.rolls += orderRolls;
                gs.revenue += o.getTotalAmount();
            }
        }

        tvQuickMeta.setText(String.format(
                Locale.getDefault(),
                "%d ventas • %d alumnos • %d no alumnos",
                allOrders.size(),
                studentOrders,
                staffOrders
        ));

        tvOverview.setText(String.format(
                Locale.getDefault(),
                "• Total: %d pedidos (%d roles)\n• Estudiantes: %d pedidos (%d roles)\n• No estudiantes: %d pedidos (%d roles)\n• Pagos: %d en efectivo • %d por transferencia",
                allOrders.size(), totalRolls,
                studentOrders, studentRolls,
                staffOrders, staffRolls,
                cashOrders, digitalOrders
        ));

        List<GroupStats> sortedCareers = new ArrayList<>(careerMap.values());
        sortedCareers.sort((a, b) -> Integer.compare(b.rolls, a.rolls));
        if (sortedCareers.isEmpty()) {
            addSummaryEmptyRow(containerCareers, "Sin ventas a estudiantes");
        } else {
            for (GroupStats gs : sortedCareers) {
                addSummaryLineRow(
                        containerCareers,
                        gs.label,
                        String.format(Locale.getDefault(), "%d ped. (%d roles) • $%.0f", gs.orders, gs.rolls, gs.revenue)
                );
            }
        }

        List<GroupStats> sortedStaff = new ArrayList<>(staffMap.values());
        sortedStaff.sort((a, b) -> Integer.compare(b.rolls, a.rolls));
        if (sortedStaff.isEmpty()) {
            addSummaryEmptyRow(containerStaff, "Sin ventas a personal o externos");
        } else {
            for (GroupStats gs : sortedStaff) {
                addSummaryLineRow(
                        containerStaff,
                        gs.label,
                        String.format(Locale.getDefault(), "%d ped. (%d roles) • $%.0f", gs.orders, gs.rolls, gs.revenue)
                );
            }
        }

        List<Map.Entry<String, Integer>> sortedFlavors = new ArrayList<>(flavorMap.entrySet());
        sortedFlavors.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        for (Map.Entry<String, Integer> entry : sortedFlavors) {
            int count = entry.getValue();
            addSummaryLineRow(
                    containerFlavors,
                    entry.getKey(),
                    count == 1 ? "1 rol" : count + " roles"
            );
        }
    }

    private void addSummaryLineRow(LinearLayout container, String leftLabel, String rightValue) {
        float density = getResources().getDisplayMetrics().density;
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = (int) (3 * density);
        row.setLayoutParams(params);

        TextView tvLeft = new TextView(this);
        tvLeft.setText("• " + leftLabel);
        tvLeft.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        tvLeft.setTextSize(12f);
        tvLeft.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvRight = new TextView(this);
        tvRight.setText(rightValue);
        tvRight.setTextColor(ContextCompat.getColor(this, R.color.burgundy));
        tvRight.setTextSize(12f);
        tvRight.setTypeface(null, android.graphics.Typeface.BOLD);

        row.addView(tvLeft);
        row.addView(tvRight);
        container.addView(row);
    }

    private void addSummaryEmptyRow(LinearLayout container, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tv.setTextSize(12f);
        container.addView(tv);
    }

    private void styleAdminFilterButton(MaterialButton button, boolean selected) {
        if (selected) {
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.burgundy));
            button.setTextColor(ContextCompat.getColor(this, R.color.white));
            button.setStrokeWidth(0);
        } else {
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.cozy_surface));
            button.setTextColor(ContextCompat.getColor(this, R.color.burgundy));
            button.setStrokeColor(ContextCompat.getColorStateList(this, R.color.burgundy));
            button.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
        }
    }

    private void populateOrderHistory(LinearLayout container, int filterMode, Runnable onOrdersChanged) {
        container.removeAllViews();
        List<Order> allOrders = salesManager.getOrders();
        List<Order> pendingFifo = new ArrayList<>();
        List<Order> deliveredOrders = new ArrayList<>();

        for (Order order : allOrders) {
            if (order.isDelivered()) {
                deliveredOrders.add(order);
            } else {
                pendingFifo.add(order);
            }
        }

        Collections.reverse(pendingFifo);
        pendingFifo.sort(Comparator.comparingLong(Order::getCreatedAtEpochMs));

        List<Order> filteredOrders = new ArrayList<>();
        if (filterMode == ADMIN_FILTER_PENDING) {
            filteredOrders.addAll(pendingFifo);
        } else if (filterMode == ADMIN_FILTER_DELIVERED) {
            filteredOrders.addAll(deliveredOrders);
        } else {
            filteredOrders.addAll(pendingFifo);
            filteredOrders.addAll(deliveredOrders);
        }

        if (filteredOrders.isEmpty()) {
            TextView emptyTv = new TextView(this);
            if (allOrders.isEmpty()) {
                emptyTv.setText("No hay ventas registradas aún.");
            } else if (filterMode == ADMIN_FILTER_PENDING) {
                emptyTv.setText("No hay pedidos pendientes.");
            } else {
                emptyTv.setText("No hay pedidos entregados aún.");
            }
            emptyTv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            emptyTv.setPadding(0, 20, 0, 20);
            container.addView(emptyTv);
            return;
        }

        for (Order order : filteredOrders) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_order_history, container, false);

            TextView tvCustomer = itemView.findViewById(R.id.tvOrderCustomer);
            TextView tvAmount = itemView.findViewById(R.id.tvOrderAmount);
            ImageView ivCareerOrRole = itemView.findViewById(R.id.ivOrderCareerOrRole);
            TextView tvCareerOrRole = itemView.findViewById(R.id.tvOrderCareerOrRole);
            View rowControl = itemView.findViewById(R.id.rowOrderControl);
            TextView tvControl = itemView.findViewById(R.id.tvOrderControl);
            View rowPhone = itemView.findViewById(R.id.rowOrderPhone);
            TextView tvPhone = itemView.findViewById(R.id.tvOrderPhone);
            TextView tvClassroom = itemView.findViewById(R.id.tvOrderClassroom);
            ImageView ivPaymentIcon = itemView.findViewById(R.id.ivOrderPaymentIcon);
            TextView tvPayment = itemView.findViewById(R.id.tvOrderPayment);
            TextView tvItems = itemView.findViewById(R.id.tvOrderItems);
            TextView tvTimestamp = itemView.findViewById(R.id.tvOrderTimestamp);
            TextView tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            MaterialButton btnDeliverOrder = itemView.findViewById(R.id.btnDeliverOrder);
            View layoutSecondaryActions = itemView.findViewById(R.id.layoutAdminOrderSecondaryActions);
            MaterialButton btnCallCustomer = itemView.findViewById(R.id.btnCallCustomer);
            MaterialButton btnDeleteOrder = itemView.findViewById(R.id.btnDeleteOrder);

            tvCustomer.setText(order.getCustomerName());
            tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", order.getTotalAmount()));

            if (order.isNotStudent()) {
                ivCareerOrRole.setImageResource(R.drawable.ic_person);
                tvCareerOrRole.setText(formatLabeledDetail("Puesto: ", order.getStaffRole()));
                rowControl.setVisibility(View.GONE);
            } else {
                ivCareerOrRole.setImageResource(R.drawable.ic_school);
                tvCareerOrRole.setText(formatLabeledDetail("Carrera: ", order.getCareer()));
                if (!TextUtils.isEmpty(order.getControlNumber())) {
                    rowControl.setVisibility(View.VISIBLE);
                    tvControl.setText(formatLabeledDetail("No. control: ", order.getControlNumber()));
                } else {
                    rowControl.setVisibility(View.GONE);
                }
            }

            if (!TextUtils.isEmpty(order.getPhoneNumber())) {
                rowPhone.setVisibility(View.VISIBLE);
                tvPhone.setText(formatLabeledDetail("Teléfono: ", order.getPhoneNumber()));
            } else {
                rowPhone.setVisibility(View.GONE);
            }

            String deliveryLocation = !TextUtils.isEmpty(order.getClassroom()) ? order.getClassroom() : "Sin ubicación";
            tvClassroom.setText(formatLabeledDetail("Entrega: ", deliveryLocation));

            boolean isDigital = getString(R.string.pay_digital).equals(order.getPaymentMethod());
            ivPaymentIcon.setImageResource(isDigital ? R.drawable.ic_bank : R.drawable.ic_cash);
            tvPayment.setText(formatLabeledDetail("Pago: ", order.getPaymentMethod()));

            StringBuilder itemsSb = new StringBuilder();
            List<OrderItem> items = order.getItems();
            for (int i = 0; i < items.size(); i++) {
                OrderItem item = items.get(i);
                itemsSb.append("• ").append(item.getQuantity()).append("x ").append(item.getProductName());
                if (i < items.size() - 1) {
                    itemsSb.append("\n");
                }
            }
            tvItems.setText(itemsSb.toString());
            tvTimestamp.setText(order.getTimestamp());

            if (order.isDelivered()) {
                tvOrderStatus.setText(R.string.status_delivered);
                tvOrderStatus.setTextColor(ContextCompat.getColor(this, R.color.green_success));
                ViewCompat.setBackgroundTintList(tvOrderStatus, ColorStateList.valueOf(Color.parseColor("#182E7D32")));
                btnDeliverOrder.setVisibility(View.GONE);
                layoutSecondaryActions.setVisibility(View.GONE);
            } else {
                tvOrderStatus.setText(R.string.status_pending);
                tvOrderStatus.setTextColor(ContextCompat.getColor(this, R.color.burgundy));
                ViewCompat.setBackgroundTintList(tvOrderStatus, ColorStateList.valueOf(Color.parseColor("#16800020")));
                btnDeliverOrder.setVisibility(View.VISIBLE);
                layoutSecondaryActions.setVisibility(View.VISIBLE);

                btnDeliverOrder.setOnClickListener(v -> showDeliveryCodePromptDialog(order, onOrdersChanged));

                if (!TextUtils.isEmpty(order.getPhoneNumber())) {
                    btnCallCustomer.setVisibility(View.VISIBLE);
                    btnCallCustomer.setOnClickListener(v -> dialPhoneNumber(order.getPhoneNumber()));
                } else {
                    btnCallCustomer.setVisibility(View.GONE);
                }

                btnDeleteOrder.setOnClickListener(v -> showStyledConfirmDialog(
                        getString(R.string.admin_delete_order_title),
                        getString(R.string.admin_delete_order_confirm),
                        getString(R.string.yes),
                        getString(R.string.no),
                        () -> {
                            salesManager.deleteOrder(order.getId());
                            Toast.makeText(MainActivity.this, R.string.admin_order_deleted, Toast.LENGTH_SHORT).show();
                            onOrdersChanged.run();
                            updateCustomerOrdersSection();
                        }
                ));
            }

            container.addView(itemView);
        }
    }

    private CharSequence formatLabeledDetail(String label, String value) {
        String safeValue = TextUtils.isEmpty(value) ? "" : value;
        SpannableStringBuilder ssb = new SpannableStringBuilder(label);
        ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(
                new ForegroundColorSpan(ContextCompat.getColor(this, R.color.burgundy_dark)),
                0,
                label.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        ssb.append(safeValue);
        return ssb;
    }

    private void showDeliveryCodePromptDialog(Order order, Runnable onDelivered) {
        if (rateLimiter.isOrderCodeLocked(order.getId())) {
            long waitSec = rateLimiter.getOrderCodeLockRemainingSeconds(order.getId());
            Toast.makeText(this, "Código bloqueado temporalmente. Espera " + waitSec + "s.", Toast.LENGTH_LONG).show();
            return;
        }

        showStyledInputDialog(
                getString(R.string.enter_order_code),
                getString(R.string.code_hint),
                InputType.TYPE_CLASS_NUMBER,
                enteredCode -> {
                    String safeCode = SecurityUtils.sanitizeDigitsOnly(enteredCode, 4);
                    if (SecurityUtils.isValidPickupCode(safeCode) && safeCode.equals(order.getPickupCode())) {
                        rateLimiter.resetOrderCodeFailures(order.getId());
                        order.setDelivered(true);
                        salesManager.updateOrder(order);
                        Toast.makeText(MainActivity.this, R.string.order_delivered_success, Toast.LENGTH_SHORT).show();
                        onDelivered.run();
                        updateCustomerOrdersSection();
                    } else {
                        rateLimiter.recordOrderCodeFailure(order.getId());
                        Toast.makeText(MainActivity.this, R.string.incorrect_code, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}