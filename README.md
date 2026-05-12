# 电子价签模板后台

这是 `eink-label-template-editor` 的 Erupt 后端管理项目，提供模板元信息管理、模板编辑器跳转链接、模板读取和保存 API，并集成店铺、商品、AP、电子价签的后台管理。

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

## 后台业务管理

后台菜单统一在 `价签运营` 下：

- `店铺管理`：维护门店代码、门店 ID、门店名称和所属组织。门店代码对应 MQTT 协议里的 `shopcode/shop`。
- `商品管理`：维护商品编码、商品名称、价格、促销价、二维码等字段，并通过 `绑定已有模板` 字段选择模板。商品下发到价签时会把商品字段映射到 `GOODS_NAME`、`GOODS_CODE`、`F_1`、`F_20`、`QRCODE` 等协议字段。
- `AP管理`：维护 AP 编码、店内 AP 编号、运行信息，并绑定店铺。行操作 `门店配置数据` 会生成协议里的 `esl/server/mgr/{ap}` + `shopcode` 配置数据。
- `电子价签管理`：维护价签十进制 ID、型号、电量、信号、协议状态，并通过 `绑定商品` 字段把电子价签和商品绑定。行操作 `商品下发数据` 会生成协议里的 `esl/server/data/{shop}` + `wtag` 下发数据。
- `模板管理`：维护模板尺寸、色彩模式、编辑器数据和 `设备模板编码`。`设备模板编码` 用作 MQTT `wtag.tmpl`，为空时使用模板名称。

组织隔离规则：

- 业务数据都绑定 Erupt 组织。
- 非管理员只能看到同组织数据，新增或更新时自动绑定当前登录用户的组织。
- AP、商品、电子价签必须绑定同组织店铺；电子价签绑定商品时要求两者属于同一店铺。

## MQTT 数据接口

这些接口需要 Erupt 登录态：

```plain
GET  /api/access-points/{id}/shop-binding-command
GET  /api/esl-labels/{id}/update-command
POST /api/esl-labels/{labelId}/bind-product/{productId}
```

`GET /api/esl-labels/{id}/update-command` 会根据 `电子价签 -> 商品 -> 模板` 的绑定关系生成 `wtag` 数据，并记录最近一次生成的任务 ID 和 payload。

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
