package edu.ucl.mrrr.recipe;

/**
 * Created by jgeyti on 17/12/14.
 */
public class MrrrFor {
    public String collection;
    public String as;

    public MrrrFor(String input) {
        String[] elems = input.split("->");
        collection = elems[0].trim();
        as = elems[1].trim();
    }
}
