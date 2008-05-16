package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
public abstract class NbJSBreakpointAnnotationListener extends DebuggerManagerAdapter  {
    
    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS };
    }
    
    /**
     * Add a Breakpoint Annotation to the editor.
     * @param b for which an annotation will be created
     */
    protected abstract void addBreakpointAnnotation( NbJSBreakpoint b);
    
    /**
     * Remove a Breakpoint Annotation to the editor.
     * @param b for which an annotation will be created
     */
    protected abstract void removeBreakpointAnnotation( NbJSBreakpoint b);
    
    protected PropertyChangeListener enableBreakpointPropertyChangeListener;
    protected final PropertyChangeListener getEnableBreakpointPropertyChangeListener() {
        if( enableBreakpointPropertyChangeListener == null){
            enableBreakpointPropertyChangeListener =  new EnableBreakpointPropertyChangeListener();
        } 
        return enableBreakpointPropertyChangeListener;
    }
    
    /**
     * All the collections for which EnableBreakpointPropertyChangeListener should respond
     * @return
     */
    protected static Set<String> getUpdateableProperties() {
        Set<String> set = new HashSet<String>(4);
        set.add(Breakpoint.PROP_ENABLED);
        set.add(Breakpoint.PROP_HIT_COUNT_FILTER);
        set.add(NbJSBreakpoint.PROP_UPDATED);
        set.add(NbJSBreakpoint.PROP_CONDITION);
        return set;

    }
    
    
    /**
     * This property listener checks for any changes that may need to be added to the property change listener.
     * @author joelle
     *
     */
    private final class EnableBreakpointPropertyChangeListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            NbJSBreakpoint breakpoint = (NbJSBreakpoint) evt.getSource();
            if (getUpdateableProperties().contains(propName)) {
                removeBreakpointAnnotation(breakpoint);
                addBreakpointAnnotation(breakpoint);
            }
        }
        
    }
}
