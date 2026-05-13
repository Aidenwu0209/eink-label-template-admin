package com.aiden.einklabel.admin.erupt;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EruptMenuBackfill implements ApplicationRunner {

    private static final String ROOT_CODE = "$esl-operation";
    private static final String LEGACY_ROOT_CODE = "$template";
    private static final List<TableMenu> TABLE_MENUS = List.of(
            new TableMenu("StoreRecord", "店铺管理", 10),
            new TableMenu("ProductRecord", "商品管理", 20),
            new TableMenu("AccessPointRecord", "AP管理", 30),
            new TableMenu("EslTagRecord", "电子价签管理", 40),
            new TableMenu("TemplateRecord", "模板管理", 50)
    );
    private static final List<ButtonMenu> BUTTON_MENUS = List.of(
            new ButtonMenu("ADD", "ADD", 10),
            new ButtonMenu("EDIT", "EDIT", 20),
            new ButtonMenu("DELETE", "DELETE", 30),
            new ButtonMenu("EXPORT", "EXPORT", 40),
            new ButtonMenu("VIEW_DETAIL", "DETAIL", 50)
    );
    private static final Map<String, List<ButtonMenu>> ROW_OPERATION_MENUS = Map.of(
            "AccessPointRecord", List.of(
                    new ButtonMenu("preview_shop_command", "预览数据", 60),
                    new ButtonMenu("submit_shop_task", "提交任务", 70),
                    new ButtonMenu("refresh_shop_task_status", "刷新状态", 80)
            ),
            "EslTagRecord", List.of(
                    new ButtonMenu("preview_label_update_command", "预览数据", 60),
                    new ButtonMenu("submit_label_update_task", "提交任务", 70),
                    new ButtonMenu("refresh_label_task_status", "刷新状态", 80)
            ),
            "TemplateRecord", List.of(
                    new ButtonMenu("open_editor", "编辑模板", 60)
            )
    );

    private final JdbcTemplate jdbcTemplate;

    public EruptMenuBackfill(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        backfill();
    }

    void backfill() {
        Long rootId = ensureRootMenu();
        for (TableMenu table : TABLE_MENUS) {
            Long tableId = ensureTableMenu(table, rootId);
            for (ButtonMenu button : BUTTON_MENUS) {
                ensureButtonMenu(table.code(), button, tableId);
            }
            for (ButtonMenu button : ROW_OPERATION_MENUS.getOrDefault(table.code(), List.of())) {
                ensureButtonMenu(table.code(), button, tableId);
            }
        }
    }

    private Long ensureRootMenu() {
        Optional<Long> existingRoot = findMenuId(ROOT_CODE);
        if (existingRoot.isPresent()) {
            updateRoot(existingRoot.get());
            return existingRoot.get();
        }
        Optional<Long> legacyRoot = findMenuId(LEGACY_ROOT_CODE);
        if (legacyRoot.isPresent()) {
            jdbcTemplate.update("""
                    update e_upms_menu
                    set code = ?, name = ?, icon = ?, sort = ?, status = 1, update_time = current_timestamp
                    where id = ?
                    """, ROOT_CODE, "价签运营", "fa fa-tags", 2, legacyRoot.get());
            return legacyRoot.get();
        }
        jdbcTemplate.update("""
                insert into e_upms_menu (create_time, update_time, code, icon, name, sort, status)
                values (current_timestamp, current_timestamp, ?, ?, ?, ?, 1)
                """, ROOT_CODE, "fa fa-tags", "价签运营", 2);
        return findMenuId(ROOT_CODE).orElseThrow();
    }

    private void updateRoot(Long rootId) {
        jdbcTemplate.update("""
                update e_upms_menu
                set name = ?, icon = ?, sort = ?, status = 1, update_time = current_timestamp
                where id = ?
                """, "价签运营", "fa fa-tags", 2, rootId);
    }

    private Long ensureTableMenu(TableMenu table, Long rootId) {
        Optional<Long> existing = findMenuId(table.code());
        if (existing.isPresent()) {
            jdbcTemplate.update("""
                    update e_upms_menu
                    set name = ?, sort = ?, status = 1, type = 'table', value = ?, parent_menu_id = ?, update_time = current_timestamp
                    where id = ?
                    """, table.name(), table.sort(), table.code(), rootId, existing.get());
            return existing.get();
        }
        jdbcTemplate.update("""
                insert into e_upms_menu (create_time, update_time, code, icon, name, sort, status, type, value, parent_menu_id)
                values (current_timestamp, current_timestamp, ?, '', ?, ?, 1, 'table', ?, ?)
                """, table.code(), table.name(), table.sort(), table.code(), rootId);
        return findMenuId(table.code()).orElseThrow();
    }

    private void ensureButtonMenu(String tableCode, ButtonMenu button, Long tableId) {
        String code = tableCode + "@" + button.code();
        Optional<Long> existing = findMenuId(code);
        if (existing.isPresent()) {
            jdbcTemplate.update("""
                    update e_upms_menu
                    set name = ?, sort = ?, status = 1, type = 'button', value = ?, parent_menu_id = ?, update_time = current_timestamp
                    where id = ?
                    """, button.name(), button.sort(), code, tableId, existing.get());
            return;
        }
        jdbcTemplate.update("""
                insert into e_upms_menu (create_time, update_time, code, name, sort, status, type, value, parent_menu_id)
                values (current_timestamp, current_timestamp, ?, ?, ?, 1, 'button', ?, ?)
                """, code, button.name(), button.sort(), code, tableId);
    }

    private Optional<Long> findMenuId(String code) {
        return jdbcTemplate.query(
                        "select id from e_upms_menu where code = ?",
                        (rs, rowNum) -> rs.getLong("id"),
                        code
                )
                .stream()
                .findFirst();
    }

    private record TableMenu(String code, String name, int sort) {
    }

    private record ButtonMenu(String code, String name, int sort) {
    }
}
