/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
package org.netbeans.modules.visualweb.insync;

import org.netbeans.modules.visualweb.api.insync.InSyncService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
// XXX Node a NB cookie, can't use!
//import org.openide.cookies.UndoRedoCookie;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;

/**
 * Represents a single undoable event
 *
 * @author  Tor Norbye
 */
public class UndoEvent implements InSyncService.WriteLock {
    private String description;
    private ArrayList modifiedUnits;
    private Model model;

    public Model getModel() {
        return model;
    }

    public UndoEvent(String description, Model model) {
        this.description = description;
        this.model = model;
    }

    /**
     * Make a note of the fact that the given source unit was updated as part of this undoable
     * event, such that its buffer's undo event is run when this entire event is rolled back. We
     * want to actually record units MULTIPLE times if they are actually flushed several times
     * during an update - that way we can undo all their changes as a single event.
     */
    public void notifyBufferUpdated(SourceUnit unit) {
        if (modifiedUnits == null) {
            modifiedUnits = new ArrayList(3);
        }
        modifiedUnits.add(unit);
    }

    /**
     *
     */
    public void undo() {
        if (modifiedUnits == null) {
            return;
        }

        Iterator it = modifiedUnits.iterator();
        while (it.hasNext()) {
            SourceUnit unit = (SourceUnit)it.next();
            DataObject dobj = unit.getDataObject();
            /*
              This doesn't work well because the JavaEditor is NOT
              a ClonableEditor!
            EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
            if (ec instanceof CloneableEditorSupport) {
                CloneableEditorSupport ces = (CloneableEditorSupport)ec;
                ces.getUndoRedoPublic().undo();
            }
            */
//            UndoRedoCookie urc = (UndoRedoCookie)dobj.getCookie(UndoRedoCookie.class);
//            if (urc != null) {
//                UndoRedo ur = urc.getUndoRedoI();
//                if (ur != null) {
//                    ur.undo();
//                }
//            }
//            RaveUndoRedoCookie urc = (RaveUndoRedoCookie)dobj.getCookie(RaveUndoRedoCookie.class);
//            if (urc != null) {
//                UndoRedo ur = urc.getUndoRedoI();
//                if (ur != null) {
//                    ur.undo();
//                }
//            }
            // Use CloneableEditorSupport's UndoRedo to perform undo
            EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
            if (ec instanceof CloneableEditorSupport) {
                CloneableEditorSupport ces = (CloneableEditorSupport)ec;
                UndoRedo undoRedo = getUndoRedo(ces);
                if (undoRedo != null) {
                	undoRedo.undo();
                }
            }
        }
    }

    /** Return true iff there have been any changes to any buffers
     * for this undo event.
     */
    public boolean hasChanges() {
        return modifiedUnits != null && modifiedUnits.size() > 0;
    }

    /**
     *
     */
    public void redo() {
        if (modifiedUnits == null) {
            return;
        }

        Iterator it = modifiedUnits.iterator();
        while (it.hasNext()) {
            SourceUnit unit = (SourceUnit)it.next();
            DataObject dobj = unit.getDataObject();
            /*
            EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
            if (ec instanceof CloneableEditorSupport) {
                CloneableEditorSupport ces = (CloneableEditorSupport)ec;
                ces.getUndoRedoPublic().redo();
            }
            */
//            UndoRedoCookie urc = (UndoRedoCookie)dobj.getCookie(UndoRedoCookie.class);
//            if (urc != null) {
//                UndoRedo ur = urc.getUndoRedoI();
//                if (ur != null) {
//                    ur.redo();
//                }
//            }
//            RaveUndoRedoCookie urc = (RaveUndoRedoCookie)dobj.getCookie(RaveUndoRedoCookie.class);
//            if (urc != null) {
//                UndoRedo ur = urc.getUndoRedoI();
//                if (ur != null) {
//                    ur.redo();
//                }
//            }
            // Use CloneableEditorSupport's UndoRedo to perform redo
            EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
            if (ec instanceof CloneableEditorSupport) {
                CloneableEditorSupport ces = (CloneableEditorSupport)ec;
                UndoRedo undoRedo = getUndoRedo(ces);
                if (undoRedo != null) {
                	undoRedo.redo();
                }                
            }
        }
    }

    /**
     * @return the never-null description for this event.
     */
    public String getDescription() {
        return description != null ? description : "";
    }
   
    public String toString() {
        return getDescription();
    }
    
    // CloneableEditorSupport's UndoRedo is protected, so call it via reflection.
    private static UndoRedo getUndoRedo(CloneableEditorSupport ces) {
        try {
            Method method = CloneableEditorSupport.class.getDeclaredMethod("getUndoRedo", new Class[0]);
            method.setAccessible(true);
            try {
                return (UndoRedo) method.invoke(ces, new Object[0]);
            } catch (IllegalArgumentException ex) {
            	ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (InvocationTargetException ex) {
            	ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (IllegalAccessException ex) {
            	ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        } catch (SecurityException ex) {
        	ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (NoSuchMethodException ex) {
        	ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }
}
