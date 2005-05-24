/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ResourceConfigHelper.java
 *
 * Created on October 17, 2002, 12:11 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;


/**
 *
 * @author  shirleyc
 */
public class ResourceConfigHelper {
    
    private ResourceConfigData datas[] = null;
    private int index;
    private boolean forEdit = false;
    
    /** Creates a new instance of ResourceConfigHelper */
    public ResourceConfigHelper(int size) {
        this(size, 0);
    }
    
    public ResourceConfigHelper(int size, int index) {
        datas = new ResourceConfigData[size];
        this.index = index;
    }
    
    public ResourceConfigHelper(ResourceConfigData data, int size, int index) {
        this(size, index);
        datas[index] = data;
    }
    
    public ResourceConfigHelper(ResourceConfigData data) {
        this(data, 1, 0);
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public boolean getForEdit() {
        return forEdit;
    }
    
    public ResourceConfigHelper setForEdit(boolean forEdit) {
        this.forEdit = forEdit;
        return this;
    }    
        
    public ResourceConfigData getData() {
        ResourceConfigData data = datas[index];
        if (data == null) {
            data = new ResourceConfigData();
            datas[index] = data;
        }
        return data;
    }
    
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("index is " + index + "\n");  //NOI18N
        for (int i = 0; i < datas.length; i++) {
            if (datas[i] == null)
                str.append("datas[ " + i + " ] is null"); //NOI18N
            else
                str.append("datas[ " + i + " ] is:\n" + datas[i].toString()); //NOI18N
        }
        return str.toString();
    }
           
}
