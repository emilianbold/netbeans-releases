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

package org.netbeans.modules.bpel.debugger.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.*;
import org.netbeans.modules.bpel.debugger.api.AnnotationType;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.api.Position;
import org.netbeans.modules.bpel.debugger.api.SourcePath;

import org.openide.util.RequestProcessor;


/**
 * Listens on {@link org.netbeans.api.debugger.DebuggerManager} on
 * {@link BpelDebugger#PROP_CURRENT_POSITION}
 * property and annotates current line in NetBeans editor.
 *
 * @author Alexander Zgursky
 */
public class CurrentPositionAnnotationListener extends DebuggerManagerAdapter {

    // annotation for current line
    private transient Object                myCurrentPositionAnnotation;
    private transient Object                myLock = new Object();
    private Position                        myCurrentPosition;
    private BpelDebugger                    myCurrentDebugger;
    private SourcePath                      mySourcePath;


    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_CURRENT_ENGINE};
    }

    /**
     * Listens BpelDebuggerImpl and DebuggerManager.
     */
    public void propertyChange (PropertyChangeEvent e) {
        if (e.getPropertyName() == DebuggerManager.PROP_CURRENT_ENGINE) {
            updateCurrentDebugger();
            updateCurrentPosition();
            annotate();
        } else if (e.getPropertyName() == BpelDebugger.PROP_CURRENT_POSITION) {
            updateCurrentPosition();
            annotate ();
        }
    }


    // helper methods ..........................................................

    private void updateCurrentDebugger () {
        BpelDebugger newDebugger = getCurrentDebugger();
        if (myCurrentDebugger == newDebugger) {
            return;
        }
        
        if (myCurrentDebugger != null) {
            myCurrentDebugger.removePropertyChangeListener(this);
        }
        if (newDebugger != null) {
            newDebugger.addPropertyChangeListener(this);
            mySourcePath = getCurrentSourcePath();
        }
        myCurrentDebugger = newDebugger;
    }
    
    private static BpelDebugger getCurrentDebugger() {
        DebuggerEngine currentEngine = DebuggerManager.
                getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        return (BpelDebugger)currentEngine.lookupFirst(null, BpelDebugger.class);
    }
    
    private static SourcePath getCurrentSourcePath() {
        DebuggerEngine currentEngine = DebuggerManager.
                getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        return (SourcePath)currentEngine.lookupFirst(null, SourcePath.class);
    }

    private void updateCurrentPosition() {
        if (myCurrentDebugger != null) {
            myCurrentPosition = myCurrentDebugger.getCurrentPosition();
        } else {
            myCurrentPosition = null;
        }
    }
    
    /**
     * Annotates current position or removes annotation.
     */
    private void annotate () {
        final Position position = myCurrentPosition;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                synchronized (myLock) {
                    if (myCurrentPositionAnnotation != null)
                        EditorContextBridge.removeAnnotation(myCurrentPositionAnnotation);
                    if (position != null) {
                        String url = mySourcePath.getSourcePath(position.getProcessQName());
                        if (url != null) {
                            myCurrentPositionAnnotation = EditorContextBridge.annotate(
                                    url,
                                    position.getXpath(),
                                    AnnotationType.CURRENT_POSITION);
                            EditorContextBridge.showSource(
                                    url,
                                    position.getXpath());
                        }
                    }
                }
            }
        });
    }
}
