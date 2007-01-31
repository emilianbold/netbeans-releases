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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.modelimpl.csm.core.LineColOffsetableBase;

/**
 * Implements CsmMacro
 * represents file defined macros:
 * #define SUM(a, b) ((a)+(b))
 * #define MACRO VALUE
 * #define MACRO
 *
 * @author Vladimir Voskresensky
 */
public class MacroImpl extends LineColOffsetableBase implements CsmMacro {
    
    /** name of macros, i.e. SUM or MACRO */
    private String name;
    
    /** 
     * body of macros, 
     * i.e. ((a)+(b)) or VALUE, or empty string
     */
    private String body;
    
    /** 
     * flag to distinguish system and other types of macros 
     * now we support only macros in file => all macros are not system
     */
    private boolean system;
    
    /** 
     * immutable list of parameters, 
     * i.e. [a, b] or null if macros without parameters
     */
    private List/*<String>*/ params;
    
    /** Creates new instance of MacroImpl based on existed macro and specified container */
    public MacroImpl(CsmMacro macro, CsmFile containingFile) {
        this(macro.getName(), macro.getParameters(), macro.getBody(), containingFile, macro);
    }
    
    /** Creates new instance of MacroImpl based on macro information and specified position */
    public MacroImpl(String macroName, List/*<String>*/ macroParams, String macroBody, CsmOffsetable macroPos) {
        this(macroName, macroParams, macroBody, null, macroPos);
    }
    
    public MacroImpl(String macroName, List/*<String>*/ macroParams, String macroBody, CsmFile containingFile, CsmOffsetable macroPos) {
        super(containingFile, macroPos);
        assert(macroName != null);
        assert(macroName.length() > 0);
        assert(macroBody != null);
        this.name = macroName;
        this.system = false;
        this.body = macroBody;
        if (macroParams != null) {
            this.params = Collections.unmodifiableList(macroParams);
        }
    }
    
    public List/*<String>*/ getParameters() {
        return params;
    }
    
    public String getBody() {
        return body;
    }
    
    public boolean isSystem() {
        return system;
    }
    
    public String getName() {
        return name;
    }

    public String toString() {
        StringBuffer retValue = new StringBuffer();
        retValue.append("#define '"); // NOI18N
        retValue.append(getName());
        if (getParameters() != null) {
            retValue.append("["); // NOI18N
            for (Iterator it = getParameters().iterator(); it.hasNext();) {
                String param = (String) it.next();
                retValue.append(param);
                if (it.hasNext()) {
                    retValue.append(", "); // NOI18N
                }                
            }
            retValue.append("]"); // NOI18N
        }
        if (getBody().length() > 0) {
            retValue.append("'='"); // NOI18N
            retValue.append(getBody());
        }
        retValue.append("' ["); // NOI18N
        retValue.append(getStartPosition()).append("-").append(getEndPosition()); // NOI18N
        retValue.append("]"); // NOI18N
        return retValue.toString();
    }   
    
    public boolean equals(Object obj) {
        boolean retValue;
        if (obj == null || !(obj instanceof CsmMacro)) {
            retValue = false;
        } else {
            MacroImpl other = (MacroImpl)obj;
            retValue = MacroImpl.equals(this, other);
        }
        return retValue;
    }
    
    private static final boolean equals(MacroImpl one, MacroImpl other) {
        // compare only name and start offset
        return (one.getStartOffset() == other.getStartOffset()) && 
                (one.getName().compareTo(other.getName()) == 0);
    }
    
    public int hashCode() {
        int retValue = 17;
        retValue = 31*retValue + getStartOffset();
        retValue = 31*retValue + getName().hashCode();
        return retValue;
    }    
}
