/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.openide.filesystems;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FileSystemTest extends NbTestCase {
    private ExtraFS fs;
    
    public FileSystemTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        File f = new File(getWorkDir(), "test.txt");
        f.createNewFile();
        fs = new ExtraFS(getWorkDir());
    }

    public void testFindExtraUIForActions() {
        FileObject fo = fs.findResource("test.txt");
        assertNotNull("test.txt found", fo);

        final Set<FileObject> c = Collections.singleton(fo);
        Object[] actions = fs.getActions(c);
        assertNotNull(actions);
        assertEquals("One is provided", actions.length, 1);
        
        Lookup lkp = fs.findExtrasFor(c);
        assertNotNull(lkp);
        Collection<? extends Action> extraAct = lkp.lookupAll(Action.class);
        assertEquals("one action", extraAct.size(), 1);
        
        assertSame("The same action is returned", actions[0], extraAct.iterator().next());
    }
    
    private static final class ExtraFS extends LocalFileSystem {
        public ExtraFS(File f) throws Exception {
            setRootDirectory(f);
        }

        @Override
        public SystemAction[] getActions(Set<FileObject> foSet) {
            return new SystemAction[] {
                SystemAction.get(MyAction.class)
            };
        }
    }
    
    public static final class MyAction extends CallbackSystemAction {
        @Override
        public String getName() {
            return "My test";
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
    }
}
