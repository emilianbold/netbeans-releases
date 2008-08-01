package org.netbeans.modules.iep.model.validator.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;

public class UnionAllValidator implements OperatorValidator {

    private static Logger mLogger = Logger.getLogger(UnionAllValidator.class.getName());
    
    private List<ResultItem> mResultItems = new ArrayList<ResultItem>();
    
    private Validator mValidator;
    
    public UnionAllValidator(Validator validator) {
        mValidator = validator;
    }
    
    public List<ResultItem> validate(OperatorComponent component) {
        List<OperatorComponent> inputs = component.getInputOperatorList();
        SchemaComponent outputSchemaA = null;
        
        if(inputs.size() > 1) {
            OperatorComponent input1 = inputs.get(0);
            outputSchemaA = input1.getOutputSchemaId();
        }
        
        for(int i = 1, I = inputs.size(); i < I; i++) {
            OperatorComponent input = inputs.get(i);
            SchemaComponent outputSchemaB = input.getOutputSchemaId();
            if(!ensureSchemasAreSame(outputSchemaA, outputSchemaB)) {
                String message = NbBundle.getMessage(UnionAllValidator.class, "ValidateForSimilarSchema.input_schemas_are_not_same");
                ResultItem item = new ResultItem(mValidator, Validator.ResultType.ERROR, component, message);
                mResultItems.add(item);
            }
        }
        
        return mResultItems;
        
    }
    
    private boolean ensureSchemasAreSame(SchemaComponent schemaA, SchemaComponent schemaB)  {
        int cntA = schemaA.getSchemaAttributes().size();// these are columns.
        int cntB = schemaB.getSchemaAttributes().size();
        boolean isSame = true;
        if(cntA != cntB) {
            isSame = false;
        }
        List<SchemaAttribute> la = schemaA.getSchemaAttributes();
        List<SchemaAttribute> lb = schemaB.getSchemaAttributes();
        int i = 0;
        while(i < cntA && isSame) {
            isSame = ensureSameColumn(la.get(i),lb.get(i));
            i++;
        }
        return isSame;
    }
    
    private boolean ensureSameColumn(SchemaAttribute cola, SchemaAttribute colb)  {
        int propCountA =  cola.getProperties().size();
        int propCountB =  colb.getProperties().size();
        if(propCountA != propCountB) {
            return  false;
        }
        try{
            if (!cola.getAttributeName().equals(colb.getAttributeName())) {
                return false;
            }
            if (!cola.getAttributeType().equals(colb.getAttributeType())) {
                return false;
            }
            if (!cola.getAttributeSize().equals(colb.getAttributeSize())) {
                return false;
            }
            if (!cola.getAttributeScale().equals(colb.getAttributeScale())) {
                return false;
            }
        } catch(Exception e) {
            mLogger.log(Level.SEVERE, "Error comparing schema attributes", e);
            return false;
        }
        return true;
    }

}
