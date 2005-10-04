/*
 * AVKLayerUtil.java
 *
 * Created on September 19, 2005, 9:53 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
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
