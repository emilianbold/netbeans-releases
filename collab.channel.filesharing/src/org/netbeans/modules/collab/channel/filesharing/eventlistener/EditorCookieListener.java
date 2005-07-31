/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.eventlistener;

import com.sun.collablet.*;

import org.openide.cookies.*;

import java.beans.*;

import javax.swing.*;

import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  ayub khan
 */
public class EditorCookieListener extends Object implements PropertyChangeListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* holds cookie */
    private EditorCookie cookie = null;
    private String fileName = null;
    private Collablet collablet = null;

    /**
     * constructor
     *
     */
    public EditorCookieListener(EditorCookie cookie, String fileName, Collablet collablet)
    throws CollabException {
        super();
        this.cookie = cookie;
        this.fileName = fileName;
        this.collablet = collablet;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * propertyChange
     *
     * @param        event
     */
    public void propertyChange(PropertyChangeEvent event) {
        Debug.log("CollabFileHandlerSupport", "EditorCookieListener, propertyChange: " + event.getPropertyName()); //NoI18n		

        if (EditorCookie.Observable.PROP_OPENED_PANES.equals(event.getPropertyName())) {
            Debug.log(
                "CollabFileHandlerSupport",
                "EditorCookieListener, received: " + //NoI18n
                EditorCookie.Observable.PROP_OPENED_PANES + " for file: " + //NoI18n
                fileName
            ); //NoI18n	

            if (cookie != null) {
                JEditorPane[] editorPanes = cookie.getOpenedPanes();

                if (editorPanes != null) {
                    Debug.log(
                        "CollabFileHandlerSupport", "EditorCookieListener, " + "pane size: " + editorPanes.length
                    ); //NoI18n

                    for (int i = 0; i < editorPanes.length; i++) {
                        if (editorPanes[i] != null) {
                            editorPanes[i].addFocusListener(new EditorComponentFocusListener(fileName, collablet));
                        }
                    }
                } else {
                    Debug.log("CollabFileHandlerSupport", "EditorCookieListener, " + "no panes opened"); //NoI18n
                }
            }
        }
    }
}
