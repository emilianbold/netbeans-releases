package popup_menus;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import lib.EditorTestCase;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 * Test behavior of main menus - Edit, View
 * @author Martin Roskanin
 */
  public class MainMenuTest extends MenuTest {

    String xmlFile =  "testMainMenu.xml";      
    
    /** Creates a new instance of Main */
    public MainMenuTest(String testMethodName) {
        super(testMethodName);
    }


    public void testMainMenu(){
        openDefaultProject();
        openDefaultSampleFile();
        try {

            EditorOperator editor = getDefaultSampleEditorOperator();
            JTextComponentOperator text = new JTextComponentOperator(editor);
            final JTextComponent target = (JTextComponent)text.getSource();

            
            boolean lineNumberVisibleSetting = SettingsUtil.getBoolean(org.netbeans.editor.Utilities.getKitClass(target), 
                                           SettingsNames.LINE_NUMBER_VISIBLE,
                                           SettingsDefaults.defaultLineNumberVisible);
            
            //enable line number
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            txtOper.pushKey(KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_S);
            
            ValueResolver resolver = new ValueResolver(){
                public Object getValue(){
                    return SettingsUtil.getValue(org.netbeans.editor.Utilities.getKitClass(target),
                            SettingsNames.LINE_NUMBER_VISIBLE,
                            Boolean.FALSE);
                }
            };
         
            waitMaxMilisForValue(2000, resolver, Boolean.TRUE);
            
            lineNumberVisibleSetting = SettingsUtil.getBoolean(org.netbeans.editor.Utilities.getKitClass(target), 
                                           SettingsNames.LINE_NUMBER_VISIBLE,
                                           SettingsDefaults.defaultLineNumberVisible);

            if (lineNumberVisibleSetting == false){
                log("XML editor set line number fails:"+org.netbeans.editor.Utilities.getKitClass(target));
            }
            
            assert lineNumberVisibleSetting == true;

            openSourceFile(getDefaultSamplePackage(), xmlFile);
            
            EditorWindowOperator editorWindow = new EditorWindowOperator(xmlFile);
            EditorOperator editorXML = editorWindow.selectPage(xmlFile);
            JTextComponentOperator textXML = new JTextComponentOperator(editorXML);
            final JTextComponent targetXML = (JTextComponent)textXML.getSource();

            //enable line number
            JEditorPaneOperator txtOperXML = editorXML.txtEditorPane();
            txtOperXML.pushKey(KeyEvent.VK_V, KeyEvent.ALT_DOWN_MASK);
            txtOperXML.pushKey(KeyEvent.VK_S);

            ValueResolver resolverXML = new ValueResolver(){
                public Object getValue(){
                    return SettingsUtil.getValue(org.netbeans.editor.Utilities.getKitClass(targetXML),
                            SettingsNames.LINE_NUMBER_VISIBLE,
                            Boolean.FALSE);
                }
            };
         
            
            waitMaxMilisForValue(2000, resolverXML, Boolean.TRUE);
            
            lineNumberVisibleSetting = SettingsUtil.getBoolean(org.netbeans.editor.Utilities.getKitClass(targetXML), 
                                           SettingsNames.LINE_NUMBER_VISIBLE,
                                           SettingsDefaults.defaultLineNumberVisible);

            if (lineNumberVisibleSetting == false){
                log("XML editor set line number fails:"+org.netbeans.editor.Utilities.getKitClass(targetXML));
            }
            
            assert lineNumberVisibleSetting == true;

        } finally {
            // now close XML file
            try {
               EditorWindowOperator editorWindow = new EditorWindowOperator(xmlFile);
               //find editor
               EditorOperator editor = editorWindow.selectPage(xmlFile);
               editor.closeDiscard();
            } catch ( TimeoutExpiredException ex) {
                log(ex.getMessage());
                log("Can't close the file:"+xmlFile);
            }

            //and java file
            closeFileWithDiscard();
            
            
        }
    }

    
}
