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
package org.netbeans.modules.refactoring.spi.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.impl.UndoManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;

/** 
 *
 * @author Martin Matula, Daniel Prusa
 */
public class UndoWatcher {

     private static Collection extractCES(Collection elements) {
        HashSet result = new HashSet();
        for (Iterator it = elements.iterator(); it.hasNext();) {
            RefactoringElement e = (RefactoringElement) it.next();
            PositionBounds pb = e.getPosition();
            if (pb != null) {
                CloneableEditorSupport ces = pb.getBegin().getCloneableEditorSupport();
                result.add(ces);
            }
        }
        return result;
    }

    public static void watch(RefactoringSession session, InvalidationListener l) {
        UndoManager.getDefault().watch(extractCES(session.getRefactoringElements()), l);
    }

    public static void stopWatching(InvalidationListener l) {
        UndoManager.getDefault().stopWatching(l);
    }
    
    public static void watch(DataObject o) {
        EditorCookie ces = o.getCookie(EditorCookie.class);
        assert ces instanceof CloneableEditorSupport;
        UndoManager.getDefault().watch(Collections.singleton((CloneableEditorSupport)ces), null);
    }
}
