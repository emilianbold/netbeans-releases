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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.rest.support;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;

/**
 *
 * @author Peter Liu
 */
public class Utils {
    
    public static String stripPackageName(String name) {
        int index = name.lastIndexOf(".");          //NOI18N
        
        if (index > 0) {
            return name.substring(index+1);
        }
        return name;
    }
    
    public static Collection<String> sortKeys(Collection<String> keys) {
        Collection<String> sortedKeys = new TreeSet<String>(
                new Comparator<String> () {
            public int compare(String str1, String str2) {
                return str1.compareTo(str2);
            }
        });
        
        sortedKeys.addAll(keys);
        return sortedKeys;
    }
    
    public static void showMethod(FileObject source, String methodName) {
        try {
            DataObject dataObj = DataObject.find(source);          
            JavaSource javaSource = JavaSource.forFileObject(source);
            
            // Force a save to make sure to make sure the line position in
            // the editor is in sync with the java source.
            SaveCookie sc = (SaveCookie) dataObj.getCookie(SaveCookie.class);
     
            if (sc != null) {
                sc.save();
            }
            
            LineCookie lc = (LineCookie) dataObj.getCookie(LineCookie.class);
            
            if (lc != null) {
                final long[] position = JavaSourceHelper.getPosition(javaSource, methodName);
                final Line line = lc.getLineSet().getOriginal((int) position[0]);
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        line.show(Line.SHOW_SHOW, (int) position[1]);
                    }
                });
            }
        } catch (Exception de) {
            de.printStackTrace();
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, de.toString());
        }    
    }
}

