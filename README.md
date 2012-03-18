## Custom Flume Plug-ins

This repository contains custom (useful) Flume plugins.

### fifoSource

With this source, Flume can read from arbitrary UNIX FIFO files.

```
$ mkfifo /path/something.fifo
$ flume shell -c flume-master
[ flume flume-master:35873:45678 ] exec config node flowId 'fifoSource("/path/something.fifo")' 'agentE2ESink("dst")'
[ flume flume-master:35873:45678 ] quit
$ echo "This is your log message" > /path/something.fifo
```
