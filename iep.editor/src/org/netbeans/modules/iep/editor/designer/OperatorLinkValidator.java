package org.netbeans.modules.iep.editor.designer;

import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorType;

public class OperatorLinkValidator {

    public static boolean validateLink(OperatorComponent from, OperatorComponent to) {
        
        //same operator both from and to
        if(from.equals(to)) {
            return false;
        }
        
        //if from is table
        if (from.getOutputType().equals(OperatorType.OPERATOR_TABLE)) {
            Integer maxTableInputAllowed = (Integer) to.getComponentType().getPropertyType(OperatorComponent.PROP_NON_PERSIST_STATIC_INPUT_MAX_COUNT_KEY).getDefaultValue();
            
            if (to.getStaticInputTableList().size() >= maxTableInputAllowed) {
                return false;
            }
        } else {
            //from is operator which is not table
            
            // Check input count
            Integer maxInputAllowed = (Integer) to.getComponentType().getPropertyType(OperatorComponent.PROP_NON_PERSIST_INPUT_MAX_COUNT_KEY).getDefaultValue();
            if (to.getInputOperatorList().size() >= maxInputAllowed.intValue()) {
                return false;
            }
           
            // Check type compatibility
            if (!to.getInputType().equals(from.getOutputType())) {
                return false;
            }
        }
        
        return true;
    }
}
