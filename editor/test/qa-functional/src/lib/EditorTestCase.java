/*
 * EditorTestCase.java
 *
 * Created on 24. srpen 2004, 12:32
 */

package lib;

import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author  Petr Felenda
 */
public class EditorTestCase extends NbTestCase {
    
    /** Default name of project is used if not specified in openProject method. */
    private String defaultProjectName = "editor_test";
    private String defaultSamplePackage = "test";
    private String defaultSampleName = "sample1";

    private static final char treeSeparator = '|';
    private final String defaultPackageNameTreePath = "Source packages"+treeSeparator+"test";
    private final String defaultFileName = "sampel1";
    private String projectName = null;
    private String treeSubPackagePathToFile = null;
    private String fileName = null;
    private final String dialogSaveTitle = "Save";  // I18N
    
    /** Creates a new instance of EditorTestCase */
    public EditorTestCase(String testMethodName) {
        super(testMethodName);
    }
    
    /**
     * Pass the class of the test from which the test method name
     * and sample name and package are determined.
     *
     * <p>
     * For a test class named "ClsNameTest" the "testClsName()" test method
     * will be executed and "ClsNameSample" sample file will be used.
     *
     * @param testClass class of the test to be executed.
     */
    public EditorTestCase(Class testClass) {
        this(splitClassName(testClass.getName()));
    }
    
    private EditorTestCase(String[] pkgAndCls) {
         // For "ClsName" it produces "testClsName"
        this("test" + pkgAndCls[1]);
        
        this.defaultSamplePackage = pkgAndCls[0];
         // For "ClsName" it produces "ClsNameSample"
        this.defaultSampleName = pkgAndCls[1] + "Sample";
    }

    /**
     * Split class full name into package name and class name.
     *
     * @param full name of the class
     * @return array containing package name and the class name.
     */
    public static String[] splitClassName(String classFullName) {
        int lastDotIndex = classFullName.lastIndexOf('.');
        return new String[] {
            (lastDotIndex >= 0) ? classFullName.substring(0, lastDotIndex) : "", // pkg name
            classFullName.substring(lastDotIndex + 1) // class name
        };
    }
    
    /** Open project. Before opening the project is checked opened projects.
     * @param projectName is name of the project stored in .../editor/test/qa-functional/data/ directory.
     */
    public void openProject(String projectName) {
        this.projectName = projectName;
        File projectPath = new File(this.getDataDir() + "/projects", projectName);
        log("data dir = "+this.getDataDir().toString());
        
        /* 1. check if project is open  */
        ProjectsTabOperator pto = new ProjectsTabOperator();
        pto.invoke();
        boolean isOpen = true;
        try {
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 2000); 
            ProjectRootNode prn = pto.getProjectRootNode(projectName);
        } catch (TimeoutExpiredException ex) {
            ex.printStackTrace();
            isOpen = false;
            
            // This excpeiton is ok, project is not open;
        }
        
        if ( isOpen ) {
            log("Project is open!");
            return;
        }
        
        
        /* 2. open project */
        Object prj= ProjectSupport.openProject(projectPath);
        
    }
    
    protected final String getDefaultProjectName() {
        return defaultProjectName;
    }
    
    protected void setDefaultProjectName(String defaultProjectName) {
        this.defaultProjectName = defaultProjectName;
    }
    
    protected void openDefaultProject() {
        openProject(getDefaultProjectName());
    }
    
    /**
     * Close the default project.
     */
    protected void closeDefaultProject() {
        closeProject(getDefaultProjectName());
    }
    
    protected void closeProject(String projectName) {
        ProjectSupport.closeProject(projectName);
    }
    
    
    /** Open file in open project
     *  @param treeSubPath e.g. "Source Packages|test","sample1" */
    public void openFile(String treeSubPackagePathToFile, String fileName) {
        this.treeSubPackagePathToFile = treeSubPackagePathToFile;
        ProjectsTabOperator pto = new ProjectsTabOperator();
        pto.invoke();
        ProjectRootNode prn = pto.getProjectRootNode(projectName);
        prn.select();
        Node node = new Node(prn,treeSubPackagePathToFile+treeSeparator+fileName);
        node.performPopupAction("Open");
    }
    
    /** Open the default file in open project */
    public void openFile() {
        openFile(defaultPackageNameTreePath,defaultFileName);
    }
    
    /** Close file in open project.
     */
    public void closeFile() {
        try {
           EditorWindowOperator editorWindow = new EditorWindowOperator(fileName);
           //find editor
           EditorOperator editor = editorWindow.selectPage(fileName);
           editor.close();
        } catch ( TimeoutExpiredException ex) {
            log(ex.getMessage());
            log("Can't close the file");
        }
    }
    
    /** Close file in open project.
     */
    public void closeFileWithSave() {
        try {
           EditorWindowOperator editorWindow = new EditorWindowOperator(fileName);
           //find editor
           EditorOperator editor = editorWindow.selectPage(fileName);
           editor.save();
           editor.close();
        } catch ( TimeoutExpiredException ex) {
            log(ex.getMessage());
            log("Can't close the file");
        }
    }
    
    
    /** Close file in open project.
     */
    public void closeFileWithDiscard() {
        try {
           EditorWindowOperator editorWindow = new EditorWindowOperator(fileName);
           //find editor
           EditorOperator editor = editorWindow.selectPage(fileName);
           editor.closeDiscard();
        } catch ( TimeoutExpiredException ex) {
            log(ex.getMessage());
            log("Can't close the file");
        }
    }
    
    /** Close dialog with added title
     * @param title dialog title */
    public void closeDialog(String title) {
        NbDialogOperator dialog = new NbDialogOperator(title);
        dialog.closeByButton();
    }
    
    /**
     * Write the text of the passed document to the ref file
     * and compare the created .ref file with the golden file.
     * <br>
     * If the two files differ the test fails and generates the diff file.
     *
     * @param testDoc document to be written to the .ref file and compared.
     */
    protected void compareReferenceFiles(Document testDoc) {
        try {
            ref(testDoc.getText(0, testDoc.getLength()));
            compareReferenceFiles();
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail();
        }
    }

    /**
     * Open a source file located in the "Source packages" in the editor.
     *
     * @param dir directory path with "|" separator.
     * @param srcName source name without suffix.
     */
    protected void openSourceFile(String dir, String srcName) {
        openFile("Source packages|" + dir, srcName);
    }
    
    protected final String getDefaultSamplePackage() {
        return defaultSamplePackage;
    }
    
    protected final String getDefaultSampleName() {
        return defaultSampleName;
    }

    protected void openDefaultSampleFile() {
        openSourceFile(defaultSamplePackage, defaultSampleName);
    }

    protected EditorOperator getDefaultSampleEditorOperator() {
        EditorWindowOperator editorWindow = new EditorWindowOperator(defaultSampleName);
        return editorWindow.selectPage(defaultSampleName);
    }
    
}
