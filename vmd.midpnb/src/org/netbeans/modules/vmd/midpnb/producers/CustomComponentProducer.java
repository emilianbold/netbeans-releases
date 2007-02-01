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


import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
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
            super(WaitScreenCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, "Wait Screen", "Wait Screen", WaitScreenCD.ICON_PATH, WaitScreenCD.ICON_LARGE_PATH)); // NOI18N
        }
    }
    
    public static final class SplashScreen extends CustomComponentProducer {
        public SplashScreen() {
            super(SplashScreenCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_DISPLAYABLES, "Splash Screen", "Splash Screen", SplashScreenCD.ICON_PATH, SplashScreenCD.ICON_LARGE_PATH)); // NOI18N
        }
    }
    
    public static final class SVGSplashScreen extends CustomComponentProducer {
        public SVGSplashScreen() {
            super(SplashScreenCD.TYPEID, new PaletteDescriptor(CATEGORY_SVG, "SVG Splash Screen", "SVG Splash Screen", SVGSplashScreenCD.ICON_PATH, SVGSplashScreenCD.ICON_LARGE_PATH)); // NOI18N
        }
    }
    
    public static final class SVGWaitScreen extends CustomComponentProducer {
        public SVGWaitScreen() {
            super(SVGWaitScreenCD.TYPEID, new PaletteDescriptor(CATEGORY_SVG, "SVG Wait Screen", "SVG Wait Screen", SVGWaitScreenCD.ICON_PATH, SVGWaitScreenCD.ICON_LARGE_PATH)); // NOI18N
        }
    }
    
    public static final class SVGAnimatorWrapper extends CustomComponentProducer {
        public SVGAnimatorWrapper() {
            super(SVGAnimatorWrapperCD.TYPEID, new PaletteDescriptor(CATEGORY_SVG, "SVG Animator Wrapper", "SVG Animator Wrapper", SVGAnimatorWrapperCD.ICON_PATH, SVGAnimatorWrapperCD.ICON_LARGE_PATH)); // NOI18N
        }
    }
    
    public static final class SVGImage extends CustomComponentProducer {
        public SVGImage() {
            super(SVGImageCD.TYPEID, new PaletteDescriptor(CATEGORY_SVG, "SVG Image", "SVG Image", ResourceCD.ICON_PATH, null)); // NOI18N
        }
    }
    
    public static final class SVGMenu extends CustomComponentProducer {
        public SVGMenu() {
            super(SVGMenuCD.TYPEID, new PaletteDescriptor(CATEGORY_SVG, "SVG Image", "SVG Image", SVGMenuCD.ICON_PATH, SVGMenuCD.ICON_LARGE_PATH)); // NOI18N
        }
    }
    
    public static final class SVGMenuItem extends CustomComponentProducer {
        public SVGMenuItem() {
            super(SVGMenuItemCD.TYPEID, new PaletteDescriptor(CATEGORY_SVG, "SVG Image Item", "SVG Image Item", SVGMenuItemCD.ICON_PATH, null)); // NOI18N
        }
    }
    
    public static final class TableItem extends CustomComponentProducer {
        public TableItem() {
            super(TableItemCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS, "Table Item", "Table Item", TableItemCD.ICON_PATH, null)); // NOI18N
        }
    }
    
    public static final class SimpleCancellableTask extends CustomComponentProducer {
        public SimpleCancellableTask() {
            super(SimpleCancellableTaskCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES, "Simple Cancellable Task", "Simple Cancellable Task", ResourceCD.ICON_PATH, null)); // NOI18N
        }
    }
    
     public static final class SimpleTableModel extends CustomComponentProducer {
        public SimpleTableModel() {
            super(SimpleTableModelCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES, "Simple Table Model", "Simple Table Model", ResourceCD.ICON_PATH, null)); // NOI18N
        }
    }
}
