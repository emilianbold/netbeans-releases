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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTAbstractWalker;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTFileCacheManager;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTWalker;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBufferFile;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;

/**
 * APT Walker which only gathers macromap. Shouldn't be used directly but
 * only overriden by walkers which don't need to gather any info in the includes
 * just want macromap from them.
 * Also this walker holds plumbing code for blocks gathering due to it's being
 * used only for semantic highlighting walkers. This should be refactored out if
 * more uses for SelfWalking would appear.
 * 
 * @author Sergey Grinev
 */
public class APTSelfWalker extends APTAbstractWalker {

    protected APTSelfWalker(APTFile apt, APTPreprocHandler preprocHandler, APTFileCacheEntry cacheEntry) {
        super(apt, preprocHandler, cacheEntry);
    }
    
    protected boolean include(ResolvedPath resolvedPath, APTInclude aptInclude, APTMacroMap.State postIncludeState) {
        if (resolvedPath != null && getIncludeHandler().pushInclude(resolvedPath.getPath(), aptInclude, resolvedPath.getIndex())) {
            try {
                APTFile apt = APTDriver.getInstance().findAPTLight(new FileBufferFile(resolvedPath.getPath()));
                APTPreprocHandler preprocHandler = getPreprocHandler();
                APTFileCacheEntry cache = APTFileCacheManager.getEntry(resolvedPath.getPath(), preprocHandler, null);
                createIncludeWalker(apt, this, resolvedPath.getPath(), cache).visit();
                // does not remember walk to safe memory
                // APTFileCacheManager.setAPTCacheEntry(resolvedPath.getPath(), preprocHandler, cache, false);
            } catch (FileNotFoundException ex) {
                APTUtils.LOG.log(Level.WARNING, "APTSelfWalker: file {0} not found", new Object[] {resolvedPath.getPath()});// NOI18N
		DiagnosticExceptoins.register(ex);
            } catch (IOException ex) {
                APTUtils.LOG.log(Level.SEVERE, "APTSelfWalker: error on including {0}:\n{1}", new Object[] {resolvedPath.getPath(), ex});
		DiagnosticExceptoins.register(ex);
            } finally {
                getIncludeHandler().popInclude();
            }
            return postIncludeState == null;
        } else {
            return false;
        }
    }
    
    protected APTWalker createIncludeWalker(APTFile apt, APTSelfWalker parent, CharSequence includePath, APTFileCacheEntry cache) {
        return new APTSelfWalker(apt, parent.getPreprocHandler(), cache);
    }

    @Override
    protected boolean hasIncludeActionSideEffects() {
        return false;
    }
}

