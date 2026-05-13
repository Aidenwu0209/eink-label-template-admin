package com.aiden.einklabel.admin.org;

import com.aiden.einklabel.admin.ap.AccessPointRecord;
import com.aiden.einklabel.admin.esl.EslTagRecord;
import com.aiden.einklabel.admin.product.ProductRecord;
import com.aiden.einklabel.admin.store.StoreRecord;
import com.aiden.einklabel.admin.template.TemplateRecord;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.stereotype.Component;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProxyContext;
import xyz.erupt.upms.model.EruptOrg;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

@Component
public class OrganizationScopedDataProxy implements DataProxy<OrganizationScoped> {

    @Resource
    private EruptUserService eruptUserService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public String beforeFetch(List<Condition> conditions) {
        EruptUser user = currentUser();
        if (isSuperAdmin(user)) {
            return null;
        }
        EruptOrg organization = currentOrganizationRequired(user);
        return DataProxyContext.currentClass().getSimpleName() + ".organization.id = " + organization.getId();
    }

    @Override
    public void beforeAdd(OrganizationScoped record) {
        applyOrganization(record);
        validateRelationships(record);
    }

    @Override
    public void beforeUpdate(OrganizationScoped record) {
        applyOrganization(record);
        validateRelationships(record);
    }

    private void applyOrganization(OrganizationScoped record) {
        EruptUser user = currentUser();
        if (isSuperAdmin(user)) {
            if (record.getOrganization() == null) {
                record.setOrganization(deriveOrganization(record));
            }
            if (record.getOrganization() == null) {
                throw new EruptWebApiRuntimeException("请选择数据所属组织");
            }
            return;
        }
        record.setOrganization(currentOrganizationRequired(user));
    }

    private void validateRelationships(OrganizationScoped record) {
        EruptOrg organization = record.getOrganization();
        if (organization == null || organization.getId() == null) {
            throw new EruptWebApiRuntimeException("业务数据必须绑定组织");
        }
        if (record instanceof ProductRecord product) {
            StoreRecord store = loadStore(product.getStore());
            TemplateRecord template = loadTemplate(product.getTemplate());
            product.setStore(store);
            product.setTemplate(template);
            requireStoreInOrganization(store, organization);
            requireTemplateInOrganization(template, organization);
        } else if (record instanceof AccessPointRecord accessPoint) {
            StoreRecord store = loadStore(accessPoint.getStore());
            accessPoint.setStore(store);
            requireStoreInOrganization(store, organization);
        } else if (record instanceof EslTagRecord tag) {
            StoreRecord store = loadStore(tag.getStore());
            AccessPointRecord accessPoint = loadAccessPoint(tag.getAccessPoint());
            ProductRecord product = loadProduct(tag.getProduct());
            tag.setStore(store);
            tag.setAccessPoint(accessPoint);
            tag.setProduct(product);
            requireStoreInOrganization(store, organization);
            requireAccessPointInStore(accessPoint, store, organization);
            requireProductInStore(product, store, organization);
        }
    }

    private EruptOrg deriveOrganization(OrganizationScoped record) {
        if (record instanceof ProductRecord product && product.getStore() != null) {
            return loadStore(product.getStore()).getOrganization();
        }
        if (record instanceof AccessPointRecord accessPoint && accessPoint.getStore() != null) {
            return loadStore(accessPoint.getStore()).getOrganization();
        }
        if (record instanceof EslTagRecord tag) {
            if (tag.getStore() != null) {
                return loadStore(tag.getStore()).getOrganization();
            }
            if (tag.getProduct() != null) {
                return loadProduct(tag.getProduct()).getOrganization();
            }
            if (tag.getAccessPoint() != null) {
                return loadAccessPoint(tag.getAccessPoint()).getOrganization();
            }
        }
        return null;
    }

    private void requireStoreInOrganization(StoreRecord store, EruptOrg organization) {
        if (store == null) {
            throw new EruptWebApiRuntimeException("请选择店铺");
        }
        if (!sameOrganization(store.getOrganization(), organization)) {
            throw new EruptWebApiRuntimeException("店铺必须属于当前组织");
        }
    }

    private void requireTemplateInOrganization(TemplateRecord template, EruptOrg organization) {
        if (template == null) {
            throw new EruptWebApiRuntimeException("商品必须绑定已有模板");
        }
        if (template.getOrganization() != null && !sameOrganization(template.getOrganization(), organization)) {
            throw new EruptWebApiRuntimeException("商品模板必须属于同一组织");
        }
    }

    private void requireAccessPointInStore(AccessPointRecord accessPoint, StoreRecord store, EruptOrg organization) {
        if (accessPoint == null) {
            return;
        }
        if (!sameOrganization(accessPoint.getOrganization(), organization)) {
            throw new EruptWebApiRuntimeException("AP 必须属于同一组织");
        }
        if (!sameRecord(accessPoint.getStore(), store)) {
            throw new EruptWebApiRuntimeException("AP 必须属于价签所在店铺");
        }
    }

    private void requireProductInStore(ProductRecord product, StoreRecord store, EruptOrg organization) {
        if (product == null) {
            return;
        }
        if (!sameOrganization(product.getOrganization(), organization)) {
            throw new EruptWebApiRuntimeException("商品必须属于同一组织");
        }
        if (!sameRecord(product.getStore(), store)) {
            throw new EruptWebApiRuntimeException("电子价签只能绑定同店铺商品");
        }
    }

    private EruptUser currentUser() {
        EruptUser user = eruptUserService.getCurrentEruptUser();
        if (user == null) {
            throw new EruptWebApiRuntimeException("当前请求未登录");
        }
        return user;
    }

    private EruptOrg currentOrganizationRequired(EruptUser user) {
        EruptOrg organization = user.getEruptOrg();
        if (organization == null || organization.getId() == null) {
            throw new EruptWebApiRuntimeException(user.getName() + " 未绑定组织，无法访问业务数据");
        }
        return organization;
    }

    private boolean isSuperAdmin(EruptUser user) {
        return Boolean.TRUE.equals(user.getIsAdmin());
    }

    private boolean sameOrganization(EruptOrg left, EruptOrg right) {
        return left != null && right != null && left.getId() != null && left.getId().equals(right.getId());
    }

    private boolean sameRecord(OrganizationScopedRecord left, OrganizationScopedRecord right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.getId() != null && right.getId() != null) {
            return left.getId().equals(right.getId());
        }
        return left == right;
    }

    private StoreRecord loadStore(StoreRecord store) {
        if (store == null || store.getId() == null) {
            return store;
        }
        StoreRecord managed = entityManager.find(StoreRecord.class, store.getId());
        if (managed == null) {
            throw new EruptWebApiRuntimeException("店铺不存在");
        }
        return managed;
    }

    private TemplateRecord loadTemplate(TemplateRecord template) {
        if (template == null || template.getId() == null) {
            return template;
        }
        TemplateRecord managed = entityManager.find(TemplateRecord.class, template.getId());
        if (managed == null) {
            throw new EruptWebApiRuntimeException("商品模板不存在");
        }
        return managed;
    }

    private AccessPointRecord loadAccessPoint(AccessPointRecord accessPoint) {
        if (accessPoint == null || accessPoint.getId() == null) {
            return accessPoint;
        }
        AccessPointRecord managed = entityManager.find(AccessPointRecord.class, accessPoint.getId());
        if (managed == null) {
            throw new EruptWebApiRuntimeException("AP 不存在");
        }
        return managed;
    }

    private ProductRecord loadProduct(ProductRecord product) {
        if (product == null || product.getId() == null) {
            return product;
        }
        ProductRecord managed = entityManager.find(ProductRecord.class, product.getId());
        if (managed == null) {
            throw new EruptWebApiRuntimeException("商品不存在");
        }
        return managed;
    }
}
