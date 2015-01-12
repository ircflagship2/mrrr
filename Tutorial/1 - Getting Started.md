# Getting Started

In this tutorial, we'll create a jar file with a few utility methods, and an mrrr recipe using those methods.

### 1. Create a utility JAR

Create a new project with the following Java class, and bundle it in a jar file named `helloworld.jar`.

```java
package my.company;
class HelloWorld {
    public String upper(String str) {
        return str.toUpperCase();
    }    
}
```

### Create some input

We need something to process as well. In this example, we'll process a text file line by line. Create a file `helloworld.txt` with the following content:

```text
HELLO,1
World,3
world,2
Hello,10
```

### Create an mrrr recipe

We now want to *use* this jar in a recipe. We'll also use a couple of common helper methods provided by the mrrr jar.
Create a new file `helloworld.yaml` with the following content:

```
jobname: Hello World
output: out/
mappers:
  - input: helloworld.txt
    inputformat: org.apache.hadoop.mapreduce.lib.input.TextInputFormat
    objects:
      convert: new edu.ucl.mrrr.helpers.Converters()
      helloworld: new my.company.HelloWorld()
	map: |
      rawCsv  = value.toString().split(",")
      text    = helloworld.upper(rawcsv[0])
      count   = convert.toInt(rawCsv[1])
      emit(text, count)
reducer:
  key: LongWritable
  value: Text
  objects:
      math: edu.ucl.mrrr.helpers.MathUtil()
  # code to execute for each key,values input
  reduce:
    sum = math.sum(values)
    emit(key, sum)
```

#### Quick introduction to MRRR recipes

Our job has `jobname`, that'll identify it in logs, and on the hadoop job manager webpage. The output
of the job (the output of the reducer) will be written to the path `output`. As we've not explicitely
specified a filesystem (`hdfs://` or `file://`), `file://` will be used when running locally, and `hdfs://`
when running on hadoop.

Mrrr supports multiple mappers, but only one reducer, but in our job, we only have one mapper
(`mappers` is a list with one entry).

Each mapper (*the* mapper in our case) parses an input path. You can use globbing (e.g. `input/*.txt`),
but in this job, we're just parsing a single file `helloworld.txt`.

As in "regular hadoop", input is parsed using an `inputformat`. By default, `TextInputFormat` is used, so
you could actually leave out the `inputformat` property in this job.

Whenever a new mapper instance is started (one is started for each split in the input), the objects in
`objects` are initiated, and will be available to the `map` method.

The `map` code will be executed for each entry in the split, in this case for each line in the input file.
The code is Groovy syntax. You don't really need to know Groovy, as it's "compatible" with Java syntax,
but it does have a couple of [really useful features](http://groovy.codehaus.org/User+Guide).
In our job, we take advantage of the fact that you don't have to explicitly specify the type when defining
a variable.

Take care that you're only emitting key-value pairs of the type expected by the reducer (`key` and `value`).
Other than that, the `reducer` works pretty much as a `mapper`, expect that instead of accessing a variable
`value`, you've got a list of `values` for each `key` - just like in a regyular Java hadoop job.

A handful of methods and variables are available to you in the `map` and `reduce` methods. In `map`, you've got:

- `key`:     The current input's key (as defined by the inputformat).
- `value`:   The current input's value (as defined by the inputformat).
- `path`:    The current input's location on the filesystem.
- `context`: The current hadoop `context`. Be careful when using the when running locally, as it's a mock object
             in that case.
- `emit(Object key, Object value)`: emit a key-value pair to the reducer. "Simple" types (integers, strings, boolean,
  floats, doubles) will be converted to their Hadoop Writable equivalent (`IntWritable`, `Text`, etc)
- `count(String group, String name, Integer value=1)`: Increment a hadoop counter.

In `reduce`, you've got access to:

- `key`:     The current input group key.
- `values`:  The current input's values (as defined by the inputformat).
- `path`:    The current input's location on the filesystem.
- `context`: The current hadoop `context`. Be careful when using the when running locally, as it's a mock object
             in that case.
- `emit(Object key, Object value)`: emit a key-value pair to the reducer.
- `count(String group, String name, Integer value=1)`: Increment a hadoop counter.

### Run locally

Now let's test this locally. First, make sure you've got the following files in your folder:

```
./helloworld.txt
./helloworld.jar
./helloworld.yaml
./mrrr.jar
```

Execute the following to process `helloworld.txt` locally:

```bash
mrrr local helloworld.yaml *.jar
```

The first argument to `mrrr` specifies whether to run the job locally (`local`) or on hadoop (`hadoop`). The second is
the path to our recipe, and all subsequent arguments are to jar files to include (globbing allowed). In our case, we
simply want all jars in our folder, as that includes both the mrrr jar, and the helloworld jar we're using in our
recipe.

When it's done, a new folder named `out/` (as specified in the recipe) should have been created.
In it, you'll find a `part-r-00000` file with the following content:

```
hello,11
world,5
```

If so, congratulations, you've just successfully executed your first mrrr job.

You don't have to use the `mrrr` script to start jobs. If you want to see the actual command that was executed,
use `echo` as the first argument to mrrr, i.e run `mrrr echo local helloworld.yaml *.jar`.

### Run on hadoop

Running the job on a hadoop cluster isn't much more advanced. Make sure you're logged on to a cluster where the
`hadoop` command is on your `PATH`, and that the input file(s) are available on hdfs. Then execute mrrr:

```bash
mrrr hadoop helloworld.yaml *.jar
```

Again, if you just want to see what command `mrrr` executes to start the job, use the following command. That might
be useful if you need to tweak it to your cluster.

```bash
mrrr echo hadoop helloworld.yaml *.jar
```

