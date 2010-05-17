/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.subversion.client.cli;

import java.io.File;
import java.io.InputStream;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 *
 * @author tomas
 */
public class MoveTest extends AbstractCLITest {
    
    public MoveTest(String testName) throws Exception {
        super(testName);
    }
    
    public void testMoveURL2URL() throws Exception {                                        
        File file = createFile("file");
        add(file);
        commit(file);
                
        File filemove = createFile("filemove");
        
        ISVNClientAdapter c = getNbClient();
        c.move(getFileUrl(file), getFileUrl(filemove), "move", SVNRevision.HEAD);

        ISVNInfo info = null;
        SVNClientException ex = null;
        try {
            getInfo(getFileUrl(file));
        } catch (SVNClientException e) {
            ex = e;
        }
        assertNull(info);
        assertNotNull(ex);
        
        info = getInfo(getFileUrl(filemove));
        assertNotNull(info);
        assertEquals(getFileUrl(filemove), info.getUrl());
        
        assertNotifiedFiles(new File[] {});        
    }        
    
    public void testMoveURL2URLPrevRevision() throws Exception {                                        
        File file = createFile("file");
        write(file, 1);
        add(file);
        commit(file);
        SVNRevision prevRev = getRevision(file);
        write(file, 2);
        commit(getWC());        
        
        File filemove = createFile("filemove");
        
        ISVNClientAdapter c = getNbClient();
        c.copy(getFileUrl(file), getFileUrl(filemove), "move", prevRev);

        ISVNLogMessage[] logs = getLog(getFileUrl(filemove));
        assertEquals(((SVNRevision.Number)prevRev).getNumber() ,logs[0].getChangedPaths()[0].getCopySrcRevision().getNumber());

        InputStream is = getContent(getFileUrl(filemove));
        assertContents(is, 1);
        assertNotifiedFiles(new File[] {});        
    }           
    
    public void testMoveFile2File() throws Exception {                                        
        File file = createFile("file");
        add(file);
        commit(file);
                
        File filemove = new File(getWC(), "filemove");
        
        ISVNClientAdapter c = getNbClient();
        c.move(file, filemove, true);

        assertTrue(filemove.exists());
        assertStatus(SVNStatusKind.ADDED, filemove);
        assertNotifiedFiles(new File[] {file, filemove});                
    }        
    
}
