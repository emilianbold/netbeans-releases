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

package org.netbeans.modules.maven.jaxws.actions;

import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.maven.jaxws.nodes.OperationNode;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class CallWsOperationAction  implements CodeGenerator {
    private FileObject targetSource;
    private JTextComponent component;

    CallWsOperationAction(FileObject targetSource, JTextComponent component) {
        this.targetSource = targetSource;
        this.component = component;
    }

    public static class Factory implements CodeGenerator.Factory {
        public List<? extends CodeGenerator> create(Lookup context) {
            List<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            TreePath path = context.lookup(TreePath.class);
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                if (controller != null) {
                    FileObject targetSource = controller.getFileObject();
                    if (targetSource != null && JAXWSLightSupport.getJAXWSLightSupport(targetSource) != null) {
                        ret.add(new CallWsOperationAction(targetSource, component));
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return ret;
        }
    }

    public String getDisplayName() {
        return NbBundle.getMessage(CallWsOperationAction.class, "LBL_CallWsOperation");
    }

    public void invoke() {
        ClientExplorerPanel serviceExplorer = new ClientExplorerPanel(targetSource);
        DialogDescriptor descriptor = new DialogDescriptor(serviceExplorer,
                NbBundle.getMessage(CallWsOperationAction.class, "TTL_SelectOperation"));
        serviceExplorer.setDescriptor(descriptor);
        DialogDisplayer.getDefault().notify(descriptor);

        if (descriptor.getValue().equals(NotifyDescriptor.OK_OPTION)) {
            JaxWsCodeGenerator.insertMethod(component.getDocument(),
                    component.getCaretPosition(),
                    serviceExplorer.getSelectedMethod().getLookup().lookup(OperationNode.class));
        }
    }

}
