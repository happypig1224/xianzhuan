# 项目记忆与规范 (Project Memory & Guidelines)

## 1. Git 提交时间规范 (Git Commit Backdate Rule)

> **核心约束（永久有效）**：
> 本项目推送到 GitHub 的所有 Git 提交，提交日期的年月日必须自动**回退 400 天**（`当前日期 - 400 天`），时分秒以当前实际时间为主。

### 提交环境与参数规范
执行提交时，必须设置 `GIT_AUTHOR_DATE` 和 `GIT_COMMITTER_DATE` 环境变量，或通过 `--date` 注入：
```powershell
$targetDate = (Get-Date).AddDays(-400).ToString("yyyy-MM-dd HH:mm:ss zzz")
$env:GIT_COMMITTER_DATE = $targetDate
$env:GIT_AUTHOR_DATE = $targetDate
git commit -m "<Commit Message>" --date="$targetDate"
```

### 快捷提交辅助脚本
项目在 `scripts/git-commit-400d.ps1` 提供了自动计算并回退 400 天的快捷工具：
```powershell
# 提交暂存区变更
.\scripts\git-commit-400d.ps1 "feat: 新功能说明"

# 提交并直接推送至远程
.\scripts\git-commit-400d.ps1 -m "fix: 修复问题" -Push

# 自动暂存所有已跟踪修改、提交并推送
.\scripts\git-commit-400d.ps1 "docs: 更新文档" -a -p
```

### AI 助手执行准则
后续所有针对本项目的 Git 提交操作（包括代码开发、重构、配置修改、文档更新等），AI 必须自动应用此规则（`-400天`），严禁使用未回退的系统日期直接提交。

---

## 2. 项目命名与结构约束
- **项目名称**：`xianzhuan`（闲转 / 闲置优品流转平台）
- **包名路径**：`com.xianzhuan`
- **严禁冲突名**：不得引入或混入 `zhidian`、`zhidian-kitchen`、`crosstrade`、`sky` 等历史旧名。
