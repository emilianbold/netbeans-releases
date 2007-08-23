package org.netbeans.modules.iep.model.validator.visitor;

import java.util.List;


import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

public class IEPSemanticsVisitor implements IEPVisitor {

	public List<ResultItem> mResultItems;
    private Validation mValidation;
    private List<Model> mValidatedModels;
    private Validator mValidator;
    
	/** Creates a new instance of IEPSemanticsVisitor */
    public IEPSemanticsVisitor(Validator validator, Validation validation, List<Model> validatedModels) {
        mValidator = validator;
        mValidation = validation;
        mValidatedModels = validatedModels;
        
        
    }
    
    public List<ResultItem> getResultItems() {
        return mResultItems;
    }
    
	public void visitComponent(Component component) {
		// TODO Auto-generated method stub
		
	}

	public void visitProperty(Property property) {
		// TODO Auto-generated method stub
		
	}

	
}
