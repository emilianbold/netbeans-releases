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
package org.netbeans.modules.wsdlextensions.ldap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.wsdlextensions.ldap.impl.SearchFilterAttribute;
import org.netbeans.modules.wsdlextensions.ldap.ldif.LdifObjectClass;

/**
 *
 * @author tianlize
 */
public class GenerateSearchFilter {

    private Map mSelecteObject;

    public GenerateSearchFilter() {

    }

    public GenerateSearchFilter(Map obj) {
        mSelecteObject = obj;
    }

    @SuppressWarnings("unchecked")
    private List getAttrsSorted() {
        List ret = new ArrayList();
        List allAttrs = getAllAttrs();
        int attrSize = allAttrs.size();
        Iterator it = getAllAttrs().iterator();
        for (int i = 0; i < attrSize; i++) {
            ret.add(i, getAttrByPosIndex(i, allAttrs));
        }
        return ret;
    }

    private SearchFilterAttribute getAttrByPosIndex(int i, List list) {
        SearchFilterAttribute ret = null;
        Iterator it = list.iterator();
        while (it.hasNext()) {
            SearchFilterAttribute sfa = (SearchFilterAttribute) it.next();
            if (sfa.getPositionIndex() == i) {
                SearchFilterAttribute sfa2 = new SearchFilterAttribute();
                sfa2.setAttributeName(sfa.getAttributeName());
                sfa2.setBracketDepth(sfa.getBracketDepth());
                sfa2.setBracketBeginDepth(sfa.getBracketBeginDepth());
                sfa2.setBracketEndDepth(sfa.getBracketEndDepth());
                sfa2.setCompareOp(sfa.getCompareOp());
                sfa2.setLogicOp(sfa.getLogicOp());
                sfa2.setObjName(sfa.getObjName());
                sfa2.setPositionIndex(sfa.getPositionIndex());
                ret = sfa2;
                break;
            }
            sfa = null;
        }
        it = null;
        return ret;
    }

    @SuppressWarnings("unchecked")
    private List getAllAttrs() {
        List ret = new ArrayList();
        Iterator it = mSelecteObject.values().iterator();
        while (it.hasNext()) {
            LdifObjectClass loc = (LdifObjectClass) it.next();
            Iterator it2 = loc.getSelected().iterator();
            while (it2.hasNext()) {
                ret.add(it2.next());
            }
        }
        return ret;
    }

    private String singleAttrFilter(SearchFilterAttribute sfa) {
        String ret = "(";
        ret += sfa.getAttributeName();
        ret += sfa.getCompareOp();
//        ret+=sfa.value();
        ret += ")";
        return ret;
    }

    private String doubleAttrFilter(SearchFilterAttribute sfa1, SearchFilterAttribute sfa2) {
        String ret = "(";
        if (sfa1 != null & sfa2 != null) {
            ret += sfa2.getLogicOp();
            ret += singleAttrFilter(sfa1);
            ret += singleAttrFilter(sfa2);
        }
        ret += ")";
        return ret;
    }

    private String getFilter(List list, int beginIndex, int endIndex) {
        if (beginIndex < 0 | endIndex < beginIndex | endIndex < 0) {
            return "";
        }
        String ret = "(";
        SearchFilterAttribute sfa = (SearchFilterAttribute) list.get(endIndex);
        if (beginIndex == endIndex) {
            return singleAttrFilter(sfa);
        }
        if (beginIndex + 1 == endIndex) {
            SearchFilterAttribute sfa2 = (SearchFilterAttribute) list.get(beginIndex);
            return doubleAttrFilter(sfa2, sfa);
        }

        int j = findOrIndex(list, beginIndex, endIndex);
        if (j > beginIndex) {
            ret += getLoginOp(list, j) + getFilter(list, beginIndex, j - 1) + getFilter(list, j, endIndex);
        } else {
            if (!sfa.isEndBracket()) {
                ret += sfa.getLogicOp() + getFilter(list, beginIndex, endIndex - 1) + singleAttrFilter(sfa);
            } else {
                int i = getBracketBeginIndex(list, sfa);
                removeBracket(list, i, endIndex);
                if (i > beginIndex) {
                    ret += getLoginOp(list, i) + getFilter(list, beginIndex, i - 1) + getFilter(list, i, endIndex);
                } else {
                    ret += getFilter(list, i, endIndex);
                }
            }
        }
        ret += ")";
        list = null;
        return ret;
    }

    private int findOrIndex(List list, int begin, int end) {
        int ret = -1;
        for (int i = end; i >= begin; i--) {
            SearchFilterAttribute sfa = (SearchFilterAttribute) list.get(i);
            if (sfa.getLogicOp().equals("Or") & (sfa.getBracketDepth() == 0 | sfa.getBracketDepth() - sfa.getBracketBeginDepth() == 0)) {
                ret = i;
                break;
            }
        }
        return ret;
    }

    private int getBracketDepth(List list, int index) {
        int ret = -1;
        SearchFilterAttribute sfa = (SearchFilterAttribute) list.get(index);
        ret = sfa.getBracketDepth();
        sfa = null;
        return ret;
    }

    private void removeBracket(List list, int beginIndex, int endIndex) {
        SearchFilterAttribute sfaBegin = (SearchFilterAttribute) list.get(beginIndex);
        SearchFilterAttribute sfaEnd = (SearchFilterAttribute) list.get(endIndex);
        sfaBegin.reduceBracketBeginDepth();
        sfaEnd.reduceBracketEndDepth();
        for (int i = beginIndex; i <= endIndex; i++) {
            SearchFilterAttribute sfa = (SearchFilterAttribute) list.get(i);
            sfa.reduceBracketDepth();
            sfa = null;
        }
        sfaBegin = null;
        sfaEnd = null;
    }

    private int getBracketBeginIndex(List list, SearchFilterAttribute sfa) {
        int ret = sfa.getPositionIndex();
        int bracketDepth = sfa.getBracketDepth();
        int bracketEndDepth = sfa.getBracketEndDepth();
        for (int i = ret - 1; i >= 0; i--) {
            SearchFilterAttribute sfa2 = (SearchFilterAttribute) list.get(i);
            if (!sfa2.isBeginBracket()) {
                bracketEndDepth += sfa2.getBracketEndDepth();
                continue;
            }
            if (bracketEndDepth - sfa2.getBracketBeginDepth() <= 0) {
                ret = sfa2.getPositionIndex();
                break;
            }
            bracketEndDepth -= sfa2.getBracketBeginDepth();
            sfa2 = null;
        }
        return ret;
    }

    private String getLoginOp(List list, int index) {
        if (index < 0) {
            return "";
        }
        SearchFilterAttribute sfa = (SearchFilterAttribute) list.get(index);
        return sfa.getLogicOp();
    }

    public String generateFilter() {
        String ret = "\n";
        List list = getAttrsSorted();
        for (int i = 0; i < list.size(); i++) {
            SearchFilterAttribute sfa = (SearchFilterAttribute) list.get(i);
            ret += sfa.getLogicOp() + " ";
            if (sfa.isBeginBracket()) {
                for (int j = 0; j < sfa.getBracketBeginDepth(); j++) {
                    ret += "(";
                }
            }
            ret += sfa.getAttributeName() + " ";
            ret += sfa.getCompareOp() + " ";
            if (sfa.isEndBracket()) {
                for (int k = 0; k < sfa.getBracketEndDepth(); k++) {
                    ret += ")";
                }
            }
        }
        ret += "\n" + getFilter(list, 0, list.size() - 1) + "\n";
        return ret;
    }
}
