/*
 * EditorTestCase.java
 *
 * Created on 24. srpen 2004, 12:32
 */

package lib;

import java.io.File;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author  Petr Felenda
 */
public class EditorTestCase extends NbTestCase {
    
    private static final int OPENED_PROJECT_ACCESS_TIMEOUT = 1000;
    
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
    
    /**
     * Creates a new instance of EditorTestCase.
     *
     * <p>
     * Initializes default sample file name (package and name)
     * so that {@link #openDefaultSampleFile()} can be used.
     * <br>
     * The rule for naming is the same like for golden files
     * i.e. package corresponds to the class name and the file
     * name corresponds to test method name.
     *
     * @param testMethodName name of the test method
     *  that should be executed.
     */
    public EditorTestCase(String testMethodName) {
        super(testMethodName);
        
        defaultSamplePackage = getClass().getName();
        defaultSampleName = getName();
    }
    
    /**
     * Split class full name into package name and class name.
     *
     * @param full name of the class
     * @return array containing package name and the class name.
     */
/*    public static String[] splitClassName(String classFullName) {
        int lastDotIndex = classFullName.lastIndexOf('.');
        return new String[] {
            (lastDotIndex >= 0) ? classFullName.substring(0, lastDotIndex) : "", // pkg name
            classFullName.substring(lastDotIndex + 1) // class name
        };
    }
 */
    
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
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", OPENED_PROJECT_ACCESS_TIMEOUT); 
            ProjectRootNode prn = pto.getProjectRootNode(projectName);
        } catch (TimeoutExpiredException ex) {
            // This excpeiton is ok, project is not open;
            //ex.printStackTrace();
            isOpen = false;
        }
        
        if ( isOpen ) {
            log("Project is open!");
            return;
        }
        
        
        /* 2. open project */
        Object prj= ProjectSupport.openProject(projectPath);
        
    }
   
    /**
     * Get the default project name to be used
     * in {@link openDefaultProject()}.
     * <br>
     * The default value is "editor_test".
     *
     * @return default project name
     */
    protected final String getDefaultProjectName() {
        return defaultProjectName;
    }
    
    /**
     * Set the default project name to be used
     * in {@link openDefaultProject()}.
     *
     * @param defaultProjectName new default project name.
     */
    protected void setDefaultProjectName(String defaultProjectName) {
        this.defaultProjectName = defaultProjectName;
    }
    
    /**
     * Open default project determined
     * by {@link #getDefaultProjectName()}.
     */
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
        
        // fix of issue #51191
        Node parent = new Node(prn, treeSubPackagePathToFile);
        final String finalFileName = fileName;
        try {
            // wait for max. 30 seconds for the file node to appear
            JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 30000);
            new Waiter(new Waitable() {
                public Object actionProduced(Object parent) {
                    return ((Node)parent).isChildPresent(finalFileName) ? 
                            Boolean.TRUE: null;
                }
                public String getDescription() {
                    return("Waiting for the tree to load.");
                }
            }).waitAction(parent);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }        
        // end of fix of issue #51191
        
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

    /** Method will wait max. <code> maxMiliSeconds </code> miliseconds for the <code> requiredValue </code>
     *  gathered by <code> resolver </code>.
     *
     *  @param maxMiliSeconds maximum time to wait for requiredValue
     *  @param resolver resolver, which is gathering an actual value
     *  @param requiredValue if resolver value equals requiredValue the wait cycle is finished
     *
     *  @return false if the given maxMiliSeconds time elapsed and the requiredValue wasn't obtained
     */
    protected boolean waitMaxMilisForValue(int maxMiliSeconds, ValueResolver resolver, Object requiredValue){
        int time = (int) maxMiliSeconds / 100;
        while (time > 0) {
            Object resolvedValue = resolver.getValue();
            if (requiredValue == null && resolvedValue == null){
                return true;
            }
            if (requiredValue != null && requiredValue.equals(resolvedValue)){
                return true;
            }
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ex) {
                time=0;
            }
            time--;
        }
        return false;
    }
    
    /** Interface for value resolver needed for i.e. waitMaxMilisForValue method.  
     *  For more details, please look at {@link #waitMaxMilisForValue()}.
     */
    public static interface ValueResolver{
        /** Returns checked value */
        Object getValue();
    }

}
