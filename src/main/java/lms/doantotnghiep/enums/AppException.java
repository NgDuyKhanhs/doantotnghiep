package lms.doantotnghiep.enums;

public class AppException extends RuntimeException {

    public AppException(ErrorConstant errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    private ErrorConstant errorCode;

    public ErrorConstant getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorConstant errorCode) {
        this.errorCode = errorCode;
    }
}
