package gui;

import enums.PaymentMethod;
import enums.PaymentStatus;
import models.Payment;
import processor.AsyncPaymentExecutor;
import processor.CardPaymentProcessor;
import processor.PaymentProcessor;
import processor.UPIPaymentProcessor;
import repository.InMemoryIdempotencyRepository;
import repository.InMemoryPaymentRepository;
import repository.IdempotencyRepository;
import repository.Repository;
import service.PaymentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PaymentGatewayApp extends JFrame {
    private final Repository paymentRepository = new InMemoryPaymentRepository();
    private final IdempotencyRepository idempotencyRepository = new InMemoryIdempotencyRepository();
    private final AsyncPaymentExecutor executor = new AsyncPaymentExecutor(paymentRepository);
    private final PaymentService paymentService;

    private final JTextField amountField = new JTextField("499.00", 12);
    private final JTextField keyField = new JTextField("order-" + java.util.UUID.randomUUID(), 25);
    private final JComboBox<PaymentMethod> methodBox = new JComboBox<>(PaymentMethod.values());
    private final JLabel banner = new JLabel("Ready — all payments are simulated; no real money moves.");
    private final JLabel countLabel = new JLabel("0 transactions");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Created", "Payment ID", "Amount", "Method", "Status", "Idempotency Key", "Details"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PaymentGatewayApp() {
        super("Payment Gateway — Desktop Console");
        Map<PaymentMethod, PaymentProcessor> processors = new EnumMap<>(PaymentMethod.class);
        processors.put(PaymentMethod.CARD, new CardPaymentProcessor(executor));
        processors.put(PaymentMethod.UPI, new UPIPaymentProcessor(executor));
        paymentService = new PaymentService(processors, paymentRepository, idempotencyRepository);
        buildUi();
        Timer timer = new Timer(500, e -> refreshHistory());
        timer.start();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { executor.shutdown(); }
        });
        setMinimumSize(new Dimension(1050, 640));
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        Color bg = new Color(245, 247, 251), ink = new Color(28, 40, 66), muted = new Color(94, 106, 128);
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(22, 24, 18, 24));
        root.setBackground(bg);

        JLabel title = new JLabel("Payment Gateway");
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        title.setForeground(ink);
        JLabel subtitle = new JLabel("Payment orchestration  /  CARD + UPI  /  Idempotency  /  Async status tracking");
        subtitle.setForeground(muted);
        JPanel heading = new JPanel(new GridLayout(0, 1, 0, 5));
        heading.setOpaque(false); heading.add(title); heading.add(subtitle);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(222, 228, 238)), new EmptyBorder(14, 14, 14, 14)));
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(5, 6, 5, 6); c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL;
        addField(form, c, 0, "Amount (₹)", amountField);
        addField(form, c, 1, "Payment method", methodBox);
        addField(form, c, 2, "Idempotency key", keyField);

        JButton create = new JButton("Create & Process");
        create.setBackground(new Color(39, 88, 190)); create.setForeground(Color.WHITE); create.setFocusPainted(false);
        create.addActionListener(e -> createPayment());
        JButton refresh = new JButton("Refresh status"); refresh.addActionListener(e -> refreshHistory());
        JButton newKey = new JButton("Generate key"); newKey.addActionListener(e -> keyField.setText("order-" + java.util.UUID.randomUUID()));
        JButton details = new JButton("View selected details"); details.addActionListener(e -> showSelectedDetails());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); buttons.setOpaque(false);
        buttons.add(create); buttons.add(refresh); buttons.add(newKey); buttons.add(details);

        JPanel top = new JPanel(new BorderLayout(0, 14)); top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        JPanel controls = new JPanel(new BorderLayout(0, 10)); controls.setOpaque(false); controls.add(form, BorderLayout.CENTER); controls.add(buttons, BorderLayout.SOUTH);
        top.add(controls, BorderLayout.CENTER);

        table.setRowHeight(29); table.setFillsViewportHeight(true); table.setAutoCreateRowSorter(true); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getColumnModel().getColumn(1).setPreferredWidth(270);
        table.getColumnModel().getColumn(5).setPreferredWidth(190);
        table.getColumnModel().getColumn(6).setPreferredWidth(260);
        JScrollPane scroll = new JScrollPane(table); scroll.setBorder(BorderFactory.createLineBorder(new Color(222, 228, 238)));
        JLabel history = new JLabel("Transaction history"); history.setFont(new Font("SansSerif", Font.BOLD, 18)); history.setForeground(ink);
        JPanel center = new JPanel(new BorderLayout(0, 10)); center.setOpaque(false); center.add(history, BorderLayout.NORTH); center.add(scroll, BorderLayout.CENTER);

        banner.setForeground(muted); countLabel.setForeground(ink); countLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false); footer.add(banner, BorderLayout.WEST); footer.add(countLabel, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH); root.add(center, BorderLayout.CENTER); root.add(footer, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void addField(JPanel p, GridBagConstraints c, int row, String label, Component field) {
        c.gridy = row; c.gridx = 0; c.weightx = 0; p.add(new JLabel(label), c);
        c.gridx = 1; c.weightx = 1; p.add(field, c);
    }

    private void createPayment() {
        final double amount;
        try {
            String raw = amountField.getText().trim();
            if (raw.isEmpty()) throw new NumberFormatException();
            amount = Double.parseDouble(raw);
            if (!Double.isFinite(amount) || amount <= 0) { error("Amount must be a positive number."); return; }
        } catch (NumberFormatException ex) { error("Enter a valid amount, for example 499.00."); return; }
        String key = keyField.getText().trim();
        if (key.isEmpty()) { error("Enter an idempotency key or click Generate key."); return; }
        PaymentMethod method = (PaymentMethod) methodBox.getSelectedItem();
        try {
            boolean duplicate = paymentService.idempotencyKeyExists(key);
            Payment payment = paymentService.createPayment(amount, method, key);
            if (!duplicate) paymentService.processPayment(payment.getPaymentId());
            refreshHistory();
            if (duplicate) {
                banner.setText("Existing payment returned — duplicate processing was not started.");
                JOptionPane.showMessageDialog(this,
                        "This idempotency key already exists.\nThe existing payment was returned; no second payment was started.\n\nPayment ID: " + payment.getPaymentId() + "\nCurrent status: " + payment.getStatus(),
                        "Existing payment", JOptionPane.INFORMATION_MESSAGE);
            } else {
                banner.setText("Payment submitted: " + payment.getPaymentId());
                JOptionPane.showMessageDialog(this,
                        "Payment created and processing started.\n\nPayment ID: " + payment.getPaymentId() + "\nMethod: " + payment.getMethod() + "\nAmount: ₹" + String.format("%.2f", payment.getAmount()) + "\nStatus: " + payment.getStatus(),
                        "Payment submitted", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (RuntimeException ex) { error(ex.getMessage() == null ? "Payment failed." : ex.getMessage()); }
    }

    private void refreshHistory() {
        List<Payment> payments = paymentService.getAllPayments();
        java.util.Set<String> existingIds = new java.util.HashSet<>();
        for (int i = 0; i < model.getRowCount(); i++) existingIds.add(String.valueOf(model.getValueAt(i, 1)));
        for (Payment p : payments) {
            int row = findRow(p.getPaymentId());
            String created = formatter.withZone(ZoneId.systemDefault()).format(p.getCreatedAt());
            Object[] values = {created, p.getPaymentId(), String.format("₹ %.2f", p.getAmount()), p.getMethod(), p.getStatus(), p.getIdempotencyKey(), p.getMessage()};
            if (row < 0) model.addRow(values);
            else for (int col = 0; col < values.length; col++) model.setValueAt(values[col], row, col);
        }
        countLabel.setText(payments.size() + (payments.size() == 1 ? " transaction" : " transactions"));
    }

    private int findRow(String id) {
        for (int row = 0; row < model.getRowCount(); row++) if (id.equals(String.valueOf(model.getValueAt(row, 1)))) return row;
        return -1;
    }

    private void showSelectedDetails() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Select a transaction first.", "No selection", JOptionPane.INFORMATION_MESSAGE); return; }
        int row = table.convertRowIndexToModel(viewRow);
        String id = String.valueOf(model.getValueAt(row, 1));
        Payment p = paymentService.getPaymentStatus(id);
        String text = "Payment ID: " + p.getPaymentId() + "\nAmount: ₹" + String.format("%.2f", p.getAmount()) + "\nMethod: " + p.getMethod() + "\nStatus: " + p.getStatus() + "\nIdempotency key: " + p.getIdempotencyKey() + "\nCreated: " + formatter.withZone(ZoneId.systemDefault()).format(p.getCreatedAt()) + "\nDetails: " + p.getMessage();
        JOptionPane.showMessageDialog(this, text, "Payment details", p.getStatus() == PaymentStatus.FAILED ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String message) {
        banner.setText("Error: " + message);
        JOptionPane.showMessageDialog(this, message, "Payment error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new PaymentGatewayApp().setVisible(true));
    }
}
