package org.netbeans.modules.iep.model;

public interface LinkComponent extends Component {

    OperatorComponent getFrom();
    
    void setFrom(OperatorComponent from);
    
    OperatorComponent getTo();
    
    void setTo(OperatorComponent to);
}
