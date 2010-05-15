package org.netbeans.modules.iep.model.validator;

import org.netbeans.modules.xml.xam.spi.Validator;


public abstract class AbstractOperatorValidator implements OperatorValidator{

    private Validator mValidator;
    
    public AbstractOperatorValidator(Validator validator) {
        mValidator = validator;
    }
    
    public Validator getValidator() {
        return this.mValidator;
    }

}
