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


package org.netbeans.modules.properties;


import java.beans.*;
import java.io.*;
import javax.swing.text.BadLocationException;

import org.openide.nodes.Node;
import org.openide.text.PositionBounds;


/** 
 * Base class for representations of elements in properties files.
 *
 * @author Petr Jiricka
 */
public abstract class Element extends Object implements Serializable {

    /** Property change support */
    private transient PropertyChangeSupport support;

    /** Position of the begin and the end of the element. Could
     * be null indicating the element is not part of properties structure yet. */
    protected PositionBounds bounds;

    
    /** Create a new element. */
    protected Element(PositionBounds bounds) {
        this.bounds = bounds;
    }

    
    /** Getter for bounds property. */
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

    /** Prints this element (and all its subelements) by calling <code>bounds.setText(...)</code>
     * If <code>bounds</code> is null does nothing. 
     * @see #bounds */
    public void print() {
        if(bounds == null)
            return;
        
        try {
            bounds.setText(printString());
        } catch (BadLocationException e) {
            // PENDING
        } catch (IOException e) {
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
        return (bounds == null) ? "(no bounds)" : "(" + bounds.getBegin().getOffset() + ", " + bounds.getEnd().getOffset() + ")"; // NOI18N
    }

    
    /** General class for basic elements, which contain value directly. */
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
            return value + "   " + super.toString(); // NOI18N
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
    } // End of nested class Basic.


    /** Class representing key element in properties file. */
    public static class KeyElem extends Basic {

        /** Generated serial version UID. */
        static final long serialVersionUID =6828294289485744331L;
        
        
        /** Create a new key element. */
        protected KeyElem(PositionBounds bounds, String value) {
            super(bounds, value);
        }

        
        /** Get a string representation of the key for printing. Treats the '=' sign as a part of the key
        * @return the string
        */
        public String printString() {
            //return UtilConvert.saveConvert(value) + "=";
            return getValue()+"="; // no converting to unicode back and forth
        }
    } // End of nested class KeyElem.
    

    /** Class representing value element in properties files. */
    public static class ValueElem extends Basic {

        /** Generated serial version UID. */
        static final long serialVersionUID =4662649023463958853L;
        
        /** Create a new value element. */
        protected ValueElem(PositionBounds bounds, String value) {
            super(bounds, value);
        }

        /** Get a string representation of the value for printing. Appends end of the line after the value.
        * @return the string
        */
        public String printString() {
            //return UtilConvert.saveConvert(value) + "\n";
            return getValue()+"\n"; // NOI18N // no converting to unicode back and forth
        }
    } // End of nested class ValueElem.

    /** Class representing comment element in properties files. <code>null</code> values of the string are legal and indicate that the comment is empty. */
    public static class CommentElem extends Basic {

        /** Genererated serial version UID. */
        static final long serialVersionUID =2418308580934815756L;
        
        
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
                return ""; // NOI18N
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
    } // End of nested CommentElem.


    /** 
     * Class representing element in  properties file. Each element contains comment (preceding the property),
     * key and value subelement.
     */
    public static class ItemElem extends Element implements Node.Cookie {

        /** Key element.  */
        private KeyElem     key;
        
        /** Value element. */        
        private ValueElem   value;
        
        /** Comment element. */
        private CommentElem comment;
        
        /** Parent of this element - active element has a non-null parent. */
        private PropertiesStructure parent;

        /** Name of the Key property */
        public static final String PROP_ITEM_KEY     = "key"; // NOI18N
        /** Name of the Value property */
        public static final String PROP_ITEM_VALUE   = "value"; // NOI18N
        /** Name of the Comment property */
        public static final String PROP_ITEM_COMMENT = "comment"; // NOI18N

        /** Generated serial version UID. */
        static final long serialVersionUID =1078147817847520586L;

        
        /** Create a new basic element. <code>key</code> and <code>value</code> may be null. */
        protected ItemElem(PositionBounds bounds, KeyElem key, ValueElem value, CommentElem comment) {
            super(bounds);
            this.key     = key;
            this.value   = value;
            this.comment = comment;
        }

        
        /** Sets the parent of this element. */
        void setParent(PropertiesStructure ps) {
            parent = ps;
        }

        /** Returns parent if not null. */
        public PropertiesStructure getParent() {
            if (parent == null)
                throw new InternalError();
            return parent;
        }

        /** Get a value string of the element.
        * @return the string
        */
        public String toString() {
            return comment.toString() + "\n" + // NOI18N
                ((key   == null) ? "" : key.toString()) + "\n" + // NOI18N
                ((value == null) ? "" : value.toString()) + "\n"; // NOI18N
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
        }

        /** Get a string representation of the element for printing.
        * @return the string
        */
        public String printString() {
            return comment.printString() +
                ((key   == null) ? "" : key.printString()) + // NOI18N
                ((value == null) ? "" : value.printString()); // NOI18N
        }

        /** Get a key by which to identify this record */
        public String getKey() {
            return (key == null) ? null : key.getValue();
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
            return (value == null) ? null : value.getValue();
        }

        /** Set the value of this item
         *  @param newValue the new value
         */                        
        public void setValue(String newValue) {
            String oldValue = value.getValue();
            if (!oldValue.equals(newValue)) {
                
                if(oldValue.equals("")) // NOI18N
                    // Reprint key for the case it's alone yet and doesn't have seprator after (= : or whitespace).
                    key.print();
                
                value.setValue(newValue);
                getParent().itemChanged(this);
                this.firePropertyChange(PROP_ITEM_VALUE, oldValue, newValue);
            }
        }

        /** Get the comment for this item */
        public String getComment() {
            return (comment == null) ? null : comment.getValue();
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
            if ( ((key==null && ie.getKeyElem()==null) || (key!=null && ie.getKeyElem()!=null && getKey().equals(ie.getKey())) ) &&
                 ((value==null && ie.getValueElem()==null) || (value!=null && ie.getValueElem()!=null && getValue().equals(ie.getValue())) ) &&
                 ((comment==null && ie.getCommentElem()==null) || (comment!=null && ie.getCommentElem()!=null && getComment().equals(ie.getComment())) ) )
                return true;
            return false;
        }
    } // End of nested class ItemElem.
}
