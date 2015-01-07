package edu.ucl.mrrr.callbacks;

import edu.ucl.mrrr.recipe.MrrrArgument;
import edu.ucl.mrrr.recipe.MrrrFor;
import edu.ucl.mrrr.recipe.MrrrMethodCall;
import edu.ucl.mrrr.recipe.MrrrStep;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jgeyti on 04/01/15.
 */
public abstract class CommonRunnerCallback implements YamlJobCommonCallback {

    public Map<String, Object> variables = new HashMap<String, Object>();
    public HashMap<Integer, MethodInstance> methods;

    // If Step
    public void preIf(MrrrMethodCall anIf, int hashCode) {}
    public Boolean evaluateIf(MrrrMethodCall anIf, int hashCode) {
        // Get method and instance to call it on
        MethodInstance methodPair = methods.get(hashCode);
        Object instance = methodPair.instance;
        Method method = methodPair.method;

        // Get input
        Object[] arguments = resolveArguments(anIf.getArguments());

        try {
            return (Boolean)method.invoke(instance, arguments);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        System.err.println("No boolean returned"); //todo: err handling
        return false;
    }

    public void preThen(List<MrrrStep> then, Boolean condition, int hashCode) {}
    public void postThen(List<MrrrStep> then, Boolean condition, int hashCode) {}
    public void preElse(List<MrrrStep> anElse, Boolean condition, int hashCode) {}
    public void postElse(List<MrrrStep> anElse, Boolean condition, int hashCode) {}
    public void postIf(MrrrMethodCall anIf, int hashCode) {}

    // Foreach Step
    public void preFor(MrrrFor foreach, int hashCode) {}
    public Iterable getForIterable(MrrrFor foreach, int hashCode) {
        Object iter = variables.get(foreach.collection);
        return (Iterable)iter;
    }

    public void setForValue(String variable, Object obj) {
        variables.put(variable, obj);
    }
    public void postFor(MrrrFor foreach, int hashCode) {
        variables.remove(foreach.as);
    }

    // Regular Step
    @Override
    public void call(MrrrMethodCall do_, int hashCode) {
        // Get method and instance to call it on
        MethodInstance methodPair = methods.get(hashCode);
        Object instance = methodPair.instance;
        Method method = methodPair.method;

        // Get input
        Object[] arguments = resolveArguments(do_.getArguments());

        try {
            Object obj = method.invoke(instance, arguments);
            String retVar = do_.getReturnVariable();
            if (retVar != null) {
                variables.put(retVar, obj);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            System.err.println("InvocationException: " + method);
            e.printStackTrace();
        }
    }

    protected Object[] resolveArguments(Iterable<MrrrArgument> mrrrArguments) {
        List<Object> args = new ArrayList<Object>();
        for (MrrrArgument yamlArg : mrrrArguments) {
            args.add(resolveArgument(yamlArg));
        }
        return args.toArray(new Object[args.size()]);
    }

    protected Object resolveArgument(MrrrArgument yamlArg) {
        if (yamlArg.getType() == MrrrArgument.Type.VARIABLE) {
            return variables.get((String) yamlArg.getValue());
        } else {
            return yamlArg.getValue();
        }
    }

}
