package org.netbeans.test.xml.schema.abe;

import java.awt.event.KeyEvent;
import org.netbeans.test.xml.schema.lib.*;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author Mikhail Matveev
 */
public class DesignViewOperator extends TopComponentOperator{
    
    private DVGenericNodeOperator m_elementsRoot;    
    private DVGenericNodeOperator m_complexTypesRoot;
    private DVNamespaceOperator m_namespace;
    
    public static final String defaultElementName="newElement";
    public static final String defaultComplexTypeName="newComplexType";
    public static final String defaultAttributeName="newComplexType";

    public static DesignViewOperator createDesignViewOperator(String schemaName){
        return new DesignViewOperator(new SchemaMultiView(schemaName).getTopComponentOperator());        
    }
    
    public DesignViewOperator(TopComponentOperator op){
        super(op);
        m_namespace=new DVNamespaceOperator(this);
        m_complexTypesRoot=new DVGenericNodeOperator(this, "Complex Types");
        m_elementsRoot=new DVGenericNodeOperator(this, "Elements");        
    }
    
    public DVGenericNodeOperator getElementsRoot(){
        return m_elementsRoot;        
    }

    public DVGenericNodeOperator getComplexTypesRoot(){
        return m_complexTypesRoot;        
    }
    
    public DVNamespaceOperator getNameSpace(){
        return m_namespace;
    }
    
    public DVNodeOperator addElement(String name){
        return getNameSpace().addElement(name);
    }    
    
    public DVNodeOperator addComplexType(String name){
        getNameSpace().clickForPopupRobot();
        new JPopupMenuOperator().pushMenu("Add|Complex Type");
        DVNodeOperator res=new DVNodeOperator(this, defaultComplexTypeName, true, true);
        res.getLabel().setText(name);
        pushKey(KeyEvent.VK_ENTER);
        return res;
    }    
    
}
