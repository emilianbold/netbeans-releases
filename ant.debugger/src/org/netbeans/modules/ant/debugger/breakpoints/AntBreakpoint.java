package org.netbeans.modules.ant.debugger.breakpoints;

import org.netbeans.api.debugger.Breakpoint;
import org.openide.text.Line;



/**
 *
 * @author  Honza
 */
public class AntBreakpoint extends Breakpoint {
    
    private boolean enabled = true;
    private Line    line;
    

    AntBreakpoint (Line line) {
        this.line = line;
    }
    
    public Line getLine () {
        return line;
    }
        
    /**
     * Test whether the breakpoint is enabled.
     *
     * @return <code>true</code> if so
     */
    public boolean isEnabled () {
        return enabled;
    }
    
    /**
     * Disables the breakpoint.
     */
    public void disable () {
        if (!enabled) return;
        enabled = false;
        firePropertyChange (PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
    }
    
    /**
     * Enables the breakpoint.
     */
    public void enable () {
        if (enabled) return;
        enabled = true;
        firePropertyChange (PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
    }
}
