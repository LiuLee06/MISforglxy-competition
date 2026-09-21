# 成果管理：本地数据库迁移到云端数据库指导

本文档说明如何把「成果管理」模块的数据从本地数据库 `achievement_system` 迁移到云端 MySQL，并让成果管理改用云端数据库。

## 前提

- 成果管理使用的库名：`achievement_system`，表名：`achievement`。
- 代码中未写死本地地址，数据源通过配置读取，改配置即可切换，无需改代码。
- 建库脚本：`backend/SQL/achievement_system.sql`。

---

## 第一步：在云端建库建表

在云端 MySQL 上执行建库脚本（使用云端账号）：

```bash
mysql -h云端IP -u云端账号 -p < backend/SQL/achievement_system.sql
```

执行后会在云端创建：

- 数据库 `achievement_system`
- 数据表 `achievement`
- 6 条示例数据（4 条已通过 + 2 条待验证）

> 如果只想建空表、不要示例数据，把 SQL 文件末尾的 `INSERT INTO achievement ...` 段落删掉再执行即可。

---

## 第二步：处理数据（二选一）

### 情况 A：本地还没有正式数据

云端直接用示例数据即可，**跳过这一步**。

### 情况 B：本地已有真实成果数据，需要同步到云端

```bash
# 1. 导出本地库（含建表语句，会覆盖云端同名表，请先确认云端没有要保留的数据）
mysqldump -uroot -p123456 achievement_system > achievement_dump.sql

# 2. 导入云端
mysql -h云端IP -u云端账号 -p < achievement_dump.sql
```

> 如果只想同步数据、不想覆盖云端表结构：
> - 导出时加 `--no-create-info`（只导出数据、不含建表语句）；
> - 导入前先在云端按第一步把表建好。

---

## 第三步：修改成果管理数据源，指向云端

成果管理数据源配置位于 `backend/src/main/resources/application.yaml` 的 `achievement.datasource` 块，默认指向本地（`127.0.0.1` + `root/123456`）。改为云端，任选下面一种方式：

### 方式 1（推荐，与主系统做法一致）：在 `application-local.yaml` 中追加

```yaml
achievement:
  datasource:
    url: jdbc:mysql://云端IP:3306/achievement_system?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&useServerPrepStmts=true
    username: 云端账号
    password: 云端密码
```

### 方式 2：直接修改 `application.yaml` 的默认值

把 `achievement.datasource` 下的 `url`、`username`、`password` 默认值改成云端地址和账号密码。

### 方式 3：启动时通过环境变量覆盖

```bash
ACHIEVEMENT_DB_URL=jdbc:mysql://云端IP:3306/achievement_system?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&useServerPrepStmts=true \
ACHIEVEMENT_DB_USERNAME=云端账号 \
ACHIEVEMENT_DB_PASSWORD=云端密码
```

---

## 第四步：重启并验证

重启后端服务，登录 `admin` 账号，进入「成果管理」的三个页面：

- 成果收集：能正常上传、OCR、提交；
- 成果信息人工验证：能看到待验证数据并审核；
- 成果信息展示：列表、统计、图表数据正常。

全部正常即说明已成功切换到云端数据库。

---

## 附注

1. **IP 白名单**：云端 MySQL 需开放访问，并把运行后端机器的 IP 加入白名单（与主系统连云端时同理）。
2. **附件文件**：上传的附件图片保存在本地 `backend/uploads/` 磁盘目录，不在数据库中。若更换服务器运行，需将该目录一并迁移。
3. **敏感信息**：云端数据库密码属于敏感信息，请勿提交到公开仓库；本地 `application-local.yaml` 已在 `.gitignore` 中排除。
