/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Micro//S ystems, Inc. Portions Copyright 1997-2006 Sun
 * Micro//S ystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.groovy.support.debug;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Iterator;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.JPDADebugger;

/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS}
 * property and annotates
 * JSP breakpoints in NetBeans editor.
 *
 * @author Martin Grebac
 * @author Martin Adamek
 */
public class GroovyBreakpointAnnotationListener extends DebuggerManagerAdapter {
    
    private HashMap breakpointToAnnotation = new HashMap ();
    private boolean listen = true;
    
    @Override
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_BREAKPOINTS};
    }
    
    /**
     * Listens on breakpoint.
     */
    @Override
    public void propertyChange (PropertyChangeEvent e) {
        String propertyName = e.getPropertyName ();
        if (propertyName == null) return;
        if (!listen) return;
        if ( (!propertyName.equals (GroovyLineBreakpoint.PROP_CONDITION)) &&
             (!propertyName.equals (GroovyLineBreakpoint.PROP_URL)) &&
             (!propertyName.equals (GroovyLineBreakpoint.PROP_LINE_NUMBER)) &&
             (!propertyName.equals (GroovyLineBreakpoint.PROP_ENABLED))
        ) return;
        GroovyLineBreakpoint b = (GroovyLineBreakpoint) e.getSource ();
        annotate (b);
    }

    /**
    * Called when some breakpoint is added.
    *
    * @param b breakpoint
    */
    @Override
    public void breakpointAdded (Breakpoint b) {
        if (b instanceof GroovyLineBreakpoint) {
            ((GroovyLineBreakpoint) b).addPropertyChangeListener (this);
            annotate ((GroovyLineBreakpoint) b);
        }
    }

    /**
    * Called when some breakpoint is removed.
    *
    * @param breakpoint
    */
    @Override
    public void breakpointRemoved (Breakpoint b) {
        if (b instanceof GroovyLineBreakpoint) {
            ((GroovyLineBreakpoint) b).removePropertyChangeListener (this);
            removeAnnotation ((GroovyLineBreakpoint) b);
        }
    }

    public GroovyLineBreakpoint findBreakpoint (String url, int lineNumber) {
        Iterator i = breakpointToAnnotation.keySet ().iterator ();
        while (i.hasNext ()) {
            GroovyLineBreakpoint lb = (GroovyLineBreakpoint) i.next ();
            if (!lb.getURL ().equals (url)) continue;
            Object annotation = breakpointToAnnotation.get (lb);
            int ln = Context.getLineNumber (annotation, null);
            if (ln == lineNumber) return lb;
        }
        return null;
    }
    
    // helper methods ..........................................................
    
    private void annotate (GroovyLineBreakpoint b) {
        // remove old annotation
        Object annotation = breakpointToAnnotation.get (b);
        if (annotation != null)
            Context.removeAnnotation (annotation);
        if (b.isHidden ()) return;
        
        // add new one
        annotation = Context.annotate (b);
        if (annotation == null)
            return;
        
        breakpointToAnnotation.put (b, annotation);
        
        DebuggerEngine de = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        Object timeStamp = null;
        if (de != null)
            timeStamp = de.lookupFirst (null, JPDADebugger.class);
        update (b, timeStamp);        
    }

    public void updateGroovyLineBreakpoints () {
        Iterator it = breakpointToAnnotation.keySet ().iterator (); 
        while (it.hasNext ()) {
            GroovyLineBreakpoint lb = (GroovyLineBreakpoint) it.next ();
            update (lb, null);
        }
    }
    
    private void update (GroovyLineBreakpoint b, Object timeStamp) {
        Object annotation = breakpointToAnnotation.get (b);
        if (annotation == null) 
            return;
        int ln = Context.getLineNumber (annotation, timeStamp);
        listen = false;
        b.setLineNumber (ln);
        listen = true;
    }
    
    private void removeAnnotation(GroovyLineBreakpoint b) {
        Object annotation = breakpointToAnnotation.remove (b);
        if (annotation != null)
            Context.removeAnnotation (annotation);
    }
}
