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
    protected final Map/*<getTokenTextKey(token), APTMacro>*/ defined_macros;
    protected final Map/*<getTokenTextKey(token), APTMacro>*/ undefined_macros;
    protected final APTMacroMapSnapshot parent;

    public APTMacroMapSnapshot(boolean syncronizedMacros, APTMacroMapSnapshot parent) {
        if (syncronizedMacros) {
            defined_macros = Collections.synchronizedMap(new HashMap());
            undefined_macros = Collections.synchronizedMap(new HashMap());
        } else {
            defined_macros = new HashMap();
            undefined_macros = new HashMap();
        }
        this.parent = parent;
    }

    public APTMacro getMacro(Token token) {
        APTMacro macro = (APTMacro) defined_macros.get(APTUtils.getTokenTextKey(token));
        if (macro != null) {
            return macro;
        }
        if (undefined_macros.containsKey(APTUtils.getTokenTextKey(token))) {
            return null;
        }
        if (parent == null) {
            return null;
        }
        return parent.getMacro(token);
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
        out.putAll(snap.defined_macros);
        for (Iterator iter=snap.undefined_macros.keySet().iterator();iter.hasNext();) {
            out.remove(iter.next());
        }
    }    
    
    public boolean isEmtpy() {
        return (defined_macros.size() + undefined_macros.size()) == 0;
    }
}
