package bankslip;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import bankslips.Application;
import bankslips.data.BankSlip;
import bankslips.data.BankSlipRepository;
import bankslips.data.dto.BankSlipDTO;
import bankslips.enumerators.BankSlipStatusEnum;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class BankSlipControllerTest {

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

	private static final String REQUEST_MAPPING = "/rest/bankslips/";

	private MockMvc mockMvc;

	@SuppressWarnings("rawtypes")
	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	private BankSlipDTO bankSlipSetup;
	private BankSlipDTO bankSlipOverDue;
	private BankSlipDTO bankSlipOverDueGt10;

	private List<BankSlipDTO> bankSlipList = new ArrayList<>();

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private BankSlipRepository bankSlipRepository;

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

		assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}

	@Before
	public void setUp() throws Exception {

		this.mockMvc = webAppContextSetup(webApplicationContext).build();

		this.bankSlipRepository.deleteAll();

		bankSlipSetup = new BankSlipDTO("2018-05-10", 100000, "Conta Azul", BankSlipStatusEnum.PENDING);
		bankSlipOverDue = new BankSlipDTO(getDateBefore(5), 100000, "Overdue 5 days", BankSlipStatusEnum.PENDING);
		bankSlipOverDueGt10 = new BankSlipDTO(getDateBefore(15), 100000, "Overdue 15 days", BankSlipStatusEnum.PENDING);

		try {

			BankSlip bankSlip = bankSlipRepository.save(bankSlipSetup.getEntityToSave());
			BankSlip bankSlip2 = bankSlipRepository.save(bankSlipOverDue.getEntityToSave());
			BankSlip bankSlip3 = bankSlipRepository.save(bankSlipOverDueGt10.getEntityToSave());

			bankSlipSetup = new BankSlipDTO(bankSlip);
			bankSlipOverDue = new BankSlipDTO(bankSlip2);
			bankSlipOverDueGt10 = new BankSlipDTO(bankSlip3);

			bankSlipList.add(bankSlipSetup);
			bankSlipList.add(bankSlipOverDue);
			bankSlipList.add(bankSlipOverDueGt10);

		} catch (ParseException e) {
			fail("setup problem");
		}
	}

	/**
	 * Retorna uma data anterior
	 * 
	 * @param days
	 * @return
	 */
	private String getDateBefore(int days) {
		Date d = new Date();// intialize your date to any date
		Date dateBefore = new Date(d.getTime() - (days * 24 * 3600 * 1000)); // Subtract n days
		return BankSlipDTO.DEFAULT_DATE_FORMAT.format(dateBefore);
	}

	@Test
	public void findAllBankSlips() throws Exception {
		mockMvc.perform(get(REQUEST_MAPPING)) //
				.andExpect(status().isOk()) //
				.andExpect(content().contentType(contentType)) //
				.andExpect(jsonPath("$", hasSize(bankSlipList.size())));
	}

	@Test
	public void getBankSlipById() throws Exception {

		// get by id
		mockMvc.perform(get(REQUEST_MAPPING + bankSlipSetup.getId())) //
				.andExpect(status().isOk()) //
				.andExpect(content().contentType(contentType));

		// id not found
		mockMvc.perform(get(REQUEST_MAPPING + new UUID(10, 10))) //
				.andExpect(status().is(HttpStatus.NOT_FOUND.value()));

		// not uuid
		mockMvc.perform(get(REQUEST_MAPPING + "its_not_an_uuid")) //
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

		// get overdue 5 days (lt 10 days)
		mockMvc.perform(get(REQUEST_MAPPING + bankSlipOverDue.getId())) //
				.andExpect(status().isOk()) //
				.andExpect(content().contentType(contentType))
				.andExpect(jsonPath("fine", is((int) (bankSlipOverDue.getTotalInCents() * 0.005))));

		// get overdue 15 days (gt 10 days)
		mockMvc.perform(get(REQUEST_MAPPING + bankSlipOverDueGt10.getId())) //
				.andExpect(status().isOk()) //
				.andExpect(content().contentType(contentType))
				.andExpect(jsonPath("fine", is((int) (bankSlipOverDueGt10.getTotalInCents() * 0.01))));

	}

	@Test
	public void payBankSlip() throws Exception {

		// create a bankslip
		this.mockMvc.perform(put(REQUEST_MAPPING + bankSlipSetup.getId().toString() + "/pay") //
				.contentType(contentType)) //
				.andExpect(status().isOk());

		// get by id
		mockMvc.perform(get(REQUEST_MAPPING + bankSlipSetup.getId().toString())) //
				.andExpect(status().isOk()) //
				.andExpect(content().contentType(contentType)) //
				.andExpect(jsonPath("id", is(bankSlipSetup.getId().toString())))
				.andExpect(jsonPath("status", is(BankSlipStatusEnum.PAID.name())));

		// not uuid
		mockMvc.perform(put(REQUEST_MAPPING + "its_not_an_uuid/pay")) //
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}

	@Test
	public void cancelBankSlip() throws Exception {

		// create a bankslip
		this.mockMvc.perform(delete(REQUEST_MAPPING + bankSlipSetup.getId().toString() + "/cancel") //
				.contentType(contentType)) //
				.andExpect(status().isOk());

		// get by id
		mockMvc.perform(get(REQUEST_MAPPING + bankSlipSetup.getId().toString())) //
				.andExpect(status().isOk()) //
				.andExpect(content().contentType(contentType)) //
				.andExpect(jsonPath("id", is(bankSlipSetup.getId().toString())))
				.andExpect(jsonPath("status", is(BankSlipStatusEnum.CANCELED.name())));

		// not uuid
		mockMvc.perform(delete(REQUEST_MAPPING + "its_not_an_uuid/cancel")) //
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
	}

	@Test
	public void createBankSlip() throws Exception {

		BankSlipDTO bankSlipToCreate = new BankSlipDTO("2018-05-10", 100000, "Customer 1", BankSlipStatusEnum.PENDING);
		BankSlipDTO bankSlipToCreateWithError1 = new BankSlipDTO("XXXXX", 1000, "Error", BankSlipStatusEnum.PENDING);
		BankSlipDTO bankSlipToCreateWithError2 = new BankSlipDTO("2018-05-10", 0, "Error", BankSlipStatusEnum.PENDING);

		// create a bankslip
		this.mockMvc.perform(post(REQUEST_MAPPING) //
				.contentType(contentType) //
				.content(json(bankSlipToCreate))) //
				.andExpect(status().isCreated());

		// bankslip without request body
		this.mockMvc.perform(post(REQUEST_MAPPING) //
				.contentType(contentType) //
				.content(json(null))) //
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

		// bankslip with a invalid request param
		this.mockMvc.perform(post(REQUEST_MAPPING) //
				.contentType(contentType) //
				.content(json(bankSlipToCreateWithError1))) //
				.andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()));

		// bankslip with a invalid request param
		this.mockMvc.perform(post(REQUEST_MAPPING) //
				.contentType(contentType) //
				.content(json(bankSlipToCreateWithError2))) //
				.andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()));

	}

	@SuppressWarnings("unchecked")
	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}

}
