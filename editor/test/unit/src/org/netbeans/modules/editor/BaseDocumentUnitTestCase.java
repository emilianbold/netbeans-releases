
package org.netbeans.modules.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.junit.NbTestCase;

/**
 * Support for creation of unit tests working with document.
 *
 * @author Miloslav Metelka
 */
public class BaseDocumentUnitTestCase extends NbTestCase {
    
    private EditorKit editorKit;
    
    private String documentText;
    
    private BaseDocument doc;
    
    private int caretOffset = -1;
    
    public BaseDocumentUnitTestCase(String testMethodName) {
        super(testMethodName);
        
    }
    
    /**
     * Set the text that the document obtained by {@link #getDocument()}
     * would be loaded with.
     *
     * <p>
     * The text is parsed and the first occurrence of "|" sets
     * the caret offset which is available by {@link #getCaretOffset()}.
     * <br>
     * The "|" itself is removed from the document text and subsequent
     * calls to {@link #getDocumentText()} do not contain it.
     */
    protected void setDocumentText(String documentText) {
        // [TODO] a more elaborated support could be done e.g. "||" to a real "|" etc.
        caretOffset = documentText.indexOf('|');
        if (caretOffset != -1) {
            documentText = documentText.substring(0, caretOffset)
                + documentText.substring(caretOffset + 1);
        }
        
        this.documentText = documentText;
    }

    /**
     * Get the text that the document obtained by {@link #getDocument()}
     * would be loaded with.
     *
     * @return text to be loaded into the document or null if nothing
     *  should be loaded.
     */
    protected final String getDocumentText() {
        return documentText;
    }
    
    /**
     * Return caret offset based on the scanning of the text passed
     * to {@link #setDocumentText(String)}.
     *
     * @return valid caret offset or -1 if no caret offset was determined
     *  in the document text.
     */
    protected final int getCaretOffset() {
        return caretOffset;
    }
    
    /**
     * Get the document that the test should work with.
     * <br>
     * If the document does not exist yet it will be created
     * and loaded with the text from {@link #getDocumentText()}.
     */
    protected synchronized BaseDocument getDocument() {
        if (doc == null) {
            doc = createAndLoadDocument();
        }
        return doc;
    }
    
    /**
     * Create editor kit instance to be returned
     * by {@link #getEditorKit()}.
     * <br>
     * The returned editor kit should return
     * <code>BaseDocument</code> instances
     * from its {@link javax.swing.text.EditorKit.createDefaultDocument()}.
     */
    protected EditorKit createEditorKit() {
        return BaseKit.getKit(BaseKit.class);
    }
    
    /**
     * Get the kit that should be used
     * when creating the <code>BaseDocument</code>
     * instance.
     * <br>
     * The editor kit instance is created in {@link #createEditorKit()}.
     *
     * @return editor kit instance.
     */
    public final EditorKit getEditorKit() {
        if (editorKit == null) {
            editorKit = createEditorKit();
        }
        return editorKit;
    }
    
    private BaseDocument createAndLoadDocument() {
        BaseDocument bd = (BaseDocument)getEditorKit().createDefaultDocument();

        if (documentText != null) {
            try {
                bd.insertString(0, documentText, null);
            } catch (BadLocationException e) {
                e.printStackTrace(getLog());
                fail();
            }
        }
        return bd;
    }

}
