package bankslips.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BankSlipInvalidUUIDException extends RuntimeException {

	private static final long serialVersionUID = -1713877996690226944L;

	public BankSlipInvalidUUIDException() {
		super("Invalid id provided - it must be a valid UUID");
	}
}
