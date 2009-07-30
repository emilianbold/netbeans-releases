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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.loaders;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/** DefaultDataObject is supposed to have open operation that shows the text
 * editor or invokes a dialog with questions.
 *
 * @author  Jaroslav Tulach
 */
public final class DefaultDataObjectHasOpenActionTest extends NbTestCase {

    private DataObject obj;

    public DefaultDataObjectHasOpenActionTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        // initialize modules
        Lookup.getDefault().lookup(ModuleInfo.class);

        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fo = fs.getRoot().createData("x.test");
        obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), DefaultDataObject.class);

        assertFalse("Designed to run outside of AWT", SwingUtilities.isEventDispatchThread());
    }

    public void testOpenActionIsAlwaysFirst() throws Exception {
        Node n = obj.getNodeDelegate();

        FileObject fo = FileUtil.getConfigFile("Actions/System/org-openide-actions-OpenAction.instance");
        Action openAction = (Action) fo.getAttribute("instanceCreate");

        assertEquals(
                "Open action is the default one",
                openAction,
                n.getPreferredAction()
                );

        Action[] actions = n.getActions(false);
        assertTrue("There are some actions", actions.length > 1);

        assertEquals(
                "First one is open",
                openAction,
                actions[0]
                );
    }

}
