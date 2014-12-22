package edu.ucl.mrrr.recipe;

/**
 * Created by jgeyti on 16/12/14.
 */
public class MrrrArgument {

    public enum Type { VARIABLE, STRING, INTEGER, DOUBLE, BOOLEAN }
    private Type type;
    private Object value;

    public MrrrArgument(String arg) {

        String esc = "<<.<!!~~ESCAPED_QUOTE~~!!>.>>";
        arg = arg.replace("\\\"", esc);

        if (arg.startsWith("\"") && arg.endsWith("\"")) {
            this.value = arg.substring(1, arg.length()-1).replace(esc, "\"");
            this.type = Type.STRING;
        } else if (arg.equals("true")) {
            this.value = true;
            this.type = Type.BOOLEAN;
        } else if (arg.equals("false")) {
            this.value = false;
            this.type = Type.BOOLEAN;
        } else if (isInteger(arg.replace(esc, "\""))) {
            this.value = Integer.parseInt(arg);
            this.type = Type.INTEGER;
        } else if (isDouble(arg.replace(esc, "\""))) {
            this.value = Double.parseDouble(arg);
            this.type = Type.DOUBLE;
        } else {
            this.value = arg.replace(esc, "\"");
            this.type = Type.VARIABLE;
        }

    }

    private boolean isDouble(String str)
    {
        try
        {
            Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    private boolean isInteger(String str)
    {
        try
        {
            Integer.parseInt(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
