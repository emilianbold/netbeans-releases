/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders;

import org.openide.loaders.DataObject;
/**
 *
 * @author dlm198383
 */
public class DDChangeEvent extends java.util.EventObject{
     /** Event fired when new web is added or copied from another location */
    public static final int WEB_ADDED = 1;
    
    /** Event fired when web is renamed or moved within one web module */
    public static final int WEB_CHANGED = 2;
    
    /** Event fired when web is deleted */
    public static final int WEB_DELETED = 3; // delete

    /** Event fired when web is moved */
    public static final int WEB_MOVED = 4;
    
    
     /** Newly set value. Usually current classname of web if it makes sense. */
    private String newValue;
    
    /** Old value. Usually old classname of web if it makes sense. */
    private String oldValue;
    
    /** Event type */
    private int type;
    
    /** placeholder for old depl. descriptor (only for web moves) */
    private DataObject oldDD;
    
    /** Creates new event.
     *
     * @param src class name of web
     * @param type type of change
     */    
    public DDChangeEvent (Object src, DataObject oldDD, String oldVal, String newVal, int type) {
        super (src);
        newValue = newVal;
        oldValue = oldVal;
        this.type = type;
        this.oldDD = oldDD;
    }
    
    /** Creates new event.
     *
     * @param src class name of web
     * @param type type of change
     */    
    public DDChangeEvent (Object src, String oldVal, String newVal, int type) {
        this (src, null, oldVal, newVal, type);
    }
    
    public String getNewValue () {
        return newValue;
    }
    
    public String getOldValue () {
        return oldValue;
    }
    
    public DataObject getOldDD () {
        return oldDD;
    }
    
    /** Getter for change type
     *
     * @return change type
     */    
    public int getType () {
        return type;
    }
    
    public String toString () {
        return "DDChangeEvent "+getSource ()+" of type "+type; // NOI18N
    }
    
}
