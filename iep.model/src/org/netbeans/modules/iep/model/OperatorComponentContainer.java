package org.netbeans.modules.iep.model;

import java.util.List;

public interface OperatorComponentContainer extends Component {

    List<OperatorComponent> getAllOperatorComponent();
    
    void addOperatorComponent(OperatorComponent operator);
    
    void removeOperatorComponent(OperatorComponent operator);
    
    OperatorComponent findChildComponent(String id);
    
    OperatorComponent findOperator(String name);
    
    /**
     * Given an operator, find all the operators where this
     * operator is an input.
     * @param operator
     * @return
     */
    List<OperatorComponent> findOutputOperator(OperatorComponent operator);
        
    List<InvokeStreamOperatorComponent> getInvokeStreamOperatorComponent();
    
    List<TableInputOperatorComponent> getTableInputOperatorComponent();
}
