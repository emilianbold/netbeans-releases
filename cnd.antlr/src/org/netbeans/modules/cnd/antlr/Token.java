package org.netbeans.modules.cnd.antlr;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

/** A token is minimally a token type.  Subclasses can add the text matched
 *  for the token and line info.
 */
public interface Token extends Cloneable {
    // constants
    int MIN_USER_TYPE = 4;
    int NULL_TREE_LOOKAHEAD = 3;
    int INVALID_TYPE = 0;
    int EOF_TYPE = 1;
    int SKIP = -1;

    public int getColumn();
    public void setColumn(int c);

    public int getLine();
    public void setLine(int l);

    public String getFilename();
    public void setFilename(String name);
    
    public String getText();
    public void setText(String t);

    public int getType();
    public void setType(int t);
}
