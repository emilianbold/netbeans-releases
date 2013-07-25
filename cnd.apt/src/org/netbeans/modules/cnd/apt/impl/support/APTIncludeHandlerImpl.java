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

package org.netbeans.modules.cnd.apt.impl.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTFileSearch;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler.IncludeInfo;
import org.netbeans.modules.cnd.apt.support.APTIncludeResolver;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 * implementation of include handler responsible for preventing recursive inclusion
 * @author Vladimir Voskresensky
 */
public class APTIncludeHandlerImpl implements APTIncludeHandler {
    private List<IncludeDirEntry> systemIncludePaths;
    private List<IncludeDirEntry> userIncludePaths;
    private List<IncludeDirEntry> userIncludeFilePaths;

    private Map<CharSequence, Integer> recurseIncludes = null;
    /* CUDA+trast example shows that 5 (and 4) is too expensive in case code model ignores pragma once
     * Boost needs at least 4 for include boost/spirit/home/classic/utility/chset_operators.hpp>
     * So it is dangerous level that can led to "infinite" parsing time.
     */
    private static final int MAX_INCLUDE_FILE_DEEP = 4;

    // there are two algorithms:
    // 1) if the following constant is negative then recurseIncludes hash map is used
    // 2) otherwise inclStack is analyzed
    // if we need to remove hash map, then set const i.e. as 100
    private static final int CHECK_INCLUDE_DEPTH = -1;
    private LinkedList<IncludeInfo> inclStack = null;
    private StartEntry startFile;
    private final APTFileSearch fileSearch;
    private MakeConfiguration projectConfiguration;
    
    /*package*/ APTIncludeHandlerImpl(StartEntry startFile, MakeConfiguration projectConfiguration) {
        this(startFile, new ArrayList<IncludeDirEntry>(0), new ArrayList<IncludeDirEntry>(0), new ArrayList<IncludeDirEntry>(0), startFile.getFileSearch(), projectConfiguration);
    }
    
    public APTIncludeHandlerImpl(StartEntry startFile,
                                    List<IncludeDirEntry> systemIncludePaths,
                                    List<IncludeDirEntry> userIncludePaths,
                                    List<IncludeDirEntry> userIncludeFilePaths, APTFileSearch fileSearch, MakeConfiguration projectConfiguration) {
        this.startFile = startFile;
        this.systemIncludePaths = systemIncludePaths;
        this.userIncludePaths = userIncludePaths;
        this.userIncludeFilePaths = userIncludeFilePaths;
        this.fileSearch = fileSearch;
        this.projectConfiguration = projectConfiguration;
    }

    @Override
    public IncludeState pushInclude(CharSequence path, APTInclude aptInclude, int resolvedDirIndex) {
        return pushIncludeImpl(path, aptInclude.getToken().getLine(), aptInclude.getToken().getOffset(), resolvedDirIndex);
    }

    @Override
    public CharSequence popInclude() {
        return popIncludeImpl();
    }
    
    @Override
    public APTIncludeResolver getResolver(FileSystem fs, CharSequence path) {
        return new APTIncludeResolverImpl(fs, path, getCurDirIndex(),
                systemIncludePaths, userIncludePaths, fileSearch, projectConfiguration);
    }
    
    @Override
    public StartEntry getStartEntry() {
        return startFile;
    }

    private CharSequence getCurPath() {
        assert (inclStack != null);
        IncludeInfo info = inclStack.getLast();
        return info.getIncludedPath();
    }
    
    private int getCurDirIndex() {
        if (inclStack != null && !inclStack.isEmpty()) {
            IncludeInfo info = inclStack.getLast();
            return info.getIncludedDirIndex();
        } else {
            return 0;
        }
    }    
    ////////////////////////////////////////////////////////////////////////////
    // manage state (save/restore)
    
    @Override
    public State getState() {
        return createStateImpl();
    }
    
    @Override
    public void setState(State state) {
        if (state instanceof StateImpl) {
	    StateImpl stateImpl = ((StateImpl)state);
	    assert ! stateImpl.isCleaned();
            stateImpl.restoreTo(this);
        }
    }
    
    private StateImpl createStateImpl() {
        return new StateImpl(this);
    }

    /*package*/List<IncludeDirEntry> getUserIncludeFilePaths() {
        return Collections.unmodifiableList(userIncludeFilePaths);
    }

    /*package*/List<IncludeDirEntry> getUserIncludePaths() {
        return Collections.unmodifiableList(userIncludePaths);
    }

    /*package*/List<IncludeDirEntry> getSystemIncludePaths() {
        return Collections.unmodifiableList(systemIncludePaths);
    }

    /*package*/boolean isFirstLevel() {
        return inclStack == null || inclStack.isEmpty();
    }
    
    /** immutable state object of include handler */
    // Not SelfPersistent any more because I have to pass unitIndex into write() method
    // It is private, so I don't think it's a problem. VK.
    public final static class StateImpl implements State, Persistent  {
        private static final List<IncludeDirEntry> CLEANED_MARKER = Collections.unmodifiableList(new ArrayList<IncludeDirEntry>(0));
        // for now just remember lists
        private final List<IncludeDirEntry> systemIncludePaths;
        private final List<IncludeDirEntry> userIncludePaths;
        private final List<IncludeDirEntry> userIncludeFilePaths;
        private final StartEntry   startFile;

        private static final IncludeInfo[] EMPTY_STACK = new IncludeInfo[0];
        private final IncludeInfo[] inclStack;
        private int hashCode = 0;
        
        protected StateImpl(APTIncludeHandlerImpl handler) {
            this.systemIncludePaths = handler.systemIncludePaths;
            this.userIncludePaths = handler.userIncludePaths;
            this.userIncludeFilePaths = handler.userIncludeFilePaths;
            this.startFile = handler.startFile;
            
            if (handler.inclStack != null && !handler.inclStack.isEmpty()) {
                this.inclStack = handler.inclStack.toArray(new IncludeInfo[handler.inclStack.size()]);
            } else {
                this.inclStack = EMPTY_STACK;
            }
        }
        
        private StateImpl(StateImpl other, boolean cleanState) {
            // shared information
            this.startFile = other.startFile;
            
            // state object is immutable => safe to share stacks
            this.inclStack = other.inclStack;
	    
            if (cleanState) {
                this.systemIncludePaths = CLEANED_MARKER;
                this.userIncludePaths = CLEANED_MARKER;
                this.userIncludeFilePaths = CLEANED_MARKER;
            } else {
                this.systemIncludePaths = other.systemIncludePaths;
                this.userIncludePaths = other.userIncludePaths;
                this.userIncludeFilePaths = other.userIncludeFilePaths;
            }
        }

        int getIncludeStackDepth() {
            return inclStack.length;
        }
        
        private void restoreTo(APTIncludeHandlerImpl handler) {
            handler.userIncludePaths = this.userIncludePaths;
            handler.userIncludeFilePaths = this.userIncludeFilePaths;
            handler.systemIncludePaths = this.systemIncludePaths;
            handler.startFile = this.startFile;
            
            // do not restore include info if state is cleaned
            if (!isCleaned()) {
                if (this.inclStack.length > 0) {
                    handler.inclStack = new LinkedList<IncludeInfo>();
                    handler.inclStack.addAll(Arrays.asList(this.inclStack));
                    if (CHECK_INCLUDE_DEPTH < 0) {
                        handler.recurseIncludes = new HashMap<CharSequence, Integer>();
                        for (IncludeInfo includeInfo : this.inclStack) {
                            CharSequence path = includeInfo.getIncludedPath();
                            Integer counter = handler.recurseIncludes.get(path);
                            counter = (counter == null) ? Integer.valueOf(1) : Integer.valueOf(counter.intValue() + 1);
                            handler.recurseIncludes.put(path, counter);
                        }
                    }
                }
            }
        }

        @Override
        public String toString() {
            return APTIncludeHandlerImpl.toString(startFile.getStartFile(), systemIncludePaths, userIncludePaths, userIncludeFilePaths, Arrays.asList(inclStack));
        }
        
        public void write(RepositoryDataOutput output, int unitIndex) throws IOException {
            assert output != null;
            startFile.write(output, unitIndex);
            
            assert systemIncludePaths != null;
            assert userIncludePaths != null;
            
            int size = systemIncludePaths.size();
            output.writeInt(size);
            for (int i = 0; i < size; i++) {
                APTSerializeUtils.writeFileNameIndex(systemIncludePaths.get(i).getAsSharedCharSequence(), output, unitIndex);
            }
            
            size = userIncludePaths.size();
            output.writeInt(size);
            
            for (int i = 0; i < size; i++) {
                APTSerializeUtils.writeFileNameIndex(userIncludePaths.get(i).getAsSharedCharSequence(), output, unitIndex);
            }
            
            size = userIncludeFilePaths.size();
            output.writeInt(size);
            
            for (int i = 0; i < size; i++) {
                APTSerializeUtils.writeFileNameIndex(userIncludeFilePaths.get(i).getAsSharedCharSequence(), output, unitIndex);
            }

            output.writeInt(inclStack.length);
            for (IncludeInfo inclInfo : inclStack) {
                assert inclInfo != null;
                final IncludeInfoImpl inclInfoImpl;
                if (inclInfo instanceof IncludeInfoImpl) {
                    inclInfoImpl = (IncludeInfoImpl) inclInfo;
                } else {
                    inclInfoImpl = new IncludeInfoImpl(
                            inclInfo.getIncludedPath(),
                            inclInfo.getIncludeDirectiveLine(),
                            inclInfo.getIncludeDirectiveOffset(),
                            inclInfo.getIncludedDirIndex());
                }
                assert inclInfoImpl != null;
                inclInfoImpl.write(output, unitIndex);
            }
        }
        
        public StateImpl(FileSystem fs, final RepositoryDataInput input, int unitIndex) throws IOException {
            assert input != null;
            
            startFile = new StartEntry(fs, input, unitIndex);

            int size = input.readInt();
            systemIncludePaths = new ArrayList<IncludeDirEntry>(size);
            for (int i = 0; i < size; i++) {
                IncludeDirEntry path = IncludeDirEntry.get(fs, APTSerializeUtils.readFileNameIndex(input, unitIndex).toString());
                systemIncludePaths.add(i, path);
            }
            
            size = input.readInt();
            userIncludePaths = new ArrayList<IncludeDirEntry>(size);
            for (int i = 0; i < size; i++) {
                IncludeDirEntry path = IncludeDirEntry.get(fs, APTSerializeUtils.readFileNameIndex(input, unitIndex).toString());
                userIncludePaths.add(i, path);                
            }

            size = input.readInt();
            userIncludeFilePaths = new ArrayList<IncludeDirEntry>(size);
            for (int i = 0; i < size; i++) {
                IncludeDirEntry path = IncludeDirEntry.get(fs, APTSerializeUtils.readFileNameIndex(input, unitIndex).toString());
                userIncludeFilePaths.add(i, path);
            }
            
            size = input.readInt();
            
            if (size == 0) {
                inclStack = EMPTY_STACK;
            } else {
                inclStack = new IncludeInfo[size];
                for (int i = 0; i < size; i++) {
                    final IncludeInfo impl = new IncludeInfoImpl(input, unitIndex);
                    assert impl != null;
                    inclStack[i] = impl;
                }
            }
        }        
	
	/* package */ final StartEntry getStartEntry() {
	    return startFile;
	}

        @Override
        public boolean equals(Object obj) {
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            StateImpl other = (StateImpl)obj;
            return this.startFile.equals(other.startFile) &&
                    compareStacks(this.inclStack, other.inclStack);
        }

        @Override
        public int hashCode() {
            int hash = hashCode;
            if (hash == 0) {
                hash = 5;
                hash = 67 * hash + (this.startFile != null ? this.startFile.hashCode() : 0);
                hash = 67 * hash + Arrays.hashCode(this.inclStack);
                hashCode = hash;
            }
            return hash;
        }
        
        private boolean compareStacks(IncludeInfo[] inclStack1, IncludeInfo[] inclStack2) {
            if (inclStack1 == inclStack2) {
                return true;
            }
            if (inclStack1.length != inclStack2.length) {
                return false;
            }
            for (int i = 0; i < inclStack1.length; i++) {
                IncludeInfo cur1 = inclStack1[i];
                IncludeInfo cur2 = inclStack2[i];
                if (!cur1.equals(cur2)) {
                    return false;
                }
            }
            return true;
        }        

        /*package*/ Collection<IncludeInfo> getIncludeStack() {
            return Arrays.asList(this.inclStack);
        }
        
        /*package*/ boolean isCleaned() {
            return this.userIncludeFilePaths == CLEANED_MARKER; // was created as clean state
        }
        
        /*package*/ APTIncludeHandler.State copy(boolean cleanState) {
            return new StateImpl(this, cleanState);
        }
        
        /*package*/ List<IncludeDirEntry> getSysIncludePaths() {
            return this.systemIncludePaths;
        }
        
        /*package*/ List<IncludeDirEntry> getUserIncludePaths() {
            return this.userIncludePaths;
        }        

        /*package*/ List<IncludeDirEntry> getUserIncludeFilePaths() {
            return this.userIncludeFilePaths;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details

    private IncludeState pushIncludeImpl(CharSequence path, int directiveLine, int directiveOffset, int resolvedDirIndex) {
        assert CharSequences.isCompact(path) : "must be char sequence key " + path; // NOI18N
        boolean okToPush = true;
        if (CHECK_INCLUDE_DEPTH > 0) {
            // variant without hash map
            if (inclStack == null) {
                inclStack = new LinkedList<IncludeInfo>();
            }
            if (inclStack.size() > CHECK_INCLUDE_DEPTH) {
                APTUtils.LOG.log(Level.WARNING, "Deep inclusion:{0} in {1} on level {2}", new Object[] { path , getCurPath() , inclStack.size() }); // NOI18N
                // check recurse inclusion
                int counter = 0;
                for (IncludeInfo includeInfo : inclStack) {
                    if (includeInfo.getIncludedPath().equals(path)) {
                        counter++;
                        if (counter > MAX_INCLUDE_FILE_DEEP) {
                            okToPush = false;
                            break;
                        }
                    }
                }
            }
        } else {
            // variant with old hash map
            if (recurseIncludes == null) {
                assert (inclStack == null) : inclStack.toString() + " started on " + startFile;
                inclStack = new LinkedList<IncludeInfo>();
                recurseIncludes = new HashMap<CharSequence, Integer>();
            }
            Integer counter = recurseIncludes.get(path);
            counter = (counter == null) ? Integer.valueOf(1) : Integer.valueOf(counter.intValue() + 1);
            if (counter.intValue() < MAX_INCLUDE_FILE_DEEP) {
                recurseIncludes.put(path, counter);
            } else {
                okToPush = false;
            }
        }
        if (okToPush) {
            inclStack.addLast(new IncludeInfoImpl(path, directiveLine, directiveOffset, resolvedDirIndex));
            return IncludeState.Success;
        } else {
            APTUtils.LOG.log(Level.WARNING, "RECURSIVE inclusion:\n\t{0}\n\tin {1}\n", new Object[] { path , getCurPath() }); // NOI18N
            return IncludeState.Recursive;
        }
    }    

    // Not SelfPersistent any more since I have to pass unitIndex into write method
    // It is private, so I don't think it's a problem. VK.
    private static final class IncludeInfoImpl implements IncludeInfo, Persistent {
        private final CharSequence path;
        private final int directiveLine;
        private final int directiveOffset;
        private final int resolvedDirIndex;
        
        public IncludeInfoImpl(CharSequence path, int directiveLine, int directiveOffset, int resolvedDirIndex) {
            assert path != null;
            this.path = path;
            // in case of -include file we have negative line/offset
            assert directiveLine >= 0 || (directiveLine < 0 && directiveOffset < 0);
            this.directiveLine = directiveLine;
            this.directiveOffset = directiveOffset;
            this.resolvedDirIndex = resolvedDirIndex;
        }
        
        public IncludeInfoImpl(final RepositoryDataInput input, int unitIndex) throws IOException {
            assert input != null;
            this.path = APTSerializeUtils.readFileNameIndex(input, FilePathCache.getManager(), unitIndex);
            directiveLine = input.readInt();
            directiveOffset = input.readInt();
            resolvedDirIndex = input.readInt();
        }

        @Override
        public CharSequence getIncludedPath() {
            return path;
        }

        @Override
        public int getIncludeDirectiveLine() {
            return directiveLine;
        }

        @Override
        public int getIncludeDirectiveOffset() {
            return directiveOffset;
        }

        @Override
        public String toString() {
            String retValue;
            
            retValue = "(" + getIncludeDirectiveLine() + "/" + getIncludeDirectiveOffset() + ": " + // NOI18N
                    getIncludedPath() + ":" + getIncludedDirIndex() + ")"; // NOI18N
            return retValue;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            IncludeInfoImpl other = (IncludeInfoImpl)obj;
            return this.directiveLine == other.directiveLine && this.directiveOffset == other.directiveOffset &&
                    this.path.equals(other.path) && (resolvedDirIndex == other.resolvedDirIndex);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 73 * hash + (this.path != null ? this.path.hashCode() : 0);
            hash = 73 * hash + this.directiveLine;
            hash = 73 * hash + this.directiveOffset;
            hash = 73 * hash + this.resolvedDirIndex;
            return hash;
        }

        public void write(final RepositoryDataOutput output, int unitIndex) throws IOException {
            assert output != null;
            
            APTSerializeUtils.writeFileNameIndex(path, output, unitIndex);
            output.writeInt(directiveLine);
            output.writeInt(directiveOffset);
            output.writeInt(resolvedDirIndex);
        }

        @Override
        public int getIncludedDirIndex() {
            return this.resolvedDirIndex;
        }
    }
      
    private CharSequence popIncludeImpl() {
        assert (inclStack != null);
        assert (!inclStack.isEmpty());
        IncludeInfo inclInfo = inclStack.removeLast();
        CharSequence path = inclInfo.getIncludedPath();
        if (CHECK_INCLUDE_DEPTH < 0) {
            assert (recurseIncludes != null);
            Integer counter = recurseIncludes.remove(path);
            assert (counter != null) : "must be added before"; // NOI18N
            // decrease include counter
            counter = Integer.valueOf(counter.intValue()-1);
            assert (counter.intValue() >= 0) : "can't be negative"; // NOI18N
            if (counter.intValue() != 0) {
                recurseIncludes.put(path, counter);
            }
        }
        return path;
    }
    
    @Override
    public String toString() {
        return APTIncludeHandlerImpl.toString(startFile.getStartFile(), systemIncludePaths, userIncludePaths, userIncludeFilePaths, inclStack);
    }    
    
    private static String toString(CharSequence startFile,
                                    List<IncludeDirEntry> systemIncludePaths,
                                    List<IncludeDirEntry> userIncludePaths,
                                    List<IncludeDirEntry> userIncludeFilePaths,
                                    Collection<IncludeInfo> inclStack) {
        StringBuilder retValue = new StringBuilder();
        if (!userIncludeFilePaths.isEmpty()) {
            retValue.append("User File Includes:\n"); // NOI18N
            retValue.append(APTUtils.includes2String(userIncludeFilePaths));
        }
        retValue.append("User includes:\n"); // NOI18N
        retValue.append(APTUtils.includes2String(userIncludePaths));
        retValue.append("\nSys includes:\n"); // NOI18N
        retValue.append(APTUtils.includes2String(systemIncludePaths));
        retValue.append("\nInclude Stack starting from:\n"); // NOI18N
        retValue.append(startFile).append("\n"); // NOI18N
        retValue.append(includesStack2String(inclStack));
        return retValue.toString();
    }

    private static String includesStack2String(Collection<IncludeInfo> inclStack) {
        StringBuilder retValue = new StringBuilder();
        if (inclStack == null) {
            retValue.append("<not from #include>"); // NOI18N
        } else {
            for (Iterator<IncludeInfo>  it = inclStack.iterator(); it.hasNext();) {
                IncludeInfo info = it.next();
                retValue.append(info);
                if (it.hasNext()) {
                    retValue.append("->\n"); // NOI18N
                }
            }
        }
        return retValue.toString();
    }
}
