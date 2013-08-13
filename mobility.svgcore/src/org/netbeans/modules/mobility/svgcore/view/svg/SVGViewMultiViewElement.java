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
package org.netbeans.modules.mobility.svgcore.view.svg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.netbeans.modules.mobility.svgcore.palette.SVGPaletteFactory;
import org.netbeans.modules.xml.multiview.AbstractMultiViewElement;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Pavel Benes
 */
public final class SVGViewMultiViewElement extends AbstractMultiViewElement {
    private static final long serialVersionUID = 7526471457562007148L;        
    private static final Logger LOG = Logger.getLogger(SVGViewMultiViewElement.class.getName());
    
    private transient SVGViewTopComponent svgView = null;

    public SVGViewMultiViewElement(SVGDataObject obj) {
        super(obj);
    }
    
    public void componentActivated() {
        if (svgView != null) {
            svgView.componentActivated();
        }
    }

    public void componentDeactivated() {        
        if (svgView != null) {
            svgView.componentDeactivated();
        }
    }
    
    public void componentClosed() {
        super.componentClosed();
        if (svgView != null) {
            svgView.componentClosed();
        }
    }

    public void componentHidden() {
        if (svgView != null) {
            svgView.componentHidden();
            getDataObject().setMultiViewElement(null);
        }
    }

    public void componentOpened() {
        super.componentOpened();
        SVGFileModel svgModel = getDataObject().getModel();
        if (svgModel.getModel() != null) {
            svgModel.attachToOpenedDocument();
            if (svgView != null) {
                svgView.componentOpened();
            }
        } else {
            LOG.log(Level.WARNING, "Can not attachToOpenedDocument. document model is not loaded.");
        }
    }

    public void componentShowing() {
        svgView.onShow();
        getDataObject().setMultiViewElement(this);
        dObj.setLastOpenView( SVGDataObject.SVG_VIEW_INDEX);
    }

    /*    
    public Action[] getActions() {
        return dObj.getNodeDelegate().getActions(false);
    }
     */    
    
    public Lookup getLookup() {
        Lookup palette = null;

        try {
            palette = Lookups.singleton( SVGPaletteFactory.getPalette());
        } catch( IOException e) {
            SceneManager.error("Palette could not be created.", e); //NOI18N
        }

        if (svgView == null) {
            getVisualRepresentation();
        }
        Lookup[] lookup;
        if (palette == null) {
            lookup = new Lookup[] { svgView.getLookup(), dObj.getNodeDelegate().getLookup()}; 
        } else {
            lookup = new Lookup[] { svgView.getLookup(), palette, dObj.getNodeDelegate().getLookup()}; 
        }
        return new ProxyLookup( lookup);
    }

    public JComponent getToolbarRepresentation() {
        getVisualRepresentation();
        return svgView.getToolbar();
    }

    public synchronized JComponent getVisualRepresentation() {
        if (svgView == null) {
            svgView = new SVGViewTopComponent((SVGDataObject) dObj);
            
        }
        return svgView;
    }
    
    private SceneManager getSceneManager() {
        return getDataObject().getSceneManager();
    }
    
    private SVGDataObject getDataObject() {
        return (SVGDataObject) this.dObj;
    }
    
    private void readObject(ObjectInputStream in) {
        try {
            in.defaultReadObject();
            getSceneManager().deserialize(in);        
        } catch( Exception e) {
            e.printStackTrace();
        }
    }
    
    private void writeObject(ObjectOutputStream out) {
        try {
            out.defaultWriteObject();
            getSceneManager().serialize(out);
        } catch( Exception e) {
            e.printStackTrace();
        }    
    }    
}
