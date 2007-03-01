/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.Token;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.utils.APTSerializeUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * APTMacroMap base implementation
 * support collection of macros and saving/restoring this collection
 * @author Vladimir Voskresensky
 */
public abstract class APTBaseMacroMap implements APTMacroMap {
    protected APTMacroMapSnapshot active;
    
    /**
     * Creates a new instance of APTBaseMacroMap
     */    
    public APTBaseMacroMap() {
        active = makeSnapshot(null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // manage define/undef macros
    
    public final void define(Token name, List value) {
        define(name, null, value);
    }

    public void define(Token name, Collection params, List value) {
        active.macros.put(APTUtils.getTokenTextKey(name), createMacro(name, params, value));
    }
    
    public void undef(Token name) {
        active.macros.put(APTUtils.getTokenTextKey(name), APTMacroMapSnapshot.UNDEFINED_MACRO);
    }
    
    /** method to implement in children */
    protected abstract APTMacro createMacro(Token name, Collection params, List/*<Token>*/ value);
    
    ////////////////////////////////////////////////////////////////////////////
    // manage macro access

    public boolean isDefined(Token token) {
        return getMacro(token) != null;
    } 

    public APTMacro getMacro(Token token) {
        return active.getMacro(token);
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
        public APTMacroMapSnapshot snap;
        
        public StateImpl(APTMacroMapSnapshot snap) {
            this.snap = snap;
        }
        
        public String toString() {
            return snap != null ? snap.toString() : "<no snap>"; // NOI18N
        }

        public boolean clean() {
            boolean cleaned = false;
            if (snap != null) {
                while (snap.parent != null) {
                    snap = snap.parent;
                    cleaned = true;
                }
            }
            return cleaned;
        }
        
        ////////////////////////////////////////////////////////////////////////
        // persistence support

        public void write(DataOutput output) throws IOException {
            APTSerializeUtils.writeSnapshot(this.snap, output);
        }

        public StateImpl(DataInput input) throws IOException {
            this.snap = APTSerializeUtils.readSnapshot(input);
        }         
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // implementation details    

    public String toString() {
        Map tmpMap = new HashMap();
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
        
        protected APTMacro createMacro(Token name, Token[] params, List value) {
            return null;
        }

        public boolean pushExpanding(Token token) {
            return false;
        }

        public void popExpanding() {
//            return null;
        }

        public boolean isExpanding(Token token) {
            return false;
        }    
        
        public boolean isDefined(Token token) {
            return false;
        }

        public APTMacro getMacro(Token token) {
            return null;
        }      

        public void define(Token name, Collection params, List value) {
        }

        public void define(Token name, List value) {
        }

        public void undef(Token name) {
        }

        public void setState(State state) {
        }

        public State getState() {
            return new StateImpl((APTMacroMapSnapshot )null);
        }               
    };    
}
