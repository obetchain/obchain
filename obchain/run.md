# How to Running

### Running multi-nodes

## Running a local node and connecting to the public testnet 

### Running a Super Representative Node for mainnet

* Use the executable JAR(Recommended way)

```bash
java -jar FullNode.jar -p your private key --witness -c your config.conf(Example：/data/java-obc/config.conf)
Example:
java -jar FullNode.jar -p 650950B193DDDDB35B6E48912DD28F7AB0E7140C1BFDEFD493348F02295BD812 --witness -c /data/java-obc/config.conf



### Running a Super Representative Node for private testnet
* use master branch
* You should modify the config.conf
  1. Replace existing entry in genesis.block.witnesses with your address.
  2. Replace existing entry in seed.node ip.list with your ip list.
  3. The first Super Node start, needSyncCheck should be set false
  4. Set p2pversion to 61 

* Use the executable JAR(Recommended way)

```bash
cd build/libs
java -jar FullNode.jar -p your private key --witness -c your config.conf (Example：/data/java-obc/config.conf)
Example:
java -jar FullNode.jar -p 650950B193DDDDB35B6E48912DD28F7AB0E7140C1BFDEFD493348F02295BD812 --witness -c /data/java-obc/config.conf


## Advanced Configurations

Read the [Advanced Configurations](common/src/main/java/org/obc/core/config/README.md).
