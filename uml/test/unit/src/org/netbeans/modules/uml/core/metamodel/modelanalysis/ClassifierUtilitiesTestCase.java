
/*
 * File       : ClassifierUtilitiesTestCase.java
 * Created on : Oct 21, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.modelanalysis;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;

/**
 * @author Aztec
 */
public class ClassifierUtilitiesTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ClassifierUtilitiesTestCase.class);
    }
    
    ClassifierUtilities cu = null;
    IClass clazz1 = null;
    IClass clazz2 = null;
    IInterface iface = null;
    
    IOperation op1 = null;
    IOperation op2 = null;
    IOperation op3 = null;
    IOperation op4 = null;
    IOperation op5 = null;
    IOperation op6 = null;
        
    
    protected void setUp() throws Exception
    {
        super.setUp();
        cu = new ClassifierUtilities();
        clazz1 = createClass("ClassOne");
        op1 = clazz1.createOperation("int", "m1");
        op2 = clazz1.createOperation("String", "m2");
        clazz1.addOperation(op1);
        clazz1.addOperation(op2);
        clazz2 = createSuperclass(clazz1, "Super");
        op3 = clazz2.createOperation("int", "m3");
        op4 = clazz2.createOperation("String", "m4");
        clazz2.addOperation(op3);
        clazz2.addOperation(op4);
        iface = createSuperinterface(clazz1, "SuperI");
        op5 = iface.createOperation("int", "m3");
        op6 = iface.createOperation("String", "m4");
        iface.addOperation(op5);
        iface.addOperation(op6);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        cu = null;
    }
    
    public void testCollectAllOperations()
    {
//        ETList<IOperation> opList = cu.collectAllOperations(clazz1);
//        assertEquals(6, opList.size());
    }
    
    public void testCollectGeneralizingClassifiers()
    {
        // Tested by testCollectAllOperations()
    }
    
    public void collectImplementedInterfaces()
    {
        // Tested by testCollectAllOperations()
    }    
}
