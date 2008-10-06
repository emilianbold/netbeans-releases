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
package org.netbeans.modules.vmd.midp.actions;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.modules.vmd.api.flow.FlowSupport;
import org.netbeans.modules.vmd.api.io.ActiveViewSupport;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;


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

    public boolean isEnabled () {
        updateDesignDocumentReference ();
        FlowScene scene = FlowSupport.getFlowSceneForDocument (document);

        if (scene == null)
            return false;

        JComponent view = scene.getView ();
        if (view == null  ||  ! view.isShowing ())
            return false;
        Rectangle rectangle = scene.getBounds ();
        return rectangle.width > 0  &&  rectangle.height > 0;
    }

    private void updateDesignDocumentReference () {
        DataObjectContext context = ActiveViewSupport.getDefault ().getActiveView ().getContext ();
        context.addDesignDocumentAwareness (this);
        context.removeDesignDocumentAwareness (this);
    }

    public void actionPerformed (ActionEvent e) {
        updateDesignDocumentReference ();
        FlowScene scene = FlowSupport.getFlowSceneForDocument (document);
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
        
        FileImageOutputStream stream = null;
        try {
            stream = new FileImageOutputStream( file );
            ImageIO.write (bi, "png", stream); // NOI18N
        }
        catch (FileNotFoundException e) {
            NotifyDescriptor descriptor = new NotifyDescriptor.Message( 
                    NbBundle.getMessage (ExportFlowAsImageAction.class, 
                            "LBL_NoWrite", file.getAbsolutePath ()) , 
                    NotifyDescriptor.   ERROR_MESSAGE );
            DialogDisplayer.getDefault().notify( descriptor );
            return;
        }
        catch (IOException e) {
            throw Debug.error(e);
        }
        finally {
            try {
                if ( stream != null ){
                    stream.close();
                }
            }
            catch (IOException e) {
                Debug.error(e);
            }
        }

        
            
        

    }

    public void setDesignDocument (DesignDocument designDocument) {
        document = designDocument;
    }

}
