package com.aiden.einklabel.admin.org;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aiden.einklabel.admin.ap.AccessPointRecord;
import com.aiden.einklabel.admin.product.ProductRecord;
import com.aiden.einklabel.admin.store.StoreRecord;
import com.aiden.einklabel.admin.template.TemplateRecord;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import xyz.erupt.upms.model.EruptOrg;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

class OrganizationScopedDataProxyTest {

    private final EruptUserService eruptUserService = mock(EruptUserService.class);

    private final EntityManager entityManager = mock(EntityManager.class);

    private OrganizationScopedDataProxy proxy;

    @BeforeEach
    void setUp() {
        proxy = new OrganizationScopedDataProxy();
        ReflectionTestUtils.setField(proxy, "eruptUserService", eruptUserService);
        ReflectionTestUtils.setField(proxy, "entityManager", entityManager);
    }

    @Test
    void loadsSubmittedStoreReferenceBeforeCheckingAccessPointOrganization() {
        EruptOrg currentOrganization = organization(10L);
        when(eruptUserService.getCurrentEruptUser()).thenReturn(user(currentOrganization, false));

        StoreRecord submittedStore = storeReference(7L);
        StoreRecord managedStore = store(7L, currentOrganization);
        when(entityManager.find(StoreRecord.class, 7L)).thenReturn(managedStore);

        AccessPointRecord accessPoint = new AccessPointRecord();
        accessPoint.setStore(submittedStore);

        proxy.beforeUpdate(accessPoint);

        assertThat(accessPoint.getOrganization()).isSameAs(currentOrganization);
        assertThat(accessPoint.getStore()).isSameAs(managedStore);
        verify(entityManager).find(StoreRecord.class, 7L);
    }

    @Test
    void rejectsSubmittedStoreReferenceFromAnotherOrganizationAfterLoadingIt() {
        EruptOrg currentOrganization = organization(10L);
        EruptOrg anotherOrganization = organization(20L);
        when(eruptUserService.getCurrentEruptUser()).thenReturn(user(currentOrganization, false));

        AccessPointRecord accessPoint = new AccessPointRecord();
        accessPoint.setStore(storeReference(7L));
        when(entityManager.find(StoreRecord.class, 7L)).thenReturn(store(7L, anotherOrganization));

        assertThatThrownBy(() -> proxy.beforeUpdate(accessPoint))
                .hasMessage("店铺必须属于当前组织");
    }

    @Test
    void superAdminCanDeriveAccessPointOrganizationFromSubmittedStoreReference() {
        EruptOrg storeOrganization = organization(10L);
        when(eruptUserService.getCurrentEruptUser()).thenReturn(user(null, true));

        StoreRecord managedStore = store(7L, storeOrganization);
        AccessPointRecord accessPoint = new AccessPointRecord();
        accessPoint.setStore(storeReference(7L));
        when(entityManager.find(StoreRecord.class, 7L)).thenReturn(managedStore);

        proxy.beforeAdd(accessPoint);

        assertThat(accessPoint.getOrganization()).isSameAs(storeOrganization);
        assertThat(accessPoint.getStore()).isSameAs(managedStore);
    }

    @Test
    void loadsProductStoreAndTemplateReferencesBeforeRelationshipValidation() {
        EruptOrg currentOrganization = organization(10L);
        when(eruptUserService.getCurrentEruptUser()).thenReturn(user(currentOrganization, false));

        StoreRecord managedStore = store(7L, currentOrganization);
        TemplateRecord managedTemplate = template(11L, currentOrganization);
        when(entityManager.find(StoreRecord.class, 7L)).thenReturn(managedStore);
        when(entityManager.find(TemplateRecord.class, 11L)).thenReturn(managedTemplate);

        ProductRecord product = new ProductRecord();
        product.setStore(storeReference(7L));
        TemplateRecord submittedTemplate = new TemplateRecord();
        submittedTemplate.setId(11L);
        product.setTemplate(submittedTemplate);

        proxy.beforeUpdate(product);

        assertThat(product.getStore()).isSameAs(managedStore);
        assertThat(product.getTemplate()).isSameAs(managedTemplate);
    }

    private EruptUser user(EruptOrg organization, boolean admin) {
        EruptUser user = new EruptUser();
        user.setName("test-user");
        user.setIsAdmin(admin);
        user.setEruptOrg(organization);
        return user;
    }

    private EruptOrg organization(long id) {
        EruptOrg organization = new EruptOrg();
        organization.setId(id);
        organization.setName("组织" + id);
        return organization;
    }

    private StoreRecord storeReference(long id) {
        StoreRecord store = new StoreRecord();
        store.setId(id);
        return store;
    }

    private StoreRecord store(long id, EruptOrg organization) {
        StoreRecord store = storeReference(id);
        store.setOrganization(organization);
        store.setName("演示门店");
        return store;
    }

    private TemplateRecord template(long id, EruptOrg organization) {
        TemplateRecord template = new TemplateRecord();
        template.setId(id);
        template.setOrganization(organization);
        return template;
    }
}
