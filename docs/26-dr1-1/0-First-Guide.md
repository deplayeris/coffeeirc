# CoffeeIRC 开发者先导指南

## 环境准备

### 系统要求
- **操作系统**: Windows 10/11, Linux, macOS（已知安卓版本不适合运行）
- **Java版本**: Java 25 或更高版本
- **内存**: 建议至少 512MB 可用内存
- **磁盘空间**: 至少 100MB 可用空间
- 推荐从v26.dr1.1或者以上版本开始进行开发

## 项目获取

### 方式一：从GitHub下载
```bash
git clone https://github.com/deplayeris/coffeeirc.git
cd coffeeirc
```

### 方式二：直接下载JAR包
从 [Releases页面](https://github.com/deplayeris/coffeeirc/releases) 下载最新版本的JAR文件<br>
一般文件名为 `coffeeirc-版本号.jar`，但如果是26.dr1.1，则文件名为 `use-this-coffeeirc-core-26.dr1.1.jar`

## 构建项目（可选）

如果您下载的是源代码，需要先构建：

### 使用Gradle构建
```bash
# Windows
gradlew.bat build

# Linux/macOS
./gradlew build
```

### 构建产物位置
构建完成后，JAR文件位于：
```
build/libs/coffeeirc-版本号.jar
```

然后请将这个jar文件按照常规的方法添加进项目依赖中。
比如在build.gradle中添加依赖：
```gradle
dependencies {
    implementation files("irccore/use-this-coffeeirc-core-26.dr1.1.jar")
    //将这个jar放到你的项目的irccore中
}
```

## 常见问题解决

### 1. 端口被占用
```
# 错误: Cannot assign requested address: bind
# 解决: 更换端口或停止占用端口的程序
java -Dserver.port=10026 -jar coffeeirc-版本号.jar
```

### 2. 中文显示乱码
```
# 解决: 添加JVM编码参数
java "-Dfile.encoding=UTF-8" -jar coffeeirc-版本号.jar
```

### 3. 客户端无法连接
- 检查服务器是否已启动
- 确认IP地址和端口正确
- 检查防火墙设置

### 4. 权限不足
```
# Linux/macOS:
chmod +x coffeeirc-版本号.jar
./coffeeirc-版本号.jar
```

## 下一步学习

- 查看 [API文档](2-API_Documentation.md) 了解详细接口
- 参考 [快速入门指南](1-Quick_Start_Guide.md) 
- 浏览 [GitHub仓库](https://github.com/deplayeris/coffeeirc) 获取最新更新
- 你也可以查看由编译器给出的[Javadoc文档](javadoc/)查看直观且涉及底层的文档，推荐下载下来查看<br>
// 这是一个网页文档，`javadoc/index.html`是首页。点击每一个方法或函数，都可以查看它的源代码。

## 技术支持

如遇到问题，请：
1. 查看日志文件获取详细错误信息
2. 访问 [GitHub Issues](https://github.com/deplayeris/coffeeirc/issues) 提交问题
3. 提供详细的环境信息和错误日志

---
*这个文档适用于 CoffeeIRC v26.dr1.1 版本*