/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.railsprojects;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;
import org.netbeans.api.ruby.platform.RubyInstallation;

import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.NbUtilities;
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
        JEditorPane pane = NbUtilities.getOpenPane();
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
            NbUtilities.open(viewFile, 0, null);
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

        // TODO - instead of relying on Path manipulation here, should I just
        // use the RubyIndex to locate the class and method?
        FileObject controllerFile = null;
        String action = file.getName();

        try {
            file = file.getParent();

            String fileName = file.getName();
            String path = "";

            if (!fileName.startsWith("_")) { // NOI18N
                                             // For partials like "_foo", just use the surrounding view
                path = fileName;
            }

            // Find app dir, and build up a relative path to the view file in the process
            FileObject app = file.getParent();

            while (app != null) {
                if (app.getName().equals("views") && // NOI18N
                        ((app.getParent() == null) || app.getParent().getName().equals("app"))) { // NOI18N
                    app = app.getParent();

                    break;
                }

                path = app.getNameExt() + "/" + path; // NOI18N
                app = app.getParent();
            }

            if (app == null) {
                notFound(target);

                return;
            }

            controllerFile = app.getFileObject("controllers/" + path + "_controller.rb"); // NOI18N
        } catch (Exception e) {
            notFound(target);

            return;
        }

        if (controllerFile == null) {
            notFound(target);

            return;
        }

        // TODO: Find the position of the #view method
        int offset = AstUtilities.findOffset(controllerFile, action);

        NbUtilities.open(controllerFile, offset, "def " + action); // NOI18N
    }
}
