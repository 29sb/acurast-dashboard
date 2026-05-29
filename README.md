# Acurast Dashboard

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-purple)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202025.01-green)](https://developer.android.com/jetpack/compose)

> **Acurast 算力看板** — 一个连接 [Acurast](https://acurast.com) 去中心化计算网络的 Android App，支持主网 (Mainnet) 和测试网 (Canary) 切换，实时查看网络状态和钱包信息。
>
> **Acurast Dashboard** — An Android app that connects to the [Acurast](https://acurast.com) decentralized compute network. Supports Mainnet / Canary switching, real-time network stats, and wallet queries.

---

## 📱 截图预览 / Screenshots

| 网络概览 / Overview | 钱包查询 / Wallet |
|---|---|
| ![Overview](screenshots/overview.png) | ![Wallet](screenshots/wallet.png) |

> *Screenshots will be added after the first run.*

---

## 🌟 功能特性 / Features

### 中文

- ✅ **双网络切换** — 一键切换 Acurast Mainnet（主网）和 Canary（测试网）
- ✅ **网络概览** — 实时显示当前链名、区块高度、版本、在线处理器数量、活跃任务数
- ✅ **钱包查询** — 输入 Substrate 地址查询 ACU 余额、处理器注册状态
- ✅ **Material Design 3** — 现代化 UI，支持动态取色 (Android 12+)
- ✅ **MVVM 架构** — 架构清晰，易于扩展

### English

- ✅ **Dual Network** — Switch between Acurast Mainnet and Canary with one tap
- ✅ **Network Overview** — Real-time chain name, block height, version, estimated processor count, and active tasks
- ✅ **Wallet Lookup** — Query ACU balance and processor registration status by Substrate address
- ✅ **Material Design 3** — Modern UI with dynamic color (Android 12+)
- ✅ **MVVM Architecture** — Clean architecture, easy to extend

---

## 🏗 技术架构 / Tech Stack

| 层级 / Layer | 技术 / Technology |
|---|---|
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose + Material Design 3 |
| Navigation | Navigation Compose (Bottom Navigation) |
| Architecture | MVVM (ViewModel + StateFlow) |
| Networking | OkHttp 4.12 + Gson |
| Chain RPC | Substrate JSON-RPC |
| SDK | Acurast Kotlin SDK (v0.1.39, JitPack) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |

---

## 🚀 快速开始 / Getting Started

### 中文

```bash
# 1. 克隆项目
git clone git@github.com:29sb/acurast-dashboard.git

# 2. 用 Android Studio 打开
cd acurast-dashboard

# 3. 配置 local.properties（如果自动生成失败）
# sdk.dir=/path/to/Android/sdk

# 4. 编译并安装
./gradlew assembleDebug
```

### English

```bash
# 1. Clone
git clone git@github.com:29sb/acurast-dashboard.git

# 2. Open with Android Studio
cd acurast-dashboard

# 3. Configure local.properties if needed
# sdk.dir=/path/to/Android/sdk

# 4. Build & install
./gradlew assembleDebug
```

---

## 🔗 网络端点 / Network Endpoints

| 网络 / Network | RPC (WSS) | RPC (HTTPS) |用途 / Purpose |
|---|---|---|---|
| **Mainnet** | `wss://archive.mainnet.acurast.com` | `https://archive.mainnet.acurast.com` | 生产环境，真实 ACU |
| **Canary** | `wss://canarynet-ws-1.acurast-h-server-2.papers.tech` | `https://acurast-canary-rpc.gateway.pinata.cloud` | 测试环境，cACU (水龙头) |

> 在 App 顶部点击 🔗 按钮即可切换网络。

---

## 📁 项目结构 / Project Structure

```
acurast-dashboard/
├── app/
│   ├── src/main/
│   │   ├── java/com/acurast/dashboard/
│   │   │   ├── MainActivity.kt            # 主入口 + 导航
│   │   │   ├── data/
│   │   │   │   └── AcurastRepository.kt    # RPC 数据仓库
│   │   │   ├── viewmodel/
│   │   │   │   └── DashboardViewModel.kt  # MVVM ViewModel
│   │   │   └── ui/
│   │   │       ├── theme/                 # MD3 主题配置
│   │   │       └── screens/
│   │   │           ├── OverviewScreen.kt  # 网络概览页
│   │   │           └── WalletScreen.kt    # 钱包查询页
│   │   └── res/                           # 资源文件
│   └── build.gradle.kts                   # App 构建配置
├── build.gradle.kts                       # 根构建配置
├── settings.gradle.kts                    # 项目设置
└── gradle/wrapper/                        # Gradle Wrapper (8.9)
```

---

## 🛠 编译说明 / Build Notes

- **Gradle**: 8.9 (wrapper 已内置)
- **AGP**: 8.7.3
- **Build Tools**: 36.0.0
- **JDK**: 17+
- **AAPT2**: 使用系统 build-tools 避免兼容性问题

---

## 📄 许可证 / License

[MIT](LICENSE)

---

## 🤝 贡献 / Contributing

欢迎提交 Issue 和 PR！ / Issues and PRs are welcome!

---

## 📬 相关链接 / Related Links

- [Acurast 官网](https://acurast.com)
- [Acurast 文档](https://docs.acurast.com)
- [Acurast GitHub](https://github.com/Acurast)
- [ACU 水龙头 (Canary)](https://faucet.acurast.com)
