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
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.cnd.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class PreprocLexer extends CndLexer {
    private static final int INIT               = 0;
    private static final int DIRECTIVE_NAME     = INIT + 1;
    private static final int MACRO_NAME         = DIRECTIVE_NAME + 1;
    private static final int MACRO_PARAMS       = MACRO_NAME + 1;
    private static final int MACRO_BODY         = MACRO_PARAMS + 1;
    private static final int EXPRESSION         = MACRO_BODY + 1;
    
    private int state = INIT;

    public PreprocLexer(LexerRestartInfo<CppTokenId> info) {
        super(info);
        fromState(info.state()); // last line in contstructor
    }

    @Override
    public Object state() {
        return Integer.valueOf(state);
    }
    
    private void fromState(Object state) {
        state = state == null ? INIT : ((Integer)state).intValue();
    }

    @Override
    protected Token<CppTokenId> handleSharp() {
        if (state == INIT) {
            return token(CppTokenId.PREPROCESSOR_START);
        }
        return null;
    }
}
