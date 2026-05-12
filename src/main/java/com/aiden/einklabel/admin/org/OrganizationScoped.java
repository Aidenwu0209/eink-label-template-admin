package com.aiden.einklabel.admin.org;

import xyz.erupt.upms.model.EruptOrg;

public interface OrganizationScoped {

    EruptOrg getOrganization();

    void setOrganization(EruptOrg organization);
}
