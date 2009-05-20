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

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.TokenStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTMacro.Kind;
import org.netbeans.modules.cnd.apt.support.APTMacroMap;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.TokenBasedTokenStream;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 *
 * @author alms
 */
public class APTPredefinedMacroMap implements APTMacroMap {
       
    private static String []preMacro = new String [] { 
         "__FILE__", "__LINE__", "__DATE__", "__TIME__", "__FUNCTION__"  // NOI18N
    };

    public APTPredefinedMacroMap() {
    }

    public APTMacroMap.State getState() {
        return null;
    }

    public boolean isDefined(APTToken token) {
        return isDefined(token.getTextID());
    }
    
    public boolean isDefined(CharSequence token) {
        int i;
        
        if (token.length() < 2 || token.charAt(0) != '_' || token.charAt(1) != '_') {
            return false;
        }
        String tokenText = token.toString();
                    
        for (i = 0; i < preMacro.length; i++) {
            if(preMacro[i].equals(tokenText)) {
                return true;
            }                
        }        
        return false;
    }

    public APTMacro getMacro(APTToken token) {
        if (isDefined(token.getTextID())) {
            return new APTPredefinedMacroImpl(token);
        }
        return null;
    }
    

    public void setState(APTMacroMap.State state) {
        APTUtils.LOG.log(Level.SEVERE, "setState is not supported", new IllegalAccessException()); // NOI18N
    }

    public void define(APTFile file, APTToken name, Collection<APTToken> params, List<APTToken> value, Kind macroType) {
        APTUtils.LOG.log(Level.SEVERE, "define is not supported", new IllegalAccessException()); // NOI18N
    }

    public void undef(APTFile file, APTToken name) {
        APTUtils.LOG.log(Level.SEVERE, "undef is not supported", new IllegalAccessException()); // NOI18N
    }
   
    public boolean pushExpanding(APTToken token) {
        APTUtils.LOG.log(Level.SEVERE, "pushExpanding is not supported", new IllegalAccessException()); // NOI18N
        return false;
    }

    public void popExpanding() {
        APTUtils.LOG.log(Level.SEVERE, "popExpanding is not supported", new IllegalAccessException()); // NOI18N
    }

    public boolean isExpanding(APTToken token) {
        APTUtils.LOG.log(Level.SEVERE, "isExpanding is not supported", new IllegalAccessException()); // NOI18N
        return false;
    }     
    
    private static final class APTPredefinedMacroImpl implements APTMacro {
        private APTToken macro;
        
        public APTPredefinedMacroImpl(APTToken macro) {
            this.macro =  macro;           
        }

        public CharSequence getFile() {
            return CharSequenceKey.empty();
        }

        public Kind getKind() {
            return Kind.POSITION_PREDEFINED;
        }

        public boolean isFunctionLike() {
            return false;
        }

        public APTToken getName() {
            return macro;
        }

        public Collection<APTToken> getParams() {
            return Collections.<APTToken>emptyList();
        }

        public TokenStream getBody() {
            APTToken tok = APTUtils.createAPTToken(macro, APTTokenTypes.STRING_LITERAL);
            
            if (!"__LINE__".contentEquals(macro.getTextID())) { // NOI18N
                tok.setType(APTTokenTypes.STRING_LITERAL);
            } else {
                tok.setType(APTTokenTypes.DECIMALINT);
                tok.setText("" + macro.getLine()); // NOI18N
            }
                        
            return new TokenBasedTokenStream(tok);
        }    
        
        @Override
        public String toString() {
            StringBuilder retValue = new StringBuilder();
            retValue.append("<P>"); // NOI18N     
            retValue.append(getName());                       
            return retValue.toString(); 
        }
    }
    
}
