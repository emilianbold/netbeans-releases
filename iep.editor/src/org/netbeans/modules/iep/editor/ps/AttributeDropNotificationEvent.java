package org.netbeans.modules.iep.editor.ps;

import java.util.EventObject;

public class AttributeDropNotificationEvent extends EventObject {

    private AttributeInfo mAttrInfo;
    
    public AttributeDropNotificationEvent(AttributeInfo info) {
        super(info);
        
        this.mAttrInfo = info;
    }
    
    public AttributeInfo getAttributeInfo() {
        return this.mAttrInfo;
    }

}
