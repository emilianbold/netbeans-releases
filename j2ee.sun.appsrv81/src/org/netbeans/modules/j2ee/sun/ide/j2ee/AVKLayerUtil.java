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
 * AVKLayerUtil.java
 *
 * Created on September 19, 2005, 9:53 AM
 *
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import org.openide.filesystems.*;
import org.openide.*;
import org.openide.util.Lookup;
import org.openide.loaders.*;
import org.openide.cookies.InstanceCookie;

import org.netbeans.modules.j2ee.sun.api.InstrumentAVK;

/**
 *
 * @author Nitya Doraisamy
 */
public class AVKLayerUtil {
    
    public static final String DIR_EXTENSION = "/J2EE/SunAppServer/AVKImplementation"; //NOI18N
    /**
     * Creates a new instance of AVKLayerUtil 
     */
    public AVKLayerUtil() {
    
    }
    public static InstrumentAVK getAVKImplemenation() {
        InstrumentAVK avkSupport = null;
        Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        FileObject dir = rep.getDefaultFileSystem().findResource(DIR_EXTENSION);
        
        FileObject[] ch =null;
        
        if(dir!=null){
            ch = dir.getChildren();
        }
        if(ch != null){
            for(int i = 0; i < ch.length; i++) {
                try{
                    DataObject dobj = DataObject.find(ch[i]);
                    InstanceCookie cookie = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
                    if(cookie != null) {
                        avkSupport = (InstrumentAVK)cookie.instanceCreate();
                    }
                } catch (Exception e){
                    //Unable to find AVK
                }
            }
        }
        return avkSupport;
    }
    
}
