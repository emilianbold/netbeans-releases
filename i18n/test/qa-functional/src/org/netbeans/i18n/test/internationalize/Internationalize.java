/*
 * Internationalize.java
 *
 * Created on October 29, 2003, 4:06 PM
 */

package org.netbeans.i18n.test.internationalize;

import java.io.File;
import java.util.Enumeration;
import org.netbeans.i18n.jelly.InternationalizeOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.FilesystemNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 *
 * @author  eh103527
 */
public class Internationalize extends JellyTestCase {
    
    String TEST_PACKAGE="data";
    String FILE_NAME="TestFrame";
    
    /**
     * Constructor - Creates new instance of this class
     */
    public Internationalize() {
        super("testInternationalize");
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Internationalize());
        return suite;
    }
    
    /**
     * This method contains body of test
     * @return void
     */
    public void testInternationalize() throws Exception {
        System.out.println();
        System.out.println("===================================================================================");
        System.out.println("=   Test :  Internationalize - complex walkthrough                                =");
        System.out.println("=   See testspec of i18n module:                                                  =");
        System.out.println("=   http://beetle.czech/modules/i18n/                                             =");
        System.out.println("===================================================================================");
        
        FileSystem[] fileSystems = Repository.getDefault().toArray();
        String fileSystemName = null;
        
        for ( int ii = 0; ii < fileSystems.length; ii++ ) {
            FileObject file = fileSystems[ii].findResource(TEST_PACKAGE);
            if (file != null) {
                System.out.println("> Used Filesystem = "+fileSystems[ii].getDisplayName());
                fileSystemName = fileSystems[ii].getDisplayName();
            }
        }
        
        if ( fileSystemName == null )
            throw new Exception("Must be mounted .../"+TEST_PACKAGE+" repository in explorer !");
        
        String path = fileSystemName.concat("|"+TEST_PACKAGE+"|"+FILE_NAME);
        new Action("Tools","Tools|Internationalization|Internationalize").performPopup(new FilesystemNode(path));
        new EventTool().waitNoEvent(2500);
        InternationalizeOperator io = new InternationalizeOperator();

    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        NbTestSuite nbTestSuite = new NbTestSuite();
        nbTestSuite.addTest(new Internationalize());
        junit.textui.TestRunner.run(nbTestSuite);
    }
}
