package com.aiden.einklabel.admin.erupt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

class EruptMenuBackfillTest {

    @Test
    void migratesLegacyTemplateMenuAndAddsOperationTables() {
        EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("menu-backfill;MODE=MySQL;NON_KEYWORDS=VALUE")
                .addScript("classpath:erupt-menu-schema.sql")
                .build();
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(database);
            jdbcTemplate.update("""
                    insert into e_upms_menu (id, create_time, update_time, code, icon, name, sort, status)
                    values (1, current_timestamp, current_timestamp, '$template', 'fa fa-object-group', '模板中心', 2, 1)
                    """);
            jdbcTemplate.update("""
                    insert into e_upms_menu (id, create_time, update_time, code, icon, name, sort, status, type, value, parent_menu_id)
                    values (2, current_timestamp, current_timestamp, 'TemplateRecord', '', '模板管理', 10, 1, 'table', 'TemplateRecord', 1)
                    """);

            new EruptMenuBackfill(jdbcTemplate).backfill();

            assertThat(menuCount(jdbcTemplate, "$template")).isZero();
            assertThat(menuCount(jdbcTemplate, "$esl-operation")).isOne();
            assertThat(menuCount(jdbcTemplate, "StoreRecord")).isOne();
            assertThat(menuCount(jdbcTemplate, "ProductRecord")).isOne();
            assertThat(menuCount(jdbcTemplate, "AccessPointRecord")).isOne();
            assertThat(menuCount(jdbcTemplate, "EslTagRecord")).isOne();
            assertThat(menuCount(jdbcTemplate, "EslTagRecord@EXPORT")).isOne();
            assertThat(menuCount(jdbcTemplate, "EslTagRecord@submit_label_update_task")).isOne();
            assertThat(menuCount(jdbcTemplate, "AccessPointRecord@submit_shop_task")).isOne();
            assertThat(menuCount(jdbcTemplate, "TemplateRecord@open_editor")).isOne();
            assertThat(parentCode(jdbcTemplate, "EslTagRecord")).isEqualTo("$esl-operation");
            assertThat(parentCode(jdbcTemplate, "TemplateRecord")).isEqualTo("$esl-operation");
        } finally {
            database.shutdown();
        }
    }

    private int menuCount(JdbcTemplate jdbcTemplate, String code) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from e_upms_menu where code = ?",
                Integer.class,
                code
        );
        return count == null ? 0 : count;
    }

    private String parentCode(JdbcTemplate jdbcTemplate, String code) {
        return jdbcTemplate.queryForObject("""
                select parent.code
                from e_upms_menu child
                join e_upms_menu parent on parent.id = child.parent_menu_id
                where child.code = ?
                """, String.class, code);
    }
}
