package edu.ucl.mrrr.callbacks;

import java.lang.reflect.Method;

/**
 * Created by jgeyti on 22/12/14.
 */
public class MethodInstance {
    public Method method;
    public Object instance;

    public MethodInstance(Method method, Object instance) {
        this.method = method;
        this.instance = instance;
    }
}
