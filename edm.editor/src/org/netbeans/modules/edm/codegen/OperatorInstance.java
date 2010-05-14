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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.edm.codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.netbeans.modules.edm.editor.utils.TagParserUtility;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLOperatorArg;
import org.netbeans.modules.edm.model.SQLOperatorDefinition;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.Attribute;
import org.netbeans.modules.edm.editor.utils.SQLUtils;
import org.netbeans.modules.edm.editor.utils.StringUtil;
import org.openide.util.NbBundle;

/**
 *
 * Holds an Operator Instance:
 * Modified from a static inner class to a public class as the usage of this class
 * has changed. Its no longer a holder for the operator definition templates but also
 * to represent the custom operator instances.
 *
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 */
public class OperatorInstance implements SQLOperatorDefinition {

    private List<SQLOperatorArg> argList;
    private List<String> outputTypeList;
    private String operatorName;
    private String script;
    private String dbSpecficName;
    private String guiName;
    private String operatorCategoryType;
    private List arg2UseList;
    private Map attributes = new HashMap();
    private static final String ATTR_NAME = "name";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_ARG2USE = "arg2Use";
    private static final String TAG_OUTPUT = "output";
    private static final String ATTR_SCRIPT = "script";
    private static final String ATTR_DBSPECFICNAME = "dbspecificname";
    private static final String ATTR_GUINAME = "guiname";
    private static final String TAG_ARG = "arg";
    private static final String TAG_OPERATOR = "operator";
    private static final String ATTR_RANGE = "range";

    public OperatorInstance() {
        argList = new ArrayList<SQLOperatorArg>();
        outputTypeList = new ArrayList<String>();
    }

    public OperatorInstance(String newName) {
        this();
        operatorName = newName;
    }

    public Object getAttributeValue(String attrName) {
        Attribute attr = getAttribute(attrName);
        return (attr != null) ? attr.getAttributeValue() : null;
    }

    public Attribute getAttribute(String attrName) {
        return (Attribute) attributes.get(attrName);
    }

    public int getArgCount() {
        if (script == null) {
            return 0;
        } else if (script.indexOf("[") == -1) {
            return argList.size();
        } else {
            return SQLConstants.MAX_SCRIPT_ARGUMENT_COUNT;
        }
    }

    public int getArgCountType() {
        if (this.script.indexOf("[") == -1) {
            return SQLConstants.OPERATOR_ARGS_FIXED;
        }
        return SQLConstants.OPERATOR_ARGS_VARIABLE;
    }

    public String getVarOperatorArgName() {
        SQLOperatorArg arg = argList.get(0);
        return arg.getArgName();
    }

    public int getArgIndex(String argName) {
        if (argList == null || argName == null) {
            return -1;
        }

        ListIterator iter = argList.listIterator();
        while (iter.hasNext()) {
            SQLOperatorArg arg = (SQLOperatorArg) iter.next();
            if (argName.equals(arg.getArgName())) {
                return iter.previousIndex();
            }
        }

        return -1;
    }

    public List getArgList() {
        return argList;
    }

    public void setArgList(List<SQLOperatorArg> args) {
        this.argList = args;
    }

    public String getArgType(int i) {
        SQLOperatorArg operatorArg = argList.get(i);
        return SQLUtils.getStdSqlType(operatorArg.getJdbcType());
    }

    public int getArgJdbcSQLType(int i) {
        if (i < argList.size()) {
            SQLOperatorArg operatorArg = argList.get(i);
            return operatorArg.getJdbcType();
        }
        return SQLConstants.JDBCSQL_TYPE_UNDEFINED;
    }

    public int getArgJdbcSQLType(String argName) {
        if (argName != null) {
            Iterator it = argList.iterator();

            while (it.hasNext()) {
                SQLOperatorArg operatorArg = (SQLOperatorArg) it.next();
                if (operatorArg.getArgName().equals(argName)) {
                    return operatorArg.getJdbcType();
                }
            }
        }
        return SQLConstants.JDBCSQL_TYPE_UNDEFINED;
    }

    public String getRange(String argName) {
        if (argName != null) {
            Iterator it = argList.iterator();

            while (it.hasNext()) {
                SQLOperatorArg operatorArg = (SQLOperatorArg) it.next();
                if (operatorArg.getArgName().equals(argName)) {
                    return operatorArg.getRange();
                }
            }
        }
        return null;
    }

    public String getOperatorName() {
        return this.operatorName;
    }

    public void setOperatorName(String anOperatorName) {
        this.operatorName = anOperatorName;
    }

    public String getDbSpecficName() {
        return this.dbSpecficName;
    }

    public String getGuiName() {
        String gName = this.guiName;
        if (gName == null || gName.trim().equals("")) {
            return getScript();
        }

        return gName;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String aScript) {
        this.script = aScript;
    }

    public boolean addArg(SQLOperatorArg operatorArg) {
        if (operatorArg.getArgName() != null && SQLUtils.getStdSqlType(operatorArg.getJdbcType()) != null) {
            argList.add(operatorArg);
            return true;
        }
        return false;
    }

    public SQLOperatorArg getOperatorArg(int index) {
        return this.argList.get(index);
    }

    public boolean addOutputType(String outputType) {
        if (outputType != null) {
            outputTypeList.add(outputType);
            return true;
        }
        return false;
    }

    public int getOutputJdbcSQLType() {
        if (outputTypeList.size() == 1) {
            return SQLUtils.getStdJdbcType(outputTypeList.get(0));
        }
        return SQLConstants.JDBCSQL_TYPE_UNDEFINED;
    }

    public void setOutputJdbcSQLType(int i) {
        outputTypeList.add(SQLUtils.getStdSqlType(i));
    }

    public void setOutputJdbcSQLType(String jdbcType) {
        outputTypeList.add(jdbcType);
    }

    public void parseXML(Element defnElement) throws EDMException {
        if (defnElement == null || !TAG_OPERATOR.equals(defnElement.getNodeName())) {
            throw new EDMException(NbBundle.getMessage(OperatorInstance.class, "MSG_null_ElementType") + TAG_OPERATOR + ".");
        }

        NodeList argNodeList;
        NodeList outputTypeNodeList;
        NodeList attributeList;

        operatorName = defnElement.getAttribute(ATTR_NAME);
        dbSpecficName = defnElement.getAttribute(ATTR_DBSPECFICNAME);
        guiName = defnElement.getAttribute(ATTR_GUINAME);

        script = defnElement.getAttribute(ATTR_SCRIPT);
        operatorCategoryType = defnElement.getAttribute(ATTR_TYPE);
        String arg2Use = defnElement.getAttribute(ATTR_ARG2USE);
        arg2UseList = StringUtil.createStringListFrom(arg2Use);

        attributeList = defnElement.getElementsByTagName(Attribute.TAG_ATTR);
        TagParserUtility.parseAttributeList(attributes, attributeList);

        argNodeList = defnElement.getElementsByTagName(TAG_ARG);
        for (int j = 0; j < argNodeList.getLength(); j++) {
            Element argElement;
            String arg;
            String argType;
            argElement = (Element) argNodeList.item(j);
            arg = argElement.getAttribute(ATTR_NAME);
            argType = argElement.getAttribute(ATTR_TYPE);

            String range = argElement.getAttribute(ATTR_RANGE);
            SQLOperatorArg operatorArg = new SQLOperatorArg(arg, SQLUtils.getStdJdbcType(argType));
            addArg(operatorArg);

            if (!StringUtil.isNullString(range)) {
                operatorArg.setRange(range);
            }
        }
        outputTypeNodeList = defnElement.getElementsByTagName(TAG_OUTPUT);

        for (int k = 0; k < outputTypeNodeList.getLength(); k++) {
            String type;
            Element outputElement;

            outputElement = (Element) outputTypeNodeList.item(k);
            type = outputElement.getAttribute(ATTR_TYPE);
            addOutputType(type);
        }
    }

    public List getArg2Use() {
        return this.arg2UseList;
    }

    public String getOperatorCategoryType() {
        return this.operatorCategoryType;
    }

    public Object clone(List operatorArgs) {
        try {
            return this.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}