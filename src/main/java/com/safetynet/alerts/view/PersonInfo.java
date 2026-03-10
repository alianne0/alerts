package com.safetynet.alerts.view;

import com.safetynet.alerts.dto.PersonInfoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * View to hold the information for the person's information
 * Accesses the DTO
 */
public class PersonInfo {
    @Getter
    @Setter
    private List<PersonInfoDTO> personInfoDTOList;


    /**
     * Constructor to instantiate the person info
     * @param personInfoDTOList
     */
    public PersonInfo(List<PersonInfoDTO> personInfoDTOList){
        this.personInfoDTOList = personInfoDTOList;
    }

}
