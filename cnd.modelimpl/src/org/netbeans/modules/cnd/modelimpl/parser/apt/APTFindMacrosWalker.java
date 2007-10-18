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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.parser.apt;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.openide.util.Exceptions;

/**
 * Basic walker to find macroes for semantic highlighting
 * TODO: maybe it should be one walker for any semantic HL activity, because
 * they used altogether.
 *
 * @author Sergey Grinev
 */
public class APTFindMacrosWalker extends APTSelfWalker {

    public APTFindMacrosWalker(APTFile apt, CsmFile csmFile, APTPreprocHandler preprocHandler) {
        super(apt, csmFile, preprocHandler);
    }

    @Override
    public TokenStream getTokenStream() {
        TokenStream ts = super.getTokenStream();
        try {
            for (Token token = ts.nextToken(); !APTUtils.isEOF(token); token = ts.nextToken()) {
                APTMacro m = getMacroMap().getMacro(token);
                if (m != null) {
                    //System.err.println("gotcha: " + m);
                    APTToken apttoken = (APTToken) token;
                    addBlock(apttoken.getOffset(), apttoken.getEndOffset());
                }
            }
        } catch (TokenStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return ts;
    }
}