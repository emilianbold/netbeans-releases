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
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PaletteDescriptor;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midp.components.MidpJavaSupport;
import org.netbeans.modules.vmd.midp.palette.MidpPaletteProvider;
import org.netbeans.modules.vmd.midpnb.components.handlers.SVGMenuEventHandlerCD;
import org.netbeans.modules.vmd.midpnb.components.items.TableItemCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleCancellableTaskCD;
import org.netbeans.modules.vmd.midpnb.components.resources.SimpleTableModelCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGPlayerCD;
import org.netbeans.modules.vmd.midpnb.palette.MidpNbPaletteProvider;

/**
 *
 * @author Karol Harezlak
 * @author Anton Chechel
 */
public abstract class CustomComponentProducer extends ComponentProducer {

    public CustomComponentProducer(TypeID typeID, PaletteDescriptor paletteDescriptor) {
        super(typeID.toString(), typeID, paletteDescriptor);
    }

    public Result createComponent(DesignDocument document) {
        return new Result(document.createComponent(getComponentTypeID()));
    }

    public boolean checkValidity(DesignDocument document) {
        return true;
    }

    public static final class SVGPlayerProducer extends CustomComponentProducer {

        public SVGPlayerProducer() {
            super(SVGPlayerCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG,
                    "SVG Player", "SVG Player", SVGPlayerCD.ICON_PATH, SVGPlayerCD.ICON_LARGE_PATH)); // NOI18N
        }

        @Override
        public boolean checkValidity(DesignDocument document) {
            return MidpJavaSupport.checkValidity(document, "javax.microedition.m2g.SVGImage") &&
                   MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Canvas"); // NOI18N
        }
    }

    public static final class SVGImageProducer extends CustomComponentProducer {

        public SVGImageProducer() {
            super(SVGImageCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG,
                    "SVG Image", "SVG Image", SVGImageCD.ICON_PATH, SVGImageCD.ICON_LARGE_PATH)); // NOI18N
        }

        @Override
        public boolean checkValidity(DesignDocument document) {
            return MidpJavaSupport.checkValidity(document, "javax.microedition.m2g.SVGImage") &&
                   MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Canvas"); // NOI18N
        }
    }

    public static final class SVGMenuEventHandlerProducer extends CustomComponentProducer {

        public SVGMenuEventHandlerProducer() {
            super(SVGMenuEventHandlerCD.TYPEID, new PaletteDescriptor(MidpNbPaletteProvider.CATEGORY_SVG,
                    "SVG Menu Action", "SVG Menu Action", SVGImageCD.ICON_PATH, SVGImageCD.ICON_LARGE_PATH)); // NOI18N
        }

        @Override
        public boolean checkValidity(DesignDocument document) {
            return MidpJavaSupport.checkValidity(document, "javax.microedition.m2g.SVGImage") &&
                   MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Canvas"); // NOI18N
        }
    }

    public static final class TableItemProducer extends CustomComponentProducer {

        public TableItemProducer() {
            super(TableItemCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_ITEMS,
                    "Table Item", "Table Item", TableItemCD.ICON_PATH, TableItemCD.ICON_LARGE_PATH)); // NOI18N
        }

        @Override
        public boolean checkValidity(DesignDocument document) {
            return MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Item"); // NOI18N
        }
    }

    public static final class SimpleCancellableTaskProducer extends CustomComponentProducer {

        public SimpleCancellableTaskProducer() {
            super(SimpleCancellableTaskCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES,
                    "Simple Cancellable Task", "Simple Cancellable Task", SimpleTableModelCD.ICON_PATH, SimpleTableModelCD.ICON_LARGE_PATH)); // NOI18N
        }

        @Override
        public boolean checkValidity(DesignDocument document) {
            return MidpJavaSupport.checkValidity(document, "java.lang.Runnable"); // NOI18N
        }
    }

    public static final class SimpleTableModelProducer extends CustomComponentProducer {

        public SimpleTableModelProducer() {
            super(SimpleTableModelCD.TYPEID, new PaletteDescriptor(MidpPaletteProvider.CATEGORY_RESOURCES,
                    "Simple Table Model", "Simple Table Model", SimpleCancellableTaskCD.ICON_PATH, SimpleCancellableTaskCD.ICON_LARGE_PATH)); // NOI18N
        }

        @Override
        public boolean checkValidity(DesignDocument document) {
            return MidpJavaSupport.checkValidity(document, "javax.microedition.lcdui.Form"); // NOI18N
        }
    }
}
