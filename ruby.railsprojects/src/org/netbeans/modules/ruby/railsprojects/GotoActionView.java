/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.ruby.railsprojects;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.text.JTextComponent;
import org.netbeans.api.ruby.platform.RubyInstallation;

import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


/**
 * Rails action for jumping to the action corresponding to a view, or the
 * view corresponding to an action.
 * 
 * @author Tor Norbye
 */
public class GotoActionView extends AbstractAction {
    public GotoActionView() {
        super(NbBundle.getMessage(GotoActionView.class, "rails-goto-action-view")); // NOI18N
        putValue("PopupMenuText", // NOI18N
            NbBundle.getBundle(GotoActionView.class).getString("editor-popup-goto-action-view")); // NOI18N
    }
    
    // TODO - move to GsfUtilities - and use the editor registry!
    private FileObject getCurrentFile() {
        Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
        if (activatedNodes == null || activatedNodes.length != 1) {
            return null;
        }

        DataObject dobj = activatedNodes[0].getLookup().lookup(DataObject.class);
        if (dobj == null) {
            return null;
        }

        FileObject fo = dobj.getPrimaryFile();
        
        return fo;
    }

    @Override
    public boolean isEnabled() {
        // This action is enabled based on the activated nodes in the TopComponent registry.
        // A seemingly cleaner solution would be to use a NodeAction, but that doesn't 
        // work because this action is ALSO registered into the Editor Popup menus;
        // and those actions need to be AbstractActions.
        FileObject fo = getCurrentFile();
        if (fo == null) {
            return false;
        }

        String mimeType = fo.getMIMEType();
        if (RubyInstallation.RHTML_MIME_TYPE.equals(mimeType)) {
            return true;
        } else if (RubyInstallation.RUBY_MIME_TYPE.equals(mimeType)) {
            String name = fo.getName();
            if (name.endsWith("_controller") || name.endsWith("_helper")) { // NOI18N
                return true;
            } else {
                String ext = fo.getExt();
                if (!(ext.equals("rb"))) {
                    for (String e : RubyUtils.RUBY_VIEW_EXTS) {
                        if (ext.equalsIgnoreCase(e)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        } else if ("haml".equals(fo.getExt())) { // Not recognized as a Ruby file yet
            return true;
        } else {
            return false;
        }
    }

    public void actionPerformed(ActionEvent ev) {
        JTextComponent pane = GsfUtilities.getOpenPane();
        FileObject fo = getCurrentFile();
        if (fo != null && pane != null) {
            actionPerformed(pane, fo);
        }
    }

    private void actionPerformed(final JTextComponent target, final FileObject fo) {
        if (fo != null) {
            // TODO - Look up project and complain if it's not a Rails project

            // See if it's a controller:
            if (fo.getName().endsWith("_controller")) { // NOI18N
                gotoView(target, fo, false, "_controller"); // NOI18N
            } else if (fo.getName().endsWith("_helper")) { // NOI18N
                gotoView(target, fo, true, "_helper"); // NOI18N
            } else {
                if (RubyUtils.isRhtmlFile(fo)) {
                    gotoAction(target, fo);
                } else {
                    String ext = fo.getExt();
                    for (String e : RubyUtils.RUBY_VIEW_EXTS) {
                        if (ext.equalsIgnoreCase(e)) {
                            gotoAction(target, fo);
                            return;
                        }
                    }

                    Utilities.setStatusBoldText(target,
                            NbBundle.getMessage(GotoActionView.class, "AppliesToControllers"));
                }
            }
        }
    }

    private void notFound(JTextComponent target) {
        Utilities.setStatusBoldText(target, NbBundle.getMessage(GotoActionView.class, "ControllerNotFound"));
    }

    /** 
     * Move from something like app/controllers/credit_card_controller.rb#debit()
     * to app/views/credit_card/debit.rhtml
     */
    private void gotoView(JTextComponent target, FileObject file, boolean isHelper, String fileSuffix) {
        // This should be a view.
        if (!file.getName().endsWith(fileSuffix)) {
            Utilities.setStatusBoldText(target, NbBundle.getMessage(GotoActionView.class, "AppliesToActions"));

            return;
        }
        FileObject controllerFile = file;

        int offset = 0;
        // Find the offset of the file we're in, if any
        if (target.getCaret() != null) {
            offset = target.getCaret().getDot();
        }

        // Get the name of the method corresponding to the offset
        String methodName = AstUtilities.getMethodName(controllerFile, offset);

        FileObject viewFile = RubyUtils.getRailsViewFor(file, methodName, isHelper, false);

        if (viewFile == null) {
            notFound(target);
        } else {
            GsfUtilities.open(viewFile, 0, null);
        }
    }
        
    // Move from something like app/views/credit_card/debit.rhtml to
    //  app/controllers/credit_card_controller.rb#debit()
    private void gotoAction(JTextComponent target, FileObject file) {
        // This should be a view.
        String ext = file.getExt();
        boolean found = false;
        for (String e : RubyUtils.RUBY_VIEW_EXTS) {
            if (ext.equalsIgnoreCase(e)) {
                found = true;
                break;
            }
        }
        
        if (!RubyUtils.isRhtmlFile(file) && !found) {
            Utilities.setStatusBoldText(target, NbBundle.getMessage(GotoActionView.class, "AppliesToViews"));

            return;
        }

        String action = file.getName();
        FileObject controllerFile = RubyUtils.getRailsControllerFor(file);

        if (controllerFile == null) {
            notFound(target);

            return;
        }

        // TODO: Find the position of the #view method
        int offset = AstUtilities.findOffset(controllerFile, action);

        GsfUtilities.open(controllerFile, offset, "def " + action); // NOI18N
    }
}
