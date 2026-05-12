# 电子价签模板后台

这是 `eink-label-template-editor` 的 Erupt 后端管理项目，提供模板元信息管理、模板编辑器跳转链接、模板读取和保存 API。

## 本地启动

```bash
./mvnw spring-boot:run
```

启动后访问：

```plain
http://127.0.0.1:8080/
```

默认 Erupt 登录账号：

```plain
账号：erupt
密码：erupt
```

## 前端联动

模板管理表单维护：

- 模板 ID
- 模板名称
- 色彩模式：`BW`、`BWR`、`BWRY`、`E6`
- 模板宽度
- 模板高度

在模板列表点击 `编辑模板` 行按钮后，后台会打开前端编辑器，并在 URL 中传入：

- `templateId`
- `templateName`
- `width`
- `height`
- `colorMode`
- `apiBase`
- `saveApi`

前端编辑器会用这些参数初始化画布；如果 URL 没有这些参数，仍按编辑器现有默认流程启动。

默认前端地址是 `http://127.0.0.1:5173/`，可以通过环境变量覆盖：

```bash
TEMPLATE_EDITOR_BASE_URL=http://127.0.0.1:4173/ ./mvnw spring-boot:run
```
