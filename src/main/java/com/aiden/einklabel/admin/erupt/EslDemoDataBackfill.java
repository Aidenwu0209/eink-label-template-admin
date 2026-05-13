package com.aiden.einklabel.admin.erupt;

import com.aiden.einklabel.admin.ap.AccessPointRecord;
import com.aiden.einklabel.admin.ap.AccessPointRecordRepository;
import com.aiden.einklabel.admin.config.AdminDemoDataProperties;
import com.aiden.einklabel.admin.esl.EslTagRecord;
import com.aiden.einklabel.admin.esl.EslTagRecordRepository;
import com.aiden.einklabel.admin.product.ProductRecord;
import com.aiden.einklabel.admin.product.ProductRecordRepository;
import com.aiden.einklabel.admin.store.StoreRecord;
import com.aiden.einklabel.admin.store.StoreRecordRepository;
import com.aiden.einklabel.admin.template.ColorMode;
import com.aiden.einklabel.admin.template.TemplateRecord;
import com.aiden.einklabel.admin.template.TemplateRecordRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.upms.model.EruptOrg;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class EslDemoDataBackfill implements ApplicationRunner {

    private static final String STORE_CODE = "ZH01";
    private static final String AP_SN = "ESLAP00000008";
    private static final String PRODUCT_CODE = "6902538004045";
    private static final String TEMPLATE_CODE = "PRICEPROMO";
    private static final long TAG_ID = 6597069770841L;

    private final AdminDemoDataProperties properties;
    private final EntityManager entityManager;
    private final StoreRecordRepository storeRepository;
    private final TemplateRecordRepository templateRepository;
    private final ProductRecordRepository productRepository;
    private final AccessPointRecordRepository accessPointRepository;
    private final EslTagRecordRepository tagRepository;

    public EslDemoDataBackfill(
            AdminDemoDataProperties properties,
            EntityManager entityManager,
            StoreRecordRepository storeRepository,
            TemplateRecordRepository templateRepository,
            ProductRecordRepository productRepository,
            AccessPointRecordRepository accessPointRepository,
            EslTagRecordRepository tagRepository
    ) {
        this.properties = properties;
        this.entityManager = entityManager;
        this.storeRepository = storeRepository;
        this.templateRepository = templateRepository;
        this.productRepository = productRepository;
        this.accessPointRepository = accessPointRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }
        EruptOrg organization = organization();
        if (findTag(organization).isPresent()) {
            return;
        }

        StoreRecord store = store(organization);
        TemplateRecord template = template(organization);
        ProductRecord product = product(organization, store, template);
        AccessPointRecord accessPoint = accessPoint(organization, store);
        EslTagRecord tag = new EslTagRecord();
        tag.setOrganization(organization);
        tag.setStore(store);
        tag.setAccessPoint(accessPoint);
        tag.setProduct(product);
        tag.setTagId(TAG_ID);
        tag.setModel(6);
        tag.setForceRefresh(true);
        tag.setProtocolStatus(4);
        tag.setBatterySoc(92);
        tag.setRssi(-42);
        tag.setLastTaskId(39137);
        tag.setToken(161986);
        tagRepository.save(tag);
    }

    private EruptOrg organization() {
        List<EruptOrg> organizations = entityManager
                .createQuery("select o from EruptOrg o order by o.id", EruptOrg.class)
                .setMaxResults(1)
                .getResultList();
        if (!organizations.isEmpty()) {
            return organizations.get(0);
        }
        EruptOrg organization = new EruptOrg();
        organization.setCode("DEFAULT");
        organization.setName("默认组织");
        organization.setSort(1);
        entityManager.persist(organization);
        entityManager.flush();
        return organization;
    }

    private StoreRecord store(EruptOrg organization) {
        return findStore(organization).orElseGet(() -> {
            StoreRecord store = new StoreRecord();
            store.setOrganization(organization);
            store.setCode(STORE_CODE);
            store.setName("演示门店");
            store.setShopId(1);
            store.setAddress("本地联调演示数据");
            return storeRepository.save(store);
        });
    }

    private TemplateRecord template(EruptOrg organization) {
        return findTemplate(organization).orElseGet(() -> {
            TemplateRecord template = new TemplateRecord();
            template.setOrganization(organization);
            template.setName("门店黑白红模板");
            template.setDeviceTemplateCode(TEMPLATE_CODE);
            template.setColorMode(ColorMode.BWR.name());
            template.setWidth(800);
            template.setHeight(480);
            template.setFullJson("{\"version\":\"5.0\",\"objects\":[]}");
            return templateRepository.save(template);
        });
    }

    private ProductRecord product(EruptOrg organization, StoreRecord store, TemplateRecord template) {
        return findProduct(organization, store).orElseGet(() -> {
            ProductRecord product = new ProductRecord();
            product.setOrganization(organization);
            product.setStore(store);
            product.setTemplate(template);
            product.setCode(PRODUCT_CODE);
            product.setName("脉动维生素饮料");
            product.setFullName("脉动 维生素饮料青柠口味 600ML");
            product.setBrand("脉动");
            product.setSpec("600ML");
            product.setPrice(new BigDecimal("10.80"));
            product.setPromoPrice(new BigDecimal("8.50"));
            product.setQrCode("esl.wdyc.cn");
            return productRepository.save(product);
        });
    }

    private AccessPointRecord accessPoint(EruptOrg organization, StoreRecord store) {
        return findAccessPoint(organization).orElseGet(() -> {
            AccessPointRecord accessPoint = new AccessPointRecord();
            accessPoint.setOrganization(organization);
            accessPoint.setStore(store);
            accessPoint.setSn(AP_SN);
            accessPoint.setShopNo(1);
            accessPoint.setStatus("ONLINE");
            accessPoint.setIpAddress("127.0.0.1");
            return accessPointRepository.save(accessPoint);
        });
    }

    private Optional<StoreRecord> findStore(EruptOrg organization) {
        return queryOne(
                "select s from StoreRecord s where s.organization.id = :organizationId and s.code = :code",
                StoreRecord.class,
                organization.getId(),
                "code",
                STORE_CODE
        );
    }

    private Optional<TemplateRecord> findTemplate(EruptOrg organization) {
        return queryOne(
                "select t from TemplateRecord t where t.organization.id = :organizationId and t.deviceTemplateCode = :code",
                TemplateRecord.class,
                organization.getId(),
                "code",
                TEMPLATE_CODE
        );
    }

    private Optional<ProductRecord> findProduct(EruptOrg organization, StoreRecord store) {
        return entityManager.createQuery("""
                        select p from ProductRecord p
                        where p.organization.id = :organizationId and p.store.id = :storeId and p.code = :code
                        """, ProductRecord.class)
                .setParameter("organizationId", organization.getId())
                .setParameter("storeId", store.getId())
                .setParameter("code", PRODUCT_CODE)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    private Optional<AccessPointRecord> findAccessPoint(EruptOrg organization) {
        return queryOne(
                "select a from AccessPointRecord a where a.organization.id = :organizationId and a.sn = :code",
                AccessPointRecord.class,
                organization.getId(),
                "code",
                AP_SN
        );
    }

    private Optional<EslTagRecord> findTag(EruptOrg organization) {
        return entityManager.createQuery("""
                        select t from EslTagRecord t
                        where t.organization.id = :organizationId and t.tagId = :tagId
                        """, EslTagRecord.class)
                .setParameter("organizationId", organization.getId())
                .setParameter("tagId", TAG_ID)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    private <T> Optional<T> queryOne(
            String query,
            Class<T> type,
            Long organizationId,
            String codeParameter,
            String code
    ) {
        return entityManager.createQuery(query, type)
                .setParameter("organizationId", organizationId)
                .setParameter(codeParameter, code)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
