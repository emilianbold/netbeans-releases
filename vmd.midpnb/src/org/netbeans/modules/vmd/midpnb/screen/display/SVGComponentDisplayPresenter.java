/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.screen.display;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.m2g.SVGImage;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.mobility.svgcore.util.Util;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.screen.display.ScreenDeviceInfo;
import org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter;
import org.netbeans.modules.vmd.api.screen.display.ScreenPropertyDescriptor;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGComponentCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGComponentEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author akorostelev
 */
public class SVGComponentDisplayPresenter extends ScreenDisplayPresenter {
    
    // component bounds rectangle
    private Rectangle myBoundsRect = null;
    private JPanel myView = null;

    @Override
    public boolean isTopLevelDisplay() {
        return false;
    }

    @Override
    public Collection<DesignComponent> getChildren() {
        return Collections.emptyList();
    }

    /**
     * Unlike implementation in super class, returns JComponent,
     * that will not be really shown to user. Component's location is also will be
     * got using #getLocation(), but not #getView().getLocation().
     * <p>
     * JComponent returned by this method will be added to the same parent
     * as SVG image for correct calculation of view location related to top component.
     * @see org.netbeans.modules.vmd.api.screen.display.ScreenDisplayPresenter#getView() 
     * @return the view
     */
    @Override
    public JComponent getView() {
        if (myView == null) {
            myView = new JPanel();
            myView.setPreferredSize(new java.awt.Dimension(0, 0));
        }
        return myView;
    }

    @Override
    public void reload(ScreenDeviceInfo deviceInfo) {
        myBoundsRect = null;
        myView = null;
    }

    @Override
    public Point getLocation() {
        Rectangle rect = getRectangle();
        return rect == null ? super.getLocation() : rect.getLocation();
    }

    @Override
    public Shape getSelectionShape() {
        Rectangle rect = getRectangle();
        if (rect != null) {
            return new Rectangle((int) rect.getWidth(), (int) rect.getHeight());
        }
        return null;
    }

    @Override
    public Collection<ScreenPropertyDescriptor> getPropertyDescriptors() {
        return Collections.emptyList();
    }

    /**
     * returns svg component to which this presenter is attached, or,
     * if presenter is attached to SVGComponentEventSourceCD child,
     * svg component connected with this SVGComponentEventSourceCD.
     * @return svg component
     */
    protected DesignComponent getSvgComponent(){
        DesignComponent svgComponent = null;
        DescriptorRegistry registry = getComponent().getDocument().getDescriptorRegistry();

        // this presenter is attached to SVGComponentCD successor
        if(registry.isInHierarchy(SVGComponentCD.TYPEID, getComponent().getType())) {
            return getComponent();
        }
        // this presenter is attached to SVGComponentEventSourceCD child
        if (getComponent().getComponentDescriptor().getPropertyDescriptor(
                SVGComponentEventSourceCD.PROP_SVGCOMPONENT) == null)
        {
            // it is not SVGComponentEventSourceCD. So it has no connected svg component
            return null;
        }
        PropertyValue value = getComponent().readProperty(
                SVGComponentEventSourceCD.PROP_SVGCOMPONENT);
        if (value != null && value.getComponent() != null) {
            svgComponent = value.getComponent();
        }

        return svgComponent;
    }

    protected String getSvgComponentId(){
        DesignComponent svgComponent = getSvgComponent();
        if (svgComponent == null){
            return null;
        }
        return (String) svgComponent.readProperty(SVGComponentCD.PROP_ID).getPrimitiveValue();
    }

    protected SVGImage getSvgImage(){
        SVGImage svgImage = null;
        DesignComponent svgForm = getComponent().getParentComponent();
        if (svgForm == null) {
            return null;
        }
        SVGPlayerDisplayPresenter presenter = svgForm.getPresenter(SVGPlayerDisplayPresenter.class);
        if (presenter != null) {
            svgImage = presenter.getSVGImage();

        }
        if (svgImage == null) {
            DesignComponent svgImageComponent = svgForm.readProperty(SVGFormCD.PROP_SVG_IMAGE).getComponent();
            if (svgImageComponent == null) {
                return null;
            }
            FileObject file = SVGFormSupport.getSVGFile(svgImageComponent);
            if (file == null){
                return null;
            }
            try {
                svgImage = Util.createSVGImage(file, true);
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, null, ex);
            }
        }
        return svgImage;

    }

    private Rectangle doCalculateRectangle() {
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        int correctionX = 0;
        int correctionY = 0;

        DesignComponent svgForm = getComponent().getParentComponent();
        if (svgForm == null) {
            return null;
        }
        SVGPlayerDisplayPresenter presenter = svgForm.getPresenter(SVGPlayerDisplayPresenter.class);
        if (presenter != null) {
            scaleX = presenter.getScaleX();
            scaleY = presenter.getScaleY();
            correctionX = presenter.getCorrectionX();
            correctionY = presenter.getCorrectionY();
        }

        String id = getSvgComponentId();
        if (id == null){
            return null;
        }

        SVGImage svgImage = getSvgImage();
        if (svgImage == null){
            return null;
        }
        Rectangle rect = Util.getElementRectangle(svgImage, id);
        if (rect != null) {
            return new Rectangle(
                    (int) Math.round(rect.getX() * scaleX) + correctionX,
                    (int) Math.round(rect.getY() * scaleY) + correctionY,
                    (int) Math.round(rect.getWidth() * scaleX) + 1,
                    (int) Math.round(rect.getHeight() * scaleY) + 1);
        }
        return null;
    }

    private Rectangle getRectangle() {
        if (myBoundsRect == null) {
            myBoundsRect = doCalculateRectangle();
        }
        return myBoundsRect;
    }

}
