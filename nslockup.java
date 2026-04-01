public class nslockup {
    import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class NSLookupApp extends JFrame {
    private JTextField txtDomain;
    private JTextArea txtResult;
    private JButton btnLookup;

    public NSLookupApp() {
        // Thiết lập tiêu đề và kích thước cửa sổ
        setTitle("Ứng dụng NSLookup đơn giản - UET");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel nhập liệu (Phần trên)
        JPanel panelInput = new JPanel(new GridLayout(2, 1, 5, 5));
        panelInput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblDomain = new JLabel("Nhập tên miền (ví dụ: google.com):");
        lblDomain.setFont(new Font("Arial", Font.BOLD, 14));
        
        txtDomain = new JTextField();
        txtDomain.setFont(new Font("Arial", Font.PLAIN, 16));
        
        panelInput.add(lblDomain);
        panelInput.add(txtDomain);

        // Nút bấm (Phần giữa)
        btnLookup = new JButton("Tra cứu IP");
        btnLookup.setFont(new Font("Arial", Font.BOLD, 14));
        btnLookup.setBackground(new Color(0, 123, 255));
        btnLookup.setForeground(Color.WHITE);

        // Panel kết quả (Phần dưới)
        txtResult = new JTextArea();
        txtResult.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtResult.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtResult);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Kết quả trả về:"));

        // Thêm các thành phần vào Frame
        add(panelInput, BorderLayout.NORTH);
        add(btnLookup, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);
        scrollPane.setPreferredSize(new Dimension(480, 200));

        // Xử lý sự kiện khi nhấn nút
        btnLookup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLookup();
            }
        });
        txtDomain.addActionListener(e -> performLookup());
    }

    private void performLookup() {
        String domain = txtDomain.getText().trim();
        if (domain.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên miền!");
            return;
        }

        try {
            // Logic chính tương tự trong video [00:14:17]
            InetAddress address = InetAddress.getByName(domain);
            String ip = address.getHostAddress();
            txtResult.setText("Tên miền: " + domain + "\nĐịa chỉ IP: " + ip);
        } catch (UnknownHostException ex) {
            // Xử lý lỗi khi không tìm thấy domain [00:15:41]
            txtResult.setText("Lỗi: Không tìm thấy địa chỉ IP cho tên miền này.");
        }
    }

    public static void main(String[] args) {
        // Chạy ứng dụng
        SwingUtilities.invokeLater(() -> {
            new NSLookupApp().setVisible(true);
        });
    }
}

    
}
