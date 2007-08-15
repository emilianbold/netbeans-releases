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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.midp.actions;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author David Kaspar
 */
public class ExportFlowAsImageAction extends SystemAction implements DesignDocumentAwareness {

    private DesignDocument document;

    public String getName () {
        return NbBundle.getMessage (ExportFlowAsImageAction.class, "NAME_ExportFlowAsImage"); // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ExportFlowAsImageAction.class);
    }

    public void actionPerformed (ActionEvent e) {
        DataObjectContext context = ActiveViewSupport.getDefault ().getActiveView ().getContext ();
        context.addDesignDocumentAwareness (this);
        context.removeDesignDocumentAwareness (this);
        FlowScene scene = FlowScene.getFlowSceneForDocument (document);
        if (scene == null)
            return;
        saveAsImage (scene);
    }

    private void saveAsImage (Scene scene) {
        Rectangle rectangle = scene.getBounds ();
        BufferedImage bi = new BufferedImage (rectangle.width, rectangle.height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = bi.createGraphics ();
        scene.paint (graphics);
        graphics.dispose ();

        JFileChooser chooser = new JFileChooser ();
        chooser.setDialogTitle (NbBundle.getMessage (ExportFlowAsImageAction.class, "TITLE_ExportFlowAsImage")); // NOI18N
        chooser.setDialogType (JFileChooser.SAVE_DIALOG);
        chooser.setMultiSelectionEnabled (false);
        chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
        chooser.setFileFilter (new FileFilter() {
            public boolean accept (File file) {
                return file.isDirectory ()  ||  file.getName ().toLowerCase ().endsWith (".png"); // NOI18N
            }
            public String getDescription () {
                return NbBundle.getMessage (ExportFlowAsImageAction.class, "LBL_FileFilterPNG"); // NOI18N
            }
        });
        if (chooser.showSaveDialog (scene.getView ()) != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile ();
        if (! file.getName ().toLowerCase ().endsWith (".png")) // NOI18N
            file = new File (file.getParentFile (), file.getName () + ".png"); // NOI18N
        if (file.exists ()) {
            DialogDescriptor descriptor = new DialogDescriptor (
                    NbBundle.getMessage (ExportFlowAsImageAction.class, "LBL_AlreadyExists", file.getAbsolutePath ()), // NOI18N
                    NbBundle.getMessage (ExportFlowAsImageAction.class, "TITLE_AlreadyExists"), // NOI18N
                    true, DialogDescriptor.YES_NO_OPTION, DialogDescriptor.NO_OPTION, null);
            DialogDisplayer.getDefault ().createDialog (descriptor).setVisible (true);
            if (descriptor.getValue () != DialogDescriptor.YES_OPTION)
                return;
        }

        try {
            ImageIO.write (bi, "png", file); // NOI18N
        } catch (IOException e) {
            throw Debug.error (e);
        }

    }

    public void setDesignDocument (DesignDocument designDocument) {
        document = designDocument;
    }

}
