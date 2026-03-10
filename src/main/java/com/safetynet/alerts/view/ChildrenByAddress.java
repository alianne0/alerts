package com.safetynet.alerts.view;
import com.safetynet.alerts.dto.ChildrenByAddressDTO;
import com.safetynet.alerts.dto.HouseholdMembersDTO;
import lombok.Data;

import java.util.List;

/**
 * Class to store children by address
 */
@Data
public class ChildrenByAddress {
    private List<ChildrenByAddressDTO> children;

    /**
     * Constructor for children by address
     * @param children
     */
    public ChildrenByAddress(List<ChildrenByAddressDTO> children) {
        this.children = children;
    }

}