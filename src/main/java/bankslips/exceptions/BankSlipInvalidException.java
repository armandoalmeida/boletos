package bankslips.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
public class BankSlipInvalidException extends RuntimeException {

	private static final long serialVersionUID = -1713877996690226944L;

	public BankSlipInvalidException() {
		super("Invalid bankslip provided.The possible reasons are:"
				+ " A field of the provided bankslip was null or with invalid values");
	}
}
