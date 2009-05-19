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

package org.netbeans.modules.cnd.modelimpl.platform;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.openide.util.NbBundle;


/**
 * provides progress bar in status bar
 */
final class ParsingProgress {
    
    private final ProgressHandle handle;
    private int curWorkedUnits = 0;
    private int maxWorkUnits = 0;
    private int addedAfterStartParsing = 0;
    private int allWork = 0; // in procent points.
    private static final double ALL_WORK_DOUBLE = 10000.0;
    private static final int ALL_WORK_INT = 10000;
    private boolean started = false;
    private boolean determinate = false;
    private static final boolean TRACE = true;
    
    /**  
     * Delay amount of miliseconds 
     * that shall pass before the progress appears in status bar
     */
    private static final int INITIAL_DELAY = 1000; // ms
    
    /**
     * Constructs progress information for project
     */
    public ParsingProgress(CsmProject project) {
        String msg=NbBundle.getMessage(ModelSupport.class, "MSG_ParsingProgress", project.getName());
        handle = ProgressHandleFactory.createHandle(msg);
    }
    
    /**
     * Start the progress indication for indeterminate task.
     * it will be visualized by a progress bar in indeterminate mode.
     */
    public void start() {
        synchronized (handle) {
            if(!started) {
                started = true;
                handle.setInitialDelay(INITIAL_DELAY);
                handle.start();
            }
        }
    }
    
    /**
     * finish the task, remove the task's component from the progress bar UI.
     */        
    public void finish() {
        synchronized (handle) {
            if( started ) {
                handle.finish();
                started = false;
            }
        }
    }

    /**
     * inform about adding header to reparse
     */
    public void addedToParse(CsmFile file) {
        addedAfterStartParsing++;
    }

    /**
     * inform about starting handling next file item
     */
    public void nextCsmFile(CsmFile file) {
        synchronized (handle) {
            if( ! started || !determinate) {
                return;
            }
            if( curWorkedUnits < maxWorkUnits + addedAfterStartParsing) {
                curWorkedUnits++;
                double ratio = 1.0;
                if (maxWorkUnits + addedAfterStartParsing > 0) {
                    ratio = ALL_WORK_DOUBLE / (maxWorkUnits + addedAfterStartParsing);
                }
                int work = (int)(ratio * curWorkedUnits);
                if (allWork <= work && work < ALL_WORK_INT) {
                    allWork = work;
                }
            }
            try {
                if (TRACE) {
                    handle.progress(file.getName().toString()+" ("+curWorkedUnits+" of "+(maxWorkUnits + addedAfterStartParsing)+")", allWork); // NOI18N
                } else  {
                    handle.progress(file.getName().toString(), allWork);
                }
                //assert(curWorkedUnits <= maxWorkUnits);
            } catch (NullPointerException ex) {
                // very strange... but do not interrupt process
                DiagnosticExceptoins.register(ex);
            }
        }
    }

    /**
     * Currently indeterminate task can be switched to show percentage completed.
     * A common usecase is to calculate the amount of work in the beginning showing
     * in indeterminate mode and later switch to the progress with known steps
     */
    public void switchToDeterminate(int maxWorkUnits) {
        synchronized (handle) {
            if( ! started ) {
                return;
            }
            this.maxWorkUnits = maxWorkUnits;
            addedAfterStartParsing = 0;
            handle.switchToDeterminate(ALL_WORK_INT);
            determinate = true;
        }
    }
}   
