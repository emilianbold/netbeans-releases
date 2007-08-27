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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTAbstractWalker;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.LibraryManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.openide.filesystems.FileUtil;

/**
 * base walker to visit project files based APTs
 * @author Vladimir Voskresensky
 */
public abstract class APTProjectFileBasedWalker extends APTAbstractWalker {
    private final FileImpl file;
    private final ProjectBase startProject;
    private int mode;
    
    public APTProjectFileBasedWalker(ProjectBase startProject, APTFile apt, FileImpl file, APTPreprocHandler preprocHandler) {
        super(apt, preprocHandler);
        this.mode = ProjectBase.GATHERING_MACROS;
        this.file = file;
        this.startProject = startProject;
        assert startProject != null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of abstract methods
    
    protected void include(ResolvedPath resolvedPath, APTInclude apt) {
        FileImpl included = null;
        if (resolvedPath != null) {
            String path = resolvedPath.getPath();
            if (path.indexOf("..") > 0) { // NOI18N
                path = FileUtil.normalizeFile(new File(path)).getAbsolutePath();
            }
            if (getIncludeHandler().pushInclude(path, apt.getToken().getLine())) {
                ProjectBase startProject = this.getStartProject();
                if (startProject != null) {
                    ProjectBase inclFileOwner = LibraryManager.getInsatnce().resolveFileProjectOnInclude(startProject, getFile(), resolvedPath);
                    try {
                        included = includeAction(inclFileOwner, path, mode, apt);
                    } catch (FileNotFoundException ex) {
                        APTUtils.LOG.log(Level.SEVERE, "file {0} not found", new Object[] {path});// NOI18N
                    } catch (IOException ex) {
                        APTUtils.LOG.log(Level.SEVERE, "error on including {0}:\n{1}", new Object[] {path, ex});
                    }
                } else {
                    APTUtils.LOG.log(Level.SEVERE, "file {0} without project!!!", new Object[] {file});// NOI18N
                    getIncludeHandler().popInclude();
                }
            }
        }
	postInclude(apt, included);
    }
    
    abstract protected FileImpl includeAction(ProjectBase inclFileOwner, String inclPath, int mode, APTInclude apt) throws IOException;

    protected void postInclude(APTInclude apt, FileImpl included) {
    }
    
    protected FileImpl getFile() {
        return this.file;
    }

    protected ProjectBase getStartProject() {
	return this.file.getProjectImpl();
    }
    
    protected void setMode(int mode) {
        this.mode = mode;
    }
    
}
