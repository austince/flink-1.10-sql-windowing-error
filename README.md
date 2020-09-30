# SQL Windowing Issue

This is a small example for reproducing an error where a Flink 1.10 Streaming SQL job shuts down before sinking
all the data.

The general structure of the job is:

```
data/descriptions.csv  -->
                            LEFT OUTER JOIN ON uuid --> toRetractStream --> time windowed de-dupe w/ custom trigger --> print sinks
data/names.csv         -->
```

## Running the example

The main `org.example.StreamingJob` just takes one arg, `--base-path`, with the path to the directory holding the csv
files. Default is `$(pwd)/data`.

Can either be run through an IDE or the maven exec plugin like so:

```
mvn exec:java -Dexec.args="--base-path data"
```

## Interpreting Results

There should be two print sinks, `results` and `deduped-results`, with 4 and 2 records output, respectively.

Expected results:
```
results:4> org.example.records.Result@61586900[uuid=dcecad9e-22a5-4bdd-92fd-c2170c40937c,name=Pooh Bear,description=<null>]
results:12> org.example.records.Result@4aff4696[uuid=6c38dde6-1296-4a4e-ba1a-4d795636fe9d,name=Eeyor,description=<null>]
results:12> org.example.records.Result@2eaf2947[uuid=6c38dde6-1296-4a4e-ba1a-4d795636fe9d,name=Eeyor,description=A grumpy gus]
results:4> org.example.records.Result@2da7e8f7[uuid=dcecad9e-22a5-4bdd-92fd-c2170c40937c,name=Pooh Bear,description=A fun guy]
deduped-results:7> org.example.records.Result@23046d78[uuid=dcecad9e-22a5-4bdd-92fd-c2170c40937c,name=Pooh Bear,description=A fun guy]
deduped-results:3> org.example.records.Result@6e2837b8[uuid=6c38dde6-1296-4a4e-ba1a-4d795636fe9d,name=Eeyor,description=A grumpy gus]
```

Sometimes the results are only partial, like:
```
results:4> org.example.records.Result@77625560[uuid=dcecad9e-22a5-4bdd-92fd-c2170c40937c,name=Pooh Bear,description=<null>]
results:12> org.example.records.Result@684d1722[uuid=6c38dde6-1296-4a4e-ba1a-4d795636fe9d,name=Eeyor,description=A grumpy gus]
results:4> org.example.records.Result@73077020[uuid=dcecad9e-22a5-4bdd-92fd-c2170c40937c,name=Pooh Bear,description=A fun guy]
deduped-results:7> org.example.records.Result@5517540d[uuid=dcecad9e-22a5-4bdd-92fd-c2170c40937c,name=Pooh Bear,description=A fun guy]
```
