
package org.netbeans.modules.junit;

import java.awt.event.ActionEvent;
import java.awt.Component;
import javax.swing.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import junit.framework.*;

public class TestsActionTest extends TestCase {
    
    public TestsActionTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TestsActionTest.class);
        
        return suite;
    }
    
    /** Test of actionPerformed method, of class org.netbeans.modules.junit.TestsAction. */
    public void testActionPerformed() {
        System.out.println("testActionPerformed");
        // there is nothing to test, the actionPerformed is never called for TestsAction
    }
    
    /** Test of getName method, of class org.netbeans.modules.junit.TestsAction. */
    public void testGetName() {
        System.out.println("testGetName");
        String name = TO.getName();
        assert(null != name);
    }
    
    /** Test of iconResource method, of class org.netbeans.modules.junit.TestsAction. */
    public void testIconResource() {
        System.out.println("testIconResource");
        String icon = TO.iconResource();
        assert(null != icon);
    }
    
    /** Test of getHelpCtx method, of class org.netbeans.modules.junit.TestsAction. */
    public void testGetHelpCtx() {
        System.out.println("testGetHelpCtx");
        HelpCtx hc = TO.getHelpCtx();
        assert(null != hc);
    }
    
    /** Test of getMenuPresenter method, of class org.netbeans.modules.junit.TestsAction. */
    public void testGetMenuPresenter() {
        System.out.println("testGetMenuPresenter");
        JMenuItem jm = TO.getMenuPresenter();
        assert(null != jm);
    }
    
    /** Test of getPopupPresenter method, of class org.netbeans.modules.junit.TestsAction. */
    public void testGetPopupPresenter() {
        System.out.println("testGetPopupPresenter");
        JMenuItem jm = TO.getPopupPresenter();
        assert(null != jm);
    }
    
    /** Test of getToolbarPresenter method, of class org.netbeans.modules.junit.TestsAction. */
    public void testGetToolbarPresenter() {
        System.out.println("testGetToolbarPresenter");
        Component c = TO.getToolbarPresenter();
        assert(null != c);
    }
    
    /* protected members */
    protected CreateTestAction TO = null;
    
    protected void setUp() {
        if (null == TO)
            TO = (CreateTestAction)CreateTestAction.findObject(CreateTestAction.class, true);
    }

    protected void tearDown() {
    }
}
