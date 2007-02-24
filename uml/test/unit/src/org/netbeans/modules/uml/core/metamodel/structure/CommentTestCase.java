package org.netbeans.modules.uml.core.metamodel.structure;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.AbstractUMLTestCase;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 *
 */

//Some test methods need to be verified.
public class CommentTestCase extends AbstractUMLTestCase
{
    private IClass clazz1 = null;
    private IComment comment = null;
    public CommentTestCase()
    {
        super();
    }
    
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(CommentTestCase.class);
    }
    
    protected void setUp()
    {
        comment = factory.createComment(null);
        project.addElement(comment);
        clazz1 = factory.createClass(null);
        project.addElement(clazz1);
    }
    
    public void testSetBody()
    {
        comment.setBody("NewBody");
        assertEquals("NewBody", comment.getBody());
    }
    
    public void testAddAnnotatedElement()
    {
        comment.addAnnotatedElement(clazz1);
        ETList<INamedElement> elems = comment.getAnnotatedElements();
        assertNotNull(elems);
        
        INamedElement namedEleGot = null;
        if (elems != null)
        {
            for (int i=0;i<elems.size();i++)
            {
                namedEleGot = elems.get(i);
            }
        }
        assertEquals(((INamedElement)clazz1).getXMIID(), namedEleGot.getXMIID());
    }
    
    public void testRemoveAnnotatedElements()
    {
        comment.removeAnnotatedElement(clazz1);
        ETList<INamedElement> elems = comment.getAnnotatedElements();
        assertTrue(elems == null || elems.size() == 0);
    }
    
    //TODO: to be completed
    public void testIsAnnotated()
    {
        boolean isAnn = comment.getIsAnnotatedElement(clazz1);
    }
}


