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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import org.netbeans.modules.j2ee.sun.api.InstrumentAVK;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;



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
