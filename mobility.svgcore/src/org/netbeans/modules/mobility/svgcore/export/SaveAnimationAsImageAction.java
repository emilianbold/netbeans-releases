/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
/*
 * SaveAnimationAsImage.java
 *
 * Created on November 22, 2005, 12:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.mobility.svgcore.export;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 */
public class SaveAnimationAsImageAction extends AbstractSaveAction {
    
    /** Creates a new instance of SaveAnimationAsImage */
    public SaveAnimationAsImageAction() {
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }
        
    public static void setDialogMinimumSize(final Dialog dlg) {
        dlg.pack();
        dlg.setSize( dlg.getPreferredSize());
        
        dlg.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = dlg.getWidth();
                int h = dlg.getHeight();
                final Dimension minSize = dlg.getPreferredSize();

                int _w = Math.max( w, minSize.width);
                int _h = Math.max( h, minSize.height);

                if ( w != _w || h != _h) {
                    dlg.setSize( new Dimension(_w, _h));
                }
            }
        });
    }
    
    protected void performAction(Node[] n) {
        SVGDataObject doj = (SVGDataObject) n[0].getLookup().lookup(SVGDataObject.class);
        if (doj != null){   
            int state = getAnimatorState(doj);
            float time = stopAnimator(doj);
            try {
                final SVGAnimationRasterizerPanel panel = new SVGAnimationRasterizerPanel(doj);
                final DialogDescriptor      dd    = new DialogDescriptor(panel, NbBundle.getMessage(SaveAnimationAsImageAction.class, "TITLE_AnimationExport"));

                panel.addPropertyChangeListener(DialogDescriptor.PROP_VALID,
                        new PropertyChangeListener() {
                            public void propertyChange(PropertyChangeEvent evt) {
                                dd.setValid(panel.isDialogValid());
                            }
                        });

                Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                setDialogMinimumSize( dlg);
                dd.setValid(panel.isDialogValid());
                dlg.setVisible(true);

                panel.stopProcessing();
                if (dd.getValue() == DialogDescriptor.OK_OPTION
                        && panel.isExportConfirmed())
                {
                    AnimationRasterizer.export(doj, panel);
                }
            } catch( javax.imageio.IIOException e) {
                String msg = NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "MSG_IMG_ENCODING_ERROR") + ": " +
                             e.getLocalizedMessage();
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message( msg, NotifyDescriptor.ERROR_MESSAGE));
            } catch( Exception e) {
                SceneManager.error("Animation export failed", e);
            }
            resumeAnimatorState(doj, state, time);
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(SaveAnimationAsImageAction.class, "LBL_ExportAnimationAction"); //NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }    

    protected int mode() {
        return CookieAction.MODE_ONE;
    }

    protected Class[] cookieClasses() {
        return new Class[] {
            SVGDataObject.class
        };
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

}
