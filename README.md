# Crypto Price Display Plugin

IntelliJ IDEA插件，在状态栏显示实时加密货币价格。

## 功能特性

- 实时显示加密货币价格（BTC, ETH, ASTER, LINK, S, UNI）
- 可配置的刷新间隔和显示精度
- 支持离线模式和错误恢复
- 主题适配和用户友好的界面

## 安装和使用

1. **构建插件**:
   ```bash
   gradlew buildPlugin
   ```

2. **安装插件**:
   - 在IntelliJ IDEA中：Settings → Plugins → Install Plugin from Disk
   - 选择 `build/distributions/crypto-price-plugin-1.0.0.zip`

3. **配置插件**:
   - 重启IDE后，访问 Settings → Tools → Crypto Price Display
   - 配置要显示的加密货币符号和刷新间隔

4. **查看价格**:
   - 价格信息将显示在IDE底部状态栏右侧
   - 左键点击查看详细信息，右键访问菜单

## 故障排除

如果插件安装后没有显示：

1. **检查状态栏**: 确保状态栏可见（View → Appearance → Status Bar）
2. **检查配置**: Settings → Tools → Crypto Price Display，确保"Show in status bar"已勾选
3. **重启IDE**: 安装插件后重启IntelliJ IDEA
4. **查看日志**: Help → Show Log in Explorer，查看idea.log中的错误信息

## 默认配置

- 符号: BTC, ETH, ASTER, LINK, S, UNI
- 基础货币: USDT
- 刷新间隔: 60秒
- 小数位数: 3位

## 技术信息

- Java版本: 11
- 支持的IDE版本: 231-251.*
- API来源: Binance Public API