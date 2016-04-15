/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.otool.debugger.api;

/**
 *
 * @author Nikolay Koldunov
 */
public final  class Target {
    private String execPath;
    private EngineType engine;
    
    public Target(String execPath) {
        this.execPath = execPath;
        this.engine = engine;
    }
    public EngineDescriptor getEngineDescriptor() {
        assert engine != null;
        return new EngineDescriptor(engine);
    }

    public EngineType getEngine() {
        assert engine != null;
	//return getEngineProfile().getEngine();
	return engine;
    }

    public void setEngine(EngineType e) {
        if (e == null) {
            e = EngineTypeManager.getFallbackEnineType();
        }
	engine = e;
    }    
    
    /*package*/ void setExecPath(String execPath) {
        this.execPath = execPath;
    }
    
    public String getExecPath() {
        return execPath;
    }

    public String getID() {
        return "netbeans-OtoolNativeTarget";
    }
}
