/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.EventQueue;
import org.netbeans.modules.ide.Factory;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.api.actions.Openable;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class OpenEditorEnablesEditMenuFactoryTest extends NbTestCase {

    public OpenEditorEnablesEditMenuFactoryTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().addTest(OpenEditorEnablesEditMenuFactoryTest.class).
            gui(true).
            clusters("ergonomics.*").
            clusters(".*").
            enableModules("ide[0-9]*", ".*").
            honorAutoloadEager(true)
        );
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        URL u = OpenEditorEnablesEditMenuFactoryTest.class.getResource("default.xml");
        assertNotNull("Default layer found", u);
        XMLFileSystem xml = new XMLFileSystem(u);
        FileObject fo = xml.findResource("Menu/Edit_hidden");
        assertNotNull("File found", fo);
        
        FileObject e = FileUtil.getConfigFile("Menu/Edit");
        assertNull("Default layer is on and Edit is hidden: " + Arrays.toString(FileUtil.getConfigFile("Menu").getChildren()), e);
    }

    public void testIfProjectFactoryInstalled() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                Mode m = WindowManager.getDefault().findMode("editor");
                assertNotNull("Editor mode found", m);
                TopComponent[] arr = WindowManager.getDefault().getOpenedTopComponents(m);
                assertEquals("No component is open", 0, arr.length);
            }
        });

        FileObject folder = FileUtil.toFileObject(getWorkDir());
        FileObject txt = folder.createData("text.txt");
        DataObject obj = DataObject.find(txt);
        Openable open = obj.getLookup().lookup(Openable.class);
        assertNotNull("Can be opened: " + obj, open);
        open.open();

        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                Mode m = WindowManager.getDefault().findMode("editor");
                assertNotNull("Editor mode found", m);
                TopComponent[] arr = WindowManager.getDefault().getOpenedTopComponents(m);
                assertEquals("One component is open", 1, arr.length);
            }
        });
        FoDFileSystem.getInstance().waitFinished();

        FileObject fo = null;
        for (int i = 0; i < 100; i++) {
            fo = FileUtil.getConfigFile("Menu/Edit");
            if (fo != null) break;
            Thread.sleep(100);
        }
        assertNotNull("Default layer is off and Edit is visible", fo);
    }
}
