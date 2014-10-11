
package org.netbeans.modules.editor.document.implspi;

/**
 * Provides character classification for the document.
 * 
 * @author sdedic
 */
public interface CharClassifier {
    public boolean isIdentifierPart(char ch);
    public boolean isWhitespace(char ch);
}
