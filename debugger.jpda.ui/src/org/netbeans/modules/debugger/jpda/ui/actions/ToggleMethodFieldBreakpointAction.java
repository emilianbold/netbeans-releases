/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;

import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.FieldBreakpointPanel;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.MethodBreakpointPanel;

import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;


/**
 *
 * @author   Martin Entlicher
 */
public class ToggleMethodFieldBreakpointAction extends AbstractAction {//implements PropertyChangeListener {
    
    private Object action;

    public ToggleMethodFieldBreakpointAction () {
        //EditorContextBridge.addPropertyChangeListener (this);
        setEnabled (true);
    }
    
    public Object getAction () {
        return action;
    }
    
    public Object getValue(String key) {
        if (key == Action.NAME) {
            return NbBundle.getMessage (ToggleMethodFieldBreakpointAction.class, "CTL_ToggleMethodFieldBreakpointAction");
        }
        Object value = super.getValue(key);
        if (key == Action.SMALL_ICON) {
            if (value instanceof String) {
                value = new ImageIcon (Utilities.loadImage (value+".gif"));// Utils.getIcon ((String) value);
            }
        }
        return value;
    }
    
    /*
    public void propertyChange (PropertyChangeEvent evt) {
        String url = EditorContextBridge.getCurrentURL();
        if (url.length() == 0) {
            setEnabled(false);
            return ;
        }
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException muex) {
            fo = null;
        }
        setEnabled (
            (fo != null && "text/x-java".equals(fo.getMIMEType())) && // NOI18N
            (EditorContextBridge.getCurrentFieldName() != null ||
             EditorContextBridge.getCurrentMethodDeclaration() != null)
            //(EditorContextBridge.getCurrentURL ().endsWith (".java"))
        );
        /*
         if ( debugger != null && 
             debugger.getState () == debugger.STATE_DISCONNECTED
        ) 
            destroy ();
         *//*
    }
     */
    
    public void actionPerformed (ActionEvent evt) {
        if (!submitFieldOrMethodBreakpoint()) {
            DebuggerManager.getDebuggerManager().getActionsManager().doAction(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
        }
    }
    
    private boolean submitFieldOrMethodBreakpoint() {
        // 1) get class name & element info
        final String[] className = new String[] { null };
        java.awt.IllegalComponentStateException cex;
        try {
            className[0] = EditorContextBridge.getContext().getCurrentClassName();
            cex = null;
        } catch (java.awt.IllegalComponentStateException icsex) {
            cex = icsex;
        }
        final String[] fieldName = new String[] { null };
        java.awt.IllegalComponentStateException fex;
        try {
            fieldName[0] = EditorContextBridge.getContext().getCurrentFieldName();
            fex = null;
        } catch (java.awt.IllegalComponentStateException icsex) {
            fex = icsex;
        }
        final String methodName;
        final String methodSignature;
        java.awt.IllegalComponentStateException mex;
        if (fex != null || fieldName[0] == null || fieldName[0].length() == 0) {
            fieldName[0] = null;
            String[] methodInfo;
            try {
                methodInfo = EditorContextBridge.getContext().getCurrentMethodDeclaration();
                mex = null;
            } catch (java.awt.IllegalComponentStateException icsex) {
                mex = icsex;
                methodInfo = null;
            }
            if (methodInfo != null) {
                methodName = methodInfo[0];
                methodSignature = methodInfo[1];
                if (methodInfo[2] != null) {
                    className[0] = methodInfo[2];
                }
            } else if (mex == null) {
                return false;
            } else {
                methodName = null;
                methodSignature = null;
            }
        } else {
            mex = null;
            methodName = null;
            methodSignature = null;
        }
        if (cex != null || fex != null || mex != null) {
            final int ln = EditorContextBridge.getContext().getCurrentLineNumber ();
            final String url = EditorContextBridge.getContext().getCurrentURL ();
            final java.awt.IllegalComponentStateException[] exs = new java.awt.IllegalComponentStateException[]
                    { cex, fex, mex };
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    // Re-try to submit the field or method breakpoint again
                    String cn = (exs[0] != null) ? exs[0].getMessage() : className[0];
                    String fn = (exs[1] != null) ? exs[1].getMessage() : fieldName[0];
                    String mn = (exs[2] != null) ? exs[2].getMessage() : methodName;
                    String ms = (exs[2] != null) ? exs[2].getLocalizedMessage() : methodSignature;
                    if (fn != null && fn.length() == 0) fn = null;
                    if (submitFieldOrMethodBreakpoint(cn, fn, mn, ms)) {
                        // We've submitted a field or method breakpoint, so delete the line one:
                        LineBreakpoint lb = ToggleBreakpointActionProvider.findBreakpoint (
                            url, ln
                        );
                        if (lb != null) {
                            DebuggerManager.getDebuggerManager().removeBreakpoint (lb);
                        }
                    }
                }
            });
            return false;
        } else {
            return submitFieldOrMethodBreakpoint(className[0], fieldName[0], methodName, methodSignature);
        }
        
        /*
        // 1) get class name & element info
        String className;
        String fieldName;
        try {
            className = EditorContextBridge.getCurrentClassName();
            fieldName = EditorContextBridge.getCurrentFieldName();
        } catch (java.awt.IllegalComponentStateException icsex) {
            final int ln = EditorContextBridge.getCurrentLineNumber ();
            final String url = EditorContextBridge.getCurrentURL ();
            
            return false;
        }
        String methodName = null;
        String methodSignature = null;
        if (fieldName == null || fieldName.length() == 0) {
            fieldName = null;
            String[] methodInfo = EditorContextBridge.getCurrentMethodDeclaration();
            if (methodInfo != null) {
                methodName = methodInfo[0];
                methodSignature = methodInfo[1];
            } else {
                return false;
            }
        }
        return submitFieldOrMethodBreakpoint(className[0], fieldName[0], methodName, methodSignature);
         */
    }
        
    private boolean submitFieldOrMethodBreakpoint(String className, String fieldName,
                                                  String methodName, String methodSignature) {
        // 2) find and remove existing line breakpoint
        JPDABreakpoint b;
        if (fieldName != null) {
            b = findBreakpoint (className, fieldName);
        } else if (methodName != null) {
            b = findBreakpoint (className, methodName, methodSignature);
        } else {
            return false;
        }
        DebuggerManager d = DebuggerManager.getDebuggerManager();
        if (b != null) {
            d.removeBreakpoint (b);
            return true;
        }
        
        // 3) create a new breakpoint
        if (fieldName != null) {
            b = FieldBreakpoint.create(className, fieldName, FieldBreakpoint.TYPE_MODIFICATION);
            b.setPrintText(NbBundle.getMessage(FieldBreakpointPanel.class, "CTL_Field_Breakpoint_Print_Text"));
        } else {
            b = MethodBreakpoint.create(className, methodName);
            ((MethodBreakpoint) b).setMethodSignature(methodSignature);
            b.setPrintText(NbBundle.getMessage(MethodBreakpointPanel.class, "CTL_Method_Breakpoint_Print_Text"));
        }
        d.addBreakpoint(b);
        return true;
    }
    
    static JPDABreakpoint getCurrentFieldMethodBreakpoint() {
        String className = EditorContextBridge.getContext().getCurrentClassName();
        String fieldName = EditorContextBridge.getContext().getCurrentFieldName();
        String methodName = null;
        String methodSignature = null;
        if (fieldName == null || fieldName.length() == 0) {
            fieldName = null;
            String[] methodInfo = EditorContextBridge.getContext().getCurrentMethodDeclaration();
            if (methodInfo != null) {
                methodName = methodInfo[0];
                methodSignature = methodInfo[1];
            } else {
                return null;
            }
        }
        
        // 2) find and remove existing line breakpoint
        JPDABreakpoint b;
        if (fieldName != null) {
            b = findBreakpoint (className, fieldName);
        } else {
            b = findBreakpoint (className, methodName, methodSignature);
        }
        return b;
    }
    
    private static FieldBreakpoint findBreakpoint(String className, String fieldName) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof FieldBreakpoint)) {
                continue;
            }
            FieldBreakpoint fb = (FieldBreakpoint) breakpoints[i];
            if (!fb.getClassName().equals(className)) continue;
            if (!fb.getFieldName().equals(fieldName)) continue;
            return fb;
        }
        return null;
    }

    private static MethodBreakpoint findBreakpoint(String className, String methodName, String methodSignature) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof MethodBreakpoint)) {
                continue;
            }
            MethodBreakpoint mb = (MethodBreakpoint) breakpoints[i];
            String[] classFilters = mb.getClassFilters();
            int j;
            for (j = 0; j < classFilters.length; j++) {
                if (match(className, classFilters[j])) {
                    break;
                }
            }
            if (j < classFilters.length) {
                if (!mb.getMethodName().equals(methodName)) continue;
                String signature = mb.getMethodSignature();
                if (signature == null || egualMethodSignatures(signature, methodSignature)) {
                    return mb;
                }
            }
        }
        return null;
    }
    
    // Compares whether the two signatures have the same arguments. We ignore return value.
    private static boolean egualMethodSignatures(String s1, String s2) {
        int i = s1.lastIndexOf(")");
        if (i > 0) s1 = s1.substring(0, i);
        i = s2.lastIndexOf(")");
        if (i > 0) s2 = s2.substring(0, i);
        return s1.equals(s2);
    }
    
    private static boolean match (String name, String pattern) {
        if (pattern.startsWith ("*"))
            return name.endsWith (pattern.substring (1));
        else
        if (pattern.endsWith ("*"))
            return name.startsWith (
                pattern.substring (0, pattern.length () - 1)
            );
        return name.equals (pattern);
    }
    
}
