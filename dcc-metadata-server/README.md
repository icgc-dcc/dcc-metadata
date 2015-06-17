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

Examples
---

##### Save entity
```bash
curl -k -XPOST https://localhost:8443/entities -H "Content-Type: application/json" -d '{"gnosId":"zzz123","fileName":"1.txt"}'
```

##### Find entities
```bash
curl -k https://localhost:8443/entities?gnosId=zzz123&fileName=1.txt
```

##### Get entity
```bash
curl -k https://localhost:8443/entities/54321
```
