# MineralWarehouse (Kho Khoáng Sản)

Plugin Minecraft hỗ trợ người chơi lưu trữ và quản lý khoáng sản trong một kho ảo thông minh, giúp tối ưu hóa không gian túi đồ và tích hợp hệ thống kinh tế.

## 🌟 Tính năng nổi bật

- **Kho ảo SQLite:** Lưu trữ dữ liệu an toàn, hiệu suất cao, không gây lag.
- **Giao diện GUI:** Trực quan, dễ sử dụng với các biểu tượng hỗ trợ.
- **Tự động cất (Auto-Deposit):** Quặng tự động bay vào kho khi đào (Có thể bật/tắt).
- **Cất nhanh (Deposit All):** Cất toàn bộ khoáng sản trong túi đồ chỉ với 1 click.
- **Hệ thống Bán (Sell):** Tích hợp Vault để bán khoáng sản lấy tiền trực tiếp từ kho.
- **Hỗ trợ Admin:** Xem và điều chỉnh kho của người chơi khác dễ dàng.
- **Tương thích:** Hoạt động tốt trên phiên bản 1.12.2 và các phiên bản lân cận.

## 🛠 Lệnh & Quyền hạn

### Dành cho Người chơi
| Lệnh | Mô tả | Quyền |
|------|-------|-------|
| `/kho` | Mở giao diện kho khoáng sản | `mineralwarehouse.use` |
| `/kho cat` | Cất vật phẩm đang cầm trên tay vào kho | `mineralwarehouse.use` |
| `/kho cathet` | Cất toàn bộ khoáng sản trong túi đồ vào kho | `mineralwarehouse.use` |
| `/kho autocat` | Bật/Tắt chế độ tự động cất khi đào | `mineralwarehouse.use` |
| `/kho ban <loại> [SL]` | Bán khoáng sản trong kho | `mineralwarehouse.use` |
| `/kho banhet` | Bán toàn bộ khoáng sản đang có trong kho | `mineralwarehouse.use` |

### Dành cho Admin
| Lệnh | Mô tả | Quyền |
|------|-------|-------|
| `/kho see <người_chơi>` | Xem kho của người chơi khác | `mineralwarehouse.admin` |
| `/kho add <tên> <loại> <SL>` | Thêm khoáng sản cho người chơi | `mineralwarehouse.admin` |
| `/kho set <tên> <loại> <SL>` | Đặt số lượng khoáng sản | `mineralwarehouse.admin` |
| `/kho take <tên> <loại> <SL>`| Lấy bớt khoáng sản của người chơi | `mineralwarehouse.admin` |
| `/kho reload` | Tải lại cấu hình plugin | `mineralwarehouse.admin` |

## 📦 Cài đặt

1. Yêu cầu có **Vault** và một plugin Kinh tế (như EssentialsX) để sử dụng chức năng bán.
2. Tải file `MineralWarehouse-1.0.0.jar` vào thư mục `plugins`.
3. Khởi động lại Server.
4. Chỉnh sửa giá cả và danh sách khoáng sản trong `config.yml`.

## ⚙️ Cấu hình (config.yml)

Bạn có thể tùy chỉnh tên hiển thị, giá bán và các tin nhắn thông báo hoàn toàn bằng tiếng Việt có dấu.

---
**Phát triển bởi AnNgTv**
