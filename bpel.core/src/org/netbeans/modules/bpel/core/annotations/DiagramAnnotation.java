package org.netbeans.modules.bpel.core.annotations;

import org.netbeans.modules.bpel.model.api.support.UniqueId;

/**
 *
 * @author Alexander Zgursky
 */
public class DiagramAnnotation {
    private UniqueId myBpelEntityId;
    private String myAnnotationType;
    
    public DiagramAnnotation(UniqueId bpelEntityId, String annotationType) {
        myBpelEntityId = bpelEntityId;
        myAnnotationType = annotationType;
    }
    
    public UniqueId getBpelEntityId() {
        return myBpelEntityId;
    }
    public String getAnnotationType() {
        return myAnnotationType;
    }
}
