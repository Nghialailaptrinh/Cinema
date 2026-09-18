# Quy trình phát triển Cinema V1 — dự án cá nhân

Dự án do một người thực hiện. Tài liệu này thay thế kế hoạch phân công nhóm cũ;
giữ đường dẫn để các liên kết trước đây vẫn hoạt động.

## Thứ tự triển khai

Theo [roadmap M0–M12](cinema-v1-plan.md), làm từng milestone và kiểm chứng
trước khi sang milestone kế tiếp. Các mục 1–18 của kế hoạch là mục tài liệu,
không phải 18 milestone kỹ thuật độc lập.

Trạng thái hiện hành: [tiến độ Cinema V1](cinema-v1-progress.md).
M0 và M1 đã triển khai; M2 — Movie là bước tiếp theo.

## Chu trình cho mỗi milestone

1. Xác định phạm vi, quy tắc nghiệp vụ và điều kiện nghiệm thu.
2. Hoàn thiện Domain, port, command/query, handler và DTO.
3. Nối adapter và HTTP cần thiết để chạy được luồng chức năng.
4. Kiểm thử hành vi thành công, dữ liệu sai và các ràng buộc liên quan.
5. Chạy build sạch và toàn bộ kiểm thử; kiểm tra thay đổi và cập nhật tài liệu.
6. Lưu thay đổi bằng commit phù hợp khi cần.

Không chia công việc theo bốn thành viên, không yêu cầu duyệt của trưởng nhóm,
không chờ PR của người khác. Có thể sửa xuyên các tầng để hoàn thành milestone,
nhưng vẫn giữ ranh giới Clean Architecture.

## Điều kiện hoàn thành

- Chức năng chạy thực tế theo đặc tả, không chỉ có skeleton.
- Các kiểm thử bắt buộc có assertion thực, không bị bỏ qua.
- Kiểm thử kiến trúc và hồi quy vẫn đạt.
- Ghi rõ số test đạt, thất bại và bỏ qua cùng lý do.
- Ghi rõ giới hạn có chủ đích của milestone.

M1 sử dụng [đặc tả](m1-specification.md) và [ma trận nghiệm thu](m1-acceptance.md).
Các tài liệu mang tên thành viên trước đây chỉ còn là liên kết chuyển tiếp.
