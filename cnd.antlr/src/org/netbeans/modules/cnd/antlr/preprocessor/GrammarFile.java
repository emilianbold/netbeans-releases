/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr.preprocessor;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import org.netbeans.modules.cnd.antlr.collections.impl.IndexedVector;

import java.util.Enumeration;
import java.io.*;

/** Stores header action, grammar preamble, file options, and
 *  list of grammars in the file
 */
public class GrammarFile {
    protected String fileName;
    protected String headerAction = "";
    protected IndexedVector options;
    protected IndexedVector grammars;
    protected boolean expanded = false;	// any grammars expanded within?
	protected org.netbeans.modules.cnd.antlr.Tool tool;

    public GrammarFile(org.netbeans.modules.cnd.antlr.Tool tool, String f) {
        fileName = f;
        grammars = new IndexedVector();
        this.tool = tool;
    }

    public void addGrammar(Grammar g) {
        grammars.appendElement(g.getName(), g);
    }

    public void generateExpandedFile() throws IOException {
        if (!expanded) {
            return;	// don't generate if nothing got expanded
        }
        String expandedFileName = nameForExpandedGrammarFile(this.getName());

        // create the new grammar file with expanded grammars
        PrintWriter expF = tool.openOutputFile(expandedFileName);
        expF.println(toString());
        expF.close();
    }

    public IndexedVector getGrammars() {
        return grammars;
    }

    public String getName() {
        return fileName;
    }

    public String nameForExpandedGrammarFile(String f) {
        if (expanded) {
            // strip path to original input, make expanded file in current dir
            return "expanded" + tool.fileMinusPath(f);
        }
        else {
            return f;
        }
    }

    public void setExpanded(boolean exp) {
        expanded = exp;
    }

    public void addHeaderAction(String a) {
        headerAction += a + System.getProperty("line.separator");
    }

    public void setOptions(IndexedVector o) {
        options = o;
    }

    public String toString() {
        String h = headerAction == null ? "" : headerAction;
        String o = options == null ? "" : Hierarchy.optionsToString(options);

        StringBuffer s = new StringBuffer(10000); s.append(h); s.append(o);
        for (Enumeration e = grammars.elements(); e.hasMoreElements();) {
            Grammar g = (Grammar)e.nextElement();
            s.append(g.toString());
        }
        return s.toString();
    }
}
