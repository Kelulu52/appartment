# 公寓租赁管理系统 — 技术文档

## 一、项目概述

本项目是一个**公寓租赁管理系统**（Lease Management System），为公寓运营方提供房间管理、租约管理、看房预约等后台能力，同时为租户用户提供房间浏览、预约看房、在线签约等功能。整体采用 Maven 多模块架构，分为后台管理端（web-admin）和用户端（web-app）两套独立应用。

---

## 二、技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.5.15 |
| JDK | Java | 17（父 POM），21（子模块编译） |
| 持久层 | MyBatis-Plus（Spring Boot 3 适配版） | 3.5.13 |
| 数据库 | MySQL | mysql-connector-j |
| 连接池 | HikariCP | Spring Boot 内嵌 |
| 缓存 | Redis（spring-boot-starter-data-redis） | — |
| 对象存储 | MinIO | 9.0.2 |
| API 文档 | Knife4j（OpenAPI 3 + Jakarta） | 4.4.0 |
| 认证 | JWT（jjwt） | 0.11.2 |
| 短信服务 | 阿里云短信（dypnsapi20170525） | 2.0.0 |
| 验证码 | EasyCaptcha | 1.6.2 |
| HTTP 客户端 | OkHttp | 4.9.3 |
| 编码工具 | Apache Commons Codec | — |
| 构建工具 | Maven（spring-boot-maven-plugin） | — |

---

## 三、模块结构

```
lease/                          父 POM（packaging=pom）
├── model/                      实体类 + 枚举，纯数据层
├── common/                     公共配置 + 工具类 + 全局异常处理
└── web/                        父 POM，聚合 web-admin 和 web-app
    ├── web-admin/              后台管理端（端口 8080）
    └── web-app/                用户端 / APP 端（端口 8081）
```

### 依赖关系

```
model ── MyBatis-Plus, Knife4j, Lombok
  │
common ── model, MySQL驱动, MinIO, OkHttp, Spring Web, MyBatis-Plus-JSqlParser,
         Swagger注解, Commons-Codec, EasyCaptcha, Redis, JWT, 阿里云SDK
  │
web-admin ── common, Spring Boot Starter Web/Test, Knife4j, Lombok
web-app  ── common, Spring Boot Starter Web/Test, Knife4j, Lombok
```

- **model**: 所有实体类和枚举，不依赖业务层
- **common**: 全局配置（MyBatis-Plus、Redis、MinIO、CORS、Jackson）、工具类（JWT、MD5）、全局异常处理
- **web-admin**: 后台管理端，端口 8080
- **web-app**: 移动端用户应用，端口 8081

---

## 四、核心实体与数据库设计

### 4.1 实体继承体系

所有实体继承 `BaseEntity`，定义公共字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 自增主键 |
| create_time | Date | 插入时自动填充 |
| update_time | Date | 更新时自动填充 |
| is_deleted | Byte | 逻辑删除标记（0=未删除，1=已删除） |

### 4.2 数据表（共 23 张）

#### 基础信息表

| 表名 | 实体类 | 关键字段 | 说明 |
|------|--------|---------|------|
| apartment_info | ApartmentInfo | name, introduction, district/city/province id&name, address_detail, latitude, longitude, phone, is_release | 公寓信息 |
| room_info | RoomInfo | room_number, rent(BigDecimal), apartment_id, is_release | 房间信息 |
| province_info | ProvinceInfo | name | 省份信息 |
| city_info | CityInfo | name, province_id | 城市信息 |
| district_info | DistrictInfo | name, city_id | 区域信息 |

#### 属性 / 配套 / 标签表

| 表名 | 实体类 | 关键字段 | 说明 |
|------|--------|---------|------|
| facility_info | FacilityInfo | type(ItemType), name, icon | 配套设施 |
| label_info | LabelInfo | type(ItemType), name | 标签 |
| fee_key | FeeKey | name | 费用项名称 |
| fee_value | FeeValue | name, unit, fee_key_id | 费用值 |
| attr_key | AttrKey | name | 房间属性 key |
| attr_value | AttrValue | name, attr_key_id | 房间属性 value |

#### 房间配置表

| 表名 | 实体类 | 关键字段 | 说明 |
|------|--------|---------|------|
| lease_term | LeaseTerm | month_count, unit | 租期 |
| payment_type | PaymentType | name, pay_month_count, additional_info | 支付方式 |
| graph_info | GraphInfo | name, item_type(ItemType), item_id, url | 图片 |

#### 多对多关联表

| 表名 | 实体类 | 关联关系 |
|------|--------|---------|
| apartment_facility | ApartmentFacility | 公寓 ↔ 配套 |
| apartment_fee_value | ApartmentFeeValue | 公寓 ↔ 费用值 |
| apartment_label | ApartmentLabel | 公寓 ↔ 标签 |
| room_facility | RoomFacility | 房间 ↔ 配套 |
| room_label | RoomLabel | 房间 ↔ 标签 |
| room_lease_term | RoomLeaseTerm | 房间 ↔ 租期 |
| room_payment_type | RoomPaymentType | 房间 ↔ 支付方式 |
| room_attr_value | RoomAttrValue | 房间 ↔ 属性值 |

#### 业务表

| 表名 | 实体类 | 关键字段 | 说明 |
|------|--------|---------|------|
| lease_agreement | LeaseAgreement | phone, name, identification_number, apartment_id, room_id, lease_start_date, lease_end_date, lease_term_id, rent, deposit, payment_type_id, status, source_type, additional_info | 租约合同 |
| view_appointment | ViewAppointment | user_id, name, phone, apartment_id, appointment_time, additional_info, appointment_status | 看房预约 |
| browsing_history | BrowsingHistory | user_id, room_id, browse_time | 浏览历史 |

#### 用户系统表

| 表名 | 实体类 | 关键字段 | 说明 |
|------|--------|---------|------|
| user_info | UserInfo | phone, password, avatar_url, nickname, status(BaseStatus) | 租户用户 |
| system_user | SystemUser | username, password, name, type(SystemUserType), phone, avatar_url, additional_info, post_id, status | 系统员工 |
| system_post | SystemPost | post_code, name, description, status | 岗位 |

### 4.3 枚举类型

| 枚举 | 值 | 说明 |
|------|-----|------|
| BaseStatus | ENABLE(1), DISABLE(0) | 通用启用/禁用状态 |
| ReleaseStatus | RELEASED(1), NOT_RELEASED(0) | 发布状态 |
| ItemType | APARTMENT(1), ROOM(2) | 对象类型 |
| AppointmentStatus | WAITING(1), CANCELED(2), VIEWED(3) | 预约状态 |
| LeaseStatus | SIGNING(1), SIGNED(2), CANCELED(3), EXPIRED(4), WITHDRAWING(5), WITHDRAWN(6), RENEWING(7) | 租约状态 |
| LeaseSourceType | NEW(1), RENEW(2) | 租约来源（新签/续租） |
| SystemUserType | ADMIN(0), COMMON(1) | 系统用户类型（超级管理员/普通员工） |

---

## 五、核心架构设计

### 5.1 认证机制（JWT）

**Admin 管理端认证流程：**
1. 请求图形验证码 → 后端生成验证码图片并缓存至 Redis（TTL 60s）
2. 提交用户名 + 密码（MD5 加密）+ 验证码 → 后端校验
3. 校验通过后签发 JWT Token，返回前端
4. 后续请求在 HTTP Header `access-token` 中携带 Token

**App 用户端认证流程：**
1. 提交手机号 → 后端生成短信验证码，通过阿里云短信下发，并缓存至 Redis（TTL 600s，60s 内不可重复发送）
2. 提交手机号 + 验证码 → 后端校验
3. 首次登录自动注册用户，签发 JWT Token

**通用机制：**
- Token 传递：HTTP Header `access-token`
- 拦截器：`AuthenticationInterceptor` 解析 JWT，将用户信息存入 `ThreadLocal`（`LoginUserHolder`）
- Token 有效期：**72 小时**（`3600000 * 24 * 3 ms`）
- 签名算法：HS256
- Redis Key 前缀：`admin:login:`、`app:login:`

### 5.2 Redis 缓存策略

| 缓存场景 | Key 格式 | TTL | 说明 |
|---------|---------|-----|------|
| Admin 图形验证码 | `admin:login:{uuid}` | 60s | 验证码图片对应验证码字符串 |
| App 短信验证码 | `app:login:{手机号}` | 600s | 短信验证码，60s 内不重复发送 |
| 房间详情缓存 | `app:room:{roomId}` | — | 房间详情页数据，更新/删除时主动清除 |

### 5.3 MyBatis-Plus 配置

- **分页插件**: `PaginationInnerInterceptor`
- **自动填充**: `MybatisMetaObjectHandler`
  - `create_time` — 插入时自动填充
  - `update_time` — 更新时自动填充
- **逻辑删除**: 基于 `is_deleted` 字段，删除操作实际更新该字段为 1
- **Mapper 扫描路径**: `com.atguigu.lease.web.*.mapper`

### 5.4 统一响应格式

所有 API 返回 `Result<T>` 统一结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

错误码体系由 `ResultCodeEnum` 定义（200=成功，201=参数缺失，202=操作成功，400=参数错误，401=认证失败，403=无权限，404=资源不存在，405=请求方法不支持，500=操作失败，600=非法token，601=服务key缺失，602=任务异常）。

### 5.5 全局异常处理

`GlobalExceptionHandler` 统一捕获异常：
- 自定义 `LeaseException` → 返回对应错误码和消息
- 通用 `Exception` → 返回 500 通用失败

### 5.6 文件存储

使用 **MinIO** 对象存储，通过 `MinioConfiguration` 自动配置，Bucket 名称为 `lease`。后台管理端提供统一的文件上传接口 `/admin/file/upload`。

### 5.7 定时任务

`ScheduleTask` 每天 **00:00:00** 执行：
- 检查所有租约到期状态
- 将已过期的 `SIGNED` / `WITHDRAWING` 租约标记为 `EXPIRED`

---

## 六、API 接口

### 6.1 后台管理端（web-admin，端口 8080）

| 控制器 | 基础路径 | 端点 | 方法 | 功能 |
|--------|---------|------|------|------|
| LoginController | `/admin` | `/login/captcha` | GET | 获取图形验证码 |
| | | `/login` | POST | 管理员登录 |
| | | `/info` | GET | 获取当前登录用户信息 |
| ApartmentController | `/admin/apartment` | `/saveOrUpdate` | POST | 保存/更新公寓 |
| | | `/pageItem` | GET | 分页查询公寓列表 |
| | | `/getDetailById` | GET | 获取公寓详情 |
| | | `/removeById` | DELETE | 删除公寓 |
| | | `/updateReleaseStatusById` | POST | 更新发布状态 |
| | | `/listInfoByDistrictId` | GET | 按区域查公寓列表 |
| RoomController | `/admin/room` | `/saveOrUpdate` | POST | 保存/更新房间 |
| | | `/pageItem` | GET | 分页查询房间 |
| | | `/getDetailById` | GET | 获取房间详情 |
| | | `/removeById` | DELETE | 删除房间 |
| | | `/updateReleaseStatusById` | POST | 更新发布状态 |
| | | `/listBasicByApartmentId` | GET | 按公寓查房间 |
| LeaseAgreementController | `/admin/agreement` | `/saveOrUpdate` | POST | 保存/更新租约 |
| | | `/page` | GET | 分页查询租约 |
| | | `/getById` | GET | 查询租约详情 |
| | | `/removeById` | DELETE | 删除租约 |
| | | `/updateStatusById` | POST | 更新租约状态 |
| ViewAppointmentController | `/admin/appointment` | `/page` | GET | 分页查询预约 |
| | | `/updateStatusById` | POST | 更新预约状态 |
| FacilityController | `/admin/facility` | `/list` | GET | 查询配套列表 |
| | | `/saveOrUpdate` | POST | 保存/更新配套 |
| | | `/deleteById` | DELETE | 删除配套 |
| LabelController | `/admin/label` | `/list` | GET | 查询标签列表 |
| | | `/saveOrUpdate` | POST | 保存/更新标签 |
| | | `/deleteById` | DELETE | 删除标签 |
| AttrController | `/admin/attr` | `/key/saveOrUpdate` | POST | 保存/更新属性 key |
| | | `/value/saveOrUpdate` | POST | 保存/更新属性 value |
| | | `/list` | GET | 查询属性列表 |
| | | `/key/deleteById` | DELETE | 删除属性 key |
| | | `/value/deleteById` | DELETE | 删除属性 value |
| FeeController | `/admin/fee` | `/key/saveOrUpdate` | POST | 保存/更新费用 key |
| | | `/value/saveOrUpdate` | POST | 保存/更新费用 value |
| | | `/list` | GET | 查询费用列表 |
| | | `/key/deleteById` | DELETE | 删除费用 key |
| | | `/value/deleteById` | DELETE | 删除费用 value |
| PaymentTypeController | `/admin/payment` | `/list` | GET | 查询支付方式列表 |
| | | `/saveOrUpdate` | POST | 保存/更新支付方式 |
| | | `/deleteById` | DELETE | 删除支付方式 |
| LeaseTermController | `/admin/term` | `/list` | GET | 查询租期列表 |
| | | `/saveOrUpdate` | POST | 保存/更新租期 |
| | | `/deleteById` | DELETE | 删除租期 |
| RegionInfoController | `/admin/region` | `/province/list` | GET | 查询省份列表 |
| | | `/city/listByProvinceId` | GET | 按省份查城市 |
| | | `/district/listByCityId` | GET | 按城市查区域 |
| SystemUserController | `/admin/system/user` | `/page` | GET | 分页查询员工 |
| | | `/getById` | GET | 查询员工详情 |
| | | `/saveOrUpdate` | POST | 保存/更新员工 |
| | | `/isUserNameAvailable` | GET | 校验用户名可用性 |
| | | `/deleteById` | DELETE | 删除员工 |
| | | `/updateStatusByUserId` | POST | 更新员工状态 |
| SystemPostController | `/admin/system/post` | `/page` | GET | 分页查询岗位 |
| | | `/saveOrUpdate` | POST | 保存/更新岗位 |
| | | `/deleteById` | DELETE | 删除岗位 |
| | | `/getById` | GET | 查询岗位详情 |
| | | `/list` | GET | 查询全部岗位 |
| | | `/updateStatusByPostId` | POST | 更新岗位状态 |
| UserInfoController | `/admin/user` | `/page` | GET | 分页查询租户用户 |
| | | `/updateStatusById` | POST | 更新租户状态 |
| FileUploadController | `/admin/file` | `/upload` | POST | 文件上传（MinIO） |

### 6.2 用户端（web-app，端口 8081）

| 控制器 | 基础路径 | 端点 | 方法 | 功能 |
|--------|---------|------|------|------|
| LoginController | `/app/` | `/login/getCode` | GET | 发送短信验证码 |
| | | `/login` | POST | 短信验证码登录（自动注册） |
| | | `/info` | GET | 获取当前用户信息 |
| ApartmentController | `/app/apartment` | `/getDetailById` | GET | 获取公寓详情 |
| RoomController | `/app/room` | `/pageItem` | GET | 分页查询房间 |
| | | `/getDetailById` | GET | 获取房间详情（Redis 缓存 + 浏览历史） |
| | | `/pageItemByApartmentId` | GET | 按公寓分页查房间 |
| ViewAppointmentController | `/app/appointment` | `/saveOrUpdate` | POST | 保存/更新看房预约 |
| | | `/listItem` | GET | 查询个人预约列表 |
| | | `/getDetailById` | GET | 查询预约详情 |
| LeaseAgreementController | `/app/agreement` | `/listItem` | GET | 查询个人租约列表 |
| | | `/getDetailById` | GET | 查询租约详情 |
| | | `/updateStatusById` | POST | 确认租约 / 提前退租 |
| | | `/saveOrUpdate` | POST | 续约 |
| LeaseTermController | `/app/term/` | `/listByRoomId` | GET | 按房间查租期 |
| PaymentTypeController | `/app/payment` | `/listByRoomId` | GET | 按房间查支付方式 |
| | | `/list` | GET | 查询全部支付方式 |
| RegionController | `/app/region` | `/province/list` | GET | 查询省份列表 |
| | | `/city/listByProvinceId` | GET | 按省份查城市 |
| | | `/district/listByCityId` | GET | 按城市查区域 |
| BrowsingHistoryController | `/app/history` | `/pageItem` | GET | 分页查询浏览历史 |

---

## 七、关键配置

### 7.1 application.yml（web-admin 示例）

```yaml
server:
  port: 8080

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://192.168.61.133:3306/lease
    username: root
    password: xxx
  data:
    redis:
      host: 192.168.61.133
      port: 6379
      database: 0

minio:
  url: http://192.168.61.133:9000
  access-key: xxx
  secret-key: xxx
  bucket-name: lease

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

knife4j:
  openapi:
    params:
      current:
        name: current
        ref: true
        example: "1"
      size:
        name: size
        ref: true
        example: "10"
    properties:
      default-flat-param-object: true
```

> web-app 配置与 web-admin 基本一致，主要区别为端口号（8081）。

---

## 八、项目特色

1. **前后端分离**：后台管理端和用户端独立部署，各自独立启动
2. **双端认证隔离**：Admin 端使用图形验证码 + 密码登录，App 端使用短信验证码登录
3. **Redis 多级缓存**：验证码缓存 + 房间详情缓存，提升高频访问性能
4. **逻辑删除 + 自动填充**：通过 MyBatis-Plus 插件实现，无需手动维护时间戳和删除标记
5. **定时任务巡检**：每日自动检查租约到期状态，保证数据一致性
6. **Knife4j 接口文档**：自动生成 Swagger 接口文档，方便前端联调
7. **MinIO 对象存储**：独立的文件存储服务，支持图片上传管理
8. **统一响应 + 全局异常**：规范 API 返回格式，集中处理异常
