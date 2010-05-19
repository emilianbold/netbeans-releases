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
import org.netbeans.modules.wsdlextensions.ldap.impl.ResultSetAttribute;
import org.netbeans.modules.wsdlextensions.ldap.impl.SearchFilterAttribute;
import org.netbeans.modules.wsdlextensions.ldap.impl.UpdateSetAttribute;

/**
 *
 * @author tianlize
 */
public class DisplayFormatControl {

    @SuppressWarnings("unchecked")
    public static List filterToAttr(List list) {
        List ret = new ArrayList();
        if (list.size() < 1) {
            return null;
        }
        Iterator it = list.iterator();
        while (it.hasNext()) {
            SearchFilterAttribute att = (SearchFilterAttribute) it.next();
            ret.add(att.getAttributeName());
            att = null;
        }
        return ret;
    }

    public static SearchFilterAttribute attrToFilter(String attrName, String logicOp, String objName, int posIndex) {
        SearchFilterAttribute ret = new SearchFilterAttribute();
        ret.setAttributeName(attrName);
        ret.setLogicOp(logicOp);
        ret.setCompareOp("=");
        ret.setObjName(objName);
        ret.setPositionIndex(posIndex);
        return ret;
    }

    public static String filterAttrToJattr(SearchFilterAttribute attr) {
        String ret = "";
        if (null == attr) {
            return null;
        }
        if (attr.getPositionIndex() == 0) {
            ret += "・";
        } else {
            ret += attr.getLogicOp();
        }
        if (attr.isBeginBracket()) {
            for (int i = 0; i < attr.getBracketBeginDepth(); i++) {
                ret += "(";
            }
        }
        ret += " " + attr.getObjName() + ".";
        ret += attr.getAttributeName() + " ";
        ret += attr.getCompareOp();
        if (attr.isEndBracket()) {
            for (int j = 0; j < attr.getBracketEndDepth(); j++) {
                ret += ")";
            }
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public static List filterListToJList(List list) {
        List ret = new ArrayList();
        if (null != list & list.size() > 0) {
            List mList = sortByPosIndex(list);
            for (int i = 0; i < mList.size(); i++) {
                SearchFilterAttribute attr = (SearchFilterAttribute) mList.get(i);
                ret.add(filterAttrToJattr(attr));
            }
        }
        return ret;
    }

    public static List sortByPosIndex(List list) {
        if (null != list & list.size() > 1) {
            for (int i = list.size() - 1; i > 0; i--) {
                SearchFilterAttribute sfaI = (SearchFilterAttribute) list.get(i);
                for (int j = 0; j < i; j++) {
                    SearchFilterAttribute sfaJ = (SearchFilterAttribute) list.get(j);
                    if (sfaI.getPositionIndex() > sfaJ.getPositionIndex()) {
                        SearchFilterAttribute sfa = sfaI;
                        sfaI = sfaJ;
                        sfaJ = sfa;
                        sfa = null;
                    }
                }
            }
        }
        return list;
    }

    public static String toAttribute(String filter) {
        String[] strArr = filter.split(" ");
        if (strArr.length < 3) {
            return null;
        }
        return strArr[1];
    }

    @SuppressWarnings("unchecked")
    public static List setToAttr(List list) {
        if (list == null) {
            return null;
        }
        List ret = new ArrayList();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            ResultSetAttribute attr = (ResultSetAttribute) it.next();
            ret.add(attr.getAttributeName());
            attr = null;
        }
        return ret;
    }

    /**
     * 
     * @param objName
     * @param attrName
     * @return list of ResultSetAttribute
     */
    @SuppressWarnings("unchecked")
    public static List attrToSet(String objName, List attrName) {
        List ret = new ArrayList();
        if (attrName == null) {
            return null;
        }
        Iterator it = attrName.iterator();
        while (it.hasNext()) {
            ResultSetAttribute attr = new ResultSetAttribute(objName, (String) it.next());
            ret.add(attr);
        }
        return ret;
    }

    /**
     * @param list
     * @return list of String to display in selectedList;
     */
    @SuppressWarnings("unchecked")
    public static List setToJlist(List list) {
        List ret = new ArrayList();
        if (list == null) {
            return null;
        }
        Iterator it = list.iterator();
        while (it.hasNext()) {
            ResultSetAttribute attr = (ResultSetAttribute) it.next();
            String selectedAttr = attr.getObjName() + "." + attr.getAttributeName();
            ret.add(selectedAttr);
        }
        return ret;
    }

    /**
     * @param list
     * @return list of attr
     */
    @SuppressWarnings("unchecked")
    public static List jListToAttr(List list) {
        List ret = new ArrayList();
        if (list == null) {
            return null;
        }
        Iterator it = list.iterator();
        while (it.hasNext()) {
            String selectedAttr = (String) it.next();
            int index = selectedAttr.indexOf(".");
            ResultSetAttribute attr = new ResultSetAttribute(selectedAttr.substring(0, index), selectedAttr.substring(index + 1));
            ret.add(attr);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public static List attrToUpdateSetAttr(String objName, List attrNames) {
        List ret = new ArrayList();
        if (attrNames == null) {
            return null;
        }
        Iterator it = attrNames.iterator();
        while (it.hasNext()) {
            UpdateSetAttribute usAttr = new UpdateSetAttribute(objName, (String) it.next(), "Replace");
            ret.add(usAttr);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public static List updateSetToJList(List updateSetAttrs) {
        List ret = new ArrayList();
        if (updateSetAttrs == null) {
            return null;
        }
        Iterator it = updateSetAttrs.iterator();
        while (it.hasNext()) {
            UpdateSetAttribute usAttr = (UpdateSetAttribute) it.next();
            String selectedAttr = usAttr.getOpType() + " " + usAttr.getObjName() + "." + usAttr.getAttrName();
            ret.add(selectedAttr);
        }
        return ret;
    }

    /**
     * @param list
     * @return list of UpdateSetAttribute
     */
    @SuppressWarnings("unchecked")
    public static List jListToUpdateSetAttr(List list) {
        List ret = new ArrayList();
        if (list == null) {
            return null;
        }
        Iterator it = list.iterator();
        while (it.hasNext()) {
            String selectedAttr = (String) it.next();
            int index = selectedAttr.indexOf(" ");
            String opType = selectedAttr.substring(0, index);
            String jListAttr = selectedAttr.substring(index + 1);
            int index2 = jListAttr.indexOf(".");
            UpdateSetAttribute attr = new UpdateSetAttribute(jListAttr.substring(0, index2), jListAttr.substring(index2 + 1), opType);
            ret.add(attr);
        }
        return ret;
    }

    /**
     * 
     * @param list
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List updateSetAttrToAttr(List list) {
        if (list == null) {
            return null;
        }
        List ret = new ArrayList();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            UpdateSetAttribute attr = (UpdateSetAttribute) it.next();
            ret.add(attr.getAttrName());
            attr = null;
        }
        return ret;
    }

    public static String jListUpdateSettoAttr(String str) {
        String[] strArr = str.split(" ");
        if (strArr.length != 2) {
            return null;
        }
        return strArr[1];
    }
    
    @SuppressWarnings("unchecked")
    public static List attrToJListAddSeletectAttr(String objName, List list){
        if (list == null) {
            return null;
        }
        List ret=new ArrayList();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            String attr=(String)it.next();
            ret.add(objName+"."+attr);
        }
        return ret;
    }
}
