# 短链项目架构方案总结

## 方案结论

本项目数据库架构采用：

- 单库：`short_code`
- 多 schema：`code_00` 到 `code_15`
- 每个 schema 下 64 张分表：`short_url_mapping_00` 到 `short_url_mapping_63`

总物理表数量为：

- `16 x 64 = 1024` 张表

该方案适用于 PostgreSQL，优先级高于以下方案：

- 分库分表
- 单库多 schema 分区表
- 分库分区表

## 为什么选择单库多 schema 分表

相较于 MySQL 常见的分库分表模式，PostgreSQL 更适合使用 schema 作为逻辑分库边界。

选择该方案的原因：

- 应用只需要维护一个数据库连接池，连接管理简单
- 不需要多数据源路由，Spring Boot 实现复杂度更低
- 跨 schema 运维、巡检、统计、迁移比跨库更方便
- schema 可以表达逻辑分片，表可以表达物理分片，职责清晰
- 后续如果单库到达瓶颈，仍然可以平滑演进到多库

## 数据库对象规划

数据库：

- `short_code`

schema：

- `code_00`
- `code_01`
- `code_02`
- `code_03`
- `code_04`
- `code_05`
- `code_06`
- `code_07`
- `code_08`
- `code_09`
- `code_10`
- `code_11`
- `code_12`
- `code_13`
- `code_14`
- `code_15`

每个 schema 中包含：

- `short_url_mapping_00`
- `short_url_mapping_01`
- ...
- `short_url_mapping_63`

## 路由设计

短链系统采用双层路由：

1. 先路由到 schema
2. 再路由到表

推荐路由方式：

- 对 `short_code` 或标准化后的 `origin_url` 计算哈希值
- 使用同一份哈希值同时计算 schema 和表编号

示例：

```text
schema_index = hash % 16
table_index = (hash / 16) % 64
```

最终路由结果示例：

```text
code_03.short_url_mapping_27
```

要求：

- 路由规则必须稳定
- 应用层必须封装统一路由组件
- 业务代码不能散落 schema/table 计算逻辑

## 为什么不优先选择分区表

本项目核心访问模式是按 `short_code` 精确点查，而不是范围查询或大批量扫描。

因此当前阶段不优先使用 PostgreSQL 分区表，原因如下：

- 分区表更适合时间范围查询、大批量归档和分区裁剪场景
- 短链主场景是单行读取，分表已经可以满足物理拆分需求
- 应用显式路由到具体 schema 和具体表，行为更直接
- schema + 分区表叠加会提升管理复杂度

## 100W QPS 下的定位

如果系统目标访问量达到 100W QPS，需要明确：

- 数据库不是主流量承载层
- PostgreSQL 只负责冷数据回源、短链创建、后台查询和异步落库

主访问链路应当采用：

- L1：应用本地缓存
- L2：Redis 集群
- L3：PostgreSQL

也就是说：

- 绝大多数短链跳转不应直接访问数据库
- 数据库分片方案决定的是冷读能力和数据落地能力
- 100W QPS 的核心瓶颈不在数据库分片方式，而在缓存命中率与边缘流量承接能力

## 表设计建议

当前主表可保留核心字段：

- `short_code`
- `origin_url`
- `origin_url_hash`
- `create_time`
- `update_time`
- `status`
- `creator`

建议优化：

- 将 `expire_days` 改为 `expire_at`
- `update_time` 使用触发器自动维护
- 是否保留 `origin_url_hash` 唯一约束，取决于是否允许同一长链生成多个短链

不建议在主表中承担高频统计写入压力：

- `access_count` 不适合在跳转主链路中同步更新
- 访问统计应异步化

## 应用层实现建议

Spring Boot 层建议采用：

- 单数据源
- 动态 schema + 动态表名路由
- 使用 `JdbcTemplate`、`NamedParameterJdbcTemplate` 或 MyBatis 动态 SQL

建议拆分以下组件：

- `ShortCodeRouter`
  负责根据 `shortCode` 或 `originUrlHash` 计算 schema 和表
- `ShortUrlRepository`
  负责对目标 schema/table 执行 SQL
- `ShortUrlService`
  负责短链创建、查询、校验和失效
- `ShortUrlStatsService`
  负责访问统计异步处理

## 演进策略

当前阶段先落地：

- 单库 `short_code`
- 16 个 schema
- 每个 schema 64 张表

后续如果单库达到瓶颈，再考虑：

- 将部分 schema 拆分到多个数据库
- 为路由规则增加版本号
- 按新旧路由规则逐步迁移数据

该演进方式优于一开始直接上多库，因为：

- 初期实现和运维成本更低
- 扩容路径更清晰
- 风险更可控

## 当前最终建议

本项目采用以下数据库架构：

- PostgreSQL 单库：`short_code`
- 16 个 schema：`code_00` 到 `code_15`
- 每个 schema 64 张表：`short_url_mapping_00` 到 `short_url_mapping_63`

这是当前阶段在 PostgreSQL 上最平衡、最适合短链系统演进的方案。
