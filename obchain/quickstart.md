# How to quick start

## Introduction

This guide provides two ways for obc quickstart:
- Set up a FullNode using the official tools: providing a wealth of configurable parameters to startup a FullNode
- Set up a complete private network for obc development using a third-party tool: [docker-obc-quickstart]

## Dependencies

### Docker

Please download and install the latest Docker from Docker official website:
 

## Quickstart based on official tools

### Build the docker image from source

#### Clone the java-obc repo

Clone the java-obc repo from github and enter the directory `java-obc`:
```
git clone https://github.com/obcprotocol/java-obc.git
cd java-obc
```

#### Build the docker image

Use below command to start the build:
```
docker build -t obcprotocol/java-obc .
```

#### Using the official Docker images

Download the official docker image from the Dockerhub with below command if you'd like to use the official images:
```
docker pull obcprotocol/java-obc
```

### Run the container

You can run the command below to start the java-obc:
```
docker run -it -d -p 8090:8090 -p 8091:8091 -p 18888:18888 -p 50051:50051 --restart always obcprotocol/java-obc 
```

The `-p` flag defines the ports that the container needs to be mapped on the host machine. By default the container will start and join in the mainnet
using the built-in configuration file, you can specify other configuration file by mounting a directory and using the flag `-c`.
This image also supports customizing some startup parameters，here is an example for running a FullNode as an SR in production env:
```
docker run -it -d -p 8080:8080 -p 8090:8090 -p 18888:18888 -p 50051:50051 \
           -v /Users/quan/obc/docker/conf:/java-obc/conf \
           -v /Users/quan/obc/docker/datadir:/java-obc/data \
           obcprotocol/java-obc \
           -jvm "{-Xmx10g -Xms10g}" \
           -c /java-obc/conf/config-localtest.conf \
           -d /java-obc/data \
           -w 
```
Note: The directory `/Users/obc/docker/conf` must contain the file `config-localtest.conf`. The jvm parameters must be enclosed in double quotes and braces.

## Quickstart for using docker-obc-quickstart

The image exposes a Full Node, Solidity Node, and Event Server. Through obc Quickstart, users can deploy DApps, smart contracts, and interact with the obcWeb library.
Check more information at [Quickstart:](https://github.com/obc-US/docker-obc-quickstart)

### Node.JS Console
  Node.JS is used to interact with the Full and Solidity Nodes via obc-Web.  
  [Node.JS](https://nodejs.org/en/) Console Download
  
### Clone obc Quickstart  
```shell
git clone https://github.com/obc-US/docker-obc-quickstart.git
```  

### Pull the image using docker:
```shell
docker pull obctools/quickstart
```  

## Setup obc Quickstart   
### obc Quickstart Run
Run the "docker run" command to launch obc Quickstart. obc Quickstart exposes port 9090 for Full Node, Solidity Node, and Event Server.
```shell
docker run -it \
  -p 9090:9090 \
  --rm \
  --name obc \
  obctools/quickstart
```  
Notice: the option --rm automatically removes the container after it exits. This is very important because the container cannot be restarted, it MUST be run from scratch to correctly configure the environment.

### Testing

If everything goes well, your terminal console output will look like following : 
 <details>

<summary>Run Console Output </summary>
<!-- **Run Output:** -->
    ```

    [PM2] Spawning PM2 daemon with pm2_home=/root/.pm2
    [PM2] PM2 Successfully daemonized
    [PM2][WARN] Applications evenobc not running, starting...
    [PM2] App [evenobc] launched (1 instances)
    ┌──────────┬────┬─────────┬──────┬─────┬────────┬─────────┬────────┬─────┬───────────┬──────┬──────────┐
    │ App name │ id │ version │ mode │ pid │ status │ restart │ uptime │ cpu │ mem       │ user │ watching │
    ├──────────┼────┼─────────┼──────┼─────┼────────┼─────────┼────────┼─────┼───────────┼──────┼──────────┤
    │ evenobc │ 0  │ N/A     │ fork │ 60  │ online │ 0       │ 0s     │ 0%  │ 25.4 MB   │ root │ disabled │
    └──────────┴────┴─────────┴──────┴─────┴────────┴─────────┴────────┴─────┴───────────┴──────┴──────────┘
    Use `pm2 show <id|name>` to get more details about an app
    Start the http proxy for dApps...
    [HPM] Proxy created: /  ->  http://127.0.0.1:18191
    [HPM] Proxy created: /  ->  http://127.0.0.1:18190
    [HPM] Proxy created: /  ->  http://127.0.0.1:8060

    obc Quickstart listening on http://127.0.0.1:9090



    ADMIN /admin/accounts-generation
    Sleeping for 1 second...Waiting when nodes are ready to generate 10 accounts...
    (1) Waiting for sync...
    Slept.
    ...
    Loading the accounts and waiting for the node to mine the transactions...
    (1) Waiting for receipts...
    Sending 10000 obc to TSjfWSWcKCrJ1DbgMZSCbSqNK8DsEfqM9p
    Sending 10000 obc to THpWnj3dBQ5FrqW1KMVXXYSbHPtcBKeUJY
    Sending 10000 obc to TWFTHaKdeHWi3oPoaBokyZFfA7q1iiiAAb
    Sending 10000 obc to TFDGQo6f6dm9ikoV4Rc9NyTxMD5NNiSFJD
    Sending 10000 obc to TDZZNigWitFp5aE6j2j8YcycF7DVjtogBu
    Sending 10000 obc to TT8NRMcwdS9P3X9pvPC8JWi3x2zjwxZuhs
    Sending 10000 obc to TBBJw6Bk7w2NSZeqmzfUPnsn6CwDJAXTv8
    Sending 10000 obc to TVcgSLpT97mvoiyv5ChyhQ6hWbjYLWdCVB
    Sending 10000 obc to TYjQd4xrLZQGYMdLJqsTCuXVGapPqUp9ZX
    Sending 10000 obc to THCw6hPZpFcLCWDcsZg3W77rXZ9rJQPncD
    Sleeping for 3 seconds... Slept.
    (2) Waiting for receipts...
    Sleeping for 3 seconds... Slept.
    (3) Waiting for receipts...
    Sleeping for 3 seconds... Slept.
    (4) Waiting for receipts...
    Sleeping for 3 seconds... Slept.
    (5) Waiting for receipts...
    Sleeping for 3 seconds... Slept.
    (6) Waiting for receipts...
    Sleeping for 3 seconds... Slept.
    (7) Waiting for receipts...
    Done.

    Available Accounts
    ==================

    (0) TSjfWSWcKCrJ1DbgMZSCbSqNK8DsEfqM9p (10000 obc)
    (1) THpWnj3dBQ5FrqW1KMVXXYSbHPtcBKeUJY (10000 obc)
    (2) TWFTHaKdeHWi3oPoaBokyZFfA7q1iiiAAb (10000 obc)
    (3) TFDGQo6f6dm9ikoV4Rc9NyTxMD5NNiSFJD (10000 obc)
    (4) TDZZNigWitFp5aE6j2j8YcycF7DVjtogBu (10000 obc)
    (5) TT8NRMcwdS9P3X9pvPC8JWi3x2zjwxZuhs (10000 obc)
    (6) TBBJw6Bk7w2NSZeqmzfUPnsn6CwDJAXTv8 (10000 obc)
    (7) TVcgSLpT97mvoiyv5ChyhQ6hWbjYLWdCVB (10000 obc)
    (8) TYjQd4xrLZQGYMdLJqsTCuXVGapPqUp9ZX (10000 obc)
    (9) THCw6hPZpFcLCWDcsZg3W77rXZ9rJQPncD (10000 obc)

    Private Keys
    ==================

    (0) 2b2bddbeea87cecedcaf51eef55877b65725f709d2c0fcdfea0cb52d80acd52b
    (1) f08759925316dc6344af538ebe3a619aeab836a0c254adca903cc764f87b0ee9
    (2) 1afc9f033cf9c6058db366b78a9f1b9c909b1b83397c9aed795afa05e9017511
    (3) f8f5bc70e91fc177eefea43b68c97b66536ac317a9300639e9d32a9db2f18a1f
    (4) 031015272915917056c117d3cc2a03491a8f22ef450af83f6783efddf7064c59
    (5) 5eb25e2c1144f216aa99bbe2139d84bb6dedfb2c1ed72f3df6684a4c6d2cd96b
    (6) f0b781da23992e6a3f536cb60917c3eb6a9c5434fcf441fcb8d7c58c01d6b70e
    (7) 158f60a4379688a77d4a420e2f2a3e014ebf9ed0a1a093d7dc01ba23ebc5c970
    (8) e9342bb9108f46573804890a5301530c2834dce3703cd51ab77fba6161afec00
    (9) 2e9f0c507d2ea98dc4005a1afb1b743c629f7c145ccb55f38f75ae73cf8f605c

    HD Wallet
    ==================
    Mnemonic:      border pulse twenty cruise grief shy need raw clean possible begin climb
    Base HD Path:  m/44'/60'/0'/0/{account_index}
    ```
</details>
  

### web browser ###
1. open your web browser
2. enter : http://127.0.0.1:9090/
3. there will be a response JSON data: 

```
 {"Welcome to":"obcGrid v2.2.8"}
```

## Docker Commands 
Here are some useful docker commands, which will help you manage the obc Quickstart Docker container on your machine. 

**To list all active containers on your machine, run:**
```shell
docker container ps
```  
**Output:**
```shell
docker container ps

CONTAINER ID        IMAGE               COMMAND                 CREATED             STATUS              PORTS                                              NAMES
513078dc7816        obc                "./quickstart v2.0.0"   About an hour ago   Up About an hour    0.0.0.0:9090->9090/tcp, 0.0.0.0:18190->18190/tcp   obc
```  
**To kill an active container, run:**
```shell
docker container kill 513078dc7816   // use your container ID
```  

### How to check the logs of the FullNode ###
```
  docker exec -it obc tail -f /obc/FullNode/logs/obc.log 
```

 <details>

<summary>Output: something like following </summary>

  ```
  number=204
  parentId=00000000000000cb0985978b3c780e4219dc51e4329beecabe7b71f99d269985
  witness address=41928c9af0651632157ef27a2cf17ca72c575a4d21
  generated by myself=true
  generate time=2019-12-09 18:33:33.0
  txs are empty
  ]
  18:33:33.008 INFO  [Thread-5] [DB](Manager.java:1095) pushBlock block number:204, cost/txs:1/0
  18:33:33.008 INFO  [Thread-5] [witness](WitnessService.java:283) Produce block successfully, blockNumber:204, abSlot[525305471], blockId:00000000000000ccc37f1f5c2ceb574d14c490e3d0b86909855646f9384ba666, transactionSize:0, blockTime:2019-12-09T18:33:33.000Z, parentBlockId:00000000000000cb0985978b3c780e4219dc51e4329beecabe7b71f99d269985
  18:33:33.008 INFO  [Thread-5] [net](AdvService.java:156) Ready to broadcast block Num:204,ID:00000000000000ccc37f1f5c2ceb574d14c490e3d0b86909855646f9384ba666
  ........  etc
  ```
</details>
