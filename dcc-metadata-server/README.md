ICGC DCC - Metadata Server
===

Entry point to the metadata server.

Build
---

From the command line:

`cd dcc-metadata`

`mvn clean package -DskipTests -am -pl :dcc-metadata-server`

Run
---

From the command line:

`java -jar dcc-metadata-server-[VERSION].jar --spring.profiles.active=[development|production]`
