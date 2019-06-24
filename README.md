# Server (s) status watcher
It pings (every minute) servers in the servers.json file. Ping is a HTTP GET request or a TCP open connect.
Telegram bot is used for notification. Every 10080 minute bot messages a report with servers uptime.
How uptime is calculated (not accurate, but ok):

    (successfull respond counts of server) * 100.0 / (alltime request count) 

"Server is down" notifications is send only when server is not responding in a 3 minutes.
Because there can be a lot of routing failures.
3 minute delay is better than wrong "server is down" notifications 

You should create a telegram bot and chat.
Then pass bot token, bot username and chat ID as ENV variables to docker container.
How to get chat ID?
Example: create a chat, add bot as chat member, then go to the web.telegram.org and select chat.
In browser's location bar you will see something like "https://web.telegram.org/#/im?p=g1234456789".
Chat ID is: -1234456789 (with minus, it's important)

## Quick usage

    mkdir -pv status-watcher-dir

    cat >> servers.json << EOF
    {
      "http": [
        {
          "url": "http://google.com"
        },
        {
          "url": "https://vk.com"
        }
      ],
      "tcp": [
        {
          "host": "localhost",
          "port": 8080
        },
        {
          "host": "localhost",
          "port": 8081
        }
      ]
    }
    EOF

    docker run --restart always -d \
               --name watcher \
               -v $PWD/status-watcher-dir:/status-watcher-dir \
               -e "token"="token" \
               -e "username"="username" \
               -e "chatId"="chatId" \
               walkingdevs/status-watcher 