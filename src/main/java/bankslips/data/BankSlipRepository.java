package bankslips.data;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import bankslips.data.dto.BankSlipDTO;

public interface BankSlipRepository extends CrudRepository<BankSlip , UUID>{
	
	/**
	 * Procura todos os boletos e converte em um objeto DTO
	 * @return
	 */
	@Query("SELECT new bankslips.data.dto.BankSlipDTO(e) FROM BankSlip e ")
	public List<BankSlipDTO> findAllDtos();
	
}
