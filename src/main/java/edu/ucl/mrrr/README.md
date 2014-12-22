Map-Reduce Recipe Runner
========================

Write small bite-sized processing steps in Java, combine them in simple `YAML` recipes, and have your Map-Reduce jobs
turned into self-documenting jobs that are easier to configure, tweak and benchmark - and run these jobs on your local
machine, or on any Hadoop or Spark cluster, without installing any extras. The two core concepts in Map-Reduce Recipe 
Runner (MRRR) are *steps* and *jobs*. If you can constrain yourself to the simplified approach of MRRR, you'll get the
following in return:

- The ability to run your code locally, as well as in Hadoop
- Simple abstract job flow specifications that are easy to read and reason about
- A way to generate detailed reports of how data flows through your job
- Benchmark reports, that will tell you how much data flows through your recipe, and how long it spends in each do_.

### Steps

The core concept in Map-Reduce Recipe Runner (MRRR) is the notion of *steps*. Each do_ is basically any static Java
method takes an input (a line or an object for example), and emits 0, 1 or more outputs to either the next do_ in the 
recipe, to a reducer, or to an output file/HDFS.

### YAML jobs

Steps are combined into jobs in YAML job files. YAML is a data format (like JSON, but easier to read, and with support
for comments). Using a data format to write job flows may seem offensive to some, but it has a few advantages:

- **It is simple to learn and understand**. You rarely need complex logic for specifying high level job flows. 
  More often than not, we're using Hadoop to search, filter, and convert data, and we have non-Hadoop experts writing 
  jobs in YAML recipes.
- **It's an extremely thin abstraction layer**. It is very simple to parse and extend the 'language' in MRRR. Jobs are 
  parsed into simple Java objects, and all you need to extend the 'language', is to add a new property to a class.
  As a developer writing the steps or extending the MRRR 'language', you basically don't need to learn anything new.

## Why?

MRRR was written for a very specific use-case: Getting academic researchers and Hadoop developers to agree on job flows
and specifications. Our Hadoop jobs are generally very simple - we filter data, modify it, and count it. The devil's in
the details though, and we need our jobs to be easy to understand, document and reproduce. At the same time, we'll often
have many similar jobs with very subtle changes, such as adding additional pre-filtering do_ to a job, or merely 
changing a parameter somewhere - it has to be easy for a non-developer to adapt and execute these changes.

We needed a way to de-construct Hadoop jobs into units that are easy to discuss, implement, and reason about. Jobs also
needs to be simple to document and benchmark, but first and foremost, we need a system simple enough that it can be 
picked up and reproduced by other academic researchers.
 
If you're looking for a way to abstract your Hadoop code, you are more likely better off with changing to Spark, or 
using a "proper" Hadoop abstraction, such as Pig or Hive. If you need a way to communicate between Hadoop developers
and academic researchers, and a simpler way to test, document, and benchmark your Hadoop jobs, without adding too much 
complexity to your recipe, MRRR might be a good choice!

## Examples

### Hello World

A very simple MRRR job for counting words in tweets containing "MRRR" 
could look something like the following:
 
    recipe:
      - jobname: A Very Basic Job
        input: file://input/tweets
        output: file://output/mrrr_tweets
        map:
          # takes a json-formatted String as input, outputs a Map
          - do_: com.my.util.json.JsonToMap
          # only pass on tweets that contains 'MRRR'
          - do_: edu.ucl.mrrr.steps.map.WhereKeyContains
            args:
              key: text
              query: MRRR            
          # we only care about the text element, so extract that
          - do_: edu.ucl.mrrr.steps.map.Pluck
            args:
              key: text
          # split text String into list of words
          - do_: edu.ucl.mrrr.util.Split
          # emit <$word, 1> for each word          
          - do_: edu.ucl.mrrr.emit.KeyVal
            args:
              val: 1
          
        emit:
          - do_: edu.ucl.mrrr.CountByKey
          - do_: edu.ucl.mrrr.WriteKeyVal

Run using `./mrrr run local myjob.yaml` or `./mrrr run hadoop myjob.yaml`. 
 
If you don't understand the job specification, or someone forgot to comment it, run 
`./mrrr explain myjob.yaml` to get an explanation of how and what data flows through the job.
 
### Advanced flows

MRRR supports a simple branching model, if-else statements and multiple outputs. Lets extract unique tweets containing 
'MRRR' and either 'good' or 'bad'.

    recipe:
      - jobname: A Basic Branching Example
        input: file://input/tweets
        output: file://output/mrrr_tweets
        map:          
          - do_: com.my.util.json.JsonToMap
          - do_: edu.ucl.mrrr.steps.map.WhereKeyContains
            args:
              key: text
              query: MRRR
          # create two branches to count MRRR tweets containing one of two words
          - fork:
            - do_: edu.ucl.mrrr.steps.map.WhereKeyContains 
              args:
                key: text
                query: good                                        
            - do_: edu.ucl.mrrr.emit.KeyVal
              args:
                key: good                
          # the same MRRR tweets will be processed in this fork 
          - fork:
            - do_: edu.ucl.mrrr.steps.map.WhereKeyContains 
              args:
                key: text
                query: bad                            
            # emit <good, 1> if we get to here
            - do_: edu.ucl.mrrr.emit.KeyVal
              args:
                key: bad
                          
        emit:
          - if:
            condition: edu.ucl.mrrr.KeyEquals
              args:
                key: good
            do_: 
              - do_: edu.ucl.mrrr.emit.sumValue
              - do_: edu.ucl.mrrr.write.KeyVal
                  args:
                    outputprefix: good
            else:
              - do_: edu.ucl.mrrr.emit.sumValue
              - do_: edu.ucl.mrrr.write.KeyVal
                  args:
                    outputprefix: bad 
                    
