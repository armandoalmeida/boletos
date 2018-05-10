package bankslips.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class BankSlipNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -2850275771360999816L;

	public BankSlipNotFoundException() {
		super("Bankslip not found with the specified id");
	}
}
