package org.netbeans.modules.iep.model.validator;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;

public class OperatorTakingStreamInputValidator extends SchemaOwnerValidator {

    public OperatorTakingStreamInputValidator(Validator validator) {
        super(validator);
    }

    @Override
    public List<ResultItem> validate(OperatorComponent component) {
        List<ResultItem> results = new ArrayList<ResultItem>();
        
        List<ResultItem> r = super.validate(component);
        if(r != null) {
            results.addAll(r);
        }
        
        //validate for one of the input to be stream input
        //stream input is required and table input is optional
        
        List<OperatorComponent> inputs = component.getInputOperatorList();
        if(inputs == null | inputs.size() == 0) {
            String message = NbBundle.getMessage(IEPSemanticsVisitor.class, 
                    "OperatorTakingStreamInputValidator.atleast_one_operator_of_type_stream_should_be_connected",  
                    component.getString(PROP_NAME));
            ResultItem item = new ResultItem(getValidator(), Validator.ResultType.ERROR, component, message);
            results.add(item);
            
        }
        return results;
    }
}
