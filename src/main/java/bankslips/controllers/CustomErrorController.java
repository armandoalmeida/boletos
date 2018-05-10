package bankslips.controllers;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * Redefinicao das respostas de erro no formato JSON
 * 
 * @author Armando Almeida <jose@armandoalmeida.com.br>
 *
 */
@RestController
public class CustomErrorController implements ErrorController {

	private static final String PATH = "/error";

	@Autowired
	private ErrorAttributes errorAttributes;

	@RequestMapping(value = PATH)
	ErrorJson error(HttpServletRequest request, HttpServletResponse response) {
		return new ErrorJson(response.getStatus(), getErrorAttributes(request, false));
	}

	@Override
	public String getErrorPath() {
		return PATH;
	}

	private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
		ServletWebRequest servletWebRequest = new ServletWebRequest(request);
		return errorAttributes.getErrorAttributes(servletWebRequest, includeStackTrace);
	}

	public class ErrorJson {

		public Integer status;
		public String message;

		public ErrorJson(int status, Map<String, Object> errorAttributes) {
			this.status = status;
			this.message = (String) errorAttributes.get("message");
		}

	}

}
