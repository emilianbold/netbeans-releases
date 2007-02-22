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
import java.util.*;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 *
 * @author gorrus
 */
public class APTMacroMapSnapshot {
    protected final Map/*<getTokenTextKey(token), APTMacro>*/ macros = new HashMap();
    protected final APTMacroMapSnapshot parent;

    public APTMacroMapSnapshot(APTMacroMapSnapshot parent) {
        this.parent = parent;
    }
    
    public final APTMacro getMacro(Token token) {
        Object key = APTUtils.getTokenTextKey(token);
        APTMacroMapSnapshot currentSnap = this;
        while (currentSnap != null) {
            Object macro = currentSnap.macros.get(key);
            if (macro != null) {
                // If UNDEFINED_MACRO is found then the requested macro is undefined, return null
                return (macro != UNDEFINED_MACRO) ? (APTMacro)macro : null;
            }
            currentSnap = currentSnap.parent;
        }
        return null;
    }
    
    public String toString() {
        Map tmpMap = new HashMap();
        addAllMacros(this, tmpMap);
        return APTUtils.macros2String(tmpMap);
    }
    
    public static void addAllMacros(APTMacroMapSnapshot snap, Map out) {
        if (snap.parent != null) {
            addAllMacros(snap.parent, out);
        }
        for (Iterator iter=snap.macros.entrySet().iterator(); iter.hasNext();) {
            Map.Entry cur = (Map.Entry)iter.next();
            if (cur.getValue() != UNDEFINED_MACRO) {
                out.put(cur.getKey(), cur.getValue());
            } else {
                out.remove(cur.getKey());
            }
        }
    }    
    
    public boolean isEmtpy() {
        return macros.isEmpty();
    }
    
    //This is a single instance of a class to indicate that macro is undefined,
    //not a child of APTMacro to track errors more easily
    public static final UndefinedMacro UNDEFINED_MACRO = new UndefinedMacro();
    private static class UndefinedMacro {
        public String toString() {
            return "Macro undefined"; // NOI18N
        }
    }
}
