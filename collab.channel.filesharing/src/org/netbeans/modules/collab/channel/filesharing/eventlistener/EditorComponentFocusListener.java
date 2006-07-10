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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
