ICGC DCC - Metadata Client
===

Metadata client module which communicates with the metadata server

Build
---

From the command line:

	mvn package

Run
---

In Eclipse
```--spring.config.location=src/main/conf/ -i <input_dir> -o <output_dir> [-m <manifest_file>]```

or run from the tarball
```./bin/dcc-metadata-client -i <input_dir> -o <output_dir> [-m <manifest_file>]```

Testing input directory: `src/test/resources/fixtures/70b07570-0571-11e5-a6c0-1697f925ec7b`
