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

package org.netbeans.modules.cnd.apt.utils;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.APTBaseMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.APTFileMacroMap;
import org.netbeans.modules.cnd.apt.impl.support.APTIncludeHandlerImpl;
import org.netbeans.modules.cnd.apt.impl.support.APTMacroImpl;
import org.netbeans.modules.cnd.apt.impl.support.APTMacroMapSnapshot;
import org.netbeans.modules.cnd.apt.impl.support.APTPreprocHandlerImpl;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.support.APTFileBuffer;
import org.netbeans.modules.cnd.apt.support.APTIncludeHandler;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.openide.util.CharSequences;

/**
 * utilities for APT serialization
 * @author Vladimir Voskresensky
 */
public class APTSerializeUtils {
    
    private APTSerializeUtils() {
    }
    
    static public void writeAPT(ObjectOutputStream out, APT apt) throws IOException {
        out.writeObject(apt);
        // the tree structure has a lot of siblings =>
        // StackOverflow exceptions during serialization of "next" field
        // we try to prevent it by using own procedure of writing 
        // tree structure
        if (apt != null) {
            writeTree(out, apt);
        }
    }
    
    // symmetric to writeObject
    static public APT readAPT(ObjectInputStream in) throws IOException, ClassNotFoundException {
        APT apt = (APT)in.readObject();
        if (apt != null) {
            // read tree structure into this node
            readTree(in, apt);
        }
        return apt;
    }

    ////////////////////////////////////////////////////////////////////////////
    // we have StackOverflow when serialize APT due to it's tree structure:
    // to many recurse calls to writeObject on writing "next" field
    // let's try to reduce depth of recursion by depth of tree
    
    private static final int CHILD = 1;
    private static final int SIBLING = 2;
    private static final int END_APT = 3;
    
    static private void writeTree(ObjectOutputStream out, APT root) throws IOException {
        assert (root != null) : "there must be something to write"; // NOI18N
        APT node = root;
        do {
            APT child = node.getFirstChild();
            if (child != null) {
                // due to not huge depth of the tree                
                // write child without optimization
                out.writeInt(CHILD);
                writeAPT(out, child);
            }
            node = node.getNextSibling();            
            if (node != null) {
                // we don't want to use recursion on writing sibling
                // to prevent StackOverflow, 
                // we use while loop for writing siblings
                out.writeInt(SIBLING);
                // write node data
                out.writeObject(node);                 
            }
        } while (node != null);
        out.writeInt(END_APT);
    }

    static private void readTree(ObjectInputStream in, APT root) throws IOException, ClassNotFoundException {
        assert (root != null) : "there must be something to read"; // NOI18N
        APT node = root;
        do {
            int kind = in.readInt();
            switch (kind) {
                case END_APT:
                    return;
                case CHILD:
                    node.setFirstChild(readAPT(in));
                    break;
                case SIBLING:
                    APT sibling = (APT) in.readObject();
                    node.setNextSibling(sibling);
                    node = sibling;
                    break;
                default:
                    assert(false);
            }            
        } while (node != null);
    }
    
    private static int fileIndex = 0;
    private static final boolean TRACE = true;
    @org.netbeans.api.annotations.common.SuppressWarnings("RV")
    static public APT testAPTSerialization(APTFileBuffer buffer, APT apt) {
        File file = buffer.getFile();
        APT aptRead = null;
        // testing caching ast
        String prefix = "cnd_apt_"+(fileIndex++); // NOI18N
        String suffix = file.getName();
        try {
            File out = File.createTempFile(prefix, suffix);                
            if (TRACE) { System.out.println("...saving APT of file " + file.getAbsolutePath() + " into tmp file " + out); } // NOI18N
            long astTime = System.currentTimeMillis();
            // write
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(out), APTTraceFlags.BUF_SIZE));
            try {
                writeAPT(oos, apt);
            } finally {
                oos.close();
            }
            long writeTime = System.currentTimeMillis() - astTime;
            if (TRACE) { System.out.println("saved APT of file " + file.getAbsolutePath() + " withing " + writeTime + "ms"); } // NOI18N
            astTime = System.currentTimeMillis();
            // read
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(out), APTTraceFlags.BUF_SIZE));
            try {
                aptRead = readAPT(ois);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            } finally {
                ois.close();                
            }
            long readTime = System.currentTimeMillis() - astTime;
            if (TRACE) { System.out.println("read APT of file " + file.getAbsolutePath() + " withing " + readTime + "ms"); } // NOI18N
            out.delete();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return aptRead;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // persistence support
    
    public static void writeSystemMacroMap(APTMacroMap macroMap, DataOutput output) throws IOException {
        //throw new UnsupportedOperationException("Not yet supported"); // NOI18N
    }
    
    public static APTMacroMap readSystemMacroMap(DataInput input) throws IOException {
        //throw new UnsupportedOperationException("Not yet supported"); // NOI18N
        return null;
    }
    
    public static void writeMacroMapState(APTMacroMap.State state, DataOutput output) throws IOException {
        assert state != null;
        if (state instanceof APTFileMacroMap.FileStateImpl) {
            output.writeInt(MACRO_MAP_FILE_STATE_IMPL);
            ((APTFileMacroMap.FileStateImpl)state).write(output);
        } else {
            assert state instanceof APTBaseMacroMap.StateImpl;
            output.writeInt(MACRO_MAP_STATE_IMPL);
            ((APTBaseMacroMap.StateImpl)state).write(output);
        }
    }
    
    public static APTMacroMap.State readMacroMapState(DataInput input) throws IOException {
        int handler = input.readInt();
        APTMacroMap.State state;
        if (handler == MACRO_MAP_FILE_STATE_IMPL) {
            state = new APTFileMacroMap.FileStateImpl(input);
        } else {
            assert handler == MACRO_MAP_STATE_IMPL;
            state = new APTBaseMacroMap.StateImpl(input);
        }
        return state;
    }
    
    public static void writeIncludeState(APTIncludeHandler.State state, DataOutput output) throws IOException {
        assert state != null;
        assert state instanceof APTIncludeHandlerImpl.StateImpl;
        ((APTIncludeHandlerImpl.StateImpl)state).write(output);
    }
    
    public static APTIncludeHandler.State readIncludeState(DataInput input) throws IOException {
        APTIncludeHandler.State state = new APTIncludeHandlerImpl.StateImpl(input);
        return state;
    }    

    public static void writePreprocState(APTPreprocHandler.State state, DataOutput output) throws IOException {
        assert state != null;
        if (state instanceof APTPreprocHandlerImpl.StateImpl) {
            output.writeInt(PREPROC_STATE_STATE_IMPL);
            ((APTPreprocHandlerImpl.StateImpl)state).write(output);
        } else {
            throw new IllegalArgumentException("unknown preprocessor state" + state);  //NOI18N
        }        
    }
    
    public static APTPreprocHandler.State readPreprocState(DataInput input) throws IOException {
        int handler = input.readInt();
        APTPreprocHandler.State out;
        switch (handler) {
            case PREPROC_STATE_STATE_IMPL:
                out = new APTPreprocHandlerImpl.StateImpl(input);
                break;
            default:
                throw new IllegalArgumentException("unknown preprocessor state handler" + handler);  //NOI18N
        }
        return out;
    } 
    
    ////////////////////////////////////////////////////////////////////////////
    // persist snapshots
    
    public static void writeSnapshot(APTMacroMapSnapshot snap, DataOutput output) throws IOException {
        // FIXUP: we do not support yet writing snapshots!
//        if (snap == null) {
            output.writeInt(NULL_POINTER);
//        } else {
//            output.writeInt(MACRO_MAP_SNAPSHOT);
//            snap.write(output);
//        }
    }

    public static APTMacroMapSnapshot readSnapshot(DataInput input) throws IOException {
        int handler = input.readInt();
        APTMacroMapSnapshot snap = null;
        if (handler != NULL_POINTER) {
            assert handler == MACRO_MAP_SNAPSHOT;
            snap = new APTMacroMapSnapshot(input);
        }
        return snap;
    }

    public static void writeStringToMacroMap(Map<CharSequence, APTMacro> macros, DataOutput output) throws IOException {
        assert macros != null;
        output.writeInt(macros.size());
        for (Entry<CharSequence, APTMacro> entry : macros.entrySet()) {
            assert entry != null;
            assert CharSequences.isCompact(entry.getKey());
            String key = entry.getKey().toString();
            output.writeUTF(key);
            APTMacro macro = entry.getValue();
            assert macro != null;
            writeMacro(macro, output);            
        }
    }

    public static void readStringToMacroMap(int collSize, Map<CharSequence, APTMacro> macros, DataInput input) throws IOException {
        for (int i = 0; i < collSize; ++i) {
            CharSequence key = CharSequences.create(input.readUTF());
            assert key != null;
            APTMacro macro = readMacro(input);
            assert macro != null;
            macros.put(key, macro);
        }
    }
    
    private static void writeMacro(APTMacro macro, DataOutput output) throws IOException {
        assert macro != null;
        if (macro == APTMacroMapSnapshot.UNDEFINED_MACRO) {
            output.writeInt(UNDEFINED_MACRO);
        } else if (macro instanceof APTMacroImpl) {
            output.writeInt(MACRO_IMPL);
            ((APTMacroImpl)macro).write(output);
        }
    }
    
    private static APTMacro readMacro(DataInput input) throws IOException {
        int handler = input.readInt();
        APTMacro macro;
        if (handler == UNDEFINED_MACRO) {
            macro = APTMacroMapSnapshot.UNDEFINED_MACRO;
        } else {
            assert handler == MACRO_IMPL;
            macro = new APTMacroImpl(input);
        }
        return macro;
    }
    
    private static final int NULL_POINTER               = -1;
    private static final int MACRO_MAP_STATE_IMPL       = 1;
    private static final int MACRO_MAP_FILE_STATE_IMPL  = MACRO_MAP_STATE_IMPL + 1;
    private static final int PREPROC_STATE_STATE_IMPL   = MACRO_MAP_FILE_STATE_IMPL + 1;
    private static final int MACRO_MAP_SNAPSHOT         = PREPROC_STATE_STATE_IMPL + 1;
    private static final int UNDEFINED_MACRO            = MACRO_MAP_SNAPSHOT + 1;
    private static final int MACRO_IMPL                 = UNDEFINED_MACRO + 1;
    
}
