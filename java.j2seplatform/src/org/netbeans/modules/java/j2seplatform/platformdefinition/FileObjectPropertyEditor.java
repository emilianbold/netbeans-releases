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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class FileObjectPropertyEditor extends PropertyEditorSupport {

    public String getAsText() {
        try {
            List fileobjs = (List) this.getValue();
            StringBuffer result = new StringBuffer ();
            boolean first = true;
            for (Iterator it = fileobjs.iterator(); it.hasNext();) {
                FileObject fo = (FileObject) it.next ();
                File f = FileUtil.toFile(fo);
                if (f != null) {
                    if (!first) {
                        result.append (File.pathSeparator);
                    }
                    else {
                        first = false;
                    }
                    result.append(f.getAbsolutePath());
                }
            }
            return result.toString ();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void setAsText(String text) throws IllegalArgumentException {
        try {
            List fileObjs = new ArrayList ();
            if (text != null) {
                StringTokenizer tk = new StringTokenizer (text, File.pathSeparator);
                while (tk.hasMoreTokens()) {
                    String path = tk.nextToken();
                    File f = new File (path);
                    FileObject[] fos = FileUtil.fromFile(f);
                    if (fos.length == 0)
                        throw new IllegalArgumentException();
                    else
                        fileObjs.add (fos[0]);
                }
            }
            setValue (fileObjs);
        } catch (Exception e) {
            e.printStackTrace ();           
        }
    }

}
