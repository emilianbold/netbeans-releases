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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTFindUnusedBlocksWalker;

/**
 * implementaion of CsmFileInfoQuery
 * @author Vladimir Voskresenskky
 */
public class FileInfoQueryImpl extends CsmFileInfoQuery {

    public List<String> getSystemIncludePaths(CsmFile file) {
        return getIncludePaths(file, true);
    }

    public List<String> getUserIncludePaths(CsmFile file) {
        return getIncludePaths(file, false);
    }
    
    private List<String> getIncludePaths(CsmFile file, boolean system) {
        List<String> out = Collections.<String>emptyList();
        if (file instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl)file;
            ProjectBase prj = fileImpl.getProjectImpl();
            if (prj != null) {
                APTPreprocHandler.State state = prj.getPreprocState(fileImpl);
                if (state != null) {
                    if (system) {
                        out = APTHandlersSupport.extractSystemIncludePaths(state);
                    } else {
                        out = APTHandlersSupport.extractUserIncludePaths(state);
                    }
                }            
            }   
        }
        return out;
    }

    public List<CsmOffsetable> getUnusedCodeBlocks(CsmFile file) {
        List<CsmOffsetable> out = Collections.<CsmOffsetable>emptyList();
        if (file instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl)file;
            try {
                APTFile apt = APTDriver.getInstance().findAPT(fileImpl.getBuffer());
                // seems next line fits better here but APTLight is not precise, see IZ115345
                // APTFile aptLight = fileImpl.getProjectImpl().getAPTLight(fileImpl);
                if (apt != null) {
                    APTFindUnusedBlocksWalker walker = 
                        new APTFindUnusedBlocksWalker(apt, fileImpl, fileImpl.getPreprocHandler());
                    walker.visit();
                    out = walker.getBlocks();
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return out;
    }
}
