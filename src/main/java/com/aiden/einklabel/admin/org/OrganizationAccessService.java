package com.aiden.einklabel.admin.org;

import org.springframework.stereotype.Service;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.upms.model.EruptOrg;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

@Service
public class OrganizationAccessService {

    private final EruptUserService eruptUserService;

    public OrganizationAccessService(EruptUserService eruptUserService) {
        this.eruptUserService = eruptUserService;
    }

    public EruptUser currentUser() {
        EruptUser user = eruptUserService.getCurrentEruptUser();
        if (user == null) {
            throw new EruptWebApiRuntimeException("当前请求未登录");
        }
        return user;
    }

    public boolean isSuperAdmin(EruptUser user) {
        return Boolean.TRUE.equals(user.getIsAdmin());
    }

    public EruptOrg currentOrganizationRequired(EruptUser user) {
        EruptOrg organization = user.getEruptOrg();
        if (organization == null || organization.getId() == null) {
            throw new EruptWebApiRuntimeException(user.getName() + " 未绑定组织，无法访问业务数据");
        }
        return organization;
    }

    public void assertCanAccess(OrganizationScoped scoped) {
        if (scoped == null) {
            throw new EruptWebApiRuntimeException("数据不存在");
        }
        EruptUser user = currentUser();
        if (isSuperAdmin(user)) {
            return;
        }
        EruptOrg currentOrganization = currentOrganizationRequired(user);
        EruptOrg recordOrganization = scoped.getOrganization();
        if (!sameId(currentOrganization, recordOrganization)) {
            throw new EruptWebApiRuntimeException("无权访问其他组织的数据");
        }
    }

    private boolean sameId(EruptOrg left, EruptOrg right) {
        return left != null && right != null && left.getId() != null && left.getId().equals(right.getId());
    }
}
