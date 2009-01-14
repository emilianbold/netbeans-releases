/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.javahelp;

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
    
    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(ValidateLayerJavaHelpTest.class)
                .clusters(".*").enableModules(".*").gui(false)
        );
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
        
        DataFolder df = DataFolder.findFolder( FileUtil.getConfigFile( rootName() ) );
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

