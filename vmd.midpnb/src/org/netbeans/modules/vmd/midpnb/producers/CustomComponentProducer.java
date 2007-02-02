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

package org.netbeans.modules.vmd.midpnb.producers;


import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.components.resources.ResourceCD;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midpnb.components.displayables.SplashScreenCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.WaitScreenCD;
import org.netbeans.modules.vmd.midpnb.components.items.TableItemCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGAnimatorWrapperCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuItemCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGSplashScreenCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGWaitScreenCD;

/**
 *
 * @author Karol Harezlak
 */
public abstract class CustomComponentProducer extends ComponentProducer {
    
    public static final String CATEGORY_SVG = "SVG Components"; // NOI18N
    
    public CustomComponentProducer(TypeID typeID, PaletteDescriptor paletteDescriptor) {
        super(typeID.toString(), typeID, paletteDescriptor);
    }

    public Result createComponent(DesignDocument document) {
        return new Result(document.createComponent(getComponentTypeID()));
    }

    public boolean checkValidity(DesignDocument document) {
        return MidpJavaSupport.checkValidity(document, getComponentTypeID());
    }

    public static final class WaitScreen extends CustomComponentProducer {
        public WaitScreen() {
            super(WaitScreenCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, "WaitScreen", "WaitScreen", WaitScreenCD.ICON_PATH, WaitScreenCD.ICON_LARGE_PATH)); // NOI18N
        }
    }
    
    public static final class SplashScreen extends CustomComponentProducer {
        public SplashScreen() {
            super(SplashScreenCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, "SplashScreen", "SplashScreen", SplashScreenCD.ICON_PATH, SplashScreenCD.ICON_LARGE_PATH)); // NOI18N
        }
    }
    
    public static final class SVGSplashScreen extends CustomComponentProducer {
        public SVGSplashScreen() {
            super(SplashScreenCD.TYPEID, new PaletteDescriptor(CATEGORY_SVG, "SVGSplashScreen", "SVGSplashScreen", SVGSplashScreenCD.ICON_PATH, SVGSplashScreenCD.ICON_LARGE_PATH)); // NOI18N
        }
    }
    
    public static final class SVGWaitScreen extends CustomComponentProducer {
        public SVGWaitScreen() {
            super(SVGWaitScreenCD.TYPEID, new PaletteDescriptor(CATEGORY_SVG, "SVGWaitScreen", "SVGWaitScreen", SVGWaitScreenCD.ICON_PATH, SVGWaitScreenCD.ICON_LARGE_PATH)); // NOI18N
        }
    }
    
    public static final class SVGAnimatorWrapper extends CustomComponentProducer {
        public SVGAnimatorWrapper() {
            super(SVGAnimatorWrapperCD.TYPEID, new PaletteDescriptor(CATEGORY_SVG, "SVGAnimatorWrapper", "SVGAnimatorWrapper", SVGAnimatorWrapperCD.ICON_PATH, SVGAnimatorWrapperCD.ICON_LARGE_PATH)); // NOI18N
        }
    }
    
    public static final class SVGImage extends CustomComponentProducer {
        public SVGImage() {
            super(SVGImageCD.TYPEID, new PaletteDescriptor(CATEGORY_SVG, "SVGImage", "SVGImage", ResourceCD.ICON_PATH, null)); // NOI18N
        }
    }
    
    public static final class SVGMenu extends CustomComponentProducer {
        public SVGMenu() {
            super(SVGMenuCD.TYPEID, new PaletteDescriptor(CATEGORY_SVG, "SVGImage", "SVGImage", SVGMenuCD.ICON_PATH, SVGMenuCD.ICON_LARGE_PATH)); // NOI18N
        }
    }
    
    public static final class SVGMenuItem extends CustomComponentProducer {
        public SVGMenuItem() {
            super(SVGMenuItemCD.TYPEID, new PaletteDescriptor(CATEGORY_SVG, "SVGImageItem", "SVGImageItem", SVGMenuItemCD.ICON_PATH, null)); // NOI18N
        }
    }
    
    public static final class TableItem extends CustomComponentProducer {
        public TableItem() {
            super(TableItemCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS, "TableItem", "TableItem", TableItemCD.ICON_PATH, null)); // NOI18N
        }
    }
    
    public static final class SimpleCancellableTask extends CustomComponentProducer {
        public SimpleCancellableTask() {
            super(SimpleCancellableTaskCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES, "SimpleCancellableTask", "SimpleCancellableTask", ResourceCD.ICON_PATH, null)); // NOI18N
        }
    }
    
     public static final class SimpleTableModel extends CustomComponentProducer {
        public SimpleTableModel() {
            super(SimpleTableModelCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES, "SimpleTableModel", "SimpleTableModel", ResourceCD.ICON_PATH, null)); // NOI18N
        }
    }
}
