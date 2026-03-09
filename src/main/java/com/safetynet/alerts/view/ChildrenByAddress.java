package com.safetynet.alerts.view;
import com.safetynet.alerts.dto.ChildrenByAddressDTO;
import com.safetynet.alerts.dto.HouseholdMembersDTO;

import java.util.List;

/**
 * Class to store children by address
 */
public class ChildrenByAddress {

    private List<ChildrenByAddressDTO> children;

    /**
     * Constructor for children by address
     * @param children
     */
    public ChildrenByAddress(List<ChildrenByAddressDTO> children) {
        this.children = children;
    }

    /**
     * Returns the children by address
     * @return
     */
    public List<ChildrenByAddressDTO> getChildren() {
        return children;
    }
}