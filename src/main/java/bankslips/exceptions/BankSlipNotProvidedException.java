package bankslips.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BankSlipNotProvidedException extends RuntimeException {

	private static final long serialVersionUID = 1482878356647730014L;

	public BankSlipNotProvidedException() {
		super("Bankslip not provided in the request body");
	}
}
