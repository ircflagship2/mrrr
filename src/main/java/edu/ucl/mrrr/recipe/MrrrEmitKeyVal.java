package edu.ucl.mrrr.recipe;

/**
 * Created by jgeyti on 17/12/14.
 */
public class MrrrEmitKeyVal {
    private MrrrArgument key;
    private MrrrArgument value;

    public MrrrEmitKeyVal(String input) {
        String esc = "<<.<!!~~ESCAPED_COMMA~~!!>.>>";
        String[] split = input.replace("\\,", esc).split(",");
        key = new MrrrArgument(split[0].replace(esc, ",").trim());
        value = new MrrrArgument(split[1].replace(esc, ",").trim());
    }

    public MrrrArgument getKey() {
        return key;
    }

    public MrrrArgument getValue() {
        return value;
    }
}
