# Nghiệm thu M1 — Cinema / Hall / Seat

Ngày kiểm chứng: 2026-09-18. Trạng thái: **hoàn thành trong phạm vi M1**.
Dự án cá nhân; không có điều kiện chờ thành viên hoặc duyệt PR nội bộ.

## Phạm vi đã kiểm chứng

| Phần | Bằng chứng kiểm thử | Số trường hợp |
| --- | --- | ---: |
| Domain | CinemaTest, HallTest, SeatTest, VenueValidationTest: null/blank, số ghế, loại ghế, trim, Locale.ROOT, giữ nguyên ID tham chiếu | 17 |
| Application command | VenueCommandHandlerTest: tạo và sinh ID, chuẩn hóa, cha thiếu, dữ liệu sai, kiểm tra trùng, truyền ConflictException và lỗi hạ tầng | 39 |
| Application query | VenueQueryHandlerTest: ánh xạ đầy đủ DTO, null/blank, không có tài nguyên, cha tồn tại nhưng danh sách con rỗng | 11 |
| In-memory | Ba InMemory*RepositoryTest và VenueRepositoryContractTest: lookup, filter/sort, ID và khóa nghiệp vụ trùng, snapshot, độc lập instance | 13 |
| HTTP tích hợp | VenueApiIntegrationTest: tạo/đọc hierarchy, đủ 8 endpoint, 400/404/409, ghế COUPLE và không ghi đè khi trùng | 13 |
| Security/cấu hình | VenueWebBoundaryTest, DefaultVenueSecurityTest, VenueDevelopmentContextTest: allowlist method/path, CSRF và profile mặc định deny-all | 40 |
| Hồi quy nền tảng | DomainRulesTest, CleanArchitectureTest, ScaffoldContractTest, CinemaApplicationTests | 21 |

Hai trường hợp cạnh tranh nằm trong VenueRepositoryContractTest. Hai luồng dùng
ID khác nhau và cùng khóa nghiệp vụ, chờ latch trước khi ghi. Kết quả bắt buộc:
đúng một thành công, một ConflictException, một bản ghi và không có ID thua
trong repository. Có timeout và đóng executor; không dùng sleep để giả đồng thời.

Test snapshot kiểm tra cả tính bất biến và việc kết quả cũ không đổi sau lần ghi mới.
Test Application dùng Mockito và ID xác định, không khởi động Spring.
Test HTTP tích hợp dùng controller, handler và repository thật trong ứng dụng;
test ProbeController riêng chỉ chứng minh ranh giới security/JSON.

## Kết quả build sạch

```powershell
.\mvnw.cmd '-Dmaven.repo.local=C:\Users\admin\.m2\repository' -o clean verify
```

JDK 17, Maven Wrapper, cache dependency có sẵn. Đường dẫn cache tùy máy;
bỏ `-o` khi cần tải dependency lần đầu. Trong môi trường kiểm chứng, cần quyền
truy cập cache ngoài workspace để javac đọc các JAR.

**BUILD SUCCESS: 162 trường hợp, 154 đạt, 0 failure, 0 error, 8 skipped.**
Tám test bị bỏ qua là BookingWorkflowTest skeleton thuộc các milestone sau;
không tính chúng là bằng chứng hoàn thành M1. Không test M1 nào bị bỏ qua.
Báo cáo chi tiết được Maven tạo trong `target/surefire-reports/`.

## Điều chỉnh sau rà soát

- Bổ sung 73 trường hợp so với bộ kiểm thử 89 trường hợp ban đầu.
- CreateSeat kiểm tra row null/blank trước lookup Hall: dữ liệu sai trả 400
  VALIDATION kể cả khi Hall không tồn tại.
- Gom tài liệu nghiệm thu và thay quy trình phân công nhóm bằng quy trình cá nhân.

## Giới hạn có chủ đích

M1 dùng in-memory trong một JVM, mất dữ liệu khi restart; API chỉ mở với
profile `venue-dev`. JPA/migration, sửa/xóa địa điểm, tạo ghế hàng loạt,
Screening, booking, thanh toán, JWT và phân quyền ADMIN thật thuộc phạm vi sau.

Bước tiếp theo: **M2 — Movie**, theo [tiến độ](cinema-v1-progress.md).
