# 轻记账 TravelLedgerApp

Android 8.0+ 的旅行记账应用，当前版本 1.6。

## 功能

- 记录收入、支出、用途、分类、日期、账户、旅行项目、备注和多张照片。
- 缓存本机用户身份，支持同时筛选多个记录人；统计与列表使用相同的筛选条件。
- 在 App 内操作、选择图片及导入/导出 JSON 备份。
- 通过二维码携带热点及邀请信息加入，无需再次输入访问码。Android 仍可能要求系统确认连接热点及授权。
- 每台手机离线编辑自己的 SQLite 数据。配对后，双方 App 位于前台且网络可达时自动同步，约每 3 秒检查更新。

## 实验性同步

同步以最后一次成功同步的共同快照进行记录级三方比较。单边修改会传播；双方同时修改保留冲突副本，冲突账目默认不计入统计。已同步记录的删除会传播，删除与编辑冲突保留编辑内容。首次配对会合并双方已有数据。

多人分别连接同一个共享节点，修改通过该节点传播。离线修改先保存在本机，网络恢复后重试。热点名称或地址改变时可能需要重新扫码。普通浏览器直接编辑主机账本，App 使用本地优先模式。

双端写入不是分布式事务，先远端后本机；成功后才更新共同快照，并用版本检查避免覆盖并发修改。每次本机同步修改前保留备份，最近 5 份可从 App 导出。单次同步 JSON（包含照片）最多 32 MB。停止共享会暂停同步并拒绝自动接收；重新扫码或展示邀请可恢复。卸载或清除应用数据会删除本机账本、身份和同步历史。

## 构建

需要 Linux/macOS、JDK 17、Python 3，以及 Android SDK 的 `platforms;android-35` 和 `build-tools;35.0.0`。项目直接调用 Android 构建工具，不依赖 Gradle。

```sh
export ANDROID_HOME=/path/to/android-sdk
sdkmanager "platforms;android-35" "build-tools;35.0.0"
chmod +x build.sh tests/run.sh
./build.sh
./tests/run.sh
```

APK 输出：`build/qing-ledger.apk`。测试脚本首次运行会下载 Maven Central 的 `org.json:json:20240303`；也可以通过 `JSON_JAR` 指定已有文件。SDK 路径可以用 `LEDGER_ANDROID_SDK` 指定，构建工具路径可以用 `LEDGER_BUILD_TOOLS` 指定。

默认生成本机调试签名，仅用于开发。**它不能覆盖安装之前使用原始私钥签名的 APK**。不同 CI 构建的调试密钥也可能不同。需要升级原安装版本时，应使用单独保管的原始签名文件：

```sh
export LEDGER_KEYSTORE=/private/path/ledger.p12
export LEDGER_KEYSTORE_PASSWORD_FILE=/private/path/password.txt
./build.sh
```

签名私钥、密码、用户账本和照片均不进入仓库。CI 只构建调试 APK，可在 GitHub Actions 的构建产物中下载；保留期限由工作流指定。

## 目录与验证

`src/app/qingledger/` 是原生 Android、局域网 HTTP 服务、身份与同步逻辑；`assets/index.html` 是内置记账界面；`res/` 和 `AndroidManifest.xml` 是 Android 资源与配置。

`tests/` 包含 JVM HTTP、照片/日期校验、并发写入、事务回滚、合并、身份归属和自动同步回归测试。JVM 测试不能替代实机相机、热点、系统授权及前后台切换测试；这些仍需 Android 手机验证。

## 第三方依赖

二维码使用 ZXing Core 3.5.3，JAR 随项目提供，许可证见 `libs/ZXING-LICENSE.txt` 与 `assets/THIRD_PARTY_LICENSES.txt`。测试使用 org.json 20240303（仅测试，不打包到 APK），遵循其上游许可证。本仓库未为应用原创代码另行指定开源许可证。
