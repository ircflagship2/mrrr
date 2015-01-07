package edu.ucl.mrrr.callbacks;

import edu.ucl.mrrr.recipe.*;

import java.util.List;

/**
 * Created by jkg on 16/09/2014.
 */
public interface YamlJobCommonCallback {

    // If Step
    void preIf(MrrrMethodCall anIf, int hashCode);
    Boolean evaluateIf(MrrrMethodCall anIf, int hashCode);

    void preThen(List<MrrrStep> then, Boolean condition, int hashCode);
    void postThen(List<MrrrStep> then, Boolean condition, int hashCode);
    void preElse(List<MrrrStep> anElse, Boolean condition, int hashCode);
    void postElse(List<MrrrStep> anElse, Boolean condition, int hashCode);
    void postIf(MrrrMethodCall anIf, int hashCode);

    // Foreach Step
    void preFor(MrrrFor foreach, int hashCode);
    Iterable getForIterable(MrrrFor foreach, int hashCode);
    void setForValue(String variable, Object obj);
    void postFor(MrrrFor foreach, int hashCode);

    // Regular Step
    void call(MrrrMethodCall do_, int hashCode);

    void emit(MrrrEmitKeyVal emit, int hashCode) throws Exception;

}
