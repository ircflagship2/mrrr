package edu.ucl.mrrr.recipe;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jgeyti on 14/12/14.
 */
public class MrrrMethodCall {

    private String[] packagePart;
    private String packagePartString;
    private String methodPart;
    private List<MrrrArgument> arguments;
    private String returnVariable;

    public MrrrMethodCall(String classAndArgs) {
        String classPartString = classAndArgs.substring(0, classAndArgs.indexOf("("));
        List<String> classParts = Arrays.asList(classPartString.split("\\."));

        packagePart = classParts.subList(0,classParts.size()-1).toArray(new String[classParts.size() - 1]);
        methodPart = classParts.get(classParts.size()-1);
        packagePartString = StringUtils.join(packagePart, ".");

        String replace = "<<.<!!~~ESCAPED_COMMA~~!!>.>>";
        String args = classAndArgs.substring(classAndArgs.indexOf("(")+1, classAndArgs.lastIndexOf(")"))
                .replace("\\,", replace);

        List<MrrrArgument> arguments = new ArrayList<MrrrArgument>();
        for (String arg: args.split(",")) {
            arg = arg.replace(replace, ",").trim();
            if (arg.length() > 0) {
                MrrrArgument mrrrArgument = new MrrrArgument(arg);
                arguments.add(mrrrArgument);
            }
        }
        this.arguments = arguments;

        // get return variable name
        returnVariable = classAndArgs.substring(classAndArgs.indexOf("->") + 2).trim();
    }

    public String getObject() {
        return packagePartString;
    }

    public String getMethod() {
        return methodPart;
    }

    public List<MrrrArgument> getArguments() {
        return arguments;
    }

    public String getReturnVariable() {
        return returnVariable;
    }
}
