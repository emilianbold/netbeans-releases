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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLOperatorDefinition;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.netbeans.modules.edm.model.EDMException;
import java.util.logging.Logger;
import org.netbeans.modules.edm.editor.utils.StringUtil;
import org.netbeans.modules.edm.editor.utils.SQLUtils;
import org.netbeans.modules.edm.editor.utils.XmlUtil;
import org.openide.util.NbBundle;

/**
 * Factory for creating instances of SQLOperatorDefinition.
 *
 * @author Ahimanikya Satapathy
 */
public class SQLOperatorFactory implements Serializable {

    private static final String LOG_CATEGORY = SQLOperatorFactory.class.getName();
    private static SQLOperatorFactory instance;
    private static final String BASE_OPERATOR_DEFINITION_FILE = "org/netbeans/modules/edm/codegen/config/operator-script.xml";
    private static final String TAG_OPERATOR_LIST = "operators";
    private static final String TAG_OPERATOR = "operator";
    private static final String ATTR_NAME = "name";
    private static final String TAG_CASTING_RULES = "casting-rules";
    private Map<String, OperatorInstance> operatorMap = null;
    private Map<String, CastingRule> castingRules = null;
    private SQLOperatorFactory parent;
    private static transient final Logger mLogger = Logger.getLogger(SQLOperatorFactory.class.getName());

    public static SQLOperatorFactory getDefault() {
        if (instance == null) {
            instance = new SQLOperatorFactory(BASE_OPERATOR_DEFINITION_FILE, null);
        }

        return instance;
    }

    public SQLOperatorFactory(String defFile, SQLOperatorFactory parent) {
        operatorMap = new HashMap<String, OperatorInstance>();
        castingRules = new HashMap<String, CastingRule>();

        this.parent = parent;

        try {
            parseXML(defFile);
        } catch (EDMException e) {            
            mLogger.log(Level.INFO,NbBundle.getMessage(SQLOperatorFactory.class, "LOG.INFO_getSQLOperatorFactory_ERROR")+e);

        }
    }

    public int getCastingRuleFor(int sourceType, int targetType) {
        String srcTypeStr = SQLUtils.getStdSqlType(sourceType);
        String trgTypeStr = SQLUtils.getStdSqlType(targetType);

        if (StringUtil.isNullString(srcTypeStr) || StringUtil.isNullString(trgTypeStr)) {
            return SQLConstants.TYPE_CHECK_INCOMPATIBLE;
        }
        return getCastingRuleFor(srcTypeStr, trgTypeStr);
    }

    public int getCastingRuleFor(String sourceType, String targetType) {
        if (StringUtil.isNullString(sourceType)) {
            throw new IllegalArgumentException(NbBundle.getMessage(SQLOperatorFactory.class, "MSG_empty_String_value_for_sourceType"));
        } else if (StringUtil.isNullString(targetType)) {
            throw new IllegalArgumentException(NbBundle.getMessage(SQLOperatorFactory.class, "MSG_empty_String_value_for_targetType"));
        }

        if (sourceType.trim().equalsIgnoreCase(targetType.trim())) {
            return SQLConstants.TYPE_CHECK_SAME;
        }

        CastingRule ruleSet = castingRules.get(sourceType.trim().toLowerCase());

        if (ruleSet == null && parent != null) {
            return parent.getCastingRuleFor(sourceType, targetType);
        }

        return (ruleSet != null) ? ruleSet.getRule(targetType.trim().toLowerCase()) : SQLConstants.TYPE_CHECK_INCOMPATIBLE;
    }

    public SQLOperatorDefinition getSQLOperatorDefinition(String operatorName) {
        if (StringUtil.isNullString(operatorName)) {
            throw new IllegalArgumentException(NbBundle.getMessage(SQLOperatorFactory.class, "MSG_empty_value_for_operatorType"));
        }

        SQLOperatorDefinition def = (SQLOperatorDefinition) operatorMap.get(operatorName);
        if (def == null && parent != null) {
            def = parent.getSQLOperatorDefinition(operatorName);
        }

        return def;
    }

    public SQLOperatorDefinition getDbSpecficOperatorDefinition(String dbOpName) {
        Iterator it = operatorMap.values().iterator();
        SQLOperatorDefinition opDef = null;

        while (it.hasNext()) {
            opDef = (SQLOperatorDefinition) it.next();
            if (opDef.getDbSpecficName().equals(dbOpName)) {
                return opDef;
            }
            opDef = null;
        }

        if (opDef == null && parent != null) {
            opDef = parent.getDbSpecficOperatorDefinition(dbOpName);
        }

        return opDef;
    }

    protected void parseXML(String defFile) throws EDMException {
        try {
            Element opListElement = XmlUtil.loadXMLFile(defFile);
            NodeList opDefsList = opListElement.getElementsByTagName(TAG_OPERATOR_LIST);
            if (opDefsList.getLength() != 0) {
                Element listElement = (Element) opDefsList.item(0);
                NodeList defList = listElement.getElementsByTagName(TAG_OPERATOR);
                for (int i = 0; i < defList.getLength(); i++) {
                    OperatorInstance defn = new OperatorInstance();
                    defn.parseXML((Element) defList.item(i));
                    operatorMap.put(defn.getOperatorName(), defn);
                }
            }

            NodeList castRuleNode = opListElement.getElementsByTagName(TAG_CASTING_RULES);
            if (castRuleNode.getLength() != 0) {
                Element castRuleElement = (Element) castRuleNode.item(0);
                NodeList ruleList = castRuleElement.getElementsByTagName(CastingRule.TAG_RULE_MAP);

                for (int i = 0; i < ruleList.getLength(); i++) {
                    Element entryElement = (Element) ruleList.item(i);
                    try {
                        CastingRule rule = new CastingRule();
                        rule.parseXML(entryElement);
                        castingRules.put(rule.getSourceType(), rule);
                    } catch (EDMException e) {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            mLogger.log(Level.INFO,NbBundle.getMessage(SQLOperatorFactory.class, "LOG.INFO_Failed_to_read_the_OperatorTemplateFile", new Object[] {LOG_CATEGORY})+e);
        }
    }

    protected static class CastingRule {

        public static final String TAG_RULE_MAP = "rule-map"; //NOI18N
        private static final String ATTR_SOURCE_TYPE = "source-type"; //NOI18N
        private static final String TAG_RULE = "rule"; //NOI18N
        private static final String ATTR_TARGET_TYPES = "target-types"; //NOI18N
        private static final String VALUE_EQUIVALENT = "equivalent"; //NOI18N
        private static final String VALUE_UPCAST = "upcast"; //NOI18N
        private static final String VALUE_DOWNCAST = "downcast"; //NOI18N
        private String sourceTypeName;
        private Map<String, String> typeToRuleMap;

        public CastingRule() {
            typeToRuleMap = new HashMap<String, String>();
        }

        public CastingRule(String typeName) {
            this();
            if (StringUtil.isNullString(typeName)) {
                throw new IllegalArgumentException(NbBundle.getMessage(SQLOperatorFactory.class, "MSG_empty_value_for_typeName"));
            }

            sourceTypeName = typeName;
        }

        public CastingRule(String typeName, int ruleVal, Collection targetTypes) {
            this(typeName);

            Iterator iter = targetTypes.iterator();
            while (iter.hasNext()) {
                String type = (String) iter.next();
                setRule(type, ruleVal);
            }
        }

        public String getSourceType() {
            return sourceTypeName;
        }

        public void setRule(String newType, int ruleVal) {
            if (StringUtil.isNullString(newType)) {
                throw new IllegalArgumentException(NbBundle.getMessage(SQLOperatorFactory.class, "MSG_empty_value_for_newType"));
            }

            switch (ruleVal) {
                case SQLConstants.TYPE_CHECK_SAME:
                case SQLConstants.TYPE_CHECK_COMPATIBLE:
                case SQLConstants.TYPE_CHECK_INCOMPATIBLE:
                case SQLConstants.TYPE_CHECK_DOWNCAST_WARNING:
                    typeToRuleMap.put(newType.trim().toLowerCase(), String.valueOf(ruleVal));
                    break;
                default:
                    throw new IllegalArgumentException(NbBundle.getMessage(SQLOperatorFactory.class, "MSG_Unrecognized_casting_rule_value") + ruleVal);
            }
        }

        public int getRule(String typeName) {
            Object o = typeToRuleMap.get(typeName);
            if (o instanceof String) {
                try {
                    return Integer.parseInt((String) o);
                } catch (NumberFormatException e) {
                    return SQLConstants.TYPE_CHECK_INCOMPATIBLE;
                }
            }
            return SQLConstants.TYPE_CHECK_INCOMPATIBLE;
        }

        public void parseXML(Element element) throws EDMException {
            if (element == null || !TAG_RULE_MAP.equals(element.getNodeName())) {
                throw new EDMException(NbBundle.getMessage(SQLOperatorFactory.class, "MSG_Null_or_invalid_element"));
            }

            sourceTypeName = element.getAttribute(ATTR_SOURCE_TYPE);
            if (StringUtil.isNullString(sourceTypeName)) {
                throw new EDMException(NbBundle.getMessage(SQLOperatorFactory.class, "MSG_CastingRule_element_must_contain_source_name"));
            }

            NodeList outcomeList = element.getElementsByTagName(TAG_RULE);
            for (int j = 0; j < outcomeList.getLength(); j++) {
                Element outcomeElement = (Element) outcomeList.item(j);
                String ruleName = outcomeElement.getAttribute(ATTR_NAME);
                if (StringUtil.isNullString(ruleName)) {
                    continue;
                }

                int ruleVal = SQLConstants.TYPE_CHECK_UNKNOWN;
                if (VALUE_EQUIVALENT.equals(ruleName)) {
                    ruleVal = SQLConstants.TYPE_CHECK_SAME;
                } else if (VALUE_UPCAST.equals(ruleName)) {
                    ruleVal = SQLConstants.TYPE_CHECK_COMPATIBLE;
                } else if (VALUE_DOWNCAST.equals(ruleName)) {
                    ruleVal = SQLConstants.TYPE_CHECK_DOWNCAST_WARNING;
                }

                List typesList = StringUtil.createStringListFrom(outcomeElement.getAttribute(ATTR_TARGET_TYPES));
                Iterator typeIter = typesList.iterator();
                while (typeIter.hasNext()) {
                    String newType = (String) typeIter.next();
                    setRule(newType, ruleVal);
                }
            }
        }
    }
}
