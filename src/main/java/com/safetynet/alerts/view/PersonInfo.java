package com.safetynet.alerts.view;

import com.safetynet.alerts.dto.PersonInfoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class PersonInfo {
    @Getter
    @Setter
    private List<PersonInfoDTO> personInfoDTOList;

    public PersonInfo(List<PersonInfoDTO> personInfoDTOList){
        this.personInfoDTOList = personInfoDTOList;
    }

}
