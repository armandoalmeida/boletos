package bankslips.controllers;

import java.text.ParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import bankslips.data.BankSlip;
import bankslips.data.BankSlipService;
import bankslips.data.dto.BankSlipDTO;
import bankslips.enumerators.BankSlipStatusEnum;
import bankslips.exceptions.BankSlipInvalidException;
import bankslips.exceptions.BankSlipInvalidUUIDException;
import bankslips.exceptions.BankSlipNotFoundException;
import bankslips.exceptions.BankSlipNotProvidedException;

@RestController
@RequestMapping("/rest/bankslips")
public class BankSlipController {

	@Autowired
	private BankSlipService bankSlipService;

	/**
	 * Criar boleto
	 * 
	 * Esse método deve receber um novo boleto e inseri-lo em um banco de dados para
	 * ser consumido pela própria API. Todos os campos são obrigatórios.
	 * 
	 * @param dueDate
	 * @param totalInCents
	 * @param customer
	 * @param status
	 * @return Boleto gravado na base
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(code = HttpStatus.CREATED)
	public BankSlipDTO createBankSlip(@RequestBody BankSlipDTO bankSlipDTO) {

		try {
			// validacao dos campos obrigatorios
			if (StringUtils.isEmpty(bankSlipDTO.getDueDate()))
				throw new BankSlipNotProvidedException();

			if (StringUtils.isEmpty(bankSlipDTO.getTotalInCents()))
				throw new BankSlipNotProvidedException();

			if (bankSlipDTO.getTotalInCents() <= 0)
				throw new BankSlipInvalidException();

			if (StringUtils.isEmpty(bankSlipDTO.getCustomer()))
				throw new BankSlipNotProvidedException();

			// salva a entidade no banco de dados
			BankSlip bankSlip = bankSlipService.save(bankSlipDTO.getEntityToSave());

			return new BankSlipDTO(bankSlip);

		} catch (ParseException e) {
			// erro na conversao da data
			throw new BankSlipInvalidException();
		}

	}

	/**
	 * Lista de boletos
	 * 
	 * Esse método da API deve retornar uma lista de boletos em formato JSON
	 * 
	 * @return Lista com todos os boletos
	 */
	@RequestMapping(method = RequestMethod.GET)
	public List<BankSlipDTO> findAllBankSlips() {
		return bankSlipService.findAllDtos();
	}

	/**
	 * Ver detalhes de um boleto
	 * 
	 * Esse método da API deve retornar um boleto filtrado pelo id, caso o boleto
	 * estiver atrasado deve ser calculado o valor da multa. <br>
	 * Regra para o cálculo da multa aplicada por dia para os boletos atrasados:
	 * <br>
	 * - Até 10 dias: Multa de 0,5% (Juros Simples) <br>
	 * - Acima de 10 dias: Multa de 1% (Juros Simples)
	 * 
	 * @param id
	 * @return Boleto de acordo com id
	 */
	@RequestMapping(path = "/{id}", method = RequestMethod.GET)
	public BankSlipDTO getBankSlipById(@PathVariable(value = "id") String id) {
		try {
			BankSlipDTO bankSlipDTO = bankSlipService.findById(UUID.fromString(id));

			// verifica se ha atraso no boleto
			bankSlipDTO.verifyOverDueAndUpdateFine();

			return bankSlipDTO;
		} catch (IllegalArgumentException e) {
			throw new BankSlipInvalidUUIDException();
		} catch (NoSuchElementException e) {
			throw new BankSlipNotFoundException();
		}
	}

	/**
	 * Pagar um boleto
	 * 
	 * Esse método da API deve alterar o status do boleto para PAID de acordo com o
	 * id.
	 * 
	 * @param id
	 * @return Boleto atualizado
	 */
	@RequestMapping(path = "/{id}/pay", method = RequestMethod.PUT)
	public BankSlipDTO payBankSlip(@PathVariable(value = "id") String id) {
		return updateBankSlipStatus(id, BankSlipStatusEnum.PAID);
	}

	/**
	 * Cancelar um boleto
	 * 
	 * Esse método da API deve alterar o status do boleto para CANCELED de acordo
	 * com o id.
	 * 
	 * @param id
	 * @return Boleto atualizado
	 */
	@RequestMapping(path = "/{id}/cancel", method = RequestMethod.DELETE)
	public BankSlipDTO cancelBankSlip(@PathVariable(value = "id") String id) {
		return updateBankSlipStatus(id, BankSlipStatusEnum.CANCELED);
	}

	/**
	 * Atualiza o status do boleto de acordo com id
	 * 
	 * @param id
	 * @param status
	 * @return Boleto atualizado
	 */
	private BankSlipDTO updateBankSlipStatus(String id, BankSlipStatusEnum status) {
		// recupera o boleto da base
		BankSlipDTO bankSlipDTO = getBankSlipById(id);

		// muda o status do boleto
		bankSlipDTO.setStatus(status);

		// salva a entidade no banco de dados
		try {
			bankSlipService.save(bankSlipDTO.getEntityToSave());
		} catch (ParseException e) {
			// erro na conversao da data
			// (nesse caso nao ocorrera a menos que haja inconsistencia nos dados)
			throw new BankSlipInvalidException();
		}

		return bankSlipDTO;
	}

}
