/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.testtools.wizards;

import java.io.*;
import java.util.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

class GroupShadowTool {
    static Object[] getLinks(DataObject dob) {
        FileObject pf = dob.getPrimaryFile();
        DataObject obj;
        String line;
        HashSet set = new HashSet();
        List linearray;
        try {
            linearray = new ArrayList();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(pf.getInputStream()));
                while ((line = br.readLine()) != null) {
                    linearray.add(line);
                }
            } catch (IOException ex) {
                throw ex;
            } finally {
                if (br != null) br.close();
            }
            Iterator it = linearray.iterator();
            while (it.hasNext()) {
                line = (String)it.next();
                try {
                    FileObject tempfo = Repository.getDefault().findResource(line);
                    obj=(tempfo!=null)?DataObject.find(tempfo):null;
                    if (obj!=null) set.add(obj);
                    else set.add(new String(line));
                } catch (DataObjectNotFoundException ex) {}
            }
        } catch (IOException ex) {}
        return set.toArray();
    }
    static boolean instanceOf(Object o) {
        if (o==null) return true;
        Class c=o.getClass();
        while (c!=null) {
            if (c.getName().endsWith(".GroupShadow")) return true; // NO I18N
            c=c.getSuperclass();
        }
        return false;
    }
}
