/*
 * newSimpleNbJUnitTest.java
 * NetBeans JUnit based test
 *
 * Created on June 22, 2005, 11:28 AM
 */

package org.netbeans.qa.form;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import junit.framework.*;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.junit.*;

/**
 *
 * @author Marek G.
 */
public class OpenForm extends JellyTestCase {
    
    public OpenForm(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(OpenForm.class);
        return suite;
    }
    
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    private List getJavaFormList() {
        String dataDir = getDataDir().getAbsolutePath();
        String fileSeparator =  System.getProperty("file.separator");
        
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        
        List list = new ArrayList();
        
        try {
//            System.out.println("PATH : " + dataDir + fileSeparator + "FormList.dat");
            fileReader = new FileReader(dataDir + fileSeparator + "FormList.dat");
            bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null){
                
                System.out.println("File: " + line);
                list.add(line);
                
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex){
            ex.printStackTrace();
        } finally {
            try {
                if (bufferedReader!= null) {
                    bufferedReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return list;
    }
    
    public void testOpenForm() {
        
//        System.out.println("XXXXXXXXXXXXX");
//        System.out.println("xtest.data          = " + System.getProperty("xtest.data"));
//        System.out.println("getDataDir          = " + getDataDir());
//        System.out.println("getDataDirPath      = " + getWorkDirPath());
//        System.out.println("xtest.module        = " + System.getProperty("xtest.module"));
//        System.out.println("system.dir          = " + System.getProperty("system.dir"));
//        System.out.println("xtest.workdir       = " + System.getProperty("xtest.workdir"));
//        System.out.println("XXXXXXXXXXXXX");
        
        String workdirpath = getWorkDirPath();
        List list = getJavaFormList();
//        String prePath = "/space/cvs-netbeans/form/src";
        String prePath = workdirpath.substring(0,workdirpath.indexOf("/form"));
//        System.out.println("prePath : " + prePath);
        String formPath = null;
        String fileSeparator = System.getProperty("file.separator");
        String lineSeparator = System.getProperty("line.separator");
        
        String dataDir = getDataDir().getAbsolutePath();
        FileReader fileReader = null;
        FileWriter fileWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        
        try {
            fileReader = new FileReader(dataDir + fileSeparator + "OpenForm.template");
            bufferedReader = new BufferedReader(fileReader);
            fileWriter = new FileWriter(workdirpath + fileSeparator +  "OpenForm.html");
            bufferedWriter = new BufferedWriter(fileWriter);
            
            String line = null;
            while (!(line = bufferedReader.readLine()).equals("<PUT CONTENT HERE>")) {
//                System.out.println("LINE : " + line + " : ");
                bufferedWriter.write(line + lineSeparator);
            }
            
            int lastindex         = 0;
            String module         = null;
            String directory      = null;
            String filename       = null;
            String filenamenoext  = null;
            String fullPath       = null;
            ActionNoBlock actionNoBlock;
            JFileChooserOperator jFileChooserOperator;
            FormDesignerOperator formDesignerOperator = null;
            for (ListIterator listIterator = list.listIterator(); listIterator.hasNext(); ) {
                formPath = (String) listIterator.next();
                fullPath = new StringBuffer(prePath).append(fileSeparator).append(formPath).toString();
                lastindex = fullPath.lastIndexOf(fileSeparator);
                module = formPath.substring(0, formPath.indexOf(fileSeparator));
                directory = fullPath.substring(0,lastindex);
                filename  = fullPath.substring(lastindex + 1);
                filenamenoext = filename.substring(0, filename.lastIndexOf("."));
                actionNoBlock = new ActionNoBlock("File|Open File...", null);
                actionNoBlock.perform();
                jFileChooserOperator = new JFileChooserOperator();
                System.out.println("directory " + directory);
                System.out.println("filename " + filename);
                jFileChooserOperator.setCurrentDirectory(new File(directory));
                jFileChooserOperator.selectFile(filename);
                jFileChooserOperator.approve();
                formDesignerOperator = new FormDesignerOperator(filenamenoext);
                org.netbeans.jemmy.util.PNGEncoder.captureScreen(formDesignerOperator.fakePane().getSource(), workdirpath + fileSeparator + filenamenoext + ".png");
                formDesignerOperator.editor().closeDiscardAll(); // strange, but have to do it !!!
                
                bufferedWriter.write("<TABLE width=\"98%\" cellspacing=\"2\" cellpadding=\"5\" border=\"0\">");
                bufferedWriter.write("<TR bgcolor=\"#A6CAF0\" align=\"center\">");
                bufferedWriter.write("<TD ALIGN=\"LEFT\" colspan=\"1\">.../" + directory.substring(directory.indexOf(module)) + "/<B>" + filenamenoext + "</B></TD>");
                bufferedWriter.write("</TR>");
                bufferedWriter.write("<TR class=\"pass\">");
                bufferedWriter.write("<TD ALIGN=\"CENTER\"><IMG BORDER=\"2\" TITLE=\"" + filenamenoext + "\" SRC=\"" + filenamenoext + ".png" + "\"></IMG></TD>");
                bufferedWriter.write("</TR>");
                bufferedWriter.write("</TABLE>");
                bufferedWriter.write("<HR>");
                
            }
            
            while ((line = bufferedReader.readLine()) != null){
//                System.out.println("XLINE : " + line + " : ");
                bufferedWriter.write(line + lineSeparator);
            }
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex){
            ex.printStackTrace();
        } finally {
            try {
                if (bufferedReader!= null) {
                    bufferedReader.close();
                }
                if (bufferedWriter!= null) {
                    bufferedWriter.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        assertTrue("Look at the work directory for particular form's screenshots", true);
    }
    
    
    
}
