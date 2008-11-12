package org.netbeans.modules.xml.wsdl.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.validator.visitor.WSDLSemanticsVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.spi.Validator.class)
public class WSDLSemanticValidator implements Validator {
    
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
                    return null;
                }
                 
                WSDLSemanticsVisitor visitor = new WSDLSemanticsVisitor(this, validation, validatedModels);
                wsdlModel.getDefinitions().accept(visitor);
                validatedModels.add(model);
                List<ResultItem> resultItems = visitor.getResultItems();
                return new ValidationResult(resultItems, validatedModels);
                
            }
        }
        return null;
    }
}
