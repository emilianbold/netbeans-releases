/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.editor.ext.html.parser;

import java.util.List;
import org.netbeans.editor.ext.html.dtd.DTD;

/**
 * Html parser result.
 * 
 * @author mfukala@netbeans.org
 */
public class SyntaxParserResult {

    private static final String FALLBACK_DOCTYPE =
            "-//W3C//DTD HTML 4.01 Transitional//EN";  // NOI18N
    
    private CharSequence source;
    private List<SyntaxElement> elements;
    private String publicID;
    private AstNode astRoot;

    public SyntaxParserResult(CharSequence source, List<SyntaxElement> elements) {
        this.source = source;
        this.elements = elements;
    }

    public CharSequence getSource() {
        return source;
    }

    public List<SyntaxElement> getElements() {
        return elements;
    }

    public synchronized AstNode getASTRoot() {
        if(this.astRoot == null) {
            this.astRoot = SyntaxTree.makeTree(getElements(), getDTD());
        }
        return astRoot;
    }

    public synchronized String getPublicID() {
        if (this.publicID == null) {
            for (SyntaxElement e : elements) {
                if (e.type() == SyntaxElement.TYPE_DECLARATION) {
                    String _publicID = ((SyntaxElement.Declaration) e).getPublicIdentifier();
                    if (_publicID != null) {
                        this.publicID = _publicID;
                        break;
                    }
                }
            }
        }
        return this.publicID;
    }

    public DTD getDTD() {
        if(getPublicID() == null) {
            return org.netbeans.editor.ext.html.dtd.Registry.getDTD(FALLBACK_DOCTYPE, null);
        } else {
            return org.netbeans.editor.ext.html.dtd.Registry.getDTD(getPublicID(), null);
        }
    }
    
}