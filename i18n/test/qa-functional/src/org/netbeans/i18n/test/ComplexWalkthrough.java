/*
 * ComplexWalkthrough.java
 *
 * Created on January 27, 2004, 2:39 PM
 */

package org.netbeans.i18n.test;

import java.io.File;
import org.netbeans.i18n.jelly.InternationalizeOperator;
import org.netbeans.i18n.jelly.NewBundleOperator;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.FormNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author  eh103527
 */
public class ComplexWalkthrough extends JellyTestCase {
    
    String dataMountPath;
    String dlm = "|";
    
    String formName = "TestFrame";
    String resources="resources";
    
    /** Creates a new instance of ComplexWalkthrough */
    public ComplexWalkthrough(String testName) {
        super(testName);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.setName("I18NComplexWalkthrough");
        suite.addTest(new ComplexWalkthrough("testInternationalize")); // NOI18N
        return suite;
    }
    
    public void setUp() {
        try {
            dataMountPath = Utilities.getPath("data", dlm); // NOI18N
            System.out.println(dataMountPath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void tearDown() {
    }
    
    // -------------------------------------------------------------------------
    
    // TEST METHOD
    public void testInternationalize() {
        RepositoryTabOperator tabOper = RepositoryTabOperator.invoke();//explorer.repositoryTab();
        JTreeOperator repoTreeOper = new JTreeOperator(tabOper);
        Action addToProjectAction = new Action(null, toolsMenuItem+dlm+internationalizationMenuItem+dlm+internationalizeMenuItem);
        
        //invoke intern. on form
        Node node1 = new Node(repoTreeOper, dataMountPath+dlm+formName); // NOI18N
        addToProjectAction.perform(node1);
        InternationalizeOperator iop = new InternationalizeOperator();
        //create new properties file in inter. dialog
        iop.clickNew();
        NewBundleOperator nbo = new NewBundleOperator();
        JTreeOperator newfs = new JTreeOperator(nbo);
        node1 = new Node(newfs, dataMountPath+dlm+resources); // NOI18N
        nbo.ok();
        iop.setComment("test comment");
        iop.replace();
        new QueueTool().waitEmpty(500);
        iop.skip();
        new QueueTool().waitEmpty(500);
        iop.info();
        JDialogOperator infod=new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_InfoPanelTitle"));
        JButtonOperator okb=new JButtonOperator(infod, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_OKButton"));
        okb.pushNoBlock();
        new QueueTool().waitEmpty(1100);
        iop.typeKey("hello_button");
        iop.replace();
        new QueueTool().waitEmpty(500);
        iop.close();
        System.out.println("OK");
    }
    
    private static final String toolsMenuItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "CTL_Tools");
    private static final String internationalizationMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "LBL_Internationalization");
    private static final String internationalizeMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_I18nAction");
}
