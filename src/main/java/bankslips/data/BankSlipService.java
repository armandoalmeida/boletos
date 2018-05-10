package bankslips.data;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bankslips.data.dto.BankSlipDTO;

@Service
public class BankSlipService {

    @Autowired
    private BankSlipRepository bankSlipRepository;
    
    public BankSlip save(BankSlip bankSlip) {
        return bankSlipRepository.save(bankSlip);
    }
    
    public List<BankSlipDTO> findAllDtos() {
    	return bankSlipRepository.findAllDtos();
    }
    
    public BankSlipDTO findById(UUID id) {
    	return new BankSlipDTO(bankSlipRepository.findById(id).get());
    }
    
}
