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
                    fileObjs.add(FileUtil.toFileObject(f));
                }
            }
            setValue (fileObjs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
