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
/*
 * OpenAction.java
 *
 * Created on September 24, 2004, 8:41 PM
 */

package org.netbeans.modules.java.navigation.actions;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JEditorPane;
import org.netbeans.api.mdr.MDRepository;
import org.netbeans.jmi.javamodel.*;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.openide.awt.*;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.*;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.text.PositionBounds;
import org.openide.util.*;

import javax.swing.*;
import java.awt.event.*;
import org.netbeans.modules.java.navigation.jmi.JUtils;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * An action that opens editor and jumps to the element given in constructor.
 * Similar to editor's go to declaration action.
 *
 * @author tim, Dafe Simonek
 */
public final class OpenAction extends AbstractAction {
    
    private final Element elem;

    /** static access to java meta model and its default repository */
    private static final JavaMetamodel jmm = JavaMetamodel.getManager();
    private static final MDRepository repo = JavaModel.getJavaRepository();

    private OpenAction (Element elem) {
        this.elem = elem;
        putValue ( Action.NAME, NbBundle.getMessage ( OpenAction.class, "LBL_Goto" ) ); //NOI18N
    }
    
    public void actionPerformed (ActionEvent ev) {
        // find source
        ClassDefinition declaringClass = elem instanceof ClassDefinition ?
                (ClassDefinition)elem : ((Feature)elem).getDeclaringClass();
        if (JUtils.getSourceForBinary(declaringClass) == null) {
            // no source attached
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(OpenAction.class, "MSG_NoSource", elem)  //NOI18N
            ); 
        } else {
            openElement(elem);
        }
    }

    public boolean isEnabled () {
        return elem != null ;
    }

    public static Action createOpenAction (Object o) {
        o = JUtils.unwrap(o);
        Element elem = null;
        if (o instanceof Element) {
            // for opening of element
            elem = (Element)o;
        } else if (o instanceof JavaDataObject) {
            // for opening of whole source
            Resource r = JavaModel.getResource(((JavaDataObject)o).getPrimaryFile());
            List classifs = r.getClassifiers();
            if (classifs != null && classifs.size() > 0) {
                elem = (JavaClass)classifs.get(0);
            }
        }
        return new OpenAction(elem);
    }

    
    // following hard-to-read code is copied from editor/JavaKit class. fuj.

    /** Opens source code of given element in editor and goes to element
     * Works only if element really has source
     */
    private static boolean openElement(final Element element) {
        repo.beginTrans(false);
        try {
            Resource resource = element.getResource();
            if (resource != null) {
                JavaModel.setClassPath(resource);
                DataObject dob = jmm.getDataObject(resource);
                if (dob != null) {
                    final EditorCookie.Observable ec = (EditorCookie.Observable)dob.getCookie(EditorCookie.Observable.class);
                    if (ec != null) {
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(OpenAction.class, "MSG_OpeningElement", // NOI18N
                                element instanceof NamedElement ? ((NamedElement)element).getName() : "")
                        ); 
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                JEditorPane[] panes = ec.getOpenedPanes();
                                if (panes != null && panes.length > 0) {
                                    // editor already opened, so just select
                                    selectElementInPane(panes[0], element, false);
                                } else {
                                    // editor not yet
                                    ec.addPropertyChangeListener(new PropertyChangeListener() {
                                        public void propertyChange(PropertyChangeEvent evt) {
                                            if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                                                final JEditorPane[] panes = ec.getOpenedPanes();
                                                if (panes != null && panes.length > 0) {
                                                    selectElementInPane(panes[0], element, true);
                                                }
                                                ec.removePropertyChangeListener(this);
                                            }
                                        }
                                    });
                                    ec.open();
                                }
                            }

                        });
                        return true;
                    }
                }
            }
        } finally {
            repo.endTrans(false);
        }
        return false;
    }
 
    /** Jumps to element in given editor pane. When delayProcessing is 
     * specified, waits for real visible open before jump 
     */
    private static void selectElementInPane(final JEditorPane pane, final Element element, boolean delayProcessing) {
        //final Cursor editCursor = pane.getCursor();
        //pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (delayProcessing) {
            // [dafe] I don't know why, but editor guys are waiting for focus
            // in delay processing, so I will do the same
            pane.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            jumpToElement(pane, element);
                        }
                    });
                    pane.removeFocusListener(this);
                }
            });
        } else {
            // immediate processing
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    jumpToElement(pane, element);
                }
            });
            // try to activate outer TopComponent
            Container temp = pane;
            while (!(temp instanceof TopComponent)) {
                temp = temp.getParent();
            }
            ((TopComponent) temp).requestActive();
        }
    }

    /** Jumps to element on given editor pane. Call only outside AWT thread!
     */
    private static void jumpToElement (JEditorPane pane, Element element) {
        int caretPos = pane.getCaretPosition();
        Container parent = pane.getParent();
        Point viewPos = parent instanceof JViewport ? ((JViewport)parent).getViewPosition() : null;
        PositionBounds bounds = null;
        // get elem position first
        repo.beginTrans(false);
        try {
            JavaModel.setClassPath(element.getResource());
            bounds = jmm.getElementPosition(element);
        } finally {
            repo.endTrans(false);
        }
        // and actually jump if user not moving already
        if (bounds != null && pane.getCaretPosition() == caretPos &&
                (viewPos == null || viewPos.equals(((JViewport)parent).getViewPosition()))) {
            pane.setCaretPosition(bounds.getBegin().getOffset());
        }
        StatusDisplayer.getDefault().setStatusText(""); // NOI18N
    }

    
}
