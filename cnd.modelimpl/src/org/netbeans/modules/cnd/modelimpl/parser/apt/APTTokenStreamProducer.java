/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.spi.APTIndexFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.ParserQueue;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.parser.spi.TokenStreamProducer;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.platform.FileBufferDoc;
import org.netbeans.modules.cnd.support.Interrupter;
import org.openide.util.Lookup;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class APTTokenStreamProducer extends TokenStreamProducer {
    private final APTFile fullAPT;
    private PreprocHandler curPreprocHandler;
    private APTBasedPCStateBuilder pcBuilder;
    
    private APTTokenStreamProducer(FileImpl file, FileContent newFileContent, APTFile fullAPT) {
        super(file, newFileContent);
        this.fullAPT = fullAPT;
    }

    public static TokenStreamProducer createImpl(FileImpl file, FileContent newFileContent, boolean index) {
        APTFile fullAPT = getFileAPT(file, true);
        if (fullAPT == null) {
            return null;
        }
        if (index) {
            if (CndTraceFlags.TEXT_INDEX) {
                Collection<? extends APTIndexFilter> indexFilters = Collections.emptyList();
                Object pp = file.getProject().getPlatformProject();
                if (pp instanceof NativeProject) {
                    final Lookup.Provider project = ((NativeProject) pp).getProject();
                    if (project != null) {
                        indexFilters = project.getLookup().lookupAll(APTIndexFilter.class);
                    }
                }
                APTIndexingWalker aptIndexingWalker = new APTIndexingWalker(fullAPT, file.getTextIndexKey(), indexFilters);
                aptIndexingWalker.index();
            }  
        }
        return new APTTokenStreamProducer(file, newFileContent, fullAPT);
    }

    @Override
    public TokenStream getTokenStream(boolean triggerParsingActivity, Interrupter interrupter) {
        FileImpl fileImpl = getMainFile();
        PreprocHandler preprocHandler = getCurrentPreprocHandler();
        // use full APT for generating token stream
        if (TraceFlags.TRACE_CACHE) {
            System.err.println("CACHE: parsing using full APT for " + fileImpl.getAbsolutePath());
        }        
        // make real parse
        PreprocHandler.State ppState = preprocHandler.getState();
        ProjectBase startProject = Utils.getStartProject(ppState);
        if (startProject == null) {
            System.err.println(" null project for " + APTHandlersSupport.extractStartEntry(ppState) + // NOI18N
                    "\n while parsing file " + fileImpl.getAbsolutePath() + "\n of project " + fileImpl.getProject()); // NOI18N
            return null;
        }        
        // We gather conditional state here as well, because sources are not included anywhere
        pcBuilder = new APTBasedPCStateBuilder(fileImpl.getAbsolutePath());
        // ask for concurrent entry if absent
        APTFileCacheEntry aptCacheEntry = fileImpl.getAPTCacheEntry(ppState, Boolean.FALSE);
        APTParseFileWalker walker = new APTParseFileWalker(startProject, fullAPT, fileImpl, preprocHandler, triggerParsingActivity, pcBuilder,aptCacheEntry);
        walker.setFileContent(getFileContent());
        if (TraceFlags.DEBUG) {
            System.err.println("doParse " + fileImpl.getAbsolutePath() + " with " + ParserQueue.tracePreprocState(ppState));
        }

        TokenStream filteredTokenStream = walker.getFilteredTokenStream(fileImpl.getLanguageFilter(ppState));
        return filteredTokenStream;
    }

    @Override
    public FilePreprocessorConditionState release() {
        return pcBuilder.build();
    }
    
    public static APTFile getFileAPT(FileImpl file, boolean full) {
        APTFile fileAPT = null;
        FileBufferDoc.ChangedSegment changedSegment = null;
        try {
            if (full) {
                fileAPT = APTDriver.findAPT(file.getBuffer(), file.getFileLanguage(), file.getFileLanguageFlavor());
            } else {
                fileAPT = APTDriver.findAPTLight(file.getBuffer());
            }
            if (file.getBuffer() instanceof FileBufferDoc) {
                changedSegment = ((FileBufferDoc) file.getBuffer()).getLastChangedSegment();
            }
        } catch (FileNotFoundException ex) {
            APTUtils.LOG.log(Level.WARNING, "FileImpl: file {0} not found, probably removed", new Object[]{file.getBuffer().getAbsolutePath()});// NOI18N
        } catch (IOException ex) {
            DiagnosticExceptoins.register(ex);
        }
        if (fileAPT != null && APTUtils.LOG.isLoggable(Level.FINE)) {
            CharSequence guardMacro = fileAPT.getGuardMacro();
            if (guardMacro.length() == 0 && !file.isSourceFile()) {
                APTUtils.LOG.log(Level.FINE, "FileImpl: file {0} does not have guard", new Object[]{file.getBuffer().getAbsolutePath()});// NOI18N
            }
        }
        return fileAPT;
    }    
}
