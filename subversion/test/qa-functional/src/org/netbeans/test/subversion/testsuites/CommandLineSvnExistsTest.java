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

package org.netbeans.test.subversion.testsuites;

import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.test.subversion.main.archeology.AnnotationsTest;
import org.netbeans.test.subversion.main.archeology.SearchHistoryUITest;
import org.netbeans.test.subversion.main.archeology.SearchRevisionsTest;
import org.netbeans.test.subversion.main.branches.CopyTest;
import org.netbeans.test.subversion.main.branches.CopyUiTest;
import org.netbeans.test.subversion.main.branches.MergeUiTest;
import org.netbeans.test.subversion.main.branches.RevertUiTest;
import org.netbeans.test.subversion.main.branches.SwitchUiTest;
import org.netbeans.test.subversion.main.checkout.CheckoutContentTest;
import org.netbeans.test.subversion.main.checkout.CheckoutUITest;
import org.netbeans.test.subversion.main.checkout.CreateProjectVersionedDirTest;
import org.netbeans.test.subversion.main.checkout.ImportUITest;
import org.netbeans.test.subversion.main.checkout.ProxySettingsUITest;
import org.netbeans.test.subversion.main.commit.CommitDataTest;
import org.netbeans.test.subversion.main.commit.CommitUiTest;
import org.netbeans.test.subversion.main.commit.IgnoreTest;
import org.netbeans.test.subversion.main.delete.DeleteTest;
import org.netbeans.test.subversion.main.delete.FilesViewDoubleRefTest;
import org.netbeans.test.subversion.main.delete.FilesViewRefTest;
import org.netbeans.test.subversion.main.delete.RefactoringTest;
import org.netbeans.test.subversion.main.diff.DiffTest;
import org.netbeans.test.subversion.main.diff.ExportDiffPatchTest;
import org.netbeans.test.subversion.main.properties.SvnPropertiesTest;

/**
 *
 * @author cyhelsky
 */
public class CommandLineSvnExistsTest extends JellyTestCase {
    
    String os_name;
    
    public CommandLineSvnExistsTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {        
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### "+getName()+" ###");
        
    }
    
    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    /**
     * Simple method uniting together all the different tests under subversion
     * tests-qa-functional
     */
     public static Test suite() {
         if (Subversion.getInstance().checkClientAvailable()) {
             return NbModuleSuite.create(NbModuleSuite.emptyConfiguration()
                 .addTest(AnnotationsTest.class)
                 .addTest(SearchHistoryUITest.class)
                 .addTest(SearchRevisionsTest.class)
                 .addTest(CopyTest.class)
                 .addTest(CopyUiTest.class)
                 .addTest(MergeUiTest.class)
                 .addTest(RevertUiTest.class)
                 .addTest(SwitchUiTest.class)
                 .addTest(CheckoutContentTest.class)
                 .addTest(CheckoutUITest.class)
                 .addTest(CreateProjectVersionedDirTest.class)
                 .addTest(ImportUITest.class) 
                 .addTest(ProxySettingsUITest.class)
                 .addTest(CommitDataTest.class)
                 .addTest(CommitUiTest.class)
                 .addTest(IgnoreTest.class)
                 .addTest(DeleteTest.class)
                 .addTest(FilesViewDoubleRefTest.class)
                 .addTest(FilesViewRefTest.class)
                 .addTest(RefactoringTest.class)
                 .addTest(DiffTest.class)
                 .addTest(ExportDiffPatchTest.class)
                 .addTest(SvnPropertiesTest.class)
                 );
         } else {
             return NbModuleSuite.create(NbModuleSuite.emptyConfiguration());
         }
     }
}