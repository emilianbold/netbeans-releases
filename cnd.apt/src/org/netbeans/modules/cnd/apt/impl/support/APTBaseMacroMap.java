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

import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.structure.APTDefineNode;
import org.netbeans.modules.cnd.apt.structure.APTDefine;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacro.Kind;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTMacroMap.State;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.openide.util.CharSequences;

/**
 * APTMacroMap base implementation
 * support collection of macros and saving/restoring this collection
 * @author Vladimir Voskresensky
 */
public abstract class APTBaseMacroMap implements APTMacroMap {

    protected APTMacroMapSnapshot active;
    
    private static final String DEFINE_PREFIX="#define "; // NOI18N
    
    /**
     * Creates a new instance of APTBaseMacroMap
     */    
    protected APTBaseMacroMap() {
        active = makeSnapshot(null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // manage define/undef macros

    
    protected final void fill(List<String> macros, boolean isSystem) {
        // update callback with user macros information
        for (Iterator<String> it = macros.iterator(); it.hasNext();) {
            String macro = it.next();
            if (APTTraceFlags.TRACE_APT) {
                System.err.println("adding macro in map " + macro); // NOI18N
            }
            define(macro, isSystem);
        }           
    }
    
    /** 
     * analyze macroText string with structure "macro=value" and put in map
     */
    private void define(String macroText, boolean isSystem) {
        macroText = DEFINE_PREFIX + macroText;
        TokenStream stream = APTTokenStreamBuilder.buildTokenStream(macroText, APTLanguageSupport.UNKNOWN);
        try {
            APTToken next = (APTToken) stream.nextToken();
            // use define node to initialize #define directive from stream
            APTDefineNode defNode = new APTDefineNode(next);
            boolean look4Equal = true;
            do {
                next = (APTToken) stream.nextToken();
                if (look4Equal && (next.getType() == APTTokenTypes.ASSIGNEQUAL)) {
                    // skip the first equal token, it's delimeter
                    look4Equal = false;
                    next = (APTToken) stream.nextToken();
                }
            } while (defNode.accept(null, next));
            // special check for macros without values, we must set it to be 1
            if (defNode.getBody().isEmpty() && look4Equal) {
                defNode.accept(null, APTUtils.DEF_MACRO_BODY.get(0));
            }
            if (isSystem) {
                defineImpl(null, defNode, Kind.COMPILER_PREDEFINED);
            } else {
                defineImpl(null, defNode, Kind.USER_SPECIFIED);
            }
        } catch (TokenStreamException ex) {
            APTUtils.LOG.log(Level.SEVERE, 
                    "error on lexing macros {0}\n\t{1}", // NOI18N
                    new Object[] {macroText, ex.getMessage()});
        }
    }

    public void define(APTFile file, APTDefine define, Kind macroType) {
        defineImpl(file, define, macroType);
    }
    
    private void defineImpl(APTFile file, APTDefine define, Kind macroType) {
        APTToken name = define.getName();
        CharSequence filePath = (file == null ? CharSequences.empty() : file.getPath());
        putMacro(name.getTextID(), createMacro(filePath, define, macroType));
    }

    public void undef(APTFile file, APTToken name) {
        putMacro(name.getTextID(), APTMacroMapSnapshot.UNDEFINED_MACRO);
    }

    protected void putMacro(CharSequence name, APTMacro macro) {
        active.putMacro(name, macro);
    }
    /** method to implement in children */
    protected abstract APTMacro createMacro(CharSequence file, APTDefine define, Kind macroType);
    
    ////////////////////////////////////////////////////////////////////////////
    // manage macro access

    public final boolean isDefined(APTToken token) {
        return getMacro(token) != null;
    } 

    public final boolean isDefined(CharSequence token) {
        token = CharSequences.create(token);
        return getMacro(token) != null;
    } 

    public APTMacro getMacro(APTToken token) {
        APTMacro res = active.getMacro(token);
        return (res != APTMacroMapSnapshot.UNDEFINED_MACRO) ? res : null;
    }

    protected APTMacro getMacro(CharSequence token) {
        assert CharSequences.isCompact(token) : "must not be String object " + token;
        APTMacro res = active.getMacro(token);
        return (res != APTMacroMapSnapshot.UNDEFINED_MACRO) ? res : null;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // manage state (save/restore)
    
    protected abstract APTMacroMapSnapshot makeSnapshot(APTMacroMapSnapshot parent);
    
    public State getState() {
        //Create new snapshot instance in the tree
        changeActiveSnapshotIfNeeded();
        return new StateImpl(active.parent);
    }
    
    protected void changeActiveSnapshotIfNeeded () {
        // do not use isEmpty approach yet.
        // not everything is clear yet, how clean of states is working in this case
        // some memory could remain and it's not good.
        // TODO: Needs additional investigation
        if (true || !active.isEmtpy()) {
            active = makeSnapshot(active);
        }
    }
    
    public void setState(State state) {
        active = makeSnapshot(((StateImpl)state).snap);
    }
    
    public static class StateImpl implements State {
        public final APTMacroMapSnapshot snap;
        
        public StateImpl(APTMacroMapSnapshot snap) {
            this.snap = snap;
        }
        
        protected StateImpl(StateImpl state, boolean cleanedState) {
            this.snap = cleanedState ? getFirstSnapshot(state.snap) : state.snap;
        }
        
        @Override
        public String toString() {
            return snap != null ? snap.toString() : "<no snap>"; // NOI18N
        }

        public StateImpl copyCleaned() {
            return new StateImpl(this, true);
        }

        boolean isEmptyActiveMacroMap() {
            return snap == null || snap.isEmtpy();
        }
        ////////////////////////////////////////////////////////////////////////
        // persistence support

        public void write(DataOutput output) throws IOException {
            APTSerializeUtils.writeSnapshot(this.snap, output);
        }

        public StateImpl(DataInput input) throws IOException {
            this.snap = APTSerializeUtils.readSnapshot(input);
        }    
        
        ////////////////////////////////////////////////////////////////////////
        private APTMacroMapSnapshot getFirstSnapshot(APTMacroMapSnapshot snap) {
            if (snap != null) {
                while (snap.parent != null) {
                    snap = snap.parent;
                }
            }
            return snap;
        }        
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details    

    @Override
    public String toString() {
        Map<CharSequence, APTMacro> tmpMap = new HashMap<CharSequence, APTMacro>();
        APTMacroMapSnapshot.addAllMacros(active, tmpMap);
        return APTUtils.macros2String(tmpMap);
    }
    
    /*public boolean equals(Object obj) {
        boolean retValue = false;
        if (obj == null) {
            return false;
        }
        if (obj instanceof APTBaseMacroMap) {
            retValue = equals(this, (APTBaseMacroMap)obj);
        }
        return retValue;
    }

    /*private static boolean equals(APTBaseMacroMap map1, APTBaseMacroMap map2) {
        boolean equals = true;
        List macrosSorted1 = new ArrayList(map1.defined_macros.keySet());
        List macrosSorted2 = new ArrayList(map2.defined_macros.keySet());
        if (macrosSorted1.size() != macrosSorted2.size()) {
            return false;
        }
        Collections.sort(macrosSorted1);
        Collections.sort(macrosSorted2);            
        for (Iterator it1 = macrosSorted1.iterator(), it2 = macrosSorted2.iterator(); equals && it1.hasNext();) {
            String key1 = (String) it1.next();
            String key2 = (String) it2.next();
            equals &= key1.equalsIgnoreCase(key2);
        }         
        return equals;
    }
    
    public int hashCode() {
        int retValue;
        
        retValue = defined_macros.keySet().hashCode();
        return retValue;
    }*/
    
    protected static final APTMacroMap EMPTY = new EmptyMacroMap();
    private static final class EmptyMacroMap implements APTMacroMap {
        private EmptyMacroMap() {
        }
        
        protected APTMacro createMacro(APTDefine define) {
            return null;
        }

        public boolean pushExpanding(APTToken token) {
            return false;
        }

        public void popExpanding() {
        }

        public boolean isExpanding(APTToken token) {
            return false;
        }    
        
        public boolean isDefined(APTToken token) {
            return false;
        }
        
        public boolean isDefined(CharSequence token) {
            return false;
        }


        public APTMacro getMacro(APTToken token) {
            return null;
        }      

        public void define(APTFile file, APTDefine define, Kind macroType) {
        }

        public void undef(APTFile file, APTToken name) {
        }

        public void setState(State state) {
        }

        public State getState() {
            return new StateImpl((APTMacroMapSnapshot )null);
        }
    };    
}
