/**
 * Lớp ngoại lệ tự định nghĩa để xử lý lỗi đăng nhập.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}