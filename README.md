# bot_cqhttp

一个基于[go-cqhttp](https://github.com/Mrs4s/go-cqhttp)的机器人。

### 它能做什么？

- 在QQ群里玩UNO游戏。
- 在群或私聊里玩猜数字游戏。
- 模拟PokémonCaféReMix的外送。
- 在群或私聊中生成一个假的转发信息。

### 关于源文件

Main/Main.java 为入口。

- src/Main：包含Main.java和其他文件，用于给go-cqhttp分发消息。
- src/Game：包含所有游戏的java文件，用于处理游戏。
- src/Function：包含所有用于处理功能的java文件。
- src/HTTPConnect：用于通过HTTP与go-cqhttp连接的文件以及用于下载图片的文件。
- lib：支持JSON的文件。(Alibaba.fastjson)

### 如何运行这个机器人

1. 配置并启动go-cqhttp。打开 "反向HTTP POST地址"，并设置你自己的接收端口。
2. 进入发布页面，下载并运行 bot_cqhttp.jar。它将需要send_port和receive_port在第一时间连接到go-cqhttp。
3. 在这之后，希望它能跑起来。

## English version

A bot based on [go-cqhttp](https://github.com/Mrs4s/go-cqhttp)

### What can it do?

- Play UNO game in QQ groups.
- Play a guess number game in group or private.
- Simulate the delivery system of PokémonCaféReMix.
- Generate a fake forward message in a group or private.

### About the source files

The Main/Main.java is the starting point.

- src/Main: contains Main.java and other files to distribute message with go-cqhttp.
- src/Game: contains all games' java files to process the games.
- src/Function: contains all java files to process the features.
- src/HTTPConnect: files used to connect with go-cqhttp through HTTP and files for download images.
- lib: support files for JSON. (Alibaba.fastjson)

### How to run the bot

1. Configure and start go-cqhttp. Do open the "反向HTTP POST地址", and set your own reception port.
2. Go to Release Page, download and run the bot_cqhttp.jar. It will require send_port and receive_port at the first time to connect to go-cqhttp
3. After this it is expected to run.