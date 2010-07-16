package org.netbeans.modules.iep.model.validator;

import java.util.List;

import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

public interface OperatorValidator extends SharedConstants {

    public List<ResultItem> validate(OperatorComponent component);
    
}
