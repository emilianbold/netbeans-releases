package org.netbeans.modules.iep.model;

import org.netbeans.modules.iep.model.share.SharedConstants;

public enum IOType {
    
    
    NONE(SharedConstants.IO_TYPE_NONE),
    STREAM(SharedConstants.IO_TYPE_STREAM),
    RELATION(SharedConstants.IO_TYPE_RELATION), 
    TABLE(SharedConstants.IO_TYPE_TABLE);
    
    private final String mType;
    
    private IOType(String type) {
        this.mType = type;
    }
    
    public static IOType getType(String type) {
        IOType t = null;
        if(type != null) {
            if(type.equals(NONE.getType())) {
                t = NONE;
            } else if(type.equals(STREAM.getType())) {
                t = STREAM;
            } else if(type.equals(RELATION.getType())) {
                t = RELATION;
            } else if(type.equals(TABLE.getType())) {
                t = TABLE;
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
