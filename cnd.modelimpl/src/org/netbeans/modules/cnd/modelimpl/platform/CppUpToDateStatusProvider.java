/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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
	CsmModelAccessor.getModel().enqueue(new Runnable() {
	    public void run() {
		CsmFile file = getCsmFile(null);
		if (file == null){
		    current = UpToDateStatus.UP_TO_DATE_DIRTY;
		} else {
		    if (file.isParsed()){
			current = UpToDateStatus.UP_TO_DATE_OK;
		    } else {
			current = UpToDateStatus.UP_TO_DATE_PROCESSING;
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
	return getPath().endsWith(file.getName());
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
	    CsmModelAccessor.getModel().enqueue(new Runnable() {
		public void run() {
		    if (getCsmFile(project) == file) {
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
	    CsmModelAccessor.getModel().enqueue(new Runnable() {
		public void run() {
		    if (getCsmFile(project) == file) {
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
	    CsmModelAccessor.getModel().enqueue(new Runnable() {
		public void run() {
		    if (getCsmFile(project) == file) {
			changeStatus(UpToDateStatus.UP_TO_DATE_OK);
		    }
		}
	    }, threadName);
        }
    }

    public void parserIdle() {
    }

    public void projectOpened(CsmProject project) {
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
            if (file == null || file.getProject() == project) {
                changeStatus(UpToDateStatus.UP_TO_DATE_DIRTY);
            }
        }
    }

    public void modelChanged(final CsmChangeEvent e) {
        if (uid != null) {
            if (mightBeMine(e.getRemovedFiles()) ) {
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
            if (file == f) {
                changeStatus(UpToDateStatus.UP_TO_DATE_DIRTY);
                filePath = null;
                uid = null;
            }
        }
    }
}
