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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler.IncludeInfo;
import org.netbeans.modules.cnd.apt.support.APTIncludeResolver;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.StartEntry;
import org.netbeans.modules.cnd.utils.cache.APTStringManager;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
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
    private static final int MAX_INCLUDE_DEEP = 5;    
    private LinkedList<IncludeInfo> inclStack = null;
    private StartEntry startFile;
    
    /*package*/ APTIncludeHandlerImpl(StartEntry startFile) {
        this(startFile, new ArrayList<IncludeDirEntry>(0), new ArrayList<IncludeDirEntry>(0), new ArrayList<IncludeDirEntry>(0));
    }
    
    public APTIncludeHandlerImpl(StartEntry startFile,
                                    List<IncludeDirEntry> systemIncludePaths,
                                    List<IncludeDirEntry> userIncludePaths,
                                    List<IncludeDirEntry> userIncludeFilePaths) {
        this.startFile =startFile;
        this.systemIncludePaths = systemIncludePaths;
        this.userIncludePaths = userIncludePaths;
        this.userIncludeFilePaths = userIncludeFilePaths;
    }

    public boolean pushInclude(CharSequence path, APTInclude aptInclude, int resolvedDirIndex) {
        return pushIncludeImpl(path, aptInclude.getToken().getLine(), aptInclude.getToken().getOffset(), resolvedDirIndex);
    }

    public CharSequence popInclude() {
        return popIncludeImpl();
    }
    
    public APTIncludeResolver getResolver(CharSequence path) {
        return new APTIncludeResolverImpl(path, getCurDirIndex(),
                systemIncludePaths, userIncludePaths);
    }
    
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
    
    public State getState() {
        return createStateImpl();
    }
    
    public void setState(State state) {
        if (state instanceof StateImpl) {
	    StateImpl stateImpl = ((StateImpl)state);
	    assert ! stateImpl.isCleaned();
            stateImpl.restoreTo(this);
        }
    }
    
    protected StateImpl createStateImpl() {
        return new StateImpl(this);
    }

    /*package*/List<IncludeDirEntry> getUserIncludeFilePaths() {
        return Collections.unmodifiableList(userIncludeFilePaths);
    }

    /*package*/boolean isFirstLevel() {
        return inclStack == null || inclStack.isEmpty();
    }
    
    /** immutable state object of include handler */ 
    public final static class StateImpl implements State, Persistent, SelfPersistent {
        // for now just remember lists
        private final List<IncludeDirEntry> systemIncludePaths;
        private final List<IncludeDirEntry> userIncludePaths;
        private final List<IncludeDirEntry> userIncludeFilePaths;
        private final StartEntry   startFile;
        
        private final Map<CharSequence, Integer> recurseIncludes;
        private final LinkedList<IncludeInfo> inclStack;
        
        protected StateImpl(APTIncludeHandlerImpl handler) {
            this.systemIncludePaths = handler.systemIncludePaths;
            this.userIncludePaths = handler.userIncludePaths;
            this.userIncludeFilePaths = handler.userIncludeFilePaths;
            this.startFile = handler.startFile;
            
            if (handler.recurseIncludes != null && !handler.recurseIncludes.isEmpty()) {
                assert (handler.inclStack != null && !handler.inclStack.isEmpty()) : "must be in sync with inclStack";
                this.recurseIncludes = new HashMap<CharSequence, Integer>(handler.recurseIncludes);
            } else {
                this.recurseIncludes = null;
            }
            if (handler.inclStack != null && !handler.inclStack.isEmpty()) {
                assert (handler.recurseIncludes != null && !handler.recurseIncludes.isEmpty()) : "must be in sync with recurseIncludes";
                this.inclStack = new LinkedList<IncludeInfo>(handler.inclStack);
            } else {
                this.inclStack = null;
            }
        }
        
        private StateImpl(StateImpl other, boolean cleanState) {
            // shared information
            this.startFile = other.startFile;
            
            // state object is immutable => safe to share stacks
            this.inclStack = other.inclStack;
	    
            if (cleanState) {
                this.systemIncludePaths = Collections.emptyList();
                this.userIncludePaths = Collections.emptyList();
                this.userIncludeFilePaths = Collections.emptyList();
                this.recurseIncludes = null;
            } else {
                this.systemIncludePaths = other.systemIncludePaths;
                this.userIncludePaths = other.userIncludePaths;
                this.userIncludeFilePaths = other.userIncludeFilePaths;
                this.recurseIncludes = other.recurseIncludes;
            }
        }

        int getIncludeStackDepth() {
            return inclStack == null ? 0 : inclStack.size();
        }
        
        private void restoreTo(APTIncludeHandlerImpl handler) {
            handler.userIncludePaths = this.userIncludePaths;
            handler.userIncludeFilePaths = this.userIncludeFilePaths;
            handler.systemIncludePaths = this.systemIncludePaths;
            handler.startFile = this.startFile;
            
            // do not restore include info if state is cleaned
            if (!isCleaned()) {
                if (this.recurseIncludes != null) {
                    handler.recurseIncludes = new HashMap<CharSequence, Integer>();
                    handler.recurseIncludes.putAll(this.recurseIncludes);
                }
                if (this.inclStack != null) {
                    handler.inclStack = new LinkedList<IncludeInfo>();
                    handler.inclStack.addAll(this.inclStack);
                }
            }
        }

        @Override
        public String toString() {
            return APTIncludeHandlerImpl.toString(startFile.getStartFile(), systemIncludePaths, userIncludePaths, userIncludeFilePaths, recurseIncludes, inclStack);
        }
        
        public void write(DataOutput output) throws IOException {
            assert output != null;
            startFile.write(output);
            
            assert systemIncludePaths != null;
            assert userIncludePaths != null;
            
            int size = systemIncludePaths.size();
            output.writeInt(size);
            for (int i = 0; i < size; i++) {
                output.writeUTF(systemIncludePaths.get(i).getAsString());
            }
            
            size = userIncludePaths.size();
            output.writeInt(size);
            
            for (int i = 0; i < size; i++) {
                output.writeUTF(userIncludePaths.get(i).getAsString());
            }
            
            size = userIncludeFilePaths.size();
            output.writeInt(size);
            
            for (int i = 0; i < size; i++) {
                output.writeUTF(userIncludeFilePaths.get(i).getAsString());
            }

            if (recurseIncludes == null) {
                output.writeInt(-1);
            } else {
                final Set<Entry<CharSequence, Integer>> entrySet = recurseIncludes.entrySet();
                assert entrySet != null;
                final Iterator<Entry<CharSequence, Integer>> setIterator = entrySet.iterator();
                assert setIterator != null;
                output.writeInt(entrySet.size());
                
                while (setIterator.hasNext()) {
                    final Entry<CharSequence, Integer> entry = setIterator.next();
                    assert entry != null;
                    
                    output.writeUTF(entry.getKey().toString());
                    output.writeInt(entry.getValue().intValue());
                }
            }
            
            if (inclStack == null) {
                output.writeInt(-1);
            } else {
                size = inclStack.size();
                output.writeInt(size);
                Iterator<IncludeInfo> it = inclStack.iterator();
                while(it.hasNext()) {
                    final IncludeInfo inclInfo = it.next();
                    assert inclInfo != null;
                    
                    final IncludeInfoImpl inclInfoImpl = new IncludeInfoImpl(
                            inclInfo.getIncludedPath(), 
                            inclInfo.getIncludeDirectiveLine(), 
                            inclInfo.getIncludeDirectiveOffset(),
                            inclInfo.getIncludedDirIndex());
                    assert inclInfoImpl != null;
                    
                    inclInfoImpl.write(output);
                    
                }
            }
        }
        
        public StateImpl(final DataInput input) throws IOException {
            assert input != null;
            final APTStringManager pathManager = FilePathCache.getManager();
            
            startFile = new StartEntry(input);

            int size = input.readInt();
            systemIncludePaths = new ArrayList<IncludeDirEntry>(size);
            for (int i = 0; i < size; i++) {
                IncludeDirEntry path = IncludeDirEntry.get(input.readUTF());
                systemIncludePaths.add(i, path);
            }
            
            size = input.readInt();
            userIncludePaths = new ArrayList<IncludeDirEntry>(size);
            for (int i = 0; i < size; i++) {
                IncludeDirEntry path = IncludeDirEntry.get(input.readUTF());
                userIncludePaths.add(i, path);                
            }

            size = input.readInt();
            userIncludeFilePaths = new ArrayList<IncludeDirEntry>(size);
            for (int i = 0; i < size; i++) {
                IncludeDirEntry path = IncludeDirEntry.get(input.readUTF());
                userIncludePaths.add(i, path);
            }

            size = input.readInt();
            if (size == -1) {
                recurseIncludes = null;
            } else {
                recurseIncludes = new HashMap<CharSequence, Integer>();
                
                for (int i = 0; i < size; i++) {
                    CharSequence key = pathManager.getString(input.readUTF());
                    final Integer value = Integer.valueOf(input.readInt());
                    
                    recurseIncludes.put(key, value);
                }
            }
            
            size = input.readInt();
            
            if (size == -1) {
                inclStack = null;
            } else {
                inclStack = new LinkedList<IncludeInfo>();
                
                for (int i = 0; i < size; i++) {
                    final IncludeInfoImpl impl = new IncludeInfoImpl(input);
                    assert impl != null;
                    
                    inclStack.add(i, impl);
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
            int hash = 5;
            hash = 67 * hash + (this.startFile != null ? this.startFile.hashCode() : 0);
            hash = 67 * hash + (this.inclStack != null ? this.inclStack.hashCode() : 0);
            return hash;
        }
        
        private boolean compareStacks(LinkedList<IncludeInfo> inclStack1, LinkedList<IncludeInfo> inclStack2) {
            if (inclStack1 != null) {
                int size = inclStack1.size();
                if (inclStack2 == null || inclStack2.size() != size) {
                    return false;
                }
                Iterator<IncludeInfo> it1 = inclStack1.iterator();
                Iterator<IncludeInfo> it2 = inclStack2.iterator();
                while(it1.hasNext()) {
                    IncludeInfo cur1 = it1.next();
                    IncludeInfo cur2 = it2.next();
                    if (!cur1.equals(cur2)) {
                        return false;
                    }
                }
                return true;
            } else {
                return inclStack2 == null;
            }
        }        

        /*package*/ LinkedList<IncludeInfo> getIncludeStack() {
            return this.inclStack;
        }
        
        /*package*/ boolean isCleaned() {
            return this.recurseIncludes == null && this.inclStack != null; // was created as clean state
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

    private boolean pushIncludeImpl(CharSequence path, int directiveLine, int directiveOffset, int resolvedDirIndex) {
        if (recurseIncludes == null) {
            assert (inclStack == null): inclStack.toString() + " started on " + startFile;
            inclStack = new LinkedList<IncludeInfo>();
            recurseIncludes = new HashMap<CharSequence, Integer>();
        }
        assert CharSequences.isCompact(path) : "must be char sequence key " + path; // NOI18N
        Integer counter = recurseIncludes.get(path);
        counter = (counter == null) ? Integer.valueOf(1) : Integer.valueOf(counter.intValue()+1);
        if (counter.intValue() < MAX_INCLUDE_DEEP) {
            recurseIncludes.put(path, counter);
            inclStack.addLast(new IncludeInfoImpl(path, directiveLine, directiveOffset, resolvedDirIndex));
            return true;
        } else {
            assert (recurseIncludes.get(path) != null) : "included file must be in map"; // NOI18N
            APTUtils.LOG.log(Level.WARNING, "RECURSIVE inclusion:\n\t{0}\n\tin {1}\n", new Object[] { path , getCurPath() }); // NOI18N
            return false;
        }
    }    
    
    private static final class IncludeInfoImpl implements IncludeInfo, SelfPersistent, Persistent {
        private final CharSequence path;
        private final int directiveLine;
        private final int directiveOffset;
        private final int resolvedDirIndex;
        
        public IncludeInfoImpl(CharSequence path, int directiveLine, int directiveOffset, int resolvedDirIndex) {
            assert path != null;
            this.path = path;
            assert directiveLine >= 0;
            this.directiveLine = directiveLine;
            this.directiveOffset = directiveOffset;
            this.resolvedDirIndex = resolvedDirIndex;
        }
        
        public IncludeInfoImpl(final DataInput input) throws IOException {
            assert input != null;
            this.path = FilePathCache.getManager().getString(input.readUTF());
            directiveLine = input.readInt();
            directiveOffset = input.readInt();
            resolvedDirIndex = input.readInt();
        }

        public CharSequence getIncludedPath() {
            return path;
        }

        public int getIncludeDirectiveLine() {
            return directiveLine;
        }

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

        public void write(final DataOutput output) throws IOException {
            assert output != null;
            
            output.writeUTF(path.toString());
            output.writeInt(directiveLine);
            output.writeInt(directiveOffset);
            output.writeInt(resolvedDirIndex);
        }

        public int getIncludedDirIndex() {
            return this.resolvedDirIndex;
        }
    }
      
    private CharSequence popIncludeImpl() {
        assert (inclStack != null);
        assert (!inclStack.isEmpty());
        assert (recurseIncludes != null);
        IncludeInfo inclInfo = inclStack.removeLast();
        CharSequence path = inclInfo.getIncludedPath();
        Integer counter = recurseIncludes.remove(path);
        assert (counter != null) : "must be added before"; // NOI18N
        // decrease include counter
        counter = Integer.valueOf(counter.intValue()-1);
        assert (counter.intValue() >= 0) : "can't be negative"; // NOI18N
        if (counter.intValue() != 0) {
            recurseIncludes.put(path, counter);
        }
        return path;
    }
    
    @Override
    public String toString() {
        return APTIncludeHandlerImpl.toString(startFile.getStartFile(), systemIncludePaths, userIncludePaths, userIncludeFilePaths, recurseIncludes, inclStack);
    }    
    
    private static String toString(CharSequence startFile,
                                    List<IncludeDirEntry> systemIncludePaths,
                                    List<IncludeDirEntry> userIncludePaths,
                                    List<IncludeDirEntry> userIncludeFilePaths,
                                    Map<CharSequence, Integer> recurseIncludes,
                                    LinkedList<IncludeInfo> inclStack) {
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

    private static String includesStack2String(LinkedList<IncludeInfo> inclStack) {
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
