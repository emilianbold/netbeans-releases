/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.otool.debugger.api;

import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.debugger.Breakpoint;

/**
 *
 * @author Nikolay Koldunov
 */
abstract public class OtoolNativeBreakpoint extends Breakpoint implements java.io.Serializable {
    
    private volatile boolean enabled = true;

    private final AtomicInteger id = new AtomicInteger();
    private OtoolNativeDebugger debugger;
    private Context context;
    private boolean isEnabled;
    

    protected OtoolNativeBreakpoint () {
      
    }
    
    
    @Override
    public void disable() {
        enabled = false;
    }

    @Override
    public void enable() {
        enabled = true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    
   public final void unbind() {
//	assert isMidlevel();

	// We don't null the context ... we become a ghost
//	for (OtoolNativeBreakpoint c : g) {
//	    c.setDebugger(null);
//	    c.setHandler(null);
//	    c.update();
//	}
	this.setDebugger(null);
	//this.update();
    }

    public void bindTo(OtoolNativeDebugger debugger) {
	//assert isSubBreakpoint() || isMidlevel();

//	assert getDebugger() == null || getDebugger() == debugger :
//	       "NativeBreakpoint.bindTo(): already have a debugger"; // NOI18N
	final String executable = debugger.getNDI().getTarget();
	final String hostname = debugger.getExecEnv().getHost();
	setContext(new Context(executable, hostname));
	setDebugger(debugger);

//	// OLD getParent().setEnabled(getParent().recalculateIsEnabled());
//	updateAndParent();
    }
    public final void setContext(Context newContext) {
	context = newContext;

//	if (isMidlevel()) {
//	    for (NativeBreakpoint c : getChildren())
//		c.context.set(newContext);
//	}
    }   
    
    private void setDebugger(OtoolNativeDebugger debugger) {
	// See comments in cleanup()
	//assert !isToplevel() : "Cannot setDebugger() on toplevel bpt";
	this.debugger = debugger;
    }    
    
    public final void setId(int newId) {
	id.set(newId);
    }
    
    public final int getId() {
        return id.get();
    }
    
    public final void setEnabled (boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

        
}
