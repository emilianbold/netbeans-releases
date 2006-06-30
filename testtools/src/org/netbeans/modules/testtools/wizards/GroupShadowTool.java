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
package org.netbeans.modules.testtools.wizards;

import java.io.*;
import java.util.*;
import org.openide.ErrorManager;
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
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ex);
        }
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
