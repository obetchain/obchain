FROM obcprotocol/obc-gradle

RUN set -o errexit -o nounset \
#  Download and build java-obc
    && echo "git clone" \
    && git clone https://github.com/obcprotocol/java-obc.git \
    && cd java-obc \
    && gradle build

# Change work directory
WORKDIR /java-obc

# open port 18888
EXPOSE 18888

