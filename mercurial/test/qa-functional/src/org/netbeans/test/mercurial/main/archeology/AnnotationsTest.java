/*
 * AnnotationsTest.java
 *
 * Created on June 30, 2006, 2:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.mercurial.main.archeology;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.mercurial.utils.MessageHandler;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author pvcs
 */
public class AnnotationsTest extends JellyTestCase {
    
    public static final String PROJECT_NAME = "JavaApp";
    public PrintStream stream;
    String os_name;
    static Logger log;
    
    /** Creates a new instance of AnnotationsTest */
    public AnnotationsTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.out.println("### "+getName()+" ###");
        if (log == null) {
            log = Logger.getLogger(TestKit.LOGGER_NAME);
            log.setLevel(Level.ALL);
            TestKit.removeHandlers(log);
        } else {
            TestKit.removeHandlers(log);
        }
        
    }
    
    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(AnnotationsTest.class)
                .addTest("testShowAnnotations")
                .enableModules(".*")
                .clusters(".*")
        );
    }
    
    public void testShowAnnotations() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
        try {
            MessageHandler mh = new MessageHandler("Annotating");
            log.addHandler(mh);
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.loadOpenProject(PROJECT_NAME, getDataDir());
            new EventTool().waitNoEvent(3000);
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Mercurial|Show Annotations");
            TestKit.waitText(mh);
            stream.flush();
            stream.close();
            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }
}
