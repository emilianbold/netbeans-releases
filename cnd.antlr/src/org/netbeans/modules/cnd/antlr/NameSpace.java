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
package org.netbeans.modules.cnd.antlr;

/**
 * ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 *
 * Container for a C++ namespace specification.  Namespaces can be
 * nested, so this contains a vector of all the nested names.
 *
 * @author David Wagner (JPL/Caltech) 8-12-00
 */

import java.util.Vector;
import java.util.Enumeration;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class NameSpace {
    private Vector names = new Vector();
    private String _name;

    public NameSpace(String name) {
    	  _name = new String(name);
        parse(name);
    }

	 public String getName()
	 {
	 	return _name;
	 }
	
    /**
     * Parse a C++ namespace declaration into seperate names
     * splitting on ::  We could easily parameterize this to make
     * the delimiter a language-specific parameter, or use subclasses
     * to support C++ namespaces versus java packages. -DAW
     */
    protected void parse(String name) {
        StringTokenizer tok = new StringTokenizer(name, "::");
        while (tok.hasMoreTokens())
            names.addElement(tok.nextToken());
    }

    /**
     * Method to generate the required C++ namespace declarations
     */
    void emitDeclarations(PrintWriter out) {
        for (Enumeration n = names.elements(); n.hasMoreElements();) {
            String s = (String)n.nextElement();
            out.println("ANTLR_BEGIN_NAMESPACE(" + s + ")");
        }
    }

    /**
     * Method to generate the required C++ namespace closures
     */
    void emitClosures(PrintWriter out) {
        for (int i = 0; i < names.size(); ++i)
            out.println("ANTLR_END_NAMESPACE");
    }
}
