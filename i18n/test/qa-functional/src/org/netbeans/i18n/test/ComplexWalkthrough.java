/*
 * ComplexWalkthrough.java
 *
 * Created on January 27, 2004, 2:39 PM
 */

package org.netbeans.i18n.test;

import java.io.File;
import java.net.URL;
import javax.swing.SwingUtilities;
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
import org.openide.filesystems.FileObject;

/**
 *
 * @author  eh103527
 */
public class ComplexWalkthrough extends JellyTestCase {
    
    String dataMountPath;
    String dlm = "|";
    
    String formName = "TestFrame";
    String resources="resources";
    String name;
    
    FileObject frameFO,propertiesFO, goldenFolder;
    
    /** Creates a new instance of ComplexWalkthrough */
    public ComplexWalkthrough(String testName) {
        super(testName);
        name=testName;
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
            frameFO=Utilities.findFileObject("data", formName, "java");
            propertiesFO=Utilities.findFileObject("data"+"."+resources, "properties", "properties");
            goldenFolder=Utilities.findFileObject("data.goldenfiles.ComplexWalkthrough",null,null);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void tearDown() {
        compareReferenceFiles();
    }
    
    // -------------------------------------------------------------------------
    
    // TEST METHOD
    public void testInternationalize() {
        RepositoryTabOperator tabOper = RepositoryTabOperator.invoke();//explorer.repositoryTab();
        JTreeOperator repoTreeOper = new JTreeOperator(tabOper);
        Action internationalize = new Action(null, toolsMenuItem+dlm+internationalizationMenuItem+dlm+internationalizeMenuItem);
        
        //invoke intern. on java source with form
        Node node1 = new Node(repoTreeOper, dataMountPath+dlm+formName); // NOI18N
        internationalize.perform(node1);
        InternationalizeOperator iop = new InternationalizeOperator();
        
        //create new properties file in inter. dialog
        iop.clickNew();
        NewBundleOperator nbo = new NewBundleOperator();
        JTreeOperator newfs = new JTreeOperator(nbo);
        node1 = new Node(newfs, newfs.findPath(dataMountPath+dlm+resources,dlm)); // NOI18N
        node1.select();
        System.out.println("try to select: "+node1.getPath());
        nbo.ok();
        new QueueTool().waitEmpty(200);
        
        //set comment, press replace
        iop.makeComponentVisible();
        iop.setComment("test comment");
        iop.replace();
        
        //skip
        iop.skip();
        
        //info
        iop.info();
        JDialogOperator infod=new JDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_InfoPanelTitle"));
        infod.makeComponentVisible();
        JButtonOperator okb=new JButtonOperator(infod, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_OKButton"));
        okb.pushNoBlock();
        new QueueTool().waitEmpty(200);
        
        iop.typeKey("hello_button");
        iop.replace();
        iop.close();
        
        EditorOperator eop=new EditorOperator(formName);
        eop.close(true);
    }
    
    public void compareReferenceFiles() {
        try {
            FileObject frameGolden = goldenFolder.getFileObject(name+".pass");
            FileObject propertiesGolden = goldenFolder.getFileObject(name+"properties.pass");
            
            File frameGF=convertURL(frameGolden.getURL());
            File propertiesGF=convertURL(propertiesGolden.getURL());
            File frameF=convertURL(frameFO.getURL());
            File propertiesF=convertURL(propertiesFO.getURL());
            
            System.out.println(frameGF);
            System.out.println(propertiesGF);
            assertFile("Golden frame file differs.",frameGF,frameF,null,new LineDiff(false));
            assertFile("Golden properties file differs.",propertiesGF,propertiesF,null,new LineDiff(false));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    protected File convertURL(URL url) {
        try {
            String path=java.net.URLDecoder.decode(url.getPath(),"UTF-8");
            path.replaceAll("/",((File.separatorChar == '\\')?File.separator+File.separator:File.separator));
            return new File(path);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    private static final String toolsMenuItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "CTL_Tools");
    private static final String internationalizationMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "LBL_Internationalization");
    private static final String internationalizeMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_I18nAction");
}
