
package org.netbeans.api.editor.document;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;

/**
 * Represents a line-oriented Document. The Document implementation
 * is able to find Element for paragraph (line) that contains the specified
 * position and also transform the position to a visual column index (takes
 * tab expansion settings into account).
 *
 * @author sdedic
 */
public interface LineDocument extends Document {
    /**
     * Returns an element that represent a line which contains position 'pos'.
     * @param pos position to find
     * @return line represented as Element
     */
    public Element getParagraphElement(int pos);
    
    
    /**
     * Creates s Swing position that maintains a bias to the offset it 
     * is anchored at. 
     * @param offset offset for the position
     * @param bias backward/forward bias
     * @return Position instance
     * @throws BadLocationException if offset points outside document content
     */
    public Position createPosition(int offset, Position.Bias bias) throws BadLocationException;

}
