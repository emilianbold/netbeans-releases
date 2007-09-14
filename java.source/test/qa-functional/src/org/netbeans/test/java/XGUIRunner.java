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

package org.netbeans.test.java;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import javax.swing.text.StyledDocument;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.util.PNGEncoder;
//import org.netbeans.modules.java.settings.JavaSynchronizationSettings;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.SharedClassObject;


/** Runner
 * @author Jan Becicka
 */
public abstract class XGUIRunner extends JellyTestCase implements Go {
    
    protected String name;
    
    protected String packageName;
    
    public XGUIRunner(java.lang.String testName) {
        super(testName);
    }
    
    public void waitEditorOpened() {
        new EditorWindowOperator().getEditor(name);
    }

    public void testRun() {
        DataObject DO = null;
        //JavaSynchronizationSettings ss = (JavaSynchronizationSettings) SharedClassObject.findObject(JavaSynchronizationSettings.class);
        //ss.setEnabled(false);
        String fullName = packageName + "." + name;
        
        boolean ok = true;
        
        try {
            ok = go(fullName, new PrintWriter(getLog()));
            if (!ok) {
                getLog().println("go() failed");
            }
            
            DO = DataObject.find(Repository.getDefault().findResource(fullName.replace('.','/') + ".java"));
            ((SaveCookie) DO.getCookie(SaveCookie.class)).save();
        } catch (Exception e) {
            ok = false;
            e.printStackTrace(getLog());
        }

        ok = writeResult(DO);
        try {
            DO.delete();
        } catch (IOException e){
            assertTrue(e.toString(), false);
        }

        assertTrue("See .log file for details", ok);
	compareReferenceFiles();
    }
    
     public File getGoldenFile(String filename) {
        String fullClassName = this.getClass().getName();
        String className = fullClassName;
        int lastDot = fullClassName.lastIndexOf('.');
        if (lastDot != -1) {
            className = fullClassName.substring(lastDot+1);
        }  
        String goldenFileName = className+".pass";
        URL url = this.getClass().getResource(goldenFileName);
        assertNotNull("Golden file "+goldenFileName+" cannot be found",url);
        String resString = convertNBFSURL(url);        
        File goldenFile = new File(resString);
        return goldenFile;
    }

    protected boolean writeResult(DataObject DO) {
        String result = "";
        try {
            EditorCookie ec=(EditorCookie)(DO.getCookie(EditorCookie.class));
            StyledDocument doc=ec.openDocument();
            result=doc.getText(0, doc.getLength());
            result=Common.unify(result);
        } catch (Exception e){
            e.printStackTrace(getLog());
            return false;
        }
        
        getRef().print(result);
        return true;
    }
    
}
