/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.properties;

import java.beans.*;
import java.io.*;
import javax.swing.text.BadLocationException;

import org.openide.nodes.Node;
import org.openide.text.PositionBounds;

/** Base class for representations of elements in the
* properties files.
*
* @author Petr Jiricka
*/
public abstract class Element extends Object
implements Serializable {
    
  /** Property change support */
  private transient PropertyChangeSupport support;

  /** Position of the begin and the end of the element. */
  protected PositionBounds bounds;

  /** Create a new element. */
  protected Element(PositionBounds bounds) {
    this.bounds = bounds;
  }
  
  public PositionBounds getBounds() {
    return bounds;
  }

  /** Updates the element fields. This method is called after reparsing.
  * @param bounds the carrier of new information.
  */
  void update(Element elem) {
    this.bounds = elem.bounds;
  }

  /** Fires property change event.
  * @param name property name
  * @param o old value
  * @param n new value
  */
  protected final void firePropertyChange(String name, Object o, Object n) {
    if (support != null) {
      support.firePropertyChange (name, o, n);
    }
  }

  /** Adds property listener */
  public synchronized void addPropertyChangeListener (PropertyChangeListener l) {
    if (support == null) {
      synchronized (this) {
        // new test under synchronized block
        if (support == null) {
          support = new PropertyChangeSupport (this);
        }
      }
    }
    support.addPropertyChangeListener (l);
  }

  /** Removes property listener */
  public void removePropertyChangeListener (PropertyChangeListener l) {
    if (support != null) {
      support.removePropertyChangeListener (l);
    }
  }

  /** Prints this element (and all its subelements)
  *   by calling <code>bounds.setText(...)</code>
  */
  public void print() {
    try {
      bounds.setText(printString());
    }
    catch (BadLocationException e) {
      // PENDING
    }  
    catch (IOException e) {
      // PENDING
    }  
  }


  /** Get a string representation of the element for printing.
  * @return the string
  */
  public abstract String printString();


  /** Get a value string of the element.
  * @return the string
  */
  public String toString() {
    return (bounds == null) ? "(no bounds)" : "(" + bounds.getBegin().getOffset() + ", " + bounds.getEnd().getOffset() + ")";
  }
  
  // -------------------- INNER CLASSES ----------------------
              
  /** General class for basic elements, which contain value directly */
  public static abstract class Basic extends Element {
              
    /** Parsed value of the element */          
    protected String value;
    
    /** Create a new basic element. */
    protected Basic(PositionBounds bounds, String value) {
      super(bounds);
      this.value = value;
    }
    
    /** Updates the element fields. This method is called after reparsing.
    * @param bounds the carrier of new information.
    */
    void update(Element elem) {
      super.update(elem);
      this.value = ((Basic)elem).value;
    }

    /** Get a string representation of the element.
    * @return the string + bounds
    */
    public String toString() {
      return value + "   " + super.toString();
    }
             
    /** Get a value of the element.
    * @return the string
    */
    public String getValue() {
      return value;
    }          
    
    /** Sets the value. Does not check if the value has changed. */
    public void setValue(String value) {
      this.value = value;
      this.print();
    }         
             
  }


  /** Class for key elements */
  public static class KeyElem extends Basic {
              
    /** Create a new key element. */
    protected KeyElem(PositionBounds bounds, String value) {
      super(bounds, value);
    }
    
    /** Get a string representation of the key for printing. Treats the '=' sign as a part of the key
    * @return the string
    */
    public String printString() {
      return UtilConvert.saveConvert(value) + "=";
    }
             
  }

  /** Class for value elements */
  public static class ValueElem extends Basic {
              
    /** Create a new value element. */
    protected ValueElem(PositionBounds bounds, String value) {
      super(bounds, value);
    }
    
    /** Get a string representation of the value for printing. Appends end of the line after the value.
    * @return the string
    */
    public String printString() {
      return UtilConvert.saveConvert(value) + "\n";
    }
             
  }

  /** Class for comment elements. <code>null</code> values of the string are legal and indicate that the comment is empty. */
  public static class CommentElem extends Basic {
              
    /** Create a new comment element. */
    protected CommentElem(PositionBounds bounds, String value) {
      super(bounds, value);
    }
    
    /** Get a string representation of the comment for printing. Makes sure every non-empty line starts with a # and
    * that the last line is terminated with an end of line marker.
    * @return the string
    */
    public String printString() {
      if (value == null || value.length() == 0)
        return "";
      else {                   
        // insert #s at the beginning of the lines which contain non-blank characters
        // holds the last position where we might have to insert a # if this line contains non-blanks
        int candidate = 0;
        StringBuffer sb = new StringBuffer(value);
        for (int i=0; i<sb.length(); ) {
          char aChar = sb.charAt(i++);
          // new line
          if (aChar == '\n') {
            candidate = i;
          }  
          else {    
            if ((candidate != -1) && (UtilConvert.whiteSpaceChars.indexOf(aChar) == -1)) {
              // nonempty symbol
              if ((aChar != '#') && (aChar != '!')) {
                // insert a #
                sb.insert(candidate, '#');
                i++;
              }  
              candidate = -1;
            }
          }
        }  
        // append the \n if missing
        if (sb.charAt(sb.length() - 1) != '\n')
          sb.append('\n');
        return sb.toString();
      }  
    }
  }
             

  /** Class properties file elements, each of them contains a comment (preceding the property),
  *   a key and a value
  */
  public static class ItemElem extends Element implements Node.Cookie {
                  
    KeyElem     key;
    ValueElem   value;
    CommentElem comment;
    boolean     justComment; // comment after the last property in a prop. file 
                             // is represented by a ItemElem with justComment set to true

    /** Parent of this element - active element has a non-null parent. */ 
    private PropertiesStructure parent;
     
    /** Name of the Key property */
    public static final String PROP_ITEM_KEY     = "key";
    /** Name of the Value property */
    public static final String PROP_ITEM_VALUE   = "value";
    /** Name of the Comment property */
    public static final String PROP_ITEM_COMMENT = "comment";
    
    /** Create a new basic element. <code>key</code> and <code>value</code> may be null, 
    *  then <code>justComment</code> will be set to true.
    */
    protected ItemElem(PositionBounds bounds, KeyElem key, ValueElem value, CommentElem comment) {
      super(bounds);
      this.key     = key;
      this.value   = value;
      this.comment = comment;
      justComment  = (key == null);
    }
                          
    /** Sets the parent of this element. */
    void setParent(PropertiesStructure ps) {
      parent = ps;
    }

    /** Returns parent if not null */
    public PropertiesStructure getParent() {
      if (parent == null)
        throw new InternalError();
      return parent;  
    }
                          
    /** Get a value string of the element.
    * @return the string
    */
    public String toString() {     
      return comment.toString() + '\n' +
             ((key   == null) ? "" : key.toString()) + '\n' +
             ((value == null) ? "" : value.toString()) + '\n';
    }             
                 
    /** Returns the key element for this item. */             
    public KeyElem getKeyElem() {
      return key;
    }
             
    /** Returns the value element for this item. */             
    public ValueElem getValueElem() {
      return value;
    }

    /** Returns the comment element for this item. */             
    public CommentElem getCommentElem() {
      return comment;
    }

    /** Updates the element fields. This method is called after reparsing.
    * @param bounds the carrier of new information.
    */
    void update(Element elem) {
      super.update(elem);
      if (this.key == null)
        this.key     = ((ItemElem)elem).key;
      else 
        this.key.update(((ItemElem)elem).key);
        
      if (this.value == null)
        this.value   = ((ItemElem)elem).value;
      else   
        this.value.update(((ItemElem)elem).value);
        
      this.comment.update(((ItemElem)elem).comment);
      this.justComment = ((ItemElem)elem).justComment;
    }

    /** Get a string representation of the element for printing.
    * @return the string
    */
    public String printString() {
      return comment.printString() + 
             ((key   == null) ? "" : key.printString()) +
             ((value == null) ? "" : value.printString());
    }
                            
    /** Get a key by which to identify this record */                        
    public String getKey() {
      return (key == null) ? "" : key.getValue();
    }
    
    /** Set the key for this item 
    *  @param key the new key
    */                        
    public void setKey(String newKey) {
      String oldKey = key.getValue();
      if (!oldKey.equals(newKey)) {
        key.setValue(newKey);
        getParent().itemKeyChanged(oldKey, this);
        this.firePropertyChange(PROP_ITEM_KEY, oldKey, newKey);
      }  
    }
    
    /** Get the value of this item */                        
    public String getValue() {
      return (value == null) ? "" : value.getValue();
    }
    
    /** Set the value of this item 
    *  @param newValue the new value
    */                        
    public void setValue(String newValue) {
      String oldValue = value.getValue();
      if (!oldValue.equals(newValue)) {
        value.setValue(newValue);
        getParent().itemChanged(this);
        this.firePropertyChange(PROP_ITEM_VALUE, oldValue, newValue);
      }  
    }

    /** Get the comment for this item */                        
    public String getComment() {
      return (comment == null) ? "" : comment.getValue();
    }
    
    /** Set the comment for this item 
    *  @param newComment the new comment
    */                        
    public void setComment(String newComment) {
      String oldComment = comment.getValue();
      if (!oldComment.equals(newComment)) {
        comment.setValue(newComment);
        getParent().itemChanged(this);
        this.firePropertyChange(PROP_ITEM_COMMENT, oldComment, newComment);
      }  
    }
    
    /** Checks for equality of two ItemElem-s */
    public boolean equals(Object item) {
      if (item == null)
        return false;
      ItemElem ie = (ItemElem)item;
      if (getKey()    .equals(ie.getKey()    ) &&  
          getValue()  .equals(ie.getValue()  ) &&
          getComment().equals(ie.getComment()))
        return true;  
      return false;
    }

  } // end of inner class ItemElem
}

/*
 * <<Log>>
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         8/18/99  Petr Jiricka    Nothing
 *  8    Gandalf   1.7         8/9/99   Petr Jiricka    Removed debug prints
 *  7    Gandalf   1.6         6/16/99  Petr Jiricka    
 *  6    Gandalf   1.5         6/10/99  Petr Jiricka    
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         6/6/99   Petr Jiricka    
 *  3    Gandalf   1.2         5/14/99  Petr Jiricka    
 *  2    Gandalf   1.1         5/13/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/12/99  Petr Jiricka    
 * $
 */
