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

import java.util.Hashtable;
import java.util.Enumeration;

class Rule {
    protected String name;
    protected String block;
    protected String args;
    protected String returnValue;
    protected String throwsSpec;
    protected String initAction;
    protected IndexedVector options;
    protected String visibility;
    protected Grammar enclosingGrammar;
    protected boolean bang = false;

    public Rule(String n, String b, IndexedVector options, Grammar gr) {
        name = n;
        block = b;
        this.options = options;
        setEnclosingGrammar(gr);
    }

    public String getArgs() {
        return args;
    }

    public boolean getBang() {
        return bang;
    }

    public String getName() {
        return name;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public String getVisibility() {
        return visibility;
    }

    /** If 'rule' narrows the visible of 'this', return true;
     *  For example, 'this' is public and 'rule' is private,
     *  true is returned.  You cannot narrow the vis. of
     *  a rule.
     */
    public boolean narrowerVisibility(Rule rule) {
        if (visibility.equals("public")) {
            if (!rule.equals("public")) {
                return true;	// everything narrower than public
            }
            return false;
        }
        else if (visibility.equals("protected")) {
            if (rule.equals("private")) {
                return true;	// private narrower than protected
            }
            return false;
        }
        else if (visibility.equals("private")) {
            return false;	// nothing is narrower than private
        }
        return false;
    }

    /** Two rules have the same signature if they have:
     *  	same name
     *		same return value
     *		same args
     *	I do a simple string compare now, but later
     *	the type could be pulled out so it is insensitive
     *	to names of args etc...
     */
    public boolean sameSignature(Rule rule) {
        boolean nSame = true;
        boolean aSame = true;
        boolean rSame = true;

        nSame = name.equals(rule.getName());
        if (args != null) {
            aSame = args.equals(rule.getArgs());
        }
        if (returnValue != null) {
            rSame = returnValue.equals(rule.getReturnValue());
        }
        return nSame && aSame && rSame;
    }

    public void setArgs(String a) {
        args = a;
    }

    public void setBang() {
        bang = true;
    }

    public void setEnclosingGrammar(Grammar g) {
        enclosingGrammar = g;
    }

    public void setInitAction(String a) {
        initAction = a;
    }

    public void setOptions(IndexedVector options) {
        this.options = options;
    }

    public void setReturnValue(String ret) {
        returnValue = ret;
    }

    public void setThrowsSpec(String t) {
        throwsSpec = t;
    }

    public void setVisibility(String v) {
        visibility = v;
    }

    public String toString() {
        String s = "";
        String retString = returnValue == null ? "" : "returns " + returnValue;
        String argString = args == null ? "" : args;
        String bang = getBang() ? "!" : "";

        s += visibility == null ? "" : visibility + " ";
        s += name + bang + argString + " " + retString + throwsSpec;
        if (options != null) {
            s += System.getProperty("line.separator") +
                "options {" +
                System.getProperty("line.separator");
            for (Enumeration e = options.elements(); e.hasMoreElements();) {
                s += (Option)e.nextElement() + System.getProperty("line.separator");
            }
            s += "}" + System.getProperty("line.separator");
        }
        if (initAction != null) {
            s += initAction + System.getProperty("line.separator");
        }
        s += block;
        return s;
    }
}
