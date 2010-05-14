package org.netbeans.modules.iep.model;

import org.netbeans.modules.iep.model.share.SharedConstants;

public enum WSType {
    
    
    NONE(SharedConstants.WS_TYPE_NONE),
    IN_ONLY(SharedConstants.WS_TYPE_IN_ONLY),
    OUT_ONLY(SharedConstants.WS_TYPE_OUT_ONLY), 
    REQUEST_REPLY(SharedConstants.WS_TYPE_REQUEST_REPLY);
    
    private final String mType;
    
    private WSType(String type) {
        this.mType = type;
    }
    
    public static WSType getType(String type) {
        WSType t = null;
        if(type != null) {
            if(type.equals(NONE.getType())) {
                t = NONE;
            } else if(type.equals(IN_ONLY.getType())) {
                t = IN_ONLY;
            } else if(type.equals(OUT_ONLY.getType())) {
                t = OUT_ONLY;
            } else if(type.equals(REQUEST_REPLY.getType())) {
                t = REQUEST_REPLY;
            }
        }
        return t;
    }
    
    public String getType() {
        return this.mType;
    }
    
    @Override
    public String toString() {
        return getType();
    }
}
