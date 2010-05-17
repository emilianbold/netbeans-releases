package org.netbeans.modules.iep.model.validator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.netbeans.modules.tbls.model.TcgPropertyType;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;

/**
 *        //applicable to
 *        StreamProjectionAndFilter 
 *         TupleSerialCorrelation 
 *         RelationMap 
 *         TupleBasedAggregator
 *         TimeBasedAggregator 
 *         RelationAggregator
 * 
 * 
 *
 */
public class SchemaOwnerValidator extends AbstractOperatorValidator {

    private static Logger mLogger = Logger.getLogger(UnionAllValidator.class.getName());
    private List<ResultItem> mResultItems = new ArrayList<ResultItem>();
    private Pattern pattern = Pattern.compile("(\\w*)\\.(\\w*)");

    public SchemaOwnerValidator(Validator validator) {
        super(validator);
    }

    public List<ResultItem> validate(OperatorComponent component) {
        SchemaComponentContainer scContainer = component.getModel().getPlanComponent().getSchemaComponentContainer();
        List allowedColumnNames = new ArrayList();

        boolean isSchemaOwner = component.getBoolean(PROP_IS_SCHEMA_OWNER);
        if (isSchemaOwner) {
            List<OperatorComponent> inputs = component.getInputOperatorList();
            Iterator<OperatorComponent> it = inputs.iterator();

            while (it.hasNext()) {
                OperatorComponent input = it.next();
                String outputSchemaId = input.getString(PROP_OUTPUT_SCHEMA_ID);
                if (!outputSchemaId.trim().equals("")) {
                    SchemaComponent scComp = scContainer.findSchema(outputSchemaId);
                    if (scComp != null) {
                        appendPossibleColumnNames(allowedColumnNames, scComp, input);
                    }
                }

            }
            inputs = component.getStaticInputList();
            it = inputs.iterator();

            while (it.hasNext()) {
                OperatorComponent input = it.next();
                String outputSchemaId = input.getString(PROP_OUTPUT_SCHEMA_ID);
                if (!outputSchemaId.trim().equals("")) {
                    SchemaComponent scComp = scContainer.findSchema(outputSchemaId);
                    if (scComp != null) {
                        appendPossibleColumnNames(allowedColumnNames, scComp, input);
                    }
                }

            }

            //to do: find way to get all the expression in a loop;
            List expressionList = getListOfExpressions(component);
            Iterator iter = expressionList.iterator();
            while (iter.hasNext()) {
                String expression = (String) iter.next();
                validateSelectExpression(allowedColumnNames,
                        expression,
                        component);

            }


        }
        return mResultItems;
    }

    private boolean validateSelectExpression(List allowedColumnsInExpression,
            String expression,
            Component component) {
        boolean validated = true;
        Matcher m = pattern.matcher(expression);
        mLogger.info("The expression is " + expression);

        while (m.find()) {
            String fm = m.group();
            if (!allowedColumnsInExpression.contains(fm) && !isNumber(fm)) {
                String message = NbBundle.getMessage(IEPSemanticsVisitor.class,
                        "Column_Not_Found", fm);
                ResultItem item = new ResultItem(getValidator(), Validator.ResultType.ERROR, component, message);
                mResultItems.add(item);
                validated = false;
            }
        }
        return validated;
    }

    private boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    private List<String> getListOfExpressions(Component component) {
        return component.getStringList(PROP_FROM_COLUMN_LIST);
    }

    private void appendPossibleColumnNames(List list,
            SchemaComponent schema,
            OperatorComponent inputComponent) {

        List<SchemaAttribute> la = schema.getSchemaAttributes();
        String inputComponentName = inputComponent.getString(PROP_NAME);
        Iterator<SchemaAttribute> columns = la.iterator();

        while (columns.hasNext()) {
            SchemaAttribute sa = columns.next();
            String colName = sa.getName();
            list.add(inputComponentName + "." + colName);
        }

    }
}
