package org.netbeans.modules.iep.model.validator.visitor;

import java.util.List;

import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

public interface OperatorValidator {

    public List<ResultItem> validate(OperatorComponent component);
    
}
