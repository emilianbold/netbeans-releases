/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.modelimpl.impl.services;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmCompilationUnit;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler.State;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.PreprocessorStatePair;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTFindMacrosWalker;
import org.netbeans.modules.cnd.modelimpl.parser.apt.GuardBlockWalker;

/**
 * implementaion of CsmFileInfoQuery
 * @author Vladimir Voskresenskky
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery.class)
public final class FileInfoQueryImpl extends CsmFileInfoQuery {

    public List<String> getSystemIncludePaths(CsmFile file) {
        return getIncludePaths(file, true);
    }

    public List<String> getUserIncludePaths(CsmFile file) {
        return getIncludePaths(file, false);
    }

    private List<String> getIncludePaths(CsmFile file, boolean system) {
        List<String> out = Collections.<String>emptyList();
        if (file instanceof FileImpl) {
            NativeFileItem item = ProjectBase.getCompiledFileItem((FileImpl) file);
            if (item != null) {
                if (item.getLanguage() == NativeFileItem.Language.C_HEADER) {
                    // It's an orphan (otherwise the getCompiledFileItem would return C or C++ item, not header).
                    // For headers, NativeFileItem does NOT contain necessary information
                    // (whe parsing, we use DefaultFileItem for headers)
                    // so for headers, we should use project iformation instead
                    NativeProject nativeProject = item.getNativeProject();
                    if (nativeProject != null) {
                        if (system) {
                            out = nativeProject.getSystemIncludePaths();
                        } else {
                            out = nativeProject.getUserIncludePaths();
                        }
                    }
                } else {
                    if (system) {
                        out = item.getSystemIncludePaths();
                    } else {
                        out = item.getUserIncludePaths();
                    }
                }
            }
        }
        return out;
    }

    public List<CsmOffsetable> getUnusedCodeBlocks(CsmFile file) {
        List<CsmOffsetable> out = Collections.<CsmOffsetable>emptyList();
        if (file instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) file;
            Collection<PreprocessorStatePair> statePairs = fileImpl.getPreprocStatePairs();
            List<CsmOffsetable> result = new ArrayList<CsmOffsetable>();
            boolean first = true;
            for (PreprocessorStatePair pair : statePairs) {
                FilePreprocessorConditionState state = pair.pcState;
                if (state != FilePreprocessorConditionState.PARSING) {
                    List<CsmOffsetable> blocks = state.createBlocksForFile(fileImpl);
                    if (first) {
                        result = blocks;
                        first = false;
                    } else {
                        result = intersection(result, blocks);
                        if (result.isEmpty()) {
                            break;
                        }
                    }
                }
            }
            CsmOffsetable error = null;
            for (CsmErrorDirective csmErrorDirective : fileImpl.getErrors()) {
                error = org.netbeans.modules.cnd.modelimpl.csm.core.Utils.createOffsetable(fileImpl, csmErrorDirective.getEndOffset(), Integer.MAX_VALUE);
                break;
            }
            if (error != null) {
                out = new ArrayList<CsmOffsetable>(result.size());
                for (CsmOffsetable offs : result) {
                    if (offs.getEndOffset() < error.getStartOffset()) {
                        out.add(offs);
                    } else {
                        break;
                    }
                }
                out.add(error);
            } else {
                out = result;
            }
        }
        return out;
    }
    
    private static boolean contains(CsmOffsetable bigger, CsmOffsetable smaller) {
        if (bigger != null && smaller != null) {
            if (bigger.getStartOffset() <= smaller.getStartOffset() &&
                smaller.getEndOffset() <= bigger.getEndOffset()) {
                return true;
            }
        }
        return false;
    }
    
    private static List<CsmOffsetable> intersection(Collection<CsmOffsetable> first, Collection<CsmOffsetable> second) {
        List<CsmOffsetable> result = new ArrayList<CsmOffsetable>(Math.max(first.size(), second.size()));
        for (CsmOffsetable o1 : first) {
            for (CsmOffsetable o2 : second) {
                if (o1 != null) { //paranoia
                    if (o1.equals(o2)) {
                        result.add(o1);
                    } else if (contains(o1, o2)) {
                        result.add(o2);
                        
                    } else if (contains(o2, o1)) {
                        result.add(o1);
                    }
                }
            }
        }
        return result;
    }

    private final ConcurrentMap<CsmFile, Object> macroUsagesLocks = new ConcurrentHashMap<CsmFile, Object>();
    private static final class NamedLock {
        private final String name;

        public NamedLock(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NamedLock other = (NamedLock) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

    }
    
    public List<CsmReference> getMacroUsages(CsmFile file) {
        List<CsmReference> out = Collections.<CsmReference>emptyList();
        if (file instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) file;
            Object lock = new NamedLock("getMacroUsages lock for " + file.getAbsolutePath()); // NOI18N
            Object prevLock = macroUsagesLocks.putIfAbsent(fileImpl, lock);
            lock = prevLock != null ? prevLock : lock;
            try {
                synchronized (lock) {
                    List<CsmReference> res = fileImpl.getLastMacroUsages();
                    if (res != null) {
                        return res;
                    }
                    try {
                        long lastParsedTime = fileImpl.getLastParsedTime();
                        APTFile apt = APTDriver.getInstance().findAPT(fileImpl.getBuffer(), fileImpl.getFileLanguage());
                        if (apt != null) {
                            Collection<APTPreprocHandler> handlers = fileImpl.getPreprocHandlers();
                            if (handlers.isEmpty()) {
                                DiagnosticExceptoins.register(new IllegalStateException("Empty preprocessor handlers for " + file.getAbsolutePath())); //NOI18N
                                return Collections.<CsmReference>emptyList();
                            } else if (handlers.size() == 1) {
                                APTPreprocHandler handler = handlers.iterator().next();
                                // ask for concurrent entry if absent
                                APTFileCacheEntry cacheEntry = fileImpl.getAPTCacheEntry(handler, Boolean.FALSE);
                                APTFindMacrosWalker walker = new APTFindMacrosWalker(apt, fileImpl, handler, cacheEntry);
                                out = walker.collectMacros();
                                // remember walk info
                                fileImpl.setAPTCacheEntry(handler, cacheEntry, false);
                            } else {
                                Comparator<CsmReference> comparator = new OffsetableComparator<CsmReference>();
                                TreeSet<CsmReference> result = new TreeSet<CsmReference>(comparator);
                                for (APTPreprocHandler handler : handlers) {
                                    // ask for concurrent entry if absent
                                    APTFileCacheEntry cacheEntry = fileImpl.getAPTCacheEntry(handler, Boolean.FALSE);
                                    APTFindMacrosWalker walker = new APTFindMacrosWalker(apt, fileImpl, handler, cacheEntry);
                                    result.addAll(walker.collectMacros());
                                    // remember walk info
                                    fileImpl.setAPTCacheEntry(handler, cacheEntry, false);
                                }
                                out = new ArrayList<CsmReference>(result);
                            }
                        }
                        if (lastParsedTime == fileImpl.getLastParsedTime()) {
                            fileImpl.setLastMacroUsages(out);
                        }
                    } catch (FileNotFoundException ex) {
                        // file could be removed
                    } catch (IOException ex) {
                        System.err.println("skip marking macros\nreason:" + ex.getMessage()); //NOI18N
                        DiagnosticExceptoins.register(ex);
                    }
                }
            } finally {
                macroUsagesLocks.remove(fileImpl, lock);
            }
        }
        return out;
    }
    
    public CsmOffsetable getGuardOffset(CsmFile file) {
        if (file instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) file;
            try {
                APTFile apt = APTDriver.getInstance().findAPT(fileImpl.getBuffer(), fileImpl.getFileLanguage());

                GuardBlockWalker guardWalker = new GuardBlockWalker(apt);
                TokenStream ts = guardWalker.getTokenStream();
                try {
                    Token token = ts.nextToken();
                    while (!APTUtils.isEOF(token)) {
                        if (!APTUtils.isCommentToken(token)) {
                            guardWalker.clearGuard();
                            break;
                        }
                        token = ts.nextToken();
                    }
                } catch (TokenStreamException ex) {
                    guardWalker.clearGuard();
                }

                Token guard = guardWalker.getGuard();
                if (guard != null) {
                    if (guard instanceof APTToken) {
                        APTToken aptGuard = ((APTToken) guard);
                        return new OffsetableBase(file, aptGuard.getOffset(), aptGuard.getEndOffset());
                    }
                }
            } catch (FileNotFoundException ex) {
                // file could be removed
            } catch (IOException ex) {
                System.err.println("IOExeption in getGuardOffset:" + ex.getMessage()); //NOI18N
            }
        }
        return null;
    }

    @Override
    public NativeFileItem getNativeFileItem(CsmFile file) {
        if (file instanceof FileImpl) {
            return ((FileImpl)file).getNativeFileItem();
        }
        return null;
    }

    @Override
    public Collection<CsmCompilationUnit> getCompilationUnits(CsmFile file, int contextOffset) {
        CsmCompilationUnit backup = CsmCompilationUnit.createCompilationUnit(file.getProject(), file.getAbsolutePath(), file);
        Collection<CsmCompilationUnit> out = new ArrayList<CsmCompilationUnit>(1);
        boolean addBackup = true;
        if (file instanceof FileImpl) {
            FileImpl impl = (FileImpl) file;
            Collection<State> states = ((ProjectBase) impl.getProject()).getPreprocStates(impl);
            for (State state : states) {
                StartEntry startEntry = APTHandlersSupport.extractStartEntry(state);
                ProjectBase startProject = ProjectBase.getStartProject(startEntry);
                if (startProject != null) {
                    CharSequence path = startEntry.getStartFile();
                    CsmFile startFile = startProject.getFile(new File(path.toString()), false);
                    if (startFile != null) {
                        addBackup = false;
                    }
                    CsmCompilationUnit cu = CsmCompilationUnit.createCompilationUnit(startProject, path, startFile);
                    out.add(cu);
                }
            }
        }
        if (addBackup) {
            out.add(backup);
        }
        return out;
    }

    @Override
    public List<CsmInclude> getIncludeStack(CsmFile file) {
        if (file instanceof FileImpl) {
            FileImpl impl = (FileImpl) file;
            APTPreprocHandler.State state = ((ProjectBase)impl.getProject()).getPreprocState(impl);
            if (state == null) {
                return Collections.<CsmInclude>emptyList();
            }
            List<APTIncludeHandler.IncludeInfo> reverseInclStack = APTHandlersSupport.extractIncludeStack(state);
            StartEntry startEntry = APTHandlersSupport.extractStartEntry(state);
            ProjectBase startProject = ProjectBase.getStartProject(startEntry);
            if (startProject != null) {
                CsmFile startFile = startProject.getFile(new File(startEntry.getStartFile().toString()), false);
                if (startFile != null) {
                    List<CsmInclude> res = new ArrayList<CsmInclude>();
                    Iterator<APTIncludeHandler.IncludeInfo> it = reverseInclStack.iterator();
                    while(it.hasNext()){
                        APTIncludeHandler.IncludeInfo info = it.next();
                        int offset = info.getIncludeDirectiveOffset();
                        CsmInclude find = null;
                        for(CsmInclude inc : startFile.getIncludes()){
                            if (offset == inc.getStartOffset()){
                                find = inc;
                                break;
                            }
                        }
                        if (find != null) {
                            res.add(find);
                            startFile = find.getIncludeFile();
                            if (startFile == null) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    return res;
                }
            }
        }
        return Collections.<CsmInclude>emptyList();
    }

    @Override
    public boolean hasBrokenIncludes(CsmFile file) {
        if (file instanceof FileImpl) {
            return ((FileImpl)file).hasBrokenIncludes();
        }
        return false;
    }

    @Override
    public Collection<CsmInclude> getBrokenIncludes(CsmFile file) {
        if (file instanceof FileImpl) {
            return ((FileImpl) file).getBrokenIncludes();
        }
        return Collections.<CsmInclude>emptyList();
    }

    private static class OffsetableComparator<T extends CsmOffsetable> implements Comparator<T> {
        public int compare(CsmOffsetable o1, CsmOffsetable o2) {
            int diff = o1.getStartOffset() - o2.getStartOffset();
            if (diff == 0) {
                return o1.getEndOffset() - o2.getEndOffset();
            } else {
                return diff;
            }
        }
    }

    @Override
    public long getFileVersion(CsmFile file) {
        if (file instanceof FileImpl) {
            return FileImpl.getParseCount();
        }
        return 0;
    }
}
