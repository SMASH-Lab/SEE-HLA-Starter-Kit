package skf.exception;

public class RegisterElementNameException extends Exception {
	
	private static final long serialVersionUID = 1162350781464584024L;

	public RegisterElementNameException(String message){
		super(message);
	}
	
	public RegisterElementNameException(String message, Throwable cause){
		super(message, cause);
	}
    
	public RegisterElementNameException(Throwable cause){
		super(cause);
	} 

}
