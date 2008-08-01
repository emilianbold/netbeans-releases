package org.netbeans.modules.iep.editor.designer.operators;

public class OperatorNodeFactory {

    private static OperatorNodeFactory mInstance;
    
    public OperatorNodeFactory getInstance() {
        
        if(mInstance != null) {
            mInstance = new OperatorNodeFactory();
        }
        
        return mInstance;
    }
    
    
    public OperatorWidget getWidget() {
        return null;
    }
    
    
}
