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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelui.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.debug.CndDiagnosticProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CodeModelDiagnosticAction extends ProjectActionBase {
    private final static Logger LOG = Logger.getLogger("CodeModelDiagnosticAction"); // NOI18N
    public CodeModelDiagnosticAction() {
        super(true);
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_CodeModelDiagnostic"); //NOI18N
    }

    @Override
    protected boolean isEnabledEx(Node[] activatedNodes, Collection<CsmProject> projects) {
        if (super.isEnabledEx(activatedNodes, projects)) {
            return true;
        }
        for (Node node : activatedNodes) {
            if (node.getLookup().lookup(NativeFileItemSet.class) != null) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected void performAction(Collection<CsmProject> csmProjects) {
        Node[] activatedNodes = getActivatedNodes();
        List<Object> lookupObjects = new ArrayList<Object>();
        JTextComponent lastFocusedComponent = EditorRegistry.lastFocusedComponent();
        Document doc = null;
        if (lastFocusedComponent != null) {
            lookupObjects.add(lastFocusedComponent);
            doc = lastFocusedComponent.getDocument();
        }
        if (doc != null) {
            lookupObjects.add(doc);
        }
        if (activatedNodes != null) {
            lookupObjects.addAll(Arrays.asList(activatedNodes));
            for (Node node : activatedNodes) {
                final DataObject dob = node.getLookup().lookup(DataObject.class);
                if (dob != null) {
                    lookupObjects.add(dob);
                }
            }
        }
        lookupObjects.addAll(csmProjects);
        LOG.log(Level.INFO, "perform actions on {0}\n nodes={1}\n", new Object[]{csmProjects, activatedNodes});
        if (!lookupObjects.isEmpty()) {
            Collection<? extends CndDiagnosticProvider> providers = Lookup.getDefault().lookupAll(CndDiagnosticProvider.class);
            Lookup context = Lookups.fixed(lookupObjects.toArray(new Object[lookupObjects.size()]));
            String tmpDir = System.getProperty("java.io.tmp"); // NOI18N
            if (tmpDir == null) {
                tmpDir = "/var/tmp";// NOI18N
            }
            try {
                File tmpFile = File.createTempFile("cnd_diagnostics_", ".txt", new File(tmpDir));// NOI18N
                PrintWriter pw = new PrintWriter(tmpFile);
                String taskName = "Cnd Diagnostics - " + tmpFile.getName(); // NOI18N
                InputOutput io = IOProvider.getDefault().getIO(taskName, false); // NOI18N
                io.select();
                final OutputWriter out = io.getOut();
                final OutputWriter err = io.getErr();
                err.printf("dumping cnd diagnostics into %s\n", tmpFile);
                for (CndDiagnosticProvider provider : providers) {
                    pw.printf("**********************\ndiagnostics of %s\n", provider.getDisplayName());
                    provider.dumpInfo(context, pw);
                }
                pw.close();
                // copy all into output window
                FileInputStream stream = new FileInputStream(tmpFile.getAbsolutePath());
                BufferedReader das = new BufferedReader(new InputStreamReader(stream));
                String line;
                do {
                    line = das.readLine();
                    if (line == null) {
                        break;
                    }
                    out.println(line);
                } while (true);
                err.printf("Cnd diagnostics is saved in %s\n", tmpFile);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }    
}
