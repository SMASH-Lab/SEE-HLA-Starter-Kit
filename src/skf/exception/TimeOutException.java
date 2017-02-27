package skf.exception;

public class TimeOutException extends RuntimeException {

	private static final long serialVersionUID = 2905830034915116015L;
	
	public TimeOutException(String message){
		super(message);
	}
	
	public TimeOutException(Throwable cause){
		super(cause);
	}
	
	public TimeOutException(String message, Throwable cause){
		super(message, cause);
	}

}
