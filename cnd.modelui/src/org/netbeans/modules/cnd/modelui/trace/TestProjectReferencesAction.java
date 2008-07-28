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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelui.trace;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.trace.TraceXRef;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Vladirmir Voskresensky
 */
public class TestProjectReferencesAction extends TestProjectActionBase {

    private static boolean running = false;
    private final boolean allReferences;
    private final boolean analyzeStatistics;
    private Boolean reportUnresolved = Boolean.TRUE;

    public static Action getSmartCompletionAnalyzerAction() {
        return SharedClassObject.findObject(SmartCompletionAnalyzerAction.class, true);
    }
    
    public static Action getTestReparseAction() {
        return SharedClassObject.findObject(TestReparseAction.class, true);
    }
    
    public static Action getDirectUsageReferencesAction() {
        return SharedClassObject.findObject(DirectUsageAction.class, true);
    }
    
    public static Action getAllReferencesAction() {
        return SharedClassObject.findObject(AllUsagesAction.class, true);
    }
    
    static final class SmartCompletionAnalyzerAction extends TestProjectReferencesAction {

        SmartCompletionAnalyzerAction() {
            super(false, true);
        }
    }
    
    static final class DirectUsageAction extends TestProjectReferencesAction {
        DirectUsageAction() {
            super(false, false);
        }
    }
    
    static final class AllUsagesAction extends TestProjectReferencesAction {

        AllUsagesAction() {
            super(true, false, null);
        }
    }

    protected TestProjectReferencesAction(boolean allReferences, boolean analyzeStatistics) {
        this(allReferences, analyzeStatistics, Boolean.TRUE);
    }
    
    protected TestProjectReferencesAction(boolean allReferences, boolean analyzeStatistics, Boolean reportUnresolved) {
        this.allReferences = allReferences;
        this.analyzeStatistics = analyzeStatistics;
        this.reportUnresolved = reportUnresolved;
    }

    public String getName() {
        String nameKey;
        if (analyzeStatistics) {
            nameKey = "CTL_TestProjectSmartCCDirectUsageReferencesAction"; // NOI18N
        } else {
            nameKey = (allReferences ? "CTL_TestProjectReferencesAction" : "CTL_TestProjectDirectUsageReferencesAction"); // NOI18N
        }
        return NbBundle.getMessage(getClass(), nameKey); // NOI18N
    }

    protected void performAction(Collection<NativeProject> projects) {
        Boolean oldReportUnresolved = reportUnresolved;
        if (reportUnresolved == null) {
            Object option = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                    "Report unresolved references?", //NOI18N
                    "Test References")); //NOI18N
            if (option == NotifyDescriptor.YES_OPTION) {
                reportUnresolved = Boolean.TRUE;
            } else if (option == NotifyDescriptor.NO_OPTION) {
                reportUnresolved = Boolean.FALSE;
            } else { // if (option == NotifyDescriptor.CANCEL_OPTION) {
                return;
            }
        }
        if (projects != null) {
            for (NativeProject p : projects) {
                testProject(p);
            }
        }
        reportUnresolved = oldReportUnresolved;
    }

    
    private void testProject(NativeProject p) {
        String task = (this.allReferences ? "All " : "Direct usage ") + "xRef - " + p.getProjectDisplayName() + (this.analyzeStatistics ? " Statistics" : ""); // NOI18N
        InputOutput io = IOProvider.getDefault().getIO(task, false);
        io.select();
        final AtomicBoolean canceled = new AtomicBoolean(false);        
        final ProgressHandle handle = ProgressHandleFactory.createHandle(task, new Cancellable() {
            public boolean cancel() {
                canceled.set(true);
                return true;
            }
        });
        handle.start();
        final OutputWriter out = io.getOut();
        final OutputWriter err = io.getErr();
        final long[] time = new long[2];
        time[0] = System.currentTimeMillis();
        Set<CsmReferenceKind> interestedElems = this.allReferences ? CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE : EnumSet.<CsmReferenceKind>of(CsmReferenceKind.DIRECT_USAGE);
            
        TraceXRef.traceProjectRefsStatistics(p, new TraceXRef.StatisticsParameters(interestedElems, analyzeStatistics,
                (reportUnresolved == null) ? true : reportUnresolved.booleanValue()), out, err, new CsmProgressAdapter() {
            private int handled = 0;
            @Override
            public void projectFilesCounted(CsmProject project, int filesCount) {
                err.flush();
                out.println("Project " + project.getName() + " has " + filesCount + " files"); // NOI18N
                out.flush();
                handle.switchToDeterminate(filesCount);
            }

            @Override
            public void fileParsingStarted(CsmFile file) {
                handle.progress("Analyzing " + file.getName(), handled++); // NOI18N
            }

            @Override
            public void projectParsingFinished(CsmProject project) {
                time[1] = System.currentTimeMillis();
            }
        }, canceled);
        handle.finish();
        out.println("Analyzing " + p.getProjectDisplayName() + " took " + (time[1]-time[0]) + "ms"); // NOI18N
        err.flush();
        out.flush();
    }
}
