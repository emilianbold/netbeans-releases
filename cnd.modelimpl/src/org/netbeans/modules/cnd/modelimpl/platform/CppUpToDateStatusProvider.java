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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import java.io.File;
import java.util.Collection;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmChangeEvent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelListener;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *org.netbeans.modules.cnd.highlight.CppUpToDateStatusProvider
 * @author Alexander Simon
 */
public class CppUpToDateStatusProvider extends UpToDateStatusProvider implements CsmProgressListener, CsmModelListener {
    private String filePath;
    private CsmUID<CsmFile> uid;
    private BaseDocument document;
    private UpToDateStatus current = UpToDateStatus.UP_TO_DATE_OK;
    private static String threadName = "Up to date status provider thread"; //NOI18N
    
    /** Creates a new instance of CppUpToDateStatusProvider */
    public CppUpToDateStatusProvider(BaseDocument document) {
        this.document = document;
        CsmModelAccessor.getModel().addProgressListener(this);
        CsmModelAccessor.getModel().addModelListener(this);
	if( TraceFlags.TRACE_UP_TO_DATE_PROVIDER ) System.err.printf("CppUpToDateStatusProvider.ctor\n");
	current = UpToDateStatus.UP_TO_DATE_DIRTY;
        // it's more like a model client, so we use enqueue, not enqueueModelTask here
	CsmModelAccessor.getModel().enqueue(new Runnable() {
	    public void run() {
		CsmFile file = getCsmFile(null);
		if (file == null){
		    changeStatus(UpToDateStatus.UP_TO_DATE_DIRTY);
		} else {
		    if (file.isParsed()){
			changeStatus(UpToDateStatus.UP_TO_DATE_OK);
		    } else {
			changeStatus(UpToDateStatus.UP_TO_DATE_PROCESSING);
		    }
		}
	    }
	}, threadName);
    }
    
    public UpToDateStatus getUpToDate() {
        return current;
    }
        
    private CsmFile getCsmFile(CsmProject project) {
        CsmFile csmFile = null;
        if (uid == null) {
            if (getPath() != null) {
                if (project == null) {
                    csmFile = CsmModelAccessor.getModel().findFile(getPath());
                } else {
                    if (project instanceof ProjectBase) {
                        csmFile = ((ProjectBase)project).getFile(new File(getPath()));
                    } else {
                        csmFile = project.findFile(getPath());
                    }
                }
            }
            if (csmFile != null) {
                uid = csmFile.getUID();
            }
        } else {
            csmFile = uid.getObject();
            if (csmFile != null && !csmFile.isValid()) {
                uid = null;
                csmFile = null;
                filePath = null;
                changeStatus(UpToDateStatus.UP_TO_DATE_DIRTY);
            }
        }
        return csmFile;
    }

    private String getPath() {
        if (filePath == null) {
            DataObject dao = NbEditorUtilities.getDataObject(document);
            if (dao == null || !dao.isValid()) {
                return "";//NOI18N
            }
            FileObject fo = dao.getPrimaryFile();
            filePath = File.separator+fo.getPath();
        }
        return filePath;
    }
    
    private void changeStatus(UpToDateStatus status){
        if (current != status){
            firePropertyChange(PROP_UP_TO_DATE, current, status);
            current = status;
        }
    }
    
    public void projectParsingStarted(CsmProject project) {
	if( TraceFlags.TRACE_UP_TO_DATE_PROVIDER ) System.err.printf("CppUpToDateStatusProvider.projectParsingStarted %s\n", project);
    }

    public void projectFilesCounted(CsmProject project, int filesCount) {
	if( TraceFlags.TRACE_UP_TO_DATE_PROVIDER ) System.err.printf("CppUpToDateStatusProvider.projectFilesCounted %s\n", project);
    }

    public void projectParsingFinished(CsmProject project) {
	if( TraceFlags.TRACE_UP_TO_DATE_PROVIDER ) System.err.printf("CppUpToDateStatusProvider.projectParsingFinished %s\n", project);
    }

    public void projectParsingCancelled(CsmProject project) {
	if( TraceFlags.TRACE_UP_TO_DATE_PROVIDER ) System.err.printf("CppUpToDateStatusProvider.projectParsingCancelled %s\n", project);
    }

    private boolean mightBeMine(CsmFile file) {
	return getPath().endsWith(file.getName().toString());
    }
    
    private boolean mightBeMine(Collection<CsmFile> files) {
	for( CsmFile file : files ) {
	    if( mightBeMine(file) ) {
		return true;
	    }
	}
	return false;
    }
    
    public void fileInvalidated(final CsmFile file) {
	if( TraceFlags.TRACE_UP_TO_DATE_PROVIDER ) System.err.printf("CppUpToDateStatusProvider.fileInvalidated %s\n", file);
        if (mightBeMine(file) ) {
            final CsmProject project = file.getProject();
            // it's more like a model client, so we use enqueue, not enqueueModelTask here
	    CsmModelAccessor.getModel().enqueue(new Runnable() {
		public void run() {
		    if (file.equals(getCsmFile(project))) {
			changeStatus(UpToDateStatus.UP_TO_DATE_PROCESSING);
		    }
		}
	    }, threadName);
        }
    }

    public void fileParsingStarted(final CsmFile file) {
	if( TraceFlags.TRACE_UP_TO_DATE_PROVIDER ) System.err.printf("CppUpToDateStatusProvider.fileParsingStarted %s\n", file);
        if (mightBeMine(file) ) {
            final CsmProject project = file.getProject();
            // it's more like a model client, so we use enqueue, not enqueueModelTask here
	    CsmModelAccessor.getModel().enqueue(new Runnable() {
		public void run() {
		    if (file.equals(getCsmFile(project))) {
			changeStatus(UpToDateStatus.UP_TO_DATE_PROCESSING);
		    }
		}
	    }, threadName);
        }
    }
    
    public void projectLoaded(CsmProject project) {
	if( TraceFlags.TRACE_UP_TO_DATE_PROVIDER ) System.err.printf("CppUpToDateStatusProvider.projectLoaded %s\n", project);
	CsmFile file = getCsmFile(project);
	if( file != null) {
	    changeStatus(file.isParsed() ? UpToDateStatus.UP_TO_DATE_OK : UpToDateStatus.UP_TO_DATE_DIRTY);
	}
    }    
    
    public void fileParsingFinished(final CsmFile file) {
	if( TraceFlags.TRACE_UP_TO_DATE_PROVIDER ) System.err.printf("CppUpToDateStatusProvider.fileParsingFinished %s\n", file);
        if (mightBeMine(file) ) {
            final CsmProject project = file.getProject();
            // it's more like a model client, so we use enqueue, not enqueueModelTask here
	    CsmModelAccessor.getModel().enqueue(new Runnable() {
		public void run() {
		    if (file.equals(getCsmFile(project))) {
			changeStatus(UpToDateStatus.UP_TO_DATE_OK);
		    }
		}
	    }, threadName);
        }
    }

    public void parserIdle() {
    }

    public void projectOpened(CsmProject project) {
	if( TraceFlags.TRACE_UP_TO_DATE_PROVIDER ) System.err.printf("CppUpToDateStatusProvider.projectOpened %s\n", project);
        CsmFile file = getCsmFile(project);
        if (file == null) {
            changeStatus(UpToDateStatus.UP_TO_DATE_DIRTY);
        } else if (file.isParsed()) {
            changeStatus(UpToDateStatus.UP_TO_DATE_OK);
        } else {
            changeStatus(UpToDateStatus.UP_TO_DATE_PROCESSING);
        }
    }

    public void projectClosed(CsmProject project) {
        if (uid != null) {
            CsmFile file = getCsmFile(project);
            if (file == null || project.equals(file.getProject())) {
                changeStatus(UpToDateStatus.UP_TO_DATE_DIRTY);
            }
        }
    }

    public void modelChanged(final CsmChangeEvent e) {
        if (uid != null) {
            if (mightBeMine(e.getRemovedFiles()) ) {
                // it's more like a model client, so we use enqueue, not enqueueModelTask here
                CsmModelAccessor.getModel().enqueue(new Runnable() {
                    public void run() {
                        modelChanged2(e);
                    }
                }, threadName);
            }
        }
    }
    
    private void modelChanged2(CsmChangeEvent e) {
        CsmFile file = null;
        for (CsmFile f : e.getRemovedFiles()){
            if (file == null) {
                file = getCsmFile(null);
                if (file == null){
                    return;
                }
            }
            if (equals(file, f)) {
                changeStatus(UpToDateStatus.UP_TO_DATE_DIRTY);
                filePath = null;
                uid = null;
            }
        }
    }
    
    private final boolean equals(CsmFile a, CsmFile b) {
	return (a == null) ? (b == null) : a.equals(b);
    }
}
