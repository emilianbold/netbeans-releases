/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.debug.breakpoints;

import java.beans.*;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;

import org.netbeans.modules.web.debug.util.Utils;
import java.util.*;

/**
 *
 * @author Martin Grebac
 */
public class JspLineBreakpoint extends Breakpoint {
    
    /** Property name for enabled status of the breakpoint. */
    public static final String          PROP_ENABLED = JPDABreakpoint.PROP_ENABLED;

    public static final String          PROP_SUSPEND = JPDABreakpoint.PROP_SUSPEND;
    public static final String          PROP_HIDDEN = JPDABreakpoint.PROP_HIDDEN;
    public static final String          PROP_PRINT_TEXT = JPDABreakpoint.PROP_PRINT_TEXT;

    public static final int             SUSPEND_ALL = JPDABreakpoint.SUSPEND_ALL;
    public static final int             SUSPEND_EVENT_THREAD = JPDABreakpoint.SUSPEND_EVENT_THREAD;
    public static final int             SUSPEND_NONE = JPDABreakpoint.SUSPEND_NONE;

    public static final String          PROP_LINE_NUMBER = LineBreakpoint.PROP_LINE_NUMBER;
    public static final String          PROP_URL = LineBreakpoint.PROP_URL;
    public static final String          PROP_CONDITION = LineBreakpoint.PROP_CONDITION;
    
    //private fields
    private PropertyChangeSupport pcs;    
    private HashSet                     breakpointListeners = new HashSet ();
    
    private boolean                     enabled = true;
    private boolean                     hidden = false;
    private int                         suspend = SUSPEND_ALL;
    private String                      printText = null;    

    private String                      url = "";       // NOI18N
    private int                         lineNumber;
    private String                      condition = ""; // NOI18N
    
    private LineBreakpoint javalb;
    
    {
        pcs = new PropertyChangeSupport(this);
    }        
    
    /** Creates a new instance of JspLineBreakpoint */
    public JspLineBreakpoint() {
    }
    
    /** Creates a new instance of JspLineBreakpoint with url, linenumber*/
    public JspLineBreakpoint(String url, int lineNumber) {
    }

    /**
     * Creates a new breakpoint for given parameters.
     *
     * @param url a url
     * @param lineNumber a line number
     * @return a new breakpoint for given parameters
     */
    public static JspLineBreakpoint create(String url, int lineNumber) {
        
        DebuggerManager d = DebuggerManager.getDebuggerManager();
        if (d == null) {
            return null;
        }
        
        JspLineBreakpoint b = new JspLineBreakpoint();
        
        String servletClass = Utils.getServletClass(url);
        Utils.getEM().log("jsp url: " + url);
        Utils.getEM().log("servletClass: " + servletClass);
        LineBreakpoint javalb = LineBreakpoint.create(servletClass, lineNumber);
        javalb.setHidden(true);
        
        d.addBreakpoint(javalb);
        
        b.setURL(url);
        b.setLineNumber(lineNumber);
        b.setJavalb(javalb);

        return b;
    }
    
    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public int getSuspend () {
        return suspend;
    }

    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public void setSuspend (int s) {
        if (s == suspend) return;
        int old = suspend;
        suspend = s;
        if (javalb != null) {
            javalb.setSuspend(s);
        }
        pcs.firePropertyChange(PROP_SUSPEND, old, s);
    }
    
    /**
     * Gets value of hidden property.
     *
     * @return value of hidden property
     */
    public boolean isHidden () {
        return hidden;
    }
    
    /**
     * Sets value of hidden property.
     *
     * @param s a new value of hidden property
     */
    public void setHidden (boolean h) {
        if (h == hidden) return;
        boolean old = hidden;
        hidden = h;
        pcs.firePropertyChange(PROP_HIDDEN, old, h);
    }
    
    /**
     * Gets value of print text property.
     *
     * @return value of print text property
     */
    public String getPrintText () {
        return printText;
    }

    /**
     * Sets value of print text property.
     *
     * @param s a new value of print text property
     */
    public void setPrintText (String printText) {
        if (this.printText == printText) return;
        String old = this.printText;
        this.printText = printText;
        pcs.firePropertyChange(PROP_PRINT_TEXT, old, printText);
    }
    
    /**
     * Called when breakpoint is removed.
     */
    protected void dispose() {
        if (javalb != null) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(javalb);
        }
    }

    /**
     * Test whether the breakpoint is enabled.
     *
     * @return <code>true</code> if so
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Disables the breakpoint.
     */
    public void disable() {
        if (!enabled) return;
        enabled = false;
        if (javalb != null) {
            javalb.disable();
        }
        pcs.firePropertyChange(PROP_ENABLED, true, false);
    }
    
    /**
     * Enables the breakpoint.
     */
    public void enable() {
        if (enabled) return;
        enabled = true;
        if (javalb != null) {
            javalb.enable();
        }
        pcs.firePropertyChange(PROP_ENABLED, false, true);
    }

    /**
     * Sets name of class to stop on.
     *
     * @param cn a new name of class to stop on
     */
    public void setURL (String url) {
        if ( (url == this.url) ||
             ((url != null) && (this.url != null) && url.equals (this.url))
        ) return;
        String old = url;
        this.url = url;
        pcs.firePropertyChange(PROP_URL, old, url);
    }

    /**
     * Gets name of class to stop on.
     *
     * @return name of class to stop on
     */
    public String getURL () {
        return url;
    }
    
    /**
     * Gets number of line to stop on.
     *
     * @return line number to stop on
     */
    public int getLineNumber () {
        return lineNumber;
    }
    
    /**
     * Sets number of line to stop on.
     *
     * @param ln a line number to stop on
     */
    public void setLineNumber (int ln) {
        if (ln == lineNumber) return;
        int old = lineNumber;
        lineNumber = ln;
        if (javalb != null) {
            javalb.setLineNumber(ln);
        }
        pcs.firePropertyChange(PROP_LINE_NUMBER, old, getLineNumber());
    }
    
    /**
     * Sets condition.
     *
     * @param c a new condition
     */
    public void setCondition (String c) {
        if (c != null) c = c.trim ();
        if ( (c == condition) ||
             ((c != null) && (condition != null) && condition.equals (c))
        ) return;
        String old = condition;
        condition = c;
        if (javalb != null) {
            javalb.setCondition(c);
        }        
        pcs.firePropertyChange(PROP_CONDITION, old, condition);
    }
    
    /**
     * Returns condition.
     *
     * @return cond a condition
     */
    public String getCondition () {
        return condition;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "JspLineBreakpoint " + url + " : " + lineNumber;
    }    
    
    /**
     * Add a property change listener.
     *
     * @param l the listener to add
     */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);        
    }

    /**
     * Remove a property change listener.
     *
     * @param l the listener to remove
     */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);        
    }
        
    /**
     * Getter for property javalb.
     * @return Value of property javalb.
     */
    public LineBreakpoint getJavalb() {
        return javalb;
    }
    
    /**
     * Setter for property javalb.
     * @param javalb New value of property javalb.
     */
    public void setJavalb(LineBreakpoint javalb) {
        this.javalb = javalb;
    }
    
    /**
     * Adds a JPDABreakpointListener.
     *
     * @param listener the listener to add
     */
    public synchronized void addJPDABreakpointListener(JPDABreakpointListener listener) {
        breakpointListeners.add (listener);
    }
  
    /** 
     * Removes a JPDABreakpointListener.
     *
     * @param listener the listener to remove
    */
    public synchronized void removeJPDABreakpointListener(JPDABreakpointListener listener) {
        breakpointListeners.remove (listener);
    }

    /**
     * Fire JPDABreakpointEvent.
     *
     * @param event a event to be fired
     */
    void fireJPDABreakpointChange (JPDABreakpointEvent event) {
        Iterator i = ((HashSet) breakpointListeners.clone ()).iterator ();
        while (i.hasNext ())
            ((JPDABreakpointListener) i.next ()).breakpointReached (event);
    }
}
