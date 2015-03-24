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
package org.netbeans.modules.cnd.modelimpl.parser.clank;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.ClankDriver.ClankPreprocessorCallback;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageFilter;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.spi.TokenStreamProducer;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class ClankTokenStreamProducer extends TokenStreamProducer {

    private ClankTokenStreamProducer(FileImpl file, FileContent newFileContent) {
        super(file, newFileContent);
    }
    
    public static TokenStreamProducer createImpl(FileImpl file, FileContent newFileContent, boolean index) {
        return new ClankTokenStreamProducer(file, newFileContent);
    }

    @Override
    public TokenStream getTokenStream(boolean triggerParsingActivity, Interrupter interrupter) {
        PreprocHandler ppHandler = getCurrentPreprocHandler();
        ClankDriver.APTTokenStreamCache extractTokenStream = ClankDriver.extractTokenStream(ppHandler);
        assert extractTokenStream != null;
        FileImpl fileImpl = getMainFile();
        if (!extractTokenStream.hasTokenStream()) {
          // do preprocessing
          MyClankPreprocessorCallback callback = new MyClankPreprocessorCallback(
                  ppHandler,
                  triggerParsingActivity,
                  fileImpl, extractTokenStream.getFileIndex());
          boolean tsFromClank = ClankDriver.preprocess(fileImpl.getBuffer(), ppHandler, callback, interrupter);
          if (!tsFromClank) {
              return null;
          }
          extractTokenStream = callback.getPPOut();
          if (extractTokenStream == null) {
            return null;
          }
        }
        TokenStream tokenStream = extractTokenStream.getTokenStream();
        if (tokenStream == null) {
          return null;
        }
        APTLanguageFilter languageFilter = fileImpl.getLanguageFilter(ppHandler.getState());
        TokenStream filteredTokenStream = languageFilter.getFilteredStream(new APTCommentsFilter(tokenStream));
        return filteredTokenStream;
    }

    @Override
    public FilePreprocessorConditionState release() {
        return CsmCorePackageAccessor.get().createPCState(getMainFile().getAbsolutePath(), new int[] {0, 10});
    }
    
    private static final class MyClankPreprocessorCallback implements ClankPreprocessorCallback {
        private final ProjectBase startProject;
        private final FileImpl startFile;
        private final PreprocHandler ppHandler;

        private final FileImpl stopFileImpl;
        private final int stopAtIndex;
        private ClankDriver.APTTokenStreamCache foundTokens;

        private boolean alreadySeenInterestedFileEnter = false;
        private final boolean triggerParsingActivity;

        private List<FileImpl> curFiles = new ArrayList<FileImpl>();

        private MyClankPreprocessorCallback(
                PreprocHandler ppHandler,
                boolean triggerParsingActivity,
                FileImpl stopFileImpl, int stopAtIndex) {
            this.ppHandler = ppHandler;
            StartEntry startEntry = ppHandler.getIncludeHandler().getStartEntry();
            this.startProject = Utils.getStartProject(startEntry);
            this.startFile = Utils.getStartFile(ppHandler.getState());
            this.triggerParsingActivity = triggerParsingActivity;
            this.stopFileImpl = stopFileImpl;
            this.stopAtIndex = stopAtIndex;
        }

        @Override
        public boolean onEnter(ClankDriver.ClankFileInfo info) {
            if (info.getFileIndex() == stopAtIndex) {
              assert !alreadySeenInterestedFileEnter;
              alreadySeenInterestedFileEnter = true;
              CndUtils.assertTrueInConsole(CharSequenceUtilities.textEquals(info.getFilePath(), stopFileImpl.getAbsolutePath()),
                      info + "\n vs. \n", stopFileImpl);
              if (triggerParsingActivity) {
                // we entered target file and after that we can
                // handle inclusive #includes
                curFiles.add(stopFileImpl);
              }
            } else if (alreadySeenInterestedFileEnter) {
              if (triggerParsingActivity) {
                // let's keep stack of inner includes
                // then onExit post process headers wthich should be parsed
                ResolvedPath resolvedPath = info.getResolvedPath();
                CndUtils.assertTrueInConsole(CharSequenceUtilities.textEquals(resolvedPath.getPath(), info.getFilePath()),
                        resolvedPath.getPath() + " vs. ", info.getFilePath());
                assert ClankDriver.extractTokenStream(ppHandler).getFileIndex() == info.getFileIndex();
                FileImpl curFile = getCurFile(false);
                CharSequence path = resolvedPath.getPath();
                ProjectBase aStartProject = startProject;
                if (aStartProject != null) {
                    if (aStartProject.isValid()) {
                        ProjectBase inclFileOwner = aStartProject.getLibraryManager().resolveFileProjectOnInclude(aStartProject, curFile, resolvedPath);
                        if (inclFileOwner == null) {
                            assert false : "something wrong when parsing " + stopFileImpl + " from " + this.startProject;
                            if (aStartProject.getFileSystem() == resolvedPath.getFileSystem()) {
                                inclFileOwner = aStartProject;
                            }
                        }
                        assert inclFileOwner != null;
                        if (CndUtils.isDebugMode()) {
                            CndUtils.assertTrue(inclFileOwner.getFileSystem() == resolvedPath.getFileSystem(), "Different FS for " + path + ": " + inclFileOwner.getFileSystem() + " vs " + resolvedPath.getFileSystem()); // NOI18N
                        }
                        FileImpl includedFile = inclFileOwner.prepareIncludedFile(aStartProject, path, ppHandler);
                        if (includedFile != null) {
                          curFiles.add(includedFile);
                        } else {
                          assert false : "something wrong when including " + path + " from " + curFile;
                          curFiles.add(curFile);
                        }
                    } else {
                      assert false : "invalid start project when including " + path + " from " + curFile;
                      curFiles.add(curFile);
                    }
                } else {
                    APTUtils.LOG.log(Level.SEVERE, "APTProjectFileBasedWalker: file {0} without project!!!", new Object[]{path});// NOI18N
                    curFiles.add(curFile);
                }
              }
            }
            return true;
        }

        private FileImpl getCurFile(boolean pop) {
          assert curFiles.size() > 0;
          FileImpl curFile;
          if (pop) {
            curFile = curFiles.remove(curFiles.size() - 1);
          } else {
            curFile = curFiles.get(curFiles.size() - 1);
          }
          assert curFile != null;
          return curFile;
        }

        private ProjectBase getStartProject() {
          return startProject;
        }

        @Override
        public boolean onExit(ClankDriver.ClankFileInfo fileInfo) {
            if (!alreadySeenInterestedFileEnter) {
              return true;
            }
            if (stopAtIndex == fileInfo.getFileIndex()) {
              CndUtils.assertTrueInConsole(CharSequenceUtilities.textEquals(fileInfo.getFilePath(), stopFileImpl.getAbsolutePath()) ,
                      "expected " + stopFileImpl.getAbsolutePath(), fileInfo);
              foundTokens = ClankDriver.extractTokenStream(ppHandler);
              assert foundTokens.hasTokenStream();
              // stop all activity
              alreadySeenInterestedFileEnter = false;
              return false;
            } else if (triggerParsingActivity) {
              assert alreadySeenInterestedFileEnter;
              try {
                assert ClankDriver.extractTokenStream(ppHandler).hasTokenStream();
                PreprocHandler.State inclState = ppHandler.getState();
                assert !inclState.isCleaned();
                FileImpl currentInclusion = getCurFile(true);
                if (currentInclusion != null) {
                  CharSequence inclPath = currentInclusion.getAbsolutePath();
                  ProjectBase inclFileOwner = currentInclusion.getProjectImpl(true);
                  ProjectBase aStartProject = getStartProject();
                  if (inclFileOwner.isDisposing() || aStartProject.isDisposing()) {
                    if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                      System.err.printf("onFileIncluded: %s file [%s] is interrupted on disposing project\n", inclPath, inclFileOwner.getName());
                    }
                  } else {
                    FilePreprocessorConditionState pcState = CsmCorePackageAccessor.get().createPCState(inclPath, fileInfo.getSkippedRanges());
                    PreprocessorStatePair ppStatePair = new PreprocessorStatePair(inclState, pcState);
                    inclFileOwner.postIncludeFile(aStartProject, currentInclusion, inclPath, ppStatePair, null);
                  }
                }
              } catch (Exception ex) {
                APTUtils.LOG.log(Level.SEVERE, "APTProjectFileBasedWalker: error on including {0}:\n{1}", new Object[]{fileInfo.getFilePath(), ex});
                DiagnosticExceptoins.register(ex);
              }
              
            }
            return true;
        }        

        private ClankDriver.APTTokenStreamCache getPPOut() {
            return foundTokens;
        }
    }
}
