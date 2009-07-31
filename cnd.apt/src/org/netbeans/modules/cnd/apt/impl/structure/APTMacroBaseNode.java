/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.apt.impl.structure;

import java.io.Serializable;
import java.util.logging.Level;
import org.netbeans.modules.cnd.debug.DebugUtils;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.apt.utils.APTTraceUtils;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * base class for #define/#undef impl
 * @author Vladimir Voskresensky
 */
public abstract class APTMacroBaseNode extends APTTokenBasedNode
                                        implements Serializable {
    private static final long serialVersionUID = 1315417078059538898L;
    private APTToken macroName = EMPTY_NAME;
    
    /** Copy constructor */
    /**package*/APTMacroBaseNode(APTMacroBaseNode orig) {
        super(orig);
        this.macroName = orig.macroName;
    }
    
    /** Constructor for serialization **/
    protected APTMacroBaseNode() {
    }
    
    /** Creates a new instance of APTMacroBaseNode */
    public APTMacroBaseNode(APTToken token) {
        super(token);
    }

    public APT getFirstChild() {
        // #define/#undef doesn't have subtree
        return null;
    }

    public void setFirstChild(APT child) {
        // do nothing
        assert (false) : "define/undef doesn't support children"; // NOI18N        
    }

    public boolean accept(APTFile curFile,APTToken token) {
        if (APTUtils.isID(token)) {
            if (macroName != EMPTY_NAME) {
                // init macro name only once
                if (DebugUtils.STANDALONE) {
                    System.err.printf("%s, line %d: warning: extra tokens at end of %s directive\n", // NOI18N
                            APTTraceUtils.toFileString(curFile), getToken().getLine(), getToken().getText().trim()); // NOI18N
                } else {
                    APTUtils.LOG.log(Level.WARNING, "{0}, line {1}: warning: extra tokens at end of {2} directive", // NOI18N
                            new Object[] {APTTraceUtils.toFileString(curFile), getToken().getLine(), getToken().getText().trim()} ); // NOI18N
                }
            } else {
                this.macroName = token;
            }
        }
        // eat all till END_PREPROC_DIRECTIVE
        return !APTUtils.isEndDirectiveToken(token.getType());
    }

    @Override
    public String getText() {
        assert (getToken() != null) : "must have valid preproc directive"; // NOI18N
        assert (getName() != null) : "must have valid macro"; // NOI18N
        String retValue = super.getText();
        if (getName() != null) {
            retValue += " MACRO{" + getName() + "}"; // NOI18N
        }
        return retValue;
    }
    
    public APTToken getName() {
        return macroName;
    }
    
    private static final NotHandledMacroName EMPTY_NAME = new NotHandledMacroName();
    
    //TODO: what about Serializable
    private static final class NotHandledMacroName extends APTTokenAbstact {
        public NotHandledMacroName() {
        }
        
        @Override
        public String getText() {
            return "<<DUMMY>>"; // NOI18N
        }

        @Override
        public int hashCode() {
            return -1;
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return this == obj;
        }

    };    
}
