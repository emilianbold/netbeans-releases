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
package org.netbeans.modules.sql.framework.ui.undo;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;

/**
 * @author Ritesh Adval
 */
public class SQLUndoManager extends UndoManager {

    private ArrayList listeners = new ArrayList();

    public SQLUndoManager() {
        super();
    }

    public void addUndoableEditListener(UndoableEditListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void fireUndoableEditEvent(UndoableEditEvent evt) {
        Iterator it = listeners.iterator();

        while (it.hasNext()) {
            UndoableEditListener l = (UndoableEditListener) it.next();
            l.undoableEditHappened(evt);
        }
    }

    public void removeUndoableEditListener(UndoableEditListener l) {
        listeners.remove(l);
    }

    public void undoableEditHappened(UndoableEditEvent e) {
        super.undoableEditHappened(e);
        fireUndoableEditEvent(e);
    }
}

