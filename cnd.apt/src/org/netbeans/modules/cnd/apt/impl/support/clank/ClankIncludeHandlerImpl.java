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

package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.clang.lex.Token;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.support.APTFileSearch;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.filesystems.FileSystem;
import org.openide.util.Parameters;

/**
 * implementation of include handler based on clank's Preprocessor
 * @author Vladimir Voskresensky
 */
public class ClankIncludeHandlerImpl implements PPIncludeHandler {
    private List<IncludeDirEntry> systemIncludePaths;
    private List<IncludeDirEntry> userIncludePaths;
    private List<IncludeDirEntry> userIncludeFilePaths;

    private StartEntry startFile;
    private final APTFileSearch fileSearch;
    private static final Token[] NO_TOKENS = new Token[0];
    
    private int inclStackIndex;
    private Token[] tokens = NO_TOKENS;
    private int nrTokens = -1;

    public  ClankIncludeHandlerImpl(StartEntry startFile) {
        this(startFile, new ArrayList<IncludeDirEntry>(0), new ArrayList<IncludeDirEntry>(0), new ArrayList<IncludeDirEntry>(0), startFile.getFileSearch());
    }
    
    public ClankIncludeHandlerImpl(StartEntry startFile,
                                    List<IncludeDirEntry> systemIncludePaths,
                                    List<IncludeDirEntry> userIncludePaths,
                                    List<IncludeDirEntry> userIncludeFilePaths, APTFileSearch fileSearch) {
        assert APTTraceFlags.USE_CLANK;
        Parameters.notNull("startFile", startFile);
        this.startFile = startFile;
        this.systemIncludePaths = systemIncludePaths;
        this.userIncludePaths = userIncludePaths;
        this.userIncludeFilePaths = userIncludeFilePaths;
        this.fileSearch = fileSearch;
        this.inclStackIndex = 0;
    }

    @Override
    public StartEntry getStartEntry() {
        return startFile;
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

    public List<IncludeDirEntry> getUserIncludeFilePaths() {
        return Collections.unmodifiableList(userIncludeFilePaths);
    }

    public List<IncludeDirEntry> getUserIncludePaths() {
        return Collections.unmodifiableList(userIncludePaths);
    }

    public List<IncludeDirEntry> getSystemIncludePaths() {
        return Collections.unmodifiableList(systemIncludePaths);
    }

    public Token[] getTokens() {
        if (tokens == NO_TOKENS) {
            return null;
        } else {
            return tokens;
        }
    }

    public int getNrTokens() {
        return nrTokens;
    }
    
    void setIncludeInfo(int inclStackIndex, Token[] tokens, int nrTokens) {
        this.inclStackIndex = inclStackIndex;
        if (tokens == null) {
            this.tokens = NO_TOKENS;
            this.nrTokens = 0;
        } else {
            this.tokens = tokens;
            this.nrTokens = nrTokens;
        }
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

        private final int inclStackIndex;
        private final Token[] tokens;
        private final int nrTokens;
        private int hashCode = 0;
        
        protected StateImpl(ClankIncludeHandlerImpl handler) {
            this.systemIncludePaths = handler.systemIncludePaths;
            this.userIncludePaths = handler.userIncludePaths;
            this.userIncludeFilePaths = handler.userIncludeFilePaths;
            this.startFile = handler.startFile;

            this.inclStackIndex = handler.inclStackIndex;
            this.tokens = handler.tokens;
            this.nrTokens = handler.nrTokens;
        }
        
        private StateImpl(StateImpl other, boolean cleanState) {
            // shared information
            this.startFile = other.startFile;
            
            // state object is immutable => safe to share stacks
            this.inclStackIndex = other.inclStackIndex;
            this.tokens = other.tokens;
            this.nrTokens = other.nrTokens;
	    
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
        
        private void restoreTo(ClankIncludeHandlerImpl handler) {
            handler.userIncludePaths = this.userIncludePaths;
            handler.userIncludeFilePaths = this.userIncludeFilePaths;
            handler.systemIncludePaths = this.systemIncludePaths;
            handler.startFile = this.startFile;
            
            handler.inclStackIndex = this.inclStackIndex;
            handler.tokens = this.tokens;
            handler.nrTokens = this.nrTokens;
            // do not restore include info if state is cleaned
            if (!isCleaned()) {
                // TODO: put tokens into handler
            }
        }

        @Override
        public String toString() {
            return ClankIncludeHandlerImpl.toString(startFile.getStartFile(), systemIncludePaths, userIncludePaths, userIncludeFilePaths, inclStackIndex);
        }
        
        public void write(RepositoryDataOutput output) throws IOException {
            assert output != null;
            startFile.write(output);
            
            assert systemIncludePaths != null;
            assert userIncludePaths != null;
            
            int size = systemIncludePaths.size();
            output.writeInt(size);
            for (int i = 0; i < size; i++) {
                IncludeDirEntry inc = systemIncludePaths.get(i);
                output.writeFileSystem(inc.getFileSystem());
                output.writeFilePathForFileSystem(inc.getFileSystem(), inc.getAsSharedCharSequence());
            }
            
            size = userIncludePaths.size();
            output.writeInt(size);
            
            for (int i = 0; i < size; i++) {
                IncludeDirEntry inc = userIncludePaths.get(i);
                output.writeFileSystem(inc.getFileSystem());
                output.writeFilePathForFileSystem(inc.getFileSystem(), inc.getAsSharedCharSequence());
            }
            
            size = userIncludeFilePaths.size();
            output.writeInt(size);
            
            for (int i = 0; i < size; i++) {
                IncludeDirEntry inc = userIncludeFilePaths.get(i);
                output.writeFileSystem(inc.getFileSystem());
                output.writeFilePathForFileSystem(inc.getFileSystem(), inc.getAsSharedCharSequence());
            }

            output.writeInt(inclStackIndex);
        }
        
        public StateImpl(final RepositoryDataInput input) throws IOException {
            assert input != null;
            
            startFile = new StartEntry(input);
            
            int size = input.readInt();
            systemIncludePaths = new ArrayList<IncludeDirEntry>(size);
            for (int i = 0; i < size; i++) {
                FileSystem fs = input.readFileSystem();
                IncludeDirEntry path = IncludeDirEntry.get(fs, input.readFilePathForFileSystem(fs).toString());
                systemIncludePaths.add(i, path);
            }
            
            size = input.readInt();
            userIncludePaths = new ArrayList<IncludeDirEntry>(size);
            for (int i = 0; i < size; i++) {
                FileSystem fs = input.readFileSystem();
                IncludeDirEntry path = IncludeDirEntry.get(fs, input.readFilePathForFileSystem(fs).toString());
                userIncludePaths.add(i, path);
            }

            size = input.readInt();
            userIncludeFilePaths = new ArrayList<IncludeDirEntry>(size);
            for (int i = 0; i < size; i++) {
                FileSystem fs = input.readFileSystem();
                IncludeDirEntry path = IncludeDirEntry.get(fs, input.readFilePathForFileSystem(fs).toString());
                userIncludeFilePaths.add(i, path);
            }
            
            inclStackIndex = input.readInt();
            nrTokens = 0;
            tokens = NO_TOKENS;
        }        
	
	public final StartEntry getStartEntry() {
	    return startFile;
	}

        @Override
        public boolean equals(Object obj) {
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            }
            StateImpl other = (StateImpl)obj;
            return this.startFile.equals(other.startFile) &&
                    (this.inclStackIndex == other.inclStackIndex);
        }

        @Override
        public int hashCode() {
            int hash = hashCode;
            if (hash == 0) {
                hash = 5;
                hash = 67 * hash + (this.startFile != null ? this.startFile.hashCode() : 0);
                hash = 67 * hash + this.inclStackIndex;
                hashCode = hash;
            }
            return hash;
        }
        
        public  boolean isCleaned() {
            return this.userIncludeFilePaths == CLEANED_MARKER; // was created as clean state
        }
        
        public  ClankIncludeHandlerImpl.State copy(boolean cleanState) {
            return new StateImpl(this, cleanState);
        }
        
        public  List<IncludeDirEntry> getSysIncludePaths() {
            return this.systemIncludePaths;
        }
        
        public  List<IncludeDirEntry> getUserIncludePaths() {
            return this.userIncludePaths;
        }        

        public  List<IncludeDirEntry> getUserIncludeFilePaths() {
            return this.userIncludeFilePaths;
        }

        public int getIncludeStackIndex() {
            return this.inclStackIndex;
        }
    }
    
    @Override
    public String toString() {
        return ClankIncludeHandlerImpl.toString(startFile.getStartFile(), systemIncludePaths, userIncludePaths, userIncludeFilePaths, inclStackIndex);
    }    
    
    private static String toString(CharSequence startFile,
                                    List<IncludeDirEntry> systemIncludePaths,
                                    List<IncludeDirEntry> userIncludePaths,
                                    List<IncludeDirEntry> userIncludeFilePaths,
                                    int inclStackIndex) {
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
        retValue.append(includesStack2String(inclStackIndex));
        return retValue.toString();
    }

    private static String includesStack2String(int inclStackIndex) {
        StringBuilder retValue = new StringBuilder();
        if (inclStackIndex == 0) {
            retValue.append("<not from #include>"); // NOI18N
        } else {
            retValue.append("from ").append(inclStackIndex).append("th #include>"); // NOI18N
        }
        return retValue.toString();
    }
}
