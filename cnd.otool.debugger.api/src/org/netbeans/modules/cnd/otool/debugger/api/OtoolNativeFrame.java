/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.otool.debugger.api;

/**
 *
 * @author root
 */
abstract public class OtoolNativeFrame {
    protected boolean range_of_hidden;
    protected boolean current;
    protected String func;
    protected String loadobj;
    protected String loadobj_base;
    protected String args;
    protected String source;
    protected String lineno;
    protected String frameno;
    protected String pc;
    protected boolean optimized;
    protected boolean attr_user_call;
    protected int attr_sig;
    protected String attr_signame;
    public boolean more;
    private String signal = null; 
      protected String fullname;
//    private int level;
//    private String function;
//    private String args;
//    private String file;
//    private int line;
    protected final OtoolNativeDebugger debugger;
    protected OtoolNativeThread thread;
    
    protected OtoolNativeFrame(OtoolNativeDebugger debugger, OtoolNativeThread thread) {
        this.debugger = debugger;
        this.thread = thread;
    }

//    protected OtoolNativeFrame(int level, String function, String args, String file, int line) {
//        this.level = level;
//        this.function = function;
//        this.args = args.replaceAll("[{\"]", "").replaceAll("name=", "").replaceAll(",value=", "(").replaceAll("}", ")");
//        this.file = file;
//        this.line = line;
//    }

    @Override
    public String toString() {
        return func + args;
    }

    public String getFile() {
        return fullname;
    }

    public int getLine() {
        return Integer.parseInt(lineno);
    }

    public int getLevel() {
        return Integer.parseInt(frameno);
    }
    
     public void setCurrent (boolean isCurrent) {
         this.current = isCurrent;
    }

    public boolean isCurrent() {
        return current;
    }    
}
