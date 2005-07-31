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

import java.awt.event.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingCollablet;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  ayub khan
 */
public class EditorComponentFocusListener extends Object implements FocusListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private String fileName = null;
    private Collablet collablet = null;

    /**
     *
     *
     */
    public EditorComponentFocusListener(String fileName, Collablet collablet) {
        super();
        this.fileName = fileName;
        this.collablet = collablet;
    }

    ///////////////////////////////////////////////////////////////////////////
    // FocusListener methods
    ///////////////////////////////////////////////////////////////////////////
    public void focusGained(FocusEvent e) {
        Debug.log("CollabFileHandlerSupport", "EditorComponentFocusListener, " + "focusGained for file: " + fileName); //NoI18n

        boolean value = true;
        ((FilesharingCollablet) collablet).getChangeSupport().firePropertyChange(
            "conversationActivated", !value, value
        );
    }

    public void focusLost(FocusEvent e) {
        Debug.log("CollabFileHandlerSupport", "EditorComponentFocusListener, " + "focusLost for file: " + fileName); //NoI18n		

        boolean value = false;
        ((FilesharingCollablet) collablet).getChangeSupport().firePropertyChange(
            "conversationActivated", !value, value
        );
    }
}
