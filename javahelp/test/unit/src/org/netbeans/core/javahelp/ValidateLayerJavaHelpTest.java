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
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.awt;

import java.io.InputStream;
import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.cookies.InstanceCookie;

import org.openide.filesystems.*;
import org.openide.loaders.*;

/** Checks the consistence of Services/JavaHelp folder.
 *
 * @author Stanislav Aubrecht
 */
public class ValidateLayerJavaHelpTest extends NbTestCase {

    /** Creates a new instance of ValidateLayerJavaHelpTest */
    public ValidateLayerJavaHelpTest( String name ) {
        super( name );
    }

    //
    // override in subclasses
    //
    
    protected String rootName () {
        return "Services/JavaHelp";
    }
    
    /** Allowes to skip filest that are know to be broken */
    protected boolean skipFile (FileObject fo) {
        // ignore these files, there are helpers for Helpset ordering
        return fo.hasExt ("txt");
    }
    
    protected boolean correctInstance (Object obj) {
        if (obj instanceof javax.help.HelpSet) {
            return true;
        }
        
        return false;
    }
    
    
    //
    // the test
    // 
    
    public void testContentCorrect () throws Exception {
        java.util.ArrayList errors = new java.util.ArrayList ();
        
        DataFolder df = DataFolder.findFolder( Repository.getDefault().getDefaultFileSystem().findResource( rootName() ) );
        verifyHelpSets( df, errors );
        
        if (!errors.isEmpty()) {
            fail ("Some files do not provide valid helpsets " + errors);
        }
    }
    
    private void verifyHelpSets( DataFolder f, java.util.ArrayList errors ) throws Exception {
        DataObject[] arr = f.getChildren();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof DataFolder) {
                verifyHelpSets( (DataFolder)arr[i], errors );
                continue;
            } 
            FileObject file = arr[i].getPrimaryFile ();
            
            if (skipFile (file)) {
                continue;
            }
            
            Object url = file.getURL();
            
            InstanceCookie ic = (InstanceCookie)arr[i].getCookie(InstanceCookie.class);
            if (ic == null) {
                errors.add ("\n    File " + file + " does not have instance cookie, url: " + url);
                continue;
            }
            
            try {
                Object obj = ic.instanceCreate();
                if (correctInstance (obj)) {
                    continue;
                }
                errors.add ("\n    File " + arr[i].getPrimaryFile () + " does not provide correct instance: " + obj + " url: " + url);
            } catch (Exception ex) {
                errors.add ("\n    File " + arr[i].getPrimaryFile () + " cannot be read " + ex + " url: " + url);
            }
        }
    }
}

