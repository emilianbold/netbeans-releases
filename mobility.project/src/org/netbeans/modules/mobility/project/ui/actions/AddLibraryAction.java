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
package org.netbeans.modules.mobility.project.ui.actions;

import java.awt.Dialog;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.mobility.project.ui.customizer.VisualClasspathSupport;
import org.netbeans.modules.project.support.customizer.LibrariesChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class AddLibraryAction extends NodeAction<Library> {

    private AddLibraryAction() {
        super(NbBundle.getMessage(AddLibraryAction.class, 
                "LBL_CustLibs_Add_Library")); //NOI18N
    }

    public static Action getStaticInstance() {
        return new AddLibraryAction();
    }

    protected Library[] getItems() {
        Library[] result = null;
        final LibrariesChooser panel = new LibrariesChooser("j2se"); //NOI18N
        final Object[] options = new Object[]{NbBundle.getMessage(VisualClasspathSupport.class, "LBL_AddLibrary"), NotifyDescriptor.CANCEL_OPTION}; //NOI18N
        final DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getMessage(VisualClasspathSupport.class, "LBL_Classpath_AddLibrary"), true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null); //NOI18N
        desc.setHelpCtx(new HelpCtx(LibrariesChooser.class));
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.setVisible(true);
        if (desc.getValue() == options[0]) {
            result = panel.getSelectedLibraries();
        }
        dlg.dispose();
        return result;
    }

    protected List<VisualClassPathItem> addItems(final Library[] libraries, final List<VisualClassPathItem> set, final Node node) {
        for (Library lib : libraries) {
            final String libraryName = lib.getName();
            set.add(new VisualClassPathItem(lib, VisualClassPathItem.TYPE_LIBRARY, "${libs." + libraryName + ".classpath}", lib.getDisplayName())); //NOI18N
        }
        return set;
    }
}
