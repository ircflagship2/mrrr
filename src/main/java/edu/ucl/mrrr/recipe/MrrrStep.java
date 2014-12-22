package edu.ucl.mrrr.recipe;

import edu.ucl.mrrr.callbacks.YamlJobTraversalCallback;
import edu.ucl.mrrr.recipe.exceptions.YamlJobParseException;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jkg on 28/03/2014.
 */
public class MrrrStep implements Serializable {

    private MrrrMethodCall do_;

    private MrrrEmitKeyVal emit;
    private String path = "";

    private MrrrMethodCall condition;
    private List<MrrrStep> then;
    private List<MrrrStep> else_;

    private MrrrFor foreach;
    private List<MrrrStep> forSteps;

    /// do //////////////////////////////////////////////////////////////////
    public MrrrMethodCall getDo() {
        return do_;
    }
    public void setDo(MrrrMethodCall do_) throws YamlJobParseException {
        this.do_ = do_;
    }

    /// emit ////////////////////////////////////////////////////////////////
    public MrrrEmitKeyVal getEmit() {
        return emit;
    }
    public void setEmit(MrrrEmitKeyVal emit) throws YamlJobParseException {
        this.emit = emit;
    }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    /// if-else /////////////////////////////////////////////////////////////
    public MrrrMethodCall getIf() {
        return condition;
    }
    public void setIf(MrrrMethodCall condition) {
        this.condition = condition;
    }

    public List<MrrrStep> getThen() {
        return then;
    }
    public void setThen(List<MrrrStep> then) throws YamlJobParseException {
        this.then = then;
    }

    public List<MrrrStep> getElse() {
        return else_;
    }
    public void setElse(List<MrrrStep> else_) throws YamlJobParseException {
        if ( condition == null )
            throw new YamlJobParseException("No IF found preceding ELSE");
        if ( then == null )
            throw new YamlJobParseException("No THEN found preceding ELSE: " + else_);
        this.else_ = else_;
    }

    /// for-loop ////////////////////////////////////////////////////////////
    public MrrrFor getFor() {
        return foreach;
    }

    public void setFor(MrrrFor foreach) {
        this.foreach = foreach;
    }

    public List<MrrrStep> getSteps() {
        return forSteps;
    }

    public void setSteps(List<MrrrStep> forSteps) throws YamlJobParseException {
        if ( foreach == null )
            throw new YamlJobParseException("No FOR loop found preceding STEPS");
        this.forSteps = forSteps;
    }

    public void traverse(YamlJobTraversalCallback traversalCallback) throws Exception {


    }


}
