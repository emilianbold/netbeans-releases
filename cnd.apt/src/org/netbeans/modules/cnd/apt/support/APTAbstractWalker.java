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

package org.netbeans.modules.cnd.apt.support;

import java.util.Collection;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.impl.support.APTHandlersSupportImpl;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.structure.APTIncludeNext;
import org.netbeans.modules.cnd.apt.structure.APTPragma;
import org.netbeans.modules.cnd.apt.structure.APTUndefine;
import org.netbeans.modules.cnd.apt.support.APTMacro.Kind;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.openide.filesystems.FileSystem;

/**
 * abstract Tree walker for APT
 * @author Vladimir Voskresensky
 */
public abstract class APTAbstractWalker extends APTWalker {
    private final APTPreprocHandler preprocHandler;
    private final CharSequence startPath;
    private final FileSystem startFileSystem;
    private final APTFileCacheEntry cacheEntry;
    
    protected APTAbstractWalker(APTFile apt, APTPreprocHandler preprocHandler, APTFileCacheEntry cacheEntry) {
        super(apt, preprocHandler == null ? null: preprocHandler.getMacroMap());
        this.startPath = apt.getPath();
        this.startFileSystem = apt.getFileSystem();
        this.preprocHandler = preprocHandler;
        this.cacheEntry = cacheEntry;
    }

    @Override
    protected void preInit() {
        super.preInit();
        if (preprocHandler != null) {
            APTIncludeHandler includeHandler = preprocHandler.getIncludeHandler();
            if (APTHandlersSupportImpl.isFirstLevel(includeHandler)) {
                // special handling of "-include file" feature of preprocessor
                final Collection<IncludeDirEntry> extractIncludeFileEntries = APTHandlersSupportImpl.extractIncludeFileEntries(includeHandler);
                int index = 0 - extractIncludeFileEntries.size();
                for (IncludeDirEntry includeDirEntry : extractIncludeFileEntries) {
                    APT fake = new APTIncludeFake(includeDirEntry.getAsSharedCharSequence().toString(), index++);
                    onAPT(fake, false);
                }
            }
        }
    }

    @Override
    protected void onInclude(APT apt) {
        if (getIncludeHandler() != null) {
            APTIncludeResolver resolver = getIncludeHandler().getResolver(startFileSystem, startPath);
            ResolvedPath resolvedPath = resolver.resolveInclude((APTInclude)apt, getMacroMap());
            if (resolvedPath == null) {
                if (DebugUtils.STANDALONE) {
                    if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                        System.err.println("FAILED INCLUDE: from " + CndPathUtilitities.getBaseName(startPath.toString()) + " for:\n\t" + apt);// NOI18N
                    }
                } else {
                    APTUtils.LOG.log(Level.WARNING,
                            "failed resolving path from {0} for {1}", // NOI18N
                            new Object[] { startPath, apt });
                }
            }
            includeImpl(resolvedPath, (APTInclude)apt);
        }
    }
    
    @Override
    protected void onIncludeNext(APT apt) {
        if (getIncludeHandler() != null) {
            APTIncludeResolver resolver = getIncludeHandler().getResolver(startFileSystem, startPath); 
            ResolvedPath resolvedPath = resolver.resolveIncludeNext((APTIncludeNext)apt, getMacroMap());
            if (resolvedPath == null) {
                if (DebugUtils.STANDALONE) {
                    if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                        System.err.println("FAILED INCLUDE: from " + CndPathUtilitities.getBaseName(startPath.toString()) + " for:\n\t" + apt);// NOI18N
                    }
                } else {
                    APTUtils.LOG.log(Level.WARNING,
                            "failed resolving path from {0} for {1}", // NOI18N
                            new Object[] { startPath, apt });
                }
            }
            includeImpl(resolvedPath, (APTInclude) apt);
        }
    }

    private void includeImpl(ResolvedPath resolvedPath, APTInclude aptInclude) {
        if (cacheEntry != null) {
            if (!startPath.equals(cacheEntry.getFilePath())) {
                System.err.println("using not expected entry " + cacheEntry + " when work with file " + startPath);
            }
            if (cacheEntry.isSerial()) {
                serialIncludeImpl(aptInclude, resolvedPath);
            } else {
                Object lock = cacheEntry.getIncludeLock(aptInclude);
                synchronized (lock) {
                    serialIncludeImpl(aptInclude, resolvedPath);
                }
            }
        } else {
            include(resolvedPath, aptInclude, null);
        }
    }

    /**
     * 
     * @param resolvedPath
     * @param aptInclude
     * @param postIncludeState cached information about visit of this include directive
     * @return true if need to cache post include state
     */
    abstract protected boolean include(ResolvedPath resolvedPath, APTInclude aptInclude, PostIncludeData postIncludeState);
    abstract protected boolean hasIncludeActionSideEffects();

    @Override
    protected void onDefine(APT apt) {
        APTDefine define = (APTDefine)apt;
        if (define.isValid()) {
            getMacroMap().define(getRootFile(), define, Kind.DEFINED);
        } else {
            if (DebugUtils.STANDALONE) {
                if (APTUtils.LOG.getLevel().intValue() <= Level.SEVERE.intValue()) {
                    System.err.println("INCORRECT #define directive: in " + CndPathUtilitities.getBaseName(startPath.toString()) + " for:\n\t" + apt);// NOI18N
                }
            } else {
                APTUtils.LOG.log(Level.SEVERE,
                        "INCORRECT #define directive: in {0} for:\n\t{1}", // NOI18N
                        new Object[] { CndPathUtilitities.getBaseName(startPath.toString()), apt });
            }
        }
    }

    @Override
    protected void onPragmaNode(APT apt) {
        APTPragma pragma = (APTPragma) apt;
        APTToken name = pragma.getName();
        if (name != null && APTPragma.PRAGMA_ONCE.contentEquals(name.getTextID())) {
            if (getMacroMap().isDefined(getFileOnceMacroName())) {
                // if already included => stop
                super.stop();
            } else {
                APTDefine fileOnce = APTUtils.createAPTDefineOnce(getFileOnceMacroName());
                getMacroMap().define(getRootFile(), fileOnce, Kind.DEFINED);
            }
        }
    }
    
    protected final CharSequence getFileOnceMacroName() {
        return APTUtils.getFileOnceMacroName(getRootFile());
    }

    @Override
    protected void onUndef(APT apt) {
        APTUndefine undef = (APTUndefine)apt;
        getMacroMap().undef(getRootFile(), undef.getName());
    }
    
    @Override
    protected boolean onIf(APT apt) {
        return eval(apt);
    }
    
    @Override
    protected boolean onIfdef(APT apt) {
        return eval(apt);
    }
    
    @Override
    protected boolean onIfndef(APT apt) {
        return eval(apt);
    }
    
    @Override
    protected boolean onElif(APT apt, boolean wasInPrevBranch) {
        return !wasInPrevBranch && eval(apt);
    }
    
    @Override
    protected boolean onElse(APT apt, boolean wasInPrevBranch) {
        return !wasInPrevBranch;
    }
    
    @Override
    protected void onEndif(APT apt, boolean wasInBranch) {
    }

    protected void onEval(APT apt, boolean result) {
    }

    protected APTPreprocHandler getPreprocHandler() {
        return preprocHandler;
    }
    
    protected APTIncludeHandler getIncludeHandler() {
        return getPreprocHandler() == null ? null: getPreprocHandler().getIncludeHandler();
    }   
 
    ////////////////////////////////////////////////////////////////////////////
    // implementation details
   
    private boolean eval(APT apt) {
        if (APTUtils.LOG.isLoggable(Level.FINE)) {
            APTUtils.LOG.log(Level.FINE, "eval condition for {0}", new Object[] {apt});// NOI18N
        }
        boolean res = false;
        try {
            Boolean cachedRes = cacheEntry != null ? cacheEntry.getEvalResult(apt) : null;
            if (cachedRes != null) {
                res = cachedRes.booleanValue();
            } else {
                res = APTConditionResolver.evaluate(apt, getMacroMap());
                if (cacheEntry != null) {
                    cacheEntry.setEvalResult(apt, res);
                }
            }
        } catch (TokenStreamException ex) {
            APTUtils.LOG.log(Level.SEVERE, "error on evaluating condition node {0}\n{1}", new Object[] { apt, ex });// NOI18N
        }
        onEval(apt, res);
        return res;
    }

    private void serialIncludeImpl(APTInclude aptInclude, ResolvedPath resolvedPath) {
        PostIncludeData postIncludeData = cacheEntry.getPostIncludeState(aptInclude);
        if (postIncludeData.hasPostIncludeMacroState() && !hasIncludeActionSideEffects()) {
            getPreprocHandler().getMacroMap().setState(postIncludeData.getPostIncludeMacroState());
            return;
        }
        if (include(resolvedPath, aptInclude, postIncludeData)) {
            APTMacroMap.State postIncludeMacroState = getPreprocHandler().getMacroMap().getState();
            PostIncludeData newData = new PostIncludeData(postIncludeMacroState, postIncludeData.getDeadBlocks());
            cacheEntry.setIncludeData(aptInclude, newData);
        } else if (postIncludeData != null && !postIncludeData.hasPostIncludeMacroState()) {
            // clean what could be set in dead blocks, because of false include activity
            postIncludeData.setDeadBlocks(null);
        }
    }
}
