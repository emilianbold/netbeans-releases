
/*
 * Created on Nov 24, 2003
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;

/**
 * @author schandra
 *
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MethodsSelectionDialogTestCase extends AbstractUMLTestCase
{
    public static boolean genMoved = false;
    private IClass subc, superc;
    private IGeneralization gen;
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(MethodsSelectionDialogTestCase.class);
    }
    
    protected void setUp()
    {
        subc = createClass("Subclass");
        superc = createClass("Superclass");
    }
    
    public void testMethodsSelectionDialog()
    {
//		IOperation op,op1,op2;
//		superc.addOperation(op = superc.createOperation("int", "washington"));
//		superc.addOperation(op1 = superc.createOperation("int", "newyork"));
//		superc.addOperation(op2 = superc.createOperation("int", "denver"));
//		op.setIsAbstract(true);
//		op1.setIsAbstract(true);
//
//		gen = relFactory.createGeneralization(superc, subc);
//
//		// Will succeed if all methods are selected.
//		  assertEquals(4, subc.getOperations().size());
    }
}

