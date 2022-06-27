# bot_cqhttp

A bot based on [go-cqhttp](https://github.com/Mrs4s/go-cqhttp)

### What can it do?

- Play UNO game in QQ groups.
- Play a guess number game in group or private.
- Simulate the delivery system of PokémonCaféReMix.
- Generate a fake forward message in a group.

### About the source files

The Main/Main.java is the starting point.

- Main: contains Main.java and other files to distribute message.
- Game: contains all four games' java files to process the game.
- HTTPConnect: files used to connect with go-cqhttp through HTTP.

### How to run the bot

1. Configure and start go-cqhttp. Do open the "反向HTTP POST地址", and set your own reception port.
2. Go to Release Page, download and run the bot_cqhttp.jar. It will require send_port and receive_port at the first time to connect to go-cqhttp
3. After this it is expected to run.