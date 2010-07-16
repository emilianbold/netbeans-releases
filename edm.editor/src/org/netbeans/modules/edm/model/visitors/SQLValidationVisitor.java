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
package org.netbeans.modules.edm.model.visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.axiondb.AxionException;
import org.axiondb.parser.AxionDateTimeFormatParser;
import org.netbeans.modules.edm.model.ColumnRef;
import org.netbeans.modules.edm.model.RuntimeInput;
import org.netbeans.modules.edm.model.SQLCaseOperator;
import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.model.SQLConnectableObject;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDBColumn;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.SQLFilter;
import org.netbeans.modules.edm.model.SQLGenericOperator;
import org.netbeans.modules.edm.model.SQLInputObject;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.SQLLiteral;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLPredicate;
import org.netbeans.modules.edm.model.SQLWhen;
import org.netbeans.modules.edm.model.SourceColumn;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.ValidationInfo;
import org.netbeans.modules.edm.model.impl.SQLCustomOperatorImpl;
import org.netbeans.modules.edm.model.impl.ValidationInfoImpl;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.StringUtil;
import org.netbeans.modules.edm.model.DBColumn;
import org.netbeans.modules.edm.editor.utils.SQLUtils;
import org.openide.util.NbBundle;

/**
 * @author Ritesh Adval
 */
public class SQLValidationVisitor implements SQLVisitor {

    private static transient final Logger mLogger = Logger.getLogger(SQLValidationVisitor.class.getName());
    private static final HashSet<String> DATE_FORMAT_OPS = new HashSet<String>();

    static {
        DATE_FORMAT_OPS.add("isvaliddatetime");
        DATE_FORMAT_OPS.add("chartodate");
        DATE_FORMAT_OPS.add("datetochar");
    }
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private List<ValidationInfo> validationInfoList = new ArrayList<ValidationInfo>();

    public List<ValidationInfo> getValidationInfoList() {
        return this.validationInfoList;
    }

    public boolean hasErrors(Collection valInfoList) {
        boolean ret = false;

        if (valInfoList != null) {
            Iterator itr = valInfoList.iterator();
            ValidationInfo vInfo = null;

            while (itr.hasNext()) {
                vInfo = (ValidationInfo) itr.next();
                if (vInfo.getValidationType() == ValidationInfo.VALIDATION_ERROR) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    public void visit(SourceTable sourceTable) {
        visit(sourceTable, false);
    }

    public void visit(SourceTable sourceTable, boolean CheckForVisibileColumns) {
        // validate extraction condition
        SQLCondition extCondition = sourceTable.getFilterCondition();
        List<DBColumn> srcCoulmns = sourceTable.getColumnList();

        if (CheckForVisibileColumns) {
            int colCount = 0;
            for (DBColumn col : sourceTable.getColumnList()) {
                SourceColumn srcCol = (SourceColumn) col;
                if (!srcCol.isVisible()) {
                    colCount++;
                }
            }
            if (colCount == srcCoulmns.size()) {
                String desc = buildErrorMessageWithObjectIdentifier(sourceTable.getQualifiedName(), NbBundle.getMessage(SQLValidationVisitor.class, "MSG_one_column_must_be_selected"));
                ValidationInfo validationInfo = new ValidationInfoImpl(sourceTable, desc, ValidationInfo.VALIDATION_ERROR);
                validationInfoList.add(validationInfo);
            }
        }

        if (extCondition != null) {
            if (!extCondition.isValid()) {
                String desc = buildErrorMessageWithObjectIdentifier(sourceTable.getQualifiedName(), NbBundle.getMessage(SQLValidationVisitor.class, "ERROR_extraction_condition_invalid"));
                ValidationInfo validationInfo = new ValidationInfoImpl(extCondition, desc, ValidationInfo.VALIDATION_ERROR);
                validationInfoList.add(validationInfo);
            } else {
                visit(extCondition);
            }
        }
    }

    public void visit(SQLCaseOperator operator) {
        visitExpression(operator, true);
    }

    public void visit(SQLCondition condition) {
        // If there is no condition then do not validate
        if (!condition.isConditionDefined()) {
            return;
        }

        // If the resulting expression does not form a valid condition, then raise an
        // error.
        if (!condition.isValid()) {
            String error = NbBundle.getMessage(SQLValidationVisitor.class, "ERROR_condition_invalid");
            ValidationInfo info = new ValidationInfoImpl(null, error, ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(info);
        }

        Collection allObjects = condition.getAllObjects();
        List expObjects = SQLObjectUtil.getAllExpressionObjects(allObjects);
        // Validate all other objects in objectMap
        validate(allObjects, expObjects);
    }

    public void visit(SQLDefinition definition) {
        for (SQLObject sqlObj : definition.getAllObjects()) {
            if (sqlObj instanceof SQLJoinView ) {
                visit((SQLJoinView) sqlObj);
            }
        }
    }

    public void visit(SQLFilter filter) {
        visitExpression(filter, true);

        if (!filter.isValid()) {
            String descriptor =filter.getDisplayName() +"-"+filter.toString();
            String message = buildErrorMessageWithObjectIdentifier(descriptor, NbBundle.getMessage(SQLValidationVisitor.class, "ERROR_predicate_invalid"));
            ValidationInfo expValidationInfo = new ValidationInfoImpl(filter, message, ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(expValidationInfo);
        }

        // Check if the next SQLFilter clause, if any, has a prefix.
        if (filter.getNextFilter() != null && StringUtil.isNullString(filter.getNextFilter().getPrefix())) {
            String descriptor = filter.getNextFilter().getDisplayName()+"-"+filter.toString();
            String message = buildErrorMessageWithObjectIdentifier(descriptor, NbBundle.getMessage(SQLValidationVisitor.class, "ERROR_predicate_missing_prefix"));

            ValidationInfo expValidationInfo = new ValidationInfoImpl(filter, message, ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(expValidationInfo);
        }
    }

    public void visit(SQLGenericOperator operator) {
        if (operator.hasVariableArgs() && operator.getInputObjectMap().size() == 0) {
            String desc = buildErrorMessageWithObjectIdentifier(operator.getDisplayName(), NbBundle.getMessage(SQLValidationVisitor.class, "ERROR_input_not_linked"));
            ValidationInfo expValidationInfo = new ValidationInfoImpl(operator, desc, ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(expValidationInfo);
        } else {
            visitExpression(operator, true);
        }
    }

    public void visit(SQLJoinOperator operator) {
        visit(operator, false);
    }
    
    public void visit(SQLJoinOperator operator, boolean CheckForVisibileColumns) {
        int tColCount = 0;
        int count = 0;
        if (CheckForVisibileColumns || operator.isRoot()) {
            Iterator it = operator.getAllSourceTables().iterator();
            while (it.hasNext()) {
                SourceTable tbl = (SourceTable) it.next();
                tColCount = tColCount + tbl.getColumnList().size();
                for (DBColumn col : tbl.getColumnList()) {
                    SourceColumn srcCol = (SourceColumn) col;
                    if (!srcCol.isVisible()) {
                        count++;
                    }
                }
            }

            if (count == tColCount) {
                String desc = buildErrorMessageWithObjectIdentifier("", NbBundle.getMessage(SQLValidationVisitor.class, "MSG_one_column_must_be_selected"));
                ValidationInfo validationInfo = new ValidationInfoImpl(operator, desc, ValidationInfo.VALIDATION_ERROR);
                validationInfoList.add(validationInfo);
            }
        }

        doJoinConditionValidation(operator);
        visitExpression(operator, true);
    }

    public void visit(SQLJoinView joinView) {
        SQLJoinOperator rJoin = joinView.getRootJoin();
        if (rJoin != null) {
            rJoin.visit(this);
        }
    }

    public void visit(SQLPredicate operator) {
        visitExpression(operator, true);
    }

    public void visit(SQLWhen when) {
    }

    private String buildErrorMessageWithObjectIdentifier(String identifier, String errorKey) {
        return buildErrorMessageWithObjectIdentifiers(identifier, errorKey, EMPTY_OBJECT_ARRAY);
    }

    private String buildErrorMessageWithObjectIdentifiers(String identifier, String errorKey, Object[] errorParams) {
        String errorMessage = errorKey+errorParams;
        return NbBundle.getMessage(SQLValidationVisitor.class, "MSG_SQLvalidateVisitor",new Object[]{identifier, errorMessage});
    }

    private void doJoinConditionValidation(SQLJoinOperator operator) {
        // If join condition is not defined then warn user about a Cartesian join being
        // created
        String identifier = "";
        SQLJoinView joinView = (SQLJoinView) operator.getParentObject();
        if (joinView != null) {
            identifier = joinView.getQualifiedName();
        }

        SQLCondition jCondition = operator.getJoinCondition();
        if (!jCondition.isConditionDefined()) {
            String desc = buildErrorMessageWithObjectIdentifier(identifier, NbBundle.getMessage(SQLValidationVisitor.class, "WARNING_join_condition_missing"));
            ValidationInfoImpl vInfo = new ValidationInfoImpl(jCondition, desc, ValidationInfo.VALIDATION_WARNING);
            validationInfoList.add(vInfo);
        } else {
            if (!jCondition.isValid()) {
                String desc = buildErrorMessageWithObjectIdentifier(identifier, NbBundle.getMessage(SQLValidationVisitor.class, "ERROR_join_condition_invalid"));
                ValidationInfoImpl vInfo = new ValidationInfoImpl(jCondition, desc, ValidationInfo.VALIDATION_ERROR);
                validationInfoList.add(vInfo);
            }
        }

        // TODO Check whether this is too restrictive - shouldn't user be able to create
        // arbitrary join conditions?
        try {
            // check for at least one = condition between columns of the two joined table.
            // also check whether such columns are primary
            SQLPredicate rootPredicate = jCondition.getRootPredicate();
            if (rootPredicate != null) {
                SQLFilter filter = SQLPredicateVisitor.visit(rootPredicate);
                boolean foundOneEqualOp = false;
                boolean foundOnePKMatch = false;
                String pkMsg = null;

                while (filter != null) {
                    SQLObject leftObj = filter.getSQLObject(SQLFilter.LEFT);
                    SQLObject rightObj = filter.getSQLObject(SQLFilter.RIGHT);
                    String op = filter.getOperator();

                    if (isValidJoinOperator(op)) {
                        if (leftObj.getObjectType() == SQLConstants.COLUMN_REF) {
                            leftObj = ((ColumnRef) leftObj).getColumn();
                        }

                        if (rightObj.getObjectType() == SQLConstants.COLUMN_REF) {
                            rightObj = ((ColumnRef) rightObj).getColumn();
                        }

                        SourceColumn sLeft = getFirstSourceColumnReferencedIn(leftObj);
                        SourceColumn sRight = getFirstSourceColumnReferencedIn(rightObj);

                        if (sLeft != null && sRight != null) {
                            foundOneEqualOp = true;
                            String errorKey = null;
                            Object[] objectNames = EMPTY_OBJECT_ARRAY;

                            if (sLeft.isPrimaryKey() && sRight.isPrimaryKey()) {
                                foundOnePKMatch = true;
                            } else if (sLeft.isPrimaryKey() && !sRight.isPrimaryKey()) {
                                errorKey = "BUND801: Column {0} used in join condition is not a primary key.";
                                objectNames = new Object[]{sRight.toString()};
                            } else if (!sLeft.isPrimaryKey() && sRight.isPrimaryKey()) {
                                errorKey = "BUND801: Column {0} used in join condition is not a primary key.";
                                objectNames = new Object[]{sLeft.toString()};
                            } else {
                                errorKey = "BUND802: Columns {0},{1} used in join condition are not primary keys.";
                                objectNames = new Object[]{sLeft.toString(), sRight.toString()};
                            }

                            if (errorKey != null) {
                                pkMsg = buildErrorMessageWithObjectIdentifiers(identifier, errorKey, objectNames);
                            }
                        }
                    }

                    filter = filter.getNextFilter();
                }

                // If an equal relation is not found between columns of both tables
                // then this is an error in join condition
                if (!foundOneEqualOp) {
                    String desc = buildErrorMessageWithObjectIdentifier(identifier, NbBundle.getMessage(SQLValidationVisitor.class, "ERROR_join_missing_equal_op"));
                    ValidationInfoImpl vInfo = new ValidationInfoImpl(jCondition, desc, ValidationInfo.VALIDATION_WARNING);
                    validationInfoList.add(vInfo);
                } else if (!foundOnePKMatch) {
                    String desc = pkMsg;
                    ValidationInfoImpl vInfo = new ValidationInfoImpl(jCondition, desc, ValidationInfo.VALIDATION_WARNING);
                    validationInfoList.add(vInfo);
                }


            }
        } catch (EDMException ex) {
            mLogger.log(Level.INFO,NbBundle.getMessage(SQLValidationVisitor.class, "Error_while_validating_SQLJoinOperator",new Object[] {operator.getDisplayName()}),ex);
        }
    }

    private SourceColumn getFirstSourceColumnReferencedIn(SQLObject sqlObj) {
        if (sqlObj instanceof SourceColumn) {
            Object parent = ((SourceColumn) sqlObj).getParent();
            return (parent instanceof RuntimeInput) ? null : (SourceColumn) sqlObj;
        } else if (sqlObj instanceof ColumnRef) {
            return getFirstSourceColumnReferencedIn(((ColumnRef) sqlObj).getColumn());
        } else if (sqlObj instanceof SQLGenericOperator) {
            SQLGenericOperator op = (SQLGenericOperator) sqlObj;
            for (Iterator iter = op.getInputObjectMap().values().iterator(); iter.hasNext();) {
                SQLInputObject inputWrapper = (SQLInputObject) iter.next();
                SQLObject wrappedObj = inputWrapper.getSQLObject();
                return getFirstSourceColumnReferencedIn(wrappedObj);
            }
        }

        return null;
    }

    private boolean isObjectMappedToExpression(SQLObject obj, List expObjects) {
        boolean result = false;
        Iterator it = expObjects.iterator();

        while (it.hasNext()) {
            SQLConnectableObject expObj = (SQLConnectableObject) it.next();
            result = SQLObjectUtil.isObjectMappedToExpression(obj, expObj);
            if (result) {
                return result;
            }
        }

        return result;
    }

    private boolean isValidJoinOperator(String op) {
        // We currently only support equijoins.
        return op != null && op.trim().equalsIgnoreCase("=");
    }

    private void validate(Collection allObjects, List expObjects) {
        Iterator it = allObjects.iterator();
        SQLObject sqlObject = null;
        SQLObject column = null;
        SQLConnectableObject exprObj = null;
        String desc = null;
        while (it.hasNext()) {
            sqlObject = (SQLObject) it.next();

            if (sqlObject.getObjectType() == SQLConstants.COLUMN_REF) {
                column = ((ColumnRef) sqlObject).getColumn();

                if ((column.getObjectType() == SQLConstants.SOURCE_COLUMN) && !(((SQLDBColumn) column).isVisible())) {
                    desc = NbBundle.getMessage(SQLValidationVisitor.class, "MSG_Column_Used_Not_Visible",new Object[] {sqlObject.getDisplayName()});
                    ValidationInfo validationInfo = new ValidationInfoImpl(sqlObject.getParentObject(), desc, ValidationInfo.VALIDATION_ERROR);
                    validationInfoList.add(validationInfo);
                }
            }

            if (sqlObject instanceof SQLConnectableObject) {
                exprObj = (SQLConnectableObject) sqlObject;
                SQLValidationVisitor vVisitor = new SQLValidationVisitor();
                vVisitor.visitExpression(exprObj, false);
                List<ValidationInfo> vInfos = vVisitor.getValidationInfoList();
                if (vInfos.size() > 0) {
                    validationInfoList.addAll(vInfos);
                }
            } else if (!isObjectMappedToExpression(sqlObject, expObjects)) {
                desc = NbBundle.getMessage(SQLValidationVisitor.class, "MSG_not_mapped_to_an_expression.",new Object[] {sqlObject.getDisplayName()});
                ValidationInfo validationInfo = new ValidationInfoImpl(sqlObject, desc, ValidationInfo.VALIDATION_ERROR);

                validationInfoList.add(validationInfo);
            }
        }
    }

    private ValidationInfoImpl validateDateFormatInput(SQLConnectableObject op, String argName, SQLObject sqlObj) {
        ValidationInfoImpl errorInfo = null;
        int validationType = ValidationInfo.VALIDATION_ERROR;

        if (sqlObj instanceof SQLLiteral) {
            SQLLiteral literal = (SQLLiteral) sqlObj;
            String value = literal.getValue();
            AxionDateTimeFormatParser formatParser = new AxionDateTimeFormatParser();

            try {
                formatParser.parseDateTimeFormatToJava(value);
            } catch (AxionException e) {
                String message = buildErrorMessageWithObjectIdentifiers(op.getDisplayName(), "BUND819: ''{0}'' is an invalid datetime format.", new Object[]{value});
                errorInfo = new ValidationInfoImpl(op, message, validationType);
            }
        }

        return errorInfo;
    }

    private void validateExpressionObjectChildren(SQLConnectableObject exp) {
        // go through children of this expression object
        List childList = exp.getChildSQLObjects();
        Iterator it = childList.iterator();

        while (it.hasNext()) {
            SQLObject sqlObject = (SQLObject) it.next();
            SQLConnectableObject exprObj = null;
            if (sqlObject instanceof SQLConnectableObject) {
                exprObj = (SQLConnectableObject) sqlObject;
                visitExpression(exprObj, true);
            }
        }
    }

    private ValidationInfoImpl validateInputDataType(SQLConnectableObject op, String argName, String displayName, SQLObject sqlObj) {
        final String identifier = op.getDisplayName();

        ValidationInfoImpl expValidationInfo = null;
        String desc = null;
        int validationType = ValidationInfo.VALIDATION_ERROR;

        int compatibility = op.isInputCompatible(argName, sqlObj);
        switch (compatibility) {
            case SQLConstants.TYPE_CHECK_INCOMPATIBLE:
                desc = buildErrorMessageWithObjectIdentifiers(identifier, "Incompatible datatype for input ''{0}''.", new Object[]{displayName});
                break;
            case SQLConstants.TYPE_CHECK_DOWNCAST_WARNING:
            case SQLConstants.TYPE_CHECK_UNKNOWN:
                String datatype = SQLUtils.getStdSqlType(sqlObj.getJdbcType());
                if (datatype == null) {
                    datatype = "unknown";
                }
                desc = buildErrorMessageWithObjectIdentifiers(identifier, "Use of {0} datatype as input for {1} may result in loss of precision, data truncation, unexpected results, or runtime error.", new Object[]{datatype, displayName});
                validationType = ValidationInfo.VALIDATION_WARNING;
                break;
            case SQLConstants.TYPE_CHECK_COMPATIBLE:
            case SQLConstants.TYPE_CHECK_SAME:
            default:
                desc = null;
        }

        if (desc != null) {
            expValidationInfo = new ValidationInfoImpl(op, desc, validationType);
        }

        return expValidationInfo;
    }

    private void visitExpression(SQLConnectableObject operator, boolean recurse) {
        final String identifier = operator.getDisplayName();

        Iterator iter = operator.getInputObjectMap().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String argName = (String) entry.getKey();
            SQLInputObject obj = (SQLInputObject) entry.getValue();
            ValidationInfo expValidationInfo = null;

            if (obj == null) {
                String desc = buildErrorMessageWithObjectIdentifiers(identifier, "BUND828: ''{0}'' is not linked.", new Object[]{obj.getDisplayName()});
                expValidationInfo = new ValidationInfoImpl(operator, desc, ValidationInfo.VALIDATION_ERROR);
            }

            SQLObject sqlObj = obj.getSQLObject();
            if (sqlObj == null) {
                String desc = buildErrorMessageWithObjectIdentifiers(identifier, "BUND828: ''{0}'' is not linked.", new Object[]{obj.getDisplayName()});
                expValidationInfo = new ValidationInfoImpl(operator, desc, ValidationInfo.VALIDATION_ERROR);
            } else {
                expValidationInfo = (sqlObj instanceof SQLCustomOperatorImpl) ? null : validateInputDataType(operator, argName, obj.getDisplayName(), sqlObj);
            }

            if (expValidationInfo != null) {
                validationInfoList.add(expValidationInfo);
            }

            if (sqlObj != null && operator.isInputStatic(argName) && DATE_FORMAT_OPS.contains(identifier.toLowerCase())) {
                if ("format".equals(argName.toLowerCase()) || "right".equals(argName.toLowerCase())) {
                    expValidationInfo = validateDateFormatInput(operator, argName, sqlObj);
                    if (expValidationInfo != null) {
                        validationInfoList.add(expValidationInfo);
                    }
                }
            }

            if (recurse && sqlObj != null && sqlObj instanceof SQLConnectableObject) {
                SQLConnectableObject inObj = (SQLConnectableObject) sqlObj;
                inObj.visit(this);
            }
        }

        // validate children
        validateExpressionObjectChildren(operator);
    }
}
