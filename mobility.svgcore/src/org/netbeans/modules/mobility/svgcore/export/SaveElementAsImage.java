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
package org.netbeans.modules.mobility.svgcore.export;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.m2g.SVGImage;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SVGObject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Pavel Benes, suchys, akorostelev
 */
public final class SaveElementAsImage extends AbstractSaveAction {// implements Presenter.Popup, Presenter.Menu {

    @Override
    protected void performAction(Node[] activatedNodes) {
        Lookup l = activatedNodes[0].getLookup();
        SVGDataObject doj = (SVGDataObject) l.lookup(SVGDataObject.class);
        SVGObject svgObj = (SVGObject) l.lookup(SVGObject.class);
        SVGImage image = (SVGImage) l.lookup(SVGImage.class);

        if (svgObj != null && image != null) {
            int state = getAnimatorState(doj);
            float time = stopAnimator(doj);
            try {
                final SVGImageRasterizerPanel panel = new SVGImageRasterizerPanel(doj, svgObj.getElementId());
                int height = panel.getImageHeight();
                int width = panel.getImageWidth();
                if (height < 1 || width < 1) {
                    // TODO write to status bar
                    Logger.getLogger(SaveElementAsImage.class.getName()).log(Level.WARNING, "Can't export image image: incorrect size"); // NOI18N
                    return;
                }

                final DialogDescriptor dd = new DialogDescriptor(panel,
                        NbBundle.getMessage(SaveElementAsImage.class, "TITLE_ImageExport")); // NOI18N

                panel.addPropertyChangeListener(DialogDescriptor.PROP_VALID,
                        new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                dd.setValid(panel.isDialogValid());
                            }
                        });

                Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                SaveAnimationAsImageAction.setDialogMinimumSize(dlg);
                dd.setValid(panel.isDialogValid());
                dlg.setVisible(true);

                if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                    AnimationRasterizer.export(doj, panel);
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            resumeAnimatorState(doj, state, time);
        }
    }

    @Override
    protected int mode() {
        return CookieAction.MODE_ANY;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SaveElementAsImage.class, "CTL_SVGExportAction"); //NOI18N
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[]{SVGDataObject.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    /**
     * First method to render a single element, based on the 226 API.
     *
     * @param elt the SVGLocatableElement to render
     * @param svgImage the containing SVGImage
     * @param doc the related Document
     * @param svg the root SVG element.
     */
    /*
    private void renderElement(SVGDataObject doj, final SVGLocatableElement elt,
    final SVGImage svgImage ) {
    FileObject primaryFile = doj.getPrimaryFile();
    
    J2MEProject project = null;
    Project p = FileOwnerQuery.getOwner (primaryFile);
    if (p != null && p instanceof J2MEProject){
    project = (J2MEProject) p;
    }
    
    //SVGRasterizerPanel panel = new SVGRasterizerPanel(ScreenSizeHelper.getCurrentDeviceScreenSize(primaryFile, null), project != null);
    SVGAnimationRasterizerPanel panel = new SVGAnimationRasterizerPanel(doj);
    DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(SaveAnimationAsImageAction.class, "TITLE_ImageExport"));
    DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    //int imageWidth = panel.getImageWidth();
    //int imageHeigth = panel.getImageHeigth();
    //AnimationRasterizer.ImageType imageType = panel.getFormat();
    //float compressionQuality = panel.getCompressionQuality();
    //boolean progressive = panel.isProgressive();        
    //boolean forAllConfig = panel.isForAllConfigurations();
    
    if (dd.getValue() == DialogDescriptor.OK_OPTION){
    //
    // The following adjusts the element's transform so that its user space is identical
    // to the root element's user space.
    //
    int h = svgImage.getViewportHeight();
    int w = svgImage.getViewportWidth();
    
    SVGSVGElement svg = (SVGSVGElement) svgImage.getDocument().getDocumentElement();
    
    // svg -> screen
    SVGMatrix svgCTM = svg.getScreenCTM();
    
    // element -> svg -> screen
    SVGMatrix eltCTM = elt.getScreenCTM();
    
    // screen -> svg
    SVGMatrix svgICTM = svgCTM.inverse();
    
    // elt-> svg matrix
    SVGMatrix eltToSvg = svgICTM.mMultiply(eltCTM);
    
    // The current elt transform, if any
    SVGMatrix origTxf = elt.getMatrixTrait("transform");
    SVGMatrix eltTxf = elt.getMatrixTrait("transform");
    
    SVGMatrix toSvgSpace= eltTxf.mMultiply(eltToSvg.inverse());
    
    // Get the current viewBox
    SVGRect viewBox = svg.getRectTrait("viewBox");
    
    // Get the overall content's bounding box, including our element.
    SVGRect allBBox = svg.getBBox();
    
    // Now, move our element 'away' from all content (move it to the right of the content)
    SVGRect bbox = elt.getBBox();
    toSvgSpace.mTranslate(-bbox.getX() + allBBox.getX() + allBBox.getWidth(), 0);
    elt.setMatrixTrait("transform", toSvgSpace);
    
    // Now, set the new viewBox.
    bbox.setX(allBBox.getX() + allBBox.getWidth());
    svg.setRectTrait("viewBox", bbox);
    
    AnimationRasterizer.exportElement(primaryFile, project, svgImage, elt.getId(), panel);
    
    svg.setRectTrait("viewBox", viewBox);
    elt.setMatrixTrait("transform", origTxf);
    svgImage.setViewportWidth(w);
    svgImage.setViewportHeight(h);
    }
     */
//    /**
//     * The overlay image.
//     */
//        BufferedImage bi = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
//        
//        Graphics g = bi.createGraphics();
//        g.setColor(Color.black);
//        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
//        ScalableGraphics sg = ScalableGraphics.createInstance();
//        sg.bindTarget(g);
//        
//        // To avoid still rendering content outside the viewport, we set the viewport
//        // to have the same aspect ratio as the viewBox.
//        // We know the height is non-zero because we got here through hit detection.
//        float ar = bbox.getWidth() / bbox.getHeight();
//        int iw = svgImage.getViewportWidth();
//        int ih = svgImage.getViewportHeight();
//        int vw = 0;
//        int vh = 0;
//        if (ar > 1) {
//            vw = bi.getWidth();
//            vh = (int) (vw / ar);
//        } else {
//            vh = bi.getHeight();
//            vw = (int) (vh * ar);
//        }
//        svgImage.setViewportWidth(vw);
//        svgImage.setViewportHeight(vh);
//        sg.render(0, 0, svgImage);
//        sg.releaseTarget();
    // Restore values.
//    }
//    public JMenuItem getPopupPresenter() {
//        JMenuItem result = new JMenuItem("Export...");  //remember JMenu is a subclass of JMenuItem
//        return result;
//    }
}
