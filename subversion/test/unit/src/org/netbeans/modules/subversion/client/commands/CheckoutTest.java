/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
// XXX add referenceclient
package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTest;
import java.io.File;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class CheckoutTest extends AbstractCommandTest {
    
    public CheckoutTest(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        importWC = true;
        if(getName().startsWith("testCheckout")) {
            importWC = false;            
        } 
        super.setUp();
        if(getName().startsWith("testCheckout")) {
            cleanUpRepo(new String[] {CI_FOLDER});
        }        
    }
    
    public void testCheckoutFile() throws Exception {
        File folder = createFolder(CI_FOLDER);        
        File file = createFile(folder, "file");
        
        importFile(folder);        

        File checkout = createFolder("checkoutfolder");
        SVNUrl url = getTestUrl().appendPath(folder.getName());
        ISVNClientAdapter c = getNbClient();         
        c.checkout(url, checkout, SVNRevision.HEAD, true);
                        
        File chFile = new File(checkout, file.getName());
        
        assertTrue(chFile.exists());
        assertStatus(SVNStatusKind.NORMAL, checkout);                   
        assertStatus(SVNStatusKind.NORMAL, chFile);      
        
        assertNotifiedFiles(chFile);
    }
    
    public void testCheckoutFolder() throws Exception {
        File cifolder = createFolder(CI_FOLDER);
        File folder1 = createFolder(cifolder, "folder1");
        File file = createFile(folder1, "file");
        
        importFile(cifolder);        

        File checkout = createFolder("checkoutfolder");
        SVNUrl url = getTestUrl().appendPath(cifolder.getName());
        ISVNClientAdapter c = getNbClient();         
        c.checkout(url, checkout, SVNRevision.HEAD, true);
                
        File chFolder1 = new File(checkout, folder1.getName());
        File chFile = new File(chFolder1, file.getName());
        
        assertTrue(chFolder1.exists());
        assertTrue(chFile.exists());
        assertStatus(SVNStatusKind.NORMAL, checkout);                   
        assertStatus(SVNStatusKind.NORMAL, chFolder1);        
        assertStatus(SVNStatusKind.NORMAL, chFile);      
        
        assertNotifiedFiles(new File[] {chFolder1, chFile});
    }
    
    public void testCheckoutFolderNonRecursivelly() throws Exception {
        File cifolder = createFolder(CI_FOLDER);
        File folder1 = createFolder(cifolder, "folder1");
        File file = createFile(folder1, "file");
        
        importFile(cifolder);        

        File checkout = createFolder("checkoutfolder");
        SVNUrl url = getTestUrl().appendPath(cifolder.getName());
        ISVNClientAdapter c = getNbClient();         
        c.checkout(url, checkout, SVNRevision.HEAD, false);
                
        File chFolder1 = new File(checkout, folder1.getName());
        File chFile = new File(chFolder1, file.getName());
        
        assertTrue(!chFolder1.exists());
        assertTrue(!chFile.exists());
        assertStatus(SVNStatusKind.NORMAL, checkout);                
        assertStatus(SVNStatusKind.UNVERSIONED, chFolder1);        
        assertStatus(SVNStatusKind.UNVERSIONED, chFile); 
        
        assertNotifiedFiles(new File[] {});
    }

    public void testCheckoutFolderPrevRevision() throws Exception {
        File cifolder = createFolder(CI_FOLDER);
        File folder1 = createFolder(cifolder, "folder1");
        File file = createFile(folder1, "file");
        
        importFile(cifolder);        
        SVNRevision revision = getRevision(getRepoUrl());
        File addedFile = createFile(folder1, "addedfile");
        add(addedFile);
        commit(cifolder);
        assertStatus(SVNStatusKind.NORMAL, addedFile);                
        
        File checkout = createFolder("checkoutfolder");
        SVNUrl url = getTestUrl().appendPath(cifolder.getName());
        ISVNClientAdapter c = getNbClient();         
        c.checkout(url, checkout, revision, true);
                
        File chFolder1 = new File(checkout, folder1.getName());
        File chFile = new File(chFolder1, file.getName());
        File chAddedFile = new File(chFolder1, addedFile.getName());
        
        assertTrue(chFolder1.exists());
        assertTrue(chFile.exists());
        assertTrue(!chAddedFile.exists());
        assertStatus(SVNStatusKind.NORMAL, checkout);                
        assertStatus(SVNStatusKind.NORMAL, chFolder1);        
        assertStatus(SVNStatusKind.NORMAL, chFile);                
        assertStatus(SVNStatusKind.UNVERSIONED, chAddedFile);                
        
        assertNotifiedFiles(new File[] {chFolder1, chFile});
    }


}
