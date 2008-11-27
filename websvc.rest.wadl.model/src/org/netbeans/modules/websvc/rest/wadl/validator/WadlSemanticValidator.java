package org.netbeans.modules.websvc.rest.wadl.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.websvc.rest.wadl.model.WadlModel;
import org.netbeans.modules.websvc.rest.wadl.validator.visitor.WadlSemanticsVisitor;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.spi.Validation;
import org.netbeans.modules.xml.xam.spi.ValidationResult;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validation.ValidationType;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.spi.Validator.class)
public class WadlSemanticValidator implements Validator {
    
    public String getName() {
        return "WadlSemanticValidator"; //NO I18N
    }
    
    public ValidationResult validate(Model model, Validation validation, ValidationType validationType) {
        if (model instanceof WadlModel) {
            WadlModel wadlModel = (WadlModel) model;

            List<Model> validatedModels = new ArrayList<Model>();
            if (validationType.equals(ValidationType.COMPLETE) ||
                    validationType.equals(ValidationType.PARTIAL)) {
                if ( wadlModel.getState() == Model.State.NOT_WELL_FORMED ){
                    return null;
                }
                 
                WadlSemanticsVisitor visitor = new WadlSemanticsVisitor(this, validation, validatedModels);
                wadlModel.getApplication().accept(visitor);
                validatedModels.add(model);
                List<ResultItem> resultItems = visitor.getResultItems();
                return new ValidationResult(resultItems, validatedModels);
                
            }
        }
        return null;
    }
}
