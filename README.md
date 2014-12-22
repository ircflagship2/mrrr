
# MRRR: Map Reduce Recipe Runner

MRRR is a tool for stringing together map-reduce workflows, based entirely on simple Java methods. The best way to get introduced to MRRR, is through an example. Given a user-provided Jar with the following class, you can process data using the following MRRR workflow:

### Class in mylib.jar

The goal of MRRR is to make it easy to string together regular Java methods, and run them either locally, or on a Hadoop cluster. The following is an example of a user-provided class (in a Jar) that exposes public methods we want to use in our MRRR workflow:

	package com.my.lib
	import com.some.PersonParser
	Class MyTool {		
		public Json hadoopInputToPerson(Text val) { return PersonParser.parse(val); }
		public String getAge(Person person)       { return person.age; }
	}

### MRRR workflow file

A valid workflow file contains various configuration parameter, but the interesting stuff goes on in the "mappers" and "reducer" elements. 

MRRR accepts multiple mappers, that will each parse a dataset (line by line as TextInputFormat by default), and *emit* key-value pairs to reducers - just like the regular Hadoop Java API. All emitted key-value pairs must be of the same type - even across different mappers.

Unlike mappers, you can only have a single type of reducer. Reducers will receive key-value pairs (hash-partioned by key) from mappers.

	jobname: old_yaml_test_2014_uk
	output: hdfs://some/output/folder
	mappers:
	  - input: some/input/folder/*.gz
	    # instantiate objects, making them usable in steps
	    objects:
	      mylib: com.my.lib.MyTool() # package com.my.lib, class MyTool, empty constructor
	    steps:
	      # variables available before first step: path, key, value
		  - do: mylib.hadoopInputToPerson(value) -> person
		  - do: mylib.getAge(person)             -> age
		  - emit: age, 1
	reducer:
  	  objects:	
  	    sumred: uk.ac.ucl.mrrr.reducers.CommonReducers()
  	  steps:
      # variables available before first step: key, values
  	    - do: sumred.sum(values) -> sum
  	    - emit: key, sum

### Running MRRR workflows

MRRR is a very thin wrapper over the hadoop Java API, and runs on a vanilla hadoop cluster, using the `hadoop jar` command. With that said, the recommended way to use MRRR, is the command line interface, that eases the process somewhat, and makes it much easier to switch between running jobs in local mode, and distributed (hadoop) mode:

MRRR requires a yaml recipe, and a bunch of jars that are required, to run your job. To run MRRR in local and distributed mode respectively, use either 

`mrrr hadoop my_workflow.yaml bundle/*.jar` or  
`mrrr local my_workflow.yaml bundle/*.jar`
    
## Documentation (move to wiki)

The following documents the possible elements you can use in your MRRR recipe.



### Root Level

 - **`jobname:`** Jobname, as it'll show up in the Hadoop scheduler. Any String allowed.  
   **Example:**
    - `My Amazing Job Name`
 - **`output`**: Job output directory. Prefix with either `hdfs://` or `file://` to force file system type. If no prefix is used, the file system will be assumed to be `hdfs://` when running in hadoop mode, and `file://` in local mode.  
   **Examples:** 
    - `output: file:///home/user/folder/files*.csv`
    - `output: hdfs://subfolder/files*.csv`
    - `output: subfolder/files*.csv`
 - **`conf:`** "Global" variables, that'll be copied to all mappers and reducers. Use like regular variables. Integer, doubles, strings and booleans are supported.
   **Example:**
    -  ```
       conf:  
           a: 123.9
           b: My long string
           c: true
          ```
       Variables are available to steps in both mappers and reducers, e.g.:

       ```
       do: myobj.somemethod(a,b,c) -> d
       ```
 - **`mappers:`** List of mappers. See *mapper*.  
   **Example:**
    - ```
      mappers:
        - input: file://my_files/*
          ... (see mapper documentation)
        - input: hdfs://my_other_files/*
          ... (see mapper documentation)
      ```
 - **`reducer:`**: The reducer. See *reducer*.  
   **Example:**
   - ```
     reducer: 
         ... (see reducer)
     ```

