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
