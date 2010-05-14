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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.ldap.impl;

/**
 *
 * @author tianlize
 */
public class BracketObjecte implements Cloneable {

    private int bracketDepth;
    private int bracketBeginDepth;
    private int bracketEndDepth;

    public BracketObjecte() {
        bracketDepth = 0;
        bracketBeginDepth = 0;
        bracketEndDepth = 0;
    }

    public void increaseBracketDepth() {
        bracketDepth++;
    }

    public void reduceBracketDepth() {
        if (bracketDepth > 0) {
            bracketDepth--;
        }
    }

    public void increaseBracketBeginDepth() {
        if (bracketEndDepth == 0) {
            bracketBeginDepth++;
        }
    }

    public void reduceBracketBeginDepth() {
        if (bracketBeginDepth > 0) {
            bracketBeginDepth--;
        }
    }

    public void increaseBracketEndDepth() {
        if (bracketBeginDepth == 0) {
            bracketEndDepth++;
        }
    }

    public void reduceBracketEndDepth() {
        if (bracketEndDepth > 0) {
            bracketEndDepth--;
        }
    }

    public boolean canBeginBracket() {
        return bracketEndDepth == 0;
    }

    public boolean isBeginBracket() {
        return bracketBeginDepth > 0;
    }

    public boolean canEndBracket() {
        return bracketBeginDepth == 0;
    }

    public boolean isEndBracket() {
        return bracketEndDepth > 0;
    }

    public int getBracketBeginDepth() {
        return bracketBeginDepth;
    }

    public void setBracketBeginDepth(int bracketBeginDepth) {
        this.bracketBeginDepth = bracketBeginDepth;
    }

    public int getBracketDepth() {
        return bracketDepth;
    }

    public void setBracketDepth(int bracketDepth) {
        this.bracketDepth = bracketDepth;
    }

    public int getBracketEndDepth() {
        return bracketEndDepth;
    }

    public void setBracketEndDepth(int bracketEndDepth) {
        this.bracketEndDepth = bracketEndDepth;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        BracketObjecte obj = (BracketObjecte) super.clone();
        obj.setBracketBeginDepth(bracketBeginDepth);
        obj.setBracketDepth(bracketDepth);
        obj.setBracketEndDepth(bracketEndDepth);
        return obj;
    }
}
