
package org.netbeans.modules.editor.java;

import javax.swing.text.EditorKit;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.BaseDocumentUnitTestCase;

/**
 * Testing support creating document instances for JavaKit.
 *
 * @author Miloslav Metelka
 */
public class JavaBaseDocumentUnitTestCase extends BaseDocumentUnitTestCase {
    
    public JavaBaseDocumentUnitTestCase(String testMethodName) {
        super(testMethodName);
    }
    
    protected EditorKit createEditorKit() {
        return new JavaKit(true); // Create compatible
    }

}
