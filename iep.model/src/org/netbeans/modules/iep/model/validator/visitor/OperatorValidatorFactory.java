package org.netbeans.modules.iep.model.validator.visitor;

import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.xml.xam.spi.Validator;

public class OperatorValidatorFactory {

    private static OperatorValidatorFactory mInstance;
    
    private Validator mValidator;
    
    private OperatorValidatorFactory(Validator validator) {
        this.mValidator = validator;
    }
    
    public static OperatorValidatorFactory getDefault(Validator validator) {
    
        if(mInstance == null) {
            mInstance = new OperatorValidatorFactory(validator);
        }
        
        return mInstance;
    }
    
    public OperatorValidator newOperatorValidator(OperatorComponent operator) {
        OperatorValidator validator = null;
        
        String type = operator.getType();
        
        if(Constants.TYPE_UNIONALL.equals(type)) {
            validator = new UnionAllValidator(this.mValidator);
        } else if( Constants.TYPE_STREAM_PROJECTION_FILTER.equals(type)
                || Constants.TYPE_TUPLE_SERIAL_CORRELATION.equals(type)
                || Constants.TYPE_RELATION_MAP.equals(type)
                || Constants.TYPE_TUPLE_BASED_AGGREGATOR.equals(type)
                || Constants.TYPE_TIME_BASED_AGGREGATOR.equals(type)
                || Constants.TYPE_RELATION_AGGREGATOR.equals(type)) {
            
            validator = new SchemaOwnerValidator(this.mValidator);
        
        }
        
        return validator;
    }
}
