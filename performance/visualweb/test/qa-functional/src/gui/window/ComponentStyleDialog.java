

package gui.window;

import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Administrator
 */
public class ComponentStyleDialog extends JSFComponentOptionsDialog {
    
    private PropertySheetOperator pto;
    private Property property;
    private JDialogOperator styleDialog;
    private String componentID;
    /**
     * 
     * @param testName 
     */
    public ComponentStyleDialog(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=3000;
        categoryName = "Basic"; // NOI18N
        componentName = "Table"; // NOI18N 
        addPoint = new java.awt.Point(50,50);        
    }
    /**
     * 
     * @param testName 
     * @param performanceDataName 
     */
    public ComponentStyleDialog(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=3000;
        categoryName = "Basic"; // NOI18N
        componentName = "Button"; // NOI18N 
        addPoint = new java.awt.Point(50,50);            
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ComponentStyleDialog("testButtonStyleDialog","Button Style Dialog Open test"));
        suite.addTest(new ComponentStyleDialog("testTableStyleDialog","Table Style Dialog Open test"));        
        suite.addTest(new ComponentStyleDialog("testLisbBoxStyleDialog","Listbox Style Dialog Open test"));
        return suite;        
    }    
    public void testButtonStyleDialog() {
        categoryName = "Basic"; // NOI18N
        componentName = "Button"; // NOI18N 
        doMeasurement();
    }
    public void testTableStyleDialog() {
        categoryName = "Basic"; // NOI18N
        componentName = "Table"; // NOI18N 
        doMeasurement();        
    }
    public void testLisbBoxStyleDialog() {
        categoryName = "Basic"; // NOI18N
        componentName = "Listbox"; // NOI18N 
        doMeasurement();        
    }
    public void initialize() {
        log("::initialize");
        super.initialize();
        pto =  new PropertySheetOperator("Page1").invoke(); 
        surface.clickOnSurface(addPoint.x+5, addPoint.y+5);
        componentID = new Property(pto,"id").getValue();        
        property = new Property(pto,"style"); // NOI18N             
          
    }
    public void prepare() {
        log(":: prepare");

    }
    public ComponentOperator open() {
        log(":: open");
        property.openEditor();
        styleDialog = new JDialogOperator(componentID);
        return null;
    }
    public void close() {
        log(":: close");
        styleDialog.close();
        super.close();
    }
    protected void shutdown() {
        super.shutdown();
        pto.close();
    }
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }    
}
