/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSContextProviderWrapper;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.tools.api.JSAbstractLocation;
import org.netbeans.modules.web.client.tools.api.JSLocation;
import org.netbeans.modules.web.client.tools.api.NbJSLocation;
import org.netbeans.modules.web.client.tools.api.NbJSToJSLocationMapper;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;

/**
 * NbJSBreakpoint is a breakpoint model for the JavaScript Debugger. If you plan
 * on implementing this class, you should also override hashcode and equals.
 * 
 * @author joelle lam
 */
public abstract class NbJSBreakpoint extends Breakpoint {

    protected PROP_SUSPEND_STATE suspend = PROP_SUSPEND_STATE.SUSPEND;
    /** Property name for enabled status of the breakpoint. */

    public static final String PROP_SUSPEND = "suspend"; // NOI18N
    public static final String PROP_CONDITION = "condition"; // NOI18N
    public static final String PROP_UPDATED = "updated"; // NOI18N
    public static final String PROP_PRINT_TEXT = "printText"; // NOI18N

    private boolean enabled;
    // private Line line;
    private String condition = ""; // NOI18N
    private String printText;
    private JSAbstractLocation location;

    /**
     * This contructor is to only be used if you do not have a line.
     * 
     * @param location
     */
    public NbJSBreakpoint(JSAbstractLocation location) {
        assert location != null;
        this.location = location;
        this.enabled = true;
    }

    /** Suspend property value constant. */
    public static enum PROP_SUSPEND_STATE {
        SUSPEND, SUSPEND_NONE
    }

    public void notifyUpdated(Object source) {
        NbJSContextProviderWrapper.getBreakpointModel().fireChanges();
        firePropertyChange(NbJSBreakpoint.PROP_UPDATED, null, null);
    }

    public void notifyUpdated(String name, Object o, Object n) {
        NbJSContextProviderWrapper.getBreakpointModel().fireChanges();
        firePropertyChange(name, o, n);
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public void disable() {
        if (enabled) {
            enabled = false;
            firePropertyChange(PROP_ENABLED, true, false);
        }
    }

    public void enable() {
        if (!enabled) {
            enabled = true;
            firePropertyChange(PROP_ENABLED, false, true);
        }
    }

    public abstract FileObject getFileObject();

    public abstract int getLineNumber();

    public abstract Line getLine();

    public abstract void setLine(Line line);

    public @Override
    String toString() {
        return getLocation().getDisplayName();
    }

    /**
     * Returns condition.
     * 
     * @return cond a condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Sets condition.
     * 
     * @param c
     *            a new condition
     */
    public void setCondition(String c) {
        String old;
        synchronized (this) {
            if (c == null) {
                c = "";
            }
            c = c.trim();
            if ((c == condition)
                    || ((c != null) && (condition != null) && condition
                            .equals(c))) {
                return;
            }
            old = condition;
            condition = c;
        }
        firePropertyChange(PROP_CONDITION, old, c);
    }

    /**
     * Gets value of print text property.
     * 
     * @return value of print text property
     */
    public String getPrintText() {
        return printText;
    }

    /**
     * Sets value of print text property.
     * 
     * @param printText
     *            a new value of print text property
     */
    public void setPrintText(String printText) {
        if (this.printText == printText) {
            return;
        }
        String old = this.printText;
        this.printText = printText;
        firePropertyChange(PROP_PRINT_TEXT, old, printText);
    }

    /**
     * Gets value of suspend property.
     * 
     * @return value of suspend property
     */
    public PROP_SUSPEND_STATE getSuspend() {
        return suspend;
    }

    /**
     * Sets value of suspend property.
     * 
     * @param s
     *            a new value of suspend property
     */
    public void setSuspend(PROP_SUSPEND_STATE s) {
        if (s == suspend) {
            return;
        }
        PROP_SUSPEND_STATE old = suspend;
        suspend = s;
        firePropertyChange(PROP_SUSPEND, old, s);
    }

    public JSAbstractLocation getLocation() {
        return location;
    }

    public void setLocation(JSAbstractLocation location){
        JSAbstractLocation orLoc = this.location;
        this.location = location;
        firePropertyChange(Line.PROP_LINE_NUMBER, orLoc, location);
    }

    public String getDisplayName() {
        return getLocation().getDisplayName();
    }

    /* Resolved URI and Line Number */
    public boolean isResolved() {
        return (getLocation().getJSLocation() != null);
    }

    public String getResolvedLocation() {
        Session session = DebuggerManager.getDebuggerManager().getCurrentSession();
        if (session != null) {
            NbJSDebugger debugger = session.lookupFirst(null, NbJSDebugger.class);

            NbJSToJSLocationMapper nbJSToJSLocationMapper = session.lookupFirst(null, NbJSToJSLocationMapper.class);
            if (nbJSToJSLocationMapper != null) {
                JSAbstractLocation nbJSALoc = getLocation();
                JSLocation jsLocation = null;
                if( nbJSALoc instanceof NbJSLocation ) {
                    jsLocation = nbJSToJSLocationMapper.getJSLocation((NbJSLocation)nbJSALoc, null);
                } else if( nbJSALoc instanceof JSLocation ){
                    jsLocation = (JSLocation)nbJSALoc;
                }

                if (debugger != null && jsLocation != null && debugger.isIgnoringQueryStrings() && jsLocation.getURI().getQuery() != null) {
                    return "";
                } else if (jsLocation != null) {
                    return jsLocation.getDisplayName();
                }
            }
        }
        return "";
    }

    public int getResolvedLineNumber() {
        JSLocation jsLoc = getLocation().getJSLocation();
        if (jsLoc == null) {
            return -1;
        }
        return jsLoc.getLineNumber();
    }
    
    public boolean isConditional() {
        return (getCondition() != null ) ? (getCondition().length() > 0 || getHitCountFilter() > 0) : false;
    }
}
