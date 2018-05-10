package bankslips.data.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import bankslips.data.BankSlip;
import bankslips.enumerators.BankSlipStatusEnum;

public class BankSlipDTO implements Serializable {

	private static final long serialVersionUID = 5036471260579734949L;

	public static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private static final int LIMIT_TO_INCREASE_FINE = 10;

	private UUID id;
	private String dueDate;
	private Integer totalInCents;
	private String customer;
	private BankSlipStatusEnum status;
	private Integer fine;

	public BankSlipDTO() {

	}

	/**
	 * Construtor com informacoes basicas
	 * 
	 * @param dueDate
	 * @param totalInCents
	 * @param customer
	 * @param status
	 */
	public BankSlipDTO(String dueDate, Integer totalInCents, String customer, BankSlipStatusEnum status) {
		this.dueDate = dueDate;
		this.totalInCents = totalInCents;
		this.customer = customer;
		this.status = status;
	}

	/**
	 * Constructor baseado na Entity
	 * 
	 * @param bankSlip
	 */
	public BankSlipDTO(BankSlip bankSlip) {
		if (bankSlip != null) {
			this.id = bankSlip.getId();
			this.dueDate = DEFAULT_DATE_FORMAT.format(bankSlip.getDueDate());
			this.totalInCents = bankSlip.getTotalInCents();
			this.customer = bankSlip.getCustomer();
			this.status = BankSlipStatusEnum.valueOf(bankSlip.getStatus());
		}
	}

	/**
	 * Cria a Entity para salvamento
	 * 
	 * @return
	 * @throws ParseException
	 */
	@JsonIgnore
	public BankSlip getEntityToSave() throws ParseException {
		BankSlip bankSlip = new BankSlip();
		if (id != null)
			bankSlip.setId(id);

		bankSlip.setDueDate(DEFAULT_DATE_FORMAT.parse(dueDate));
		bankSlip.setTotalInCents(totalInCents);
		bankSlip.setCustomer(customer);
		bankSlip.setStatus(getStatus().name());

		return bankSlip;
	}

	/**
	 * Regra para o cálculo da multa aplicada por dia para os boletos atrasados:
	 * <br>
	 * - Até 10 dias: Multa de 0,5% (Juros Simples) <br>
	 * - Acima de 10 dias: Multa de 1% (Juros Simples)
	 */
	public void verifyOverDueAndUpdateFine() {
		// verifica apenas boletos que nao estao pendentes
		if (status.equals(BankSlipStatusEnum.PENDING)) {
			try {
				Date dueDate = DEFAULT_DATE_FORMAT.parse(this.dueDate);
				Date today = DEFAULT_DATE_FORMAT.parse(DEFAULT_DATE_FORMAT.format(new Date()));

				long diff = today.getTime() - dueDate.getTime();
				long overDueDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

				if (overDueDays > 0) {
					BigDecimal tax = new BigDecimal(0.005);
					if (overDueDays > LIMIT_TO_INCREASE_FINE) {
						tax = new BigDecimal(0.01);
					}

					this.fine = new BigDecimal(this.totalInCents).multiply(tax).toBigInteger().intValue();
				}

			} catch (ParseException e) {
				this.fine = null;
			}
		}
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	@JsonProperty("due_date")
	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	@JsonProperty("total_in_cents")
	public Integer getTotalInCents() {
		return totalInCents;
	}

	public void setTotalInCents(Integer totalInCents) {
		this.totalInCents = totalInCents;
	}

	@JsonProperty("customer")
	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	@JsonProperty("status")
	public BankSlipStatusEnum getStatus() {
		if (status == null)
			status = BankSlipStatusEnum.PENDING;
		return status;
	}

	public void setStatus(BankSlipStatusEnum status) {
		this.status = status;
	}

	@JsonProperty("fine")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Integer getFine() {
		return fine;
	}

	public void setFine(Integer fine) {
		this.fine = fine;
	}

}
