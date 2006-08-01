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


package org.netbeans.modules.editor.mimelookup.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Martin Roskanin
 */
public class PopupActions implements InstanceProvider{

    List ordered;

    public PopupActions(){
    }

    public PopupActions(List ordered){
        this.ordered = ordered;
    }

    public List getPopupActions(){
        List retList = new ArrayList();
        for (int i = 0; i<ordered.size(); i++){
            FileObject fo = (FileObject) ordered.get(i);
            DataObject dob;
            
            try {
                dob = DataObject.find(fo);
            } catch (DataObjectNotFoundException dnfe) {
                // ignore
                continue;
            }
            
            InstanceCookie ic = (InstanceCookie)dob.getCookie(InstanceCookie.class);
            if (ic!=null){
                try{
                    if (String.class.isAssignableFrom(ic.instanceClass()) ||
                        Action.class.isAssignableFrom(ic.instanceClass()) ||
                        SystemAction.class.isAssignableFrom(ic.instanceClass()) ||
                        JSeparator.class.isAssignableFrom(ic.instanceClass())){
                        Object instance = ic.instanceCreate();
                        retList.add(instance);
                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }catch(ClassNotFoundException cnfe){
                    cnfe.printStackTrace();
                }
            } else{
                retList.add(dob.getName());
            }
        }
        return retList;
    }

    public Object createInstance(List ordered) {
        return new PopupActions(ordered);
    }
}
