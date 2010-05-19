/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.ldap.impl;

/**
 *
 * @author tianlize
 */
public class SearchFilterAttribute implements Cloneable {

    private int positionIndex;
    private String objName;
    private String logicOp;
    private String attributeName;
    private String compareOp;
//    private int bracketDepth;
    private BracketObjecte bracket = new BracketObjecte();

    public SearchFilterAttribute() {
    }

    public BracketObjecte getBracket() {
        return bracket;
    }

    public void setBracket(BracketObjecte bracket) {
        this.bracket = bracket;
    }

    public boolean isBeginBracket() {
        return bracket.isBeginBracket();
    }

    public boolean isEndBracket() {
        return bracket.isEndBracket();
    }

    public void increaseBracketDepth() {
        this.bracket.increaseBracketDepth();
    }

    public void reduceBracketDepth() {
        this.bracket.reduceBracketDepth();
    }

    public void increaseBracketBeginDepth() {
        this.bracket.increaseBracketBeginDepth();
    }

    public void reduceBracketBeginDepth() {
        this.bracket.reduceBracketBeginDepth();
    }

    public void increaseBracketEndDepth() {
        this.bracket.increaseBracketEndDepth();
    }

    public void reduceBracketEndDepth() {
        this.bracket.reduceBracketEndDepth();
    }

    public int getBracketBeginDepth() {
        return this.bracket.getBracketBeginDepth();
    }

    public void setBracketBeginDepth(int i) {
        this.bracket.setBracketBeginDepth(i);
    }

    public int getBracketEndDepth() {
        return this.bracket.getBracketEndDepth();
    }

    public void setBracketEndDepth(int i) {
        this.bracket.setBracketEndDepth(i);
    }

    public String getObjName() {
        return objName;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public int getBracketDepth() {
        return bracket.getBracketDepth();
    }

    public void setBracketDepth(int bracketDepth) {
        this.bracket.setBracketDepth(bracketDepth);
    }

    public String getCompareOp() {
        return compareOp;
    }

    public void setCompareOp(String compareOp) {
        this.compareOp = compareOp;
    }

    public String getLogicOp() {
        return logicOp;
    }

    public void setLogicOp(String logicOp) {
        this.logicOp = logicOp;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SearchFilterAttribute obj = (SearchFilterAttribute) super.clone();
        obj.setPositionIndex(positionIndex);
        obj.setObjName(objName);
        obj.setLogicOp(logicOp);
        obj.setAttributeName(attributeName);
        obj.setCompareOp(compareOp);
        obj.setBracket((BracketObjecte) bracket.clone());
        return obj;
    }
}
