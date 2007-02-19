package org.netbeans.modules.xml.wsdl.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.validator.visitor.WSDLSemanticsVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;

public class WSDLSemanticValidator implements Validator {
    
    @SuppressWarnings("unchecked")
    public static final ValidationResult EMPTY_RESULT = 
        new ValidationResult( Collections.EMPTY_SET, Collections.EMPTY_SET);
    
    public String getName() {
        return "WSDLSemanticValidator"; //NO I18N
    }
    
    public ValidationResult validate(Model model, Validation validation, ValidationType validationType) {
        if (model instanceof WSDLModel) {
            WSDLModel wsdlModel = (WSDLModel) model;

            List<Model> validatedModels = new ArrayList<Model>();
            if (validationType.equals(ValidationType.COMPLETE) ||
                    validationType.equals(ValidationType.PARTIAL)) {
                if ( wsdlModel.getState() == Model.State.NOT_WELL_FORMED ){
                    return EMPTY_RESULT;
                }
                 
                WSDLSemanticsVisitor visitor = new WSDLSemanticsVisitor(this, validation, validatedModels);
                wsdlModel.getDefinitions().accept(visitor);
                validatedModels.add(model);
                List<ResultItem> resultItems = visitor.getResultItems();
                return new ValidationResult(resultItems, validatedModels);
                
            }
        }
        return EMPTY_RESULT;
    }
    
}
