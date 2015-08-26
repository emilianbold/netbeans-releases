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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.MacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.SystemMacroImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileBuffer;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileSystem;

/**
 *
 * @author Alexander Simon
 */
public class ClankMacroUsagesProducer {

    private final PreprocHandler curPreprocHandler;
    private final FileImpl fileImpl;

    private ClankMacroUsagesProducer(FileImpl fileImpl, PreprocHandler curPreprocHandler) {
        this.fileImpl = fileImpl;
        this.curPreprocHandler = curPreprocHandler;
    }

    public static ClankMacroUsagesProducer createImpl(FileImpl file, PreprocHandler curPreprocHandler) {
        return new ClankMacroUsagesProducer(file, curPreprocHandler);
    }

    public List<CsmReference> getMacroUsages(Interrupter interrupter) {
        List<CsmReference> res = new ArrayList<>();
        ClankDriver.APTTokenStreamCache tokStreamCache = ClankDriver.extractTokenStream(curPreprocHandler);
        assert tokStreamCache != null;
        FileImpl startFile =  Utils.getStartFile(curPreprocHandler.getState());
        if (startFile == null) {
            startFile = fileImpl;
        }
        // do preprocessing
        FileMacroUsagesCallback callback = new FileMacroUsagesCallback(
                curPreprocHandler,
                startFile,
                tokStreamCache.getFileIndex());
        FileBuffer buffer = startFile.getBuffer();
        if (ClankDriver.preprocess(buffer, curPreprocHandler, callback, interrupter)) {
            ClankDriver.ClankFileInfo foundFileInfo = callback.getFoundFileInfo();
            if (foundFileInfo != null) {
                addPreprocessorDirectives(startFile, res, foundFileInfo);
                addMacroExpansions(startFile, res, startFile, foundFileInfo);
            }
        }
        return res;
    }


    private static void addPreprocessorDirectives(FileImpl curFile, List<CsmReference> res, ClankDriver.ClankFileInfo cache) {
        assert res != null;
        assert curFile != null;
        assert cache != null;
        for (ClankDriver.ClankPreprocessorDirective cur : cache.getPreprocessorDirectives()) {
            if (cur instanceof ClankDriver.ClankMacroDirective) {
                addMacro(curFile, res, (ClankDriver.ClankMacroDirective)cur);
            }
        }
    }

    private static void addMacroExpansions(FileImpl curFile, List<CsmReference> res, FileImpl startFile, ClankDriver.ClankFileInfo cache) {
        Map<Integer, ClankDriver.ClankMacroDirective> macroDefinitions = cache.getMacroDefinitions();
        for (ClankDriver.MacroExpansion cur : cache.getMacroExpansions()) {
            int referencedDeclaration = cur.getReferencedMacroID();
            if (referencedDeclaration != 0) {
                ClankDriver.ClankMacroDirective directive = macroDefinitions.get(referencedDeclaration);
                assert directive != null : "Not found referenced ClankMacroDirective "+referencedDeclaration;
                res.add(MacroReference.createMacroReference(curFile, cur.getStartOfset(), cur.getStartOfset()+cur.getMacroNameLength(), startFile, directive));
            } else {
                // TODO: process invalid macro definition
            }
        }
        for(ClankDriver.MacroUsage cur : cache.getMacroUsages()) {
            int referencedDeclaration = cur.getReferencedMacroID();
            if (referencedDeclaration != 0) {
                ClankDriver.ClankMacroDirective directive = macroDefinitions.get(referencedDeclaration);
                assert directive != null : "Not found referenced ClankMacroDirective "+referencedDeclaration;
                res.add(MacroReference.createMacroReference(curFile, cur.getStartOfset(), cur.getEndOfset(), startFile, directive));
            } else {
                // TODO: process invalid macro definition
            }
        }
    }

    private static void addMacro(FileImpl curFile, List<CsmReference> res, ClankDriver.ClankMacroDirective ppDirective) {
        if (!ppDirective.isDefined()) {
            // only #define are handled by old model, not #undef
            return;
        }
        CsmMacro.Kind kind = CsmMacro.Kind.DEFINED;
        List<CharSequence> params = ppDirective.getParameters();
        CharSequence name = ppDirective.getMacroName();
        String body = "";
        int startOffset = ppDirective.getDirectiveStartOffset();
        int endOffset = ppDirective.getDirectiveEndOffset();
        int macroNameOffset = ppDirective.getMacroNameOffset();
        CsmMacro impl = MacroImpl.create(name, params, body/*sb.toString()*/, curFile, startOffset, endOffset, kind);
        MacroDeclarationReference macroDeclarationReference = new MacroDeclarationReference(curFile, impl, macroNameOffset);
        res.add(macroDeclarationReference);
    }

    private static final class FileMacroUsagesCallback implements ClankDriver.ClankPreprocessorCallback {
        private final ProjectBase startProject;
        private final PreprocHandler ppHandler;

        private final FileImpl stopFileImpl;
        private final int stopAtIndex;
        private ClankDriver.ClankFileInfo foundFileInfo;

        private enum State {
          INITIAL,
          SEEN,
          EXITED
        }
        private State alreadySeenInterestedFileEnter = State.INITIAL;
        private boolean insideInterestedFile = false;

        private List<FileImpl> curFiles = new ArrayList<>();

        private FileMacroUsagesCallback(
                PreprocHandler ppHandler,
                FileImpl stopFileImpl,
                int stopAtIndex) {
            this.ppHandler = ppHandler;
            StartEntry startEntry = ppHandler.getIncludeHandler().getStartEntry();
            this.startProject = Utils.getStartProject(startEntry);
            this.stopFileImpl = stopFileImpl;
            this.stopAtIndex = stopAtIndex;
        }

        boolean isTrace() {
          if (false && stopFileImpl.getName().toString().endsWith(".h")) {// NOI18N
            return true;
          }
          return false;
        }

        @Override
        public void onErrorDirective(ClankDriver.ClankFileInfo directiveOwner, ClankDriver.ClankErrorDirective directive) {
        }

        @Override
        public boolean needTokens() {
          return false;
        }

        @Override
        public boolean needSkippedRanges() {
          return false;
        }

        @Override
        public boolean needMacroExpansion() {
          return this.insideInterestedFile || true;
        }

        @Override
        public boolean needComments() {
          return false;
        }

        @Override
        public void onInclusionDirective(ClankDriver.ClankFileInfo directiveOwner, ClankDriver.ClankInclusionDirective directive) {
            if ((alreadySeenInterestedFileEnter == State.SEEN) && (insideInterestedFile)) {
              // let's resolve include as FileImpl
              ResolvedPath resolvedPath = directive.getResolvedPath();
              if (resolvedPath == null) {
                // broken #include path
                return;
              }
              FileImpl curFile = getCurFile(false);
              FileImpl includedFile = null;
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
                      includedFile = inclFileOwner.prepareIncludedFile(aStartProject, path, ppHandler);
                      if (includedFile == null) {
                        assert false : "something wrong when including " + path + " from " + curFile;
                      }
                  } else {
                    APTUtils.LOG.log(Level.INFO, "invalid start project {0} when including {1} from {2}", new Object[] {aStartProject, path, curFile});
                    // assert false : "invalid start project when including " + path + " from " + curFile;
                  }
              } else {
                  APTUtils.LOG.log(Level.SEVERE, "FileTokenStreamCallback: file {0} without project!!!", new Object[]{path});// NOI18N
              }
              directive.setAnnotation(includedFile);
            }
        }

        @Override
        public void onEnter(ClankDriver.ClankFileInfo enteredFrom, ClankDriver.ClankFileInfo enteredTo) {
            assert enteredTo != null;
            if (enteredTo.getFileIndex() == stopAtIndex) {
                assert alreadySeenInterestedFileEnter == State.INITIAL;
                alreadySeenInterestedFileEnter = State.SEEN;
                CndUtils.assertPathsEqualInConsole(enteredTo.getFilePath(), stopFileImpl.getAbsolutePath(),
                        "{0}\n vs. \n{1}", enteredTo, stopFileImpl);// NOI18N
                // we entered target file and after that we can
                // handle inclusive #includes
                curFiles.add(stopFileImpl);
            } else {
              if ((alreadySeenInterestedFileEnter == State.SEEN)) {
                // let's keep stack of inner includes
                // then onExit post process headers wthich should be parsed
                FileImpl curFile = getCurFile(false);
                FileImpl includedFile = (FileImpl) enteredTo.getInclusionDirective().getAnnotation();
                if (includedFile != null) {
                  curFiles.add(includedFile);
                } else {
                  curFiles.add(curFile);
                }
              }
            }
            insideInterestedFile = (enteredTo.getFileIndex() == stopAtIndex);
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

        @Override
        public boolean onExit(ClankDriver.ClankFileInfo exitedFrom, ClankDriver.ClankFileInfo exitedTo) {
            assert exitedFrom != null;
            if (alreadySeenInterestedFileEnter == State.EXITED) {
              return false;
            } else if (alreadySeenInterestedFileEnter == State.INITIAL) {
              return true;
            }
            insideInterestedFile = (exitedTo != null) && (exitedTo.getFileIndex() == stopAtIndex);
            if (stopAtIndex == exitedFrom.getFileIndex()) {
              CndUtils.assertPathsEqualInConsole(exitedFrom.getFilePath(), stopFileImpl.getAbsolutePath(),
                      "{0} expected {1}", stopFileImpl.getAbsolutePath(), exitedFrom);// NOI18N
              exitedFrom.getFileGuard();
              foundFileInfo = exitedFrom;
              // stop all activity
              alreadySeenInterestedFileEnter = State.EXITED;
              return false;
            } else {
              assert alreadySeenInterestedFileEnter == State.SEEN;
            }
            return true;
        }

        @Override
        public void onMacroDefineDirective(ClankDriver.ClankFileInfo directiveOwner, ClankDriver.ClankMacroDirective directive) {
            if (directive.isDefined()) {
                // for #define A BODY macro name location is macro-ID
                ((ClankDriver.APTTokenStreamCache)directiveOwner).getMacroDefinitions().put(directive.getMacroNameLocation(), directive);
            }
        }

        private ClankDriver.ClankFileInfo getFoundFileInfo() {
            return foundFileInfo;
        }
    }

    public static final class MacroReference extends OffsetableBase implements CsmReference {
        private final CsmMacro referencedMacro;

        private static MacroReference createMacroReference(FileImpl curFile, int startOffset, int endOffset, FileImpl startFile, final ClankDriver.ClankMacroDirective directive) {
            CsmMacro referencedMacro;
            if (CharSequenceUtilities.equals(ClankDriver.ClankMacroDirective.BUILD_IN_FILE, directive.getFile())) {
                referencedMacro =  SystemMacroImpl.create(directive.getMacroName(), findBody(startFile, directive.getMacroName()), directive.getParameters(), startFile, findType(startFile, directive.getMacroName()));
            } else {
                referencedMacro =  MacroImpl.create(directive.getMacroName(), directive.getParameters(),
                    "", getTargetFile(curFile, directive.getFile()), directive.getDirectiveStartOffset(), directive.getDirectiveEndOffset(), CsmMacro.Kind.DEFINED);
            }
            return new MacroReference(curFile, startOffset, endOffset, referencedMacro);
        }

        public static CsmMacro.Kind findType(FileImpl startFile, CharSequence macroName) {
            CsmMacro.Kind res = CsmMacro.Kind.COMPILER_PREDEFINED;
            NativeFileItem item = startFile.getNativeFileItem();
            if (item != null) {
                for(String m : item.getUserMacroDefinitions()) {
                    if (CharSequenceUtilities.startsWith(m, macroName)) {
                        res = CsmMacro.Kind.USER_SPECIFIED;
                        break;
                    }
                }
            }
            return res;
        }

        public static CharSequence findBody(FileImpl startFile, CharSequence macroName) {
            CharSequence res = "";
            NativeFileItem item = startFile.getNativeFileItem();
            if (item != null) {
                for(String m : item.getSystemMacroDefinitions()) {
                    if (CharSequenceUtilities.startsWith(m, macroName)) {
                        int i = CharSequenceUtilities.indexOf(m, '=');
                        if (i > 0) {
                            res = m.substring(i+1);
                        }
                    }
                }
                for(String m : item.getUserMacroDefinitions()) {
                    if (CharSequenceUtilities.startsWith(m, macroName)) {
                        int i = CharSequenceUtilities.indexOf(m, '=');
                        if (i > 0) {
                            res = m.substring(i+1);
                        }
                    }
                }
            }
            return res;
        }

        private MacroReference(FileImpl curFile, int startOffset, int endOffset, CsmMacro referencedMacro) {
            super(curFile, startOffset, endOffset);
            this.referencedMacro =  referencedMacro;
        }

        @Override
        public CsmObject getReferencedObject() {
            return referencedMacro;
        }

        @Override
        public CsmObject getOwner() {
            return null;
        }

        @Override
        public CsmReferenceKind getKind() {
            return CsmReferenceKind.DIRECT_USAGE;
        }

        @Override
        public CharSequence getText() {
            return referencedMacro.getName();
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return getContainingFile();
        }

        static CsmFile getTargetFile(FileImpl current, CharSequence macroContainerFile) {
            CsmFile target = null;
            if (current != null && macroContainerFile.length() > 0) {
                FileSystem fs = null;
                ProjectBase currentPrj = (ProjectBase) current.getProject();
                ProjectBase targetPrj = currentPrj.findFileProject(macroContainerFile, true);
                if (targetPrj != null) {
                    target = targetPrj.findFile(macroContainerFile, true, false);
                    fs = targetPrj.getFileSystem();
                } else {
                    fs = currentPrj.getFileSystem();
                }
                // try full model?
                if (target == null) {
                    target = CsmModelAccessor.getModel().findFile(new FSPath(fs, macroContainerFile.toString()), true, false);
                }
                if (target == null && targetPrj != null) {
                    target = targetPrj.getUnresolvedFile();
                }
                if (target == null) {
                    target = currentPrj.getUnresolvedFile();
                }
            }
            return target;
        }
    }

    private static final class MacroDeclarationReference extends OffsetableBase implements CsmReference {
        private final CsmMacro referencedMacro;

        private MacroDeclarationReference(FileImpl curFile, CsmMacro referencedMacro, int macroNameOffset) {
            super(curFile, macroNameOffset, macroNameOffset + referencedMacro.getName().length());
            this.referencedMacro = referencedMacro;
        }

        @Override
        public CsmObject getReferencedObject() {
            return referencedMacro;
        }

        @Override
        public CsmObject getOwner() {
            return referencedMacro;
        }

        @Override
        public CsmReferenceKind getKind() {
            return CsmReferenceKind.DECLARATION;
        }

        @Override
        public CharSequence getText() {
            return referencedMacro.getName();
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return getContainingFile();
        }
    }
}
