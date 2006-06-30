/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
