package edu.ucl.mrrr.callbacks;

import edu.ucl.mrrr.recipe.*;
import net.jodah.typetools.TypeResolver;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.InputFormat;

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by jgeyti on 17/12/14.
 */
public class MethodCachingValidateCallback implements YamlJobMapperCallback, YamlJobReducerCallback {

    public Map<String, Class> globalVariables = new HashMap<String, Class>();
    public Map<String, Type> variables = new HashMap<String, Type>();

    public Map<String, Object> objects = new HashMap<String, Object>();
    public HashMap<Integer, MethodInstance> methods = new HashMap<Integer, MethodInstance>();

    public Class mapEmitKeyType;
    public Class mapEmitValType;
    public Class emitKeyType;
    public Class emitValType;

    public List<MapperMetaData> mapperMetaData = new ArrayList<MapperMetaData>();
    private MapperMetaData currentMapperMetaData;
    public ReducerMetaData reduceMetaData;

    public boolean firstMap = true;
    public boolean firstReduce = true;
    ;

    @Override
    public void preMappers(Recipe job) {
        // Store global variables, so we can add them to each mapper's variables
        Set<Map.Entry<String, Object>> objects = job.getConf().entrySet();
        for (Map.Entry<String, Object> obj : objects) {
            globalVariables.put(obj.getKey(), obj.getValue().getClass());
        }
    }

    @Override
    public void postMappers(Recipe job) {

    }

    @Override
    public void preMapper(MapperRecipe mapper) {

        try {
            // reset variables
            resetVariables();

            // resolve the input key and value types.
            setInputKeyVal(mapper.getInputformat());

            // set input path type, so it can be references correctly
            variables.put("path", String.class);

            // Create object instances
            this.objects = createObjects(mapper.getObjects());

            // Store information about this mapper, so it can be used outside of this class
            MapperMetaData metaData = new MapperMetaData();
            this.currentMapperMetaData = metaData;
            metaData.inputPath = mapper.getInput();
            metaData.inputFormat = (Class<InputFormat<?, ?>>) Class.forName(mapper.getInputformat());
            this.mapperMetaData.add(metaData);

        } catch (IllegalArgumentException e) {

            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // todo: better error handling
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> createObjects(Map<String, MrrrObject> objects) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Map<String, Object> generatedObjects = new HashMap<String, Object>();
        for (Map.Entry<String, MrrrObject> objectEntry : objects.entrySet()) {
            String objPkgName = objectEntry.getValue().getPackageString();
            String objClsName = objectEntry.getValue().getClassName();
            List<MrrrArgument> objArgs = objectEntry.getValue().getArguments();
            List<Type> argumentTypes = new ArrayList<Type>();
            List<Object> arguments = new ArrayList<Object>();
            for (MrrrArgument arg : objArgs) {
                if (arg.getType() == MrrrArgument.Type.VARIABLE) {
                    argumentTypes.add(variables.get(arg.getValue()));
                    // at this point, only global variables could be available
                    // so luckily we can get actual objects from there
                    arguments.add(globalVariables.get(arg.getValue()));
                } else {
                    argumentTypes.add(arg.getValue().getClass());
                    arguments.add(arg.getValue());
                }

            }

            Class cls = Class.forName(objPkgName + "." + objClsName);
            Constructor constructor = cls.getConstructor(argumentTypes.toArray(new Class[argumentTypes.size()]));

            Object o = constructor.newInstance(arguments.toArray());
            generatedObjects.put(objectEntry.getKey(), o);
        }

        return generatedObjects;
    }

    private void setInputKeyVal(String stepInputFormat) throws ClassNotFoundException {
        Class<InputFormat<?,?>> inputFormatClass = (Class<InputFormat<?, ?>>) Class.forName(stepInputFormat);
        // [ KeyClass,ValueClass ]
        Class<?>[] keyValTypes = TypeResolver.resolveRawArguments(InputFormat.class, inputFormatClass);

        variables.put("key", keyValTypes[0]);
        variables.put("value", keyValTypes[1]);
    }

    private void resetVariables() {
        variables.clear();
        for (Map.Entry<String, Class> keyVal : globalVariables.entrySet()) {
            variables.put(keyVal.getKey(), keyVal.getValue());
        }
    }

    @Override
    public void postMapper(MapperRecipe mapStep) {
        currentMapperMetaData.emitKeyType = this.emitKeyType;
        currentMapperMetaData.emitValType = this.emitValType;
    }

    @Override
    public void preReduce(Recipe recipe) {
        // Clear out all objects from the mappers
        try {
            this.objects = createObjects(recipe.getReducer().getObjects());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        // set keys and value types
        resetVariables();
        if (emitKeyType != null) {
            variables.put("key", emitKeyType);
            List<Class> dummyValue = new ArrayList<Class>();
            variables.put("values", Iterable.class); // todo: how do I specify generic type?
        }


        // then reset emit key and value types, as the reducer output type is not dependent on the mapper output
        // types

        emitKeyType = null;
        emitValType = null;
    }

    @Override
    public void postReduce(Recipe recipe) {
        this.reduceMetaData = new ReducerMetaData();
        reduceMetaData.emitKeyType = this.emitKeyType;
        reduceMetaData.emitValType = this.emitValType;
    }

    @Override
    public void preIf(MrrrMethodCall yamlObject, int hashCode) {
        // cache method if not exist
        if (!methods.containsKey(hashCode)) {
            try {
                // Get instance to find method on
                Object instance = objects.get(yamlObject.getObject());

                // Create callable method via reflection

                // Method method = instance.getClass().getMethod(yamlObject.getMethod(),
                // resolveArgumentTypes(yamlObject.getArguments()));


                Class[] arguments = resolveArgumentTypes(yamlObject.getArguments());
                Method method = MethodUtils.getAccessibleMethod(instance.getClass(),
                        yamlObject.getMethod(), arguments);

                // todo: better error handling
                if (method == null)
                    throw new NoSuchMethodException(instance.getClass() + "." + yamlObject.getMethod() + " - " + arguments);

                // Cache it
                methods.put(hashCode, new MethodInstance(method, instance));

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public Boolean evaluateIf(MrrrMethodCall anIf, int hashCode) {
        // get the method
        MethodInstance methodPair = methods.get(hashCode);

        // while validating, we don't want to actually invoke anything,
        // but instead just check that the return type of the requested
        // method is boolean.

        Class returnType = methodPair.method.getReturnType();
        if (returnType.equals(Boolean.TYPE) || returnType.equals(boolean.class)) {
            return null; // todo: don't use null as a special var, use an enum instead
        } else {
            System.err.println("Not an IF"); //todo: exception handling
            //throw new Exception("Not an if");
            return null;
        }
    }

    @Override
    public void preThen(List<MrrrStep> then, Boolean condition, int hashCode) {

    }

    @Override
    public void postThen(List<MrrrStep> then, Boolean condition, int hashCode) {

    }

    @Override
    public void preElse(List<MrrrStep> anElse, Boolean condition, int hashCode) {

    }

    @Override
    public void postElse(List<MrrrStep> anElse, Boolean condition, int hashCode) {

    }

    @Override
    public void postIf(MrrrMethodCall anIf, int hashCode) {

    }

    @Override
    public void preFor(MrrrFor foreach, int hashCode) {
        // do not allow using existing variable names
        if (variables.containsKey(foreach.as)) {
            System.err.println("FOR VARIABLE ALREADY EXISTS"); // todo: exception
        }
    }

    @Override
    public Iterable getForIterable(MrrrFor foreach, int hashCode) {
        Type variable = variables.get(foreach.collection);
        Class collectionType = null;
        //if (variable instanceof ParameterizedTypeImpl)

        //= (Class);

        // check that the requested variable is an iterable

        // todo: reintroduce check
//        if (!Iterable.class.isAssignableFrom((Class)variable)) {
//            System.err.println("Variable is not iterable"); // todo: exception
//        }


        Class containedType = null;
        if ( variable instanceof ParameterizedType) {
            // todo: this works for simple generic containers, such as List<containedType>, but is that enough?
            containedType = (Class)((ParameterizedType) variable).getActualTypeArguments()[0];
        }
        else if (((Class)variable).isArray()) {
            containedType = ((Class)(variable)).getComponentType();
        }

        // We don't actually work with "real" objects during validation,
        // but we'll need an Iterable to dive into. Return a dummy Iterable
        // with the correct type instead, so we can check it

        List<Class> dummyIter = new ArrayList();
        dummyIter.add(containedType);

        return dummyIter;
    }

    @Override
    public void setForValue(String variable, Object obj) {
        variables.put(variable, (Class)obj);
    }

    @Override
    public void postFor(MrrrFor foreach, int hashCode) {
        // clear the for variable so it's not available outside the for loop
        variables.remove(foreach.as);
    }

    @Override
    public void emit(MrrrEmitKeyVal emit, int hashCode) {
        MrrrArgument keyArg = emit.getKey();
        MrrrArgument valueArg = emit.getValue();

        Class key;
        Class val;

        if (keyArg.getType() == MrrrArgument.Type.VARIABLE)
            key = (Class)variables.get(keyArg.getValue());
        else
            key = keyArg.getValue().getClass();

        if (valueArg.getType() == MrrrArgument.Type.VARIABLE)
            val = (Class)variables.get(valueArg.getValue());
        else
            val = valueArg.getValue().getClass();

        Class writableKey = simpleTypeToWritable(key);
        Class writableVal = simpleTypeToWritable(val);

        if (writableKey == null) {
            System.err.println("Not a valid key");
        } else if (writableVal == null) {
            System.err.println("Not a valid value");
        }

        if (!Writable.class.isAssignableFrom(writableKey)) {
            System.err.println("Key " + key + " not a Writable. Cannot emit.");
        }
        if (!Writable.class.isAssignableFrom(writableVal)) {
            System.err.println("Value " + val + " not a Writable. Cannot emit.");
        }

        // store the output type if this is the first time we output something
        if (emitKeyType == null) {
            emitKeyType = writableKey;
        }
        if (emitValType == null) {
            emitValType = writableVal;
        }

        if (!writableKey.equals(emitKeyType)) {
            System.err.println("Only one type of key can be emitted.");
        }
        if (!writableVal.equals(emitValType)) {
            System.err.println("Only one type of key can be emitted.");
        }

    }

    @Override
    public void call(MrrrMethodCall do_, int hashCode) {
        // cache method if not exist
        if (!methods.containsKey(hashCode)) {
            try {
                // Get instance to find method on
                Object instance = objects.get(do_.getObject());

                // Create callable method via reflection
//                Method method = instance.getClass().getMethod(do_.getMethod(),
//                        resolveArgumentTypes(do_.getArguments()));

                Class[] arguments = resolveArgumentTypes(do_.getArguments());
                Method method = MethodUtils.getAccessibleMethod(instance.getClass(),
                        do_.getMethod(), arguments);

                // todo: better error handling
                if (method == null)
                    throw new NoSuchMethodException(instance.getClass() + "." + do_.getMethod() + " - " + StringUtils.join(arguments, ","));

                // Cache it
                methods.put(hashCode, new MethodInstance(method, instance));

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        // Get method and return type.
        MethodInstance methodPair = methods.get(hashCode);
        Type returnType = methodPair.method.getGenericReturnType();

        // "Remember" what sort of variable this method stores
        variables.put(do_.getReturnVariable(), returnType);
    }

    @Override
    public boolean mapHasNext() {
        // Run mapper steps once
        if (firstMap) {
            firstMap = false;
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean reducerHasNext() {
        // Run reducer steps once
        if (firstReduce) {
            firstReduce = false;
            return true;
        } else {
            return false;
        }
    }

    //
    private Class[] resolveArgumentTypes(Iterable<MrrrArgument> yamlArguments) {
        List<Class> argTypes = new ArrayList<Class>();
        for (MrrrArgument yamlArg : yamlArguments) {
            if (yamlArg.getType() == MrrrArgument.Type.VARIABLE) {
                Type t = variables.get((String) yamlArg.getValue());
                if ( t instanceof ParameterizedType) {
                    // todo: this works for simple generic containers, such as List<containedType>, but is that enough?
                    Class c = (Class)((ParameterizedType) t).getRawType();
                    argTypes.add(c);
                }
                else {
                    argTypes.add((Class)t);
                }



            } else {
                argTypes.add(yamlArg.getValue().getClass());
            }
        }
        return argTypes.toArray(new Class[argTypes.size()]);
    }

    public Class<? extends Writable> simpleTypeToWritable(Class in) {
        if (in.equals(String.class)) {
            return Text.class;
        } else if (in .equals( Integer.class)) {
            return IntWritable.class;
        } else if (in .equals( Float.class)) {
            return FloatWritable.class;
        } else if (in .equals( Double.class)) {
            return DoubleWritable.class;
        } else if (in .equals( Boolean.class)) {
            return BooleanWritable.class;
        }
        return in;
    }

}
