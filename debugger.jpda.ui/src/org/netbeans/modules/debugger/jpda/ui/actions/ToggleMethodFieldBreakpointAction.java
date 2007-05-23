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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;

import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.FieldBreakpointPanel;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.MethodBreakpointPanel;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
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
        String className = EditorContextBridge.getCurrentClassName();
        String fieldName = EditorContextBridge.getCurrentFieldName();
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
        
        // 2) find and remove existing line breakpoint
        JPDABreakpoint b;
        if (fieldName != null) {
            b = ToggleBreakpointActionProvider.getBreakpointAnnotationListener().
                findBreakpoint (className, fieldName);
        } else {
            b = ToggleBreakpointActionProvider.getBreakpointAnnotationListener().
                findBreakpoint (className, methodName, methodSignature);
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
        String className = EditorContextBridge.getCurrentClassName();
        String fieldName = EditorContextBridge.getCurrentFieldName();
        String methodName = null;
        String methodSignature = null;
        if (fieldName == null || fieldName.length() == 0) {
            fieldName = null;
            String[] methodInfo = EditorContextBridge.getCurrentMethodDeclaration();
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
            b = ToggleBreakpointActionProvider.getBreakpointAnnotationListener().
                findBreakpoint (className, fieldName);
        } else {
            b = ToggleBreakpointActionProvider.getBreakpointAnnotationListener().
                findBreakpoint (className, methodName, methodSignature);
        }
        return b;
    }
    
}
