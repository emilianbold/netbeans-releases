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

import com.netbeans.ide.nodes.Node;
import com.netbeans.ide.text.PositionBounds;

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
  PositionBounds bounds;

  /** Create a new element. */
  protected Element(PositionBounds bounds) {
    this.bounds = bounds;
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
      System.out.println("Properties Element - silently caught BadLocationException !");
    }  
    catch (IOException e) {
      System.out.println("Properties Element - silently caught IOException !");
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
    return "(" + bounds.getBegin().getOffset() + ", " + bounds.getEnd().getOffset() + ")";
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
             
    public void setValue(String value) {
      if (!this.value.equals(value)) {
        this.value = value;
        this.print();
      }
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
      if (value == null)
        return "";
      else {                   
        // insert #s at the beginning of the lines which contain non-blank characters
        // holds the last position where we might have to insert a # if this line contains non-blanks
        int candidate = -1;
        StringBuffer sb = new StringBuffer(value);
        for (int i=0; i<sb.length(); ) {
          char aChar = sb.charAt(i++);
          // new line
          if (aChar == '\n') {
            candidate = -1;
            if ((sb.charAt(i) != '#') && (sb.charAt(i) != '!')) {
              candidate = i;
            }                 
          }  
          else {    
            if ((candidate != -1) && (UtilConvert.whiteSpaceChars.indexOf(aChar) == -1)) {
              // insert a #
              sb.insert(candidate, '#');
              i++;
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
  public static class ItemElem extends Element {
                  
    KeyElem     key;
    ValueElem   value;
    CommentElem comment;
    boolean     justComment; // comment after the last property in a prop. file 
                             // is represented by a ItemElem with justComment set to true
    
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
    
    /** Get a value string of the element.
    * @return the string
    */
    public String toString() {     
      return comment.toString() + '\n' +
             ((key   == null) ? "" : key.toString()) + '\n' +
             ((value == null) ? "" : value.toString()) + '\n';
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
    public void setKey(String key) {
      // PENDING
      // set + fire
    }
    
    /** Get the value of this item */                        
    public String getValue() {
      return (value == null) ? "" : value.getValue();
    }
    
    /** Set the value of this item 
    *  @param key the new key
    */                        
    public void setValue(String value) {
      // PENDING
      // set + fire
    }

    /** Get the comment for this item */                        
    public String getComment() {
      return (comment == null) ? "" : comment.getValue();
    }
    
    /** Set the comment for this item 
    *  @param key the new key
    */                        
    public void setComment(String comment) {
      // PENDING
      // set + fire
    }

  } // end of inner class ItemElem
}

// PENDING 
// fire property change after updating elements or setting a value ?

/*
 * <<Log>>
 *  1    Gandalf   1.0         5/12/99  Petr Jiricka    
 * $
 */
