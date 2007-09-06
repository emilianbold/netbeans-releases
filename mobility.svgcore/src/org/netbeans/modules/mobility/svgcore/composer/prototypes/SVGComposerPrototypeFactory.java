/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 */

package org.netbeans.modules.mobility.svgcore.composer.prototypes;

import com.sun.perseus.builder.SVGTinyModelFactory;
import com.sun.perseus.model.*;
import com.sun.perseus.model.DocumentNode;
import com.sun.perseus.model.SVG;
import com.sun.perseus.util.SVGConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author Pavel Benes
 */
@SuppressWarnings({"unchecked"})
public abstract class SVGComposerPrototypeFactory  {
    private static final Map<DocumentNode, Vector> s_prototypeMap = new HashMap<DocumentNode, Vector>();
    
    public static synchronized Vector getPrototypes(final DocumentNode doc) {
       assert doc != null;
       
       Vector v;
       if ( (v=s_prototypeMap.get(doc)) == null) {
           v = new Vector();

            //
            // == Structure Module =================================================
            //
            v.addElement(new SVG(doc));
            v.addElement(new PatchedGroup(doc));
            v.addElement(new PatchedUse(doc));
            v.addElement(new PatchedDefs(doc));
            v.addElement(new PatchedImageNode(doc));
            v.addElement(new PatchedSwitch(doc));
            v.addElement(new PatchedSymbol(doc));

            // 
            // == Shape Module =====================================================
            //
            v.addElement(new PatchedShapeNode(doc, SVGConstants.SVG_PATH_TAG));
            v.addElement(new PatchedRect(doc));
            v.addElement(new PatchedLine(doc));
            v.addElement(new PatchedEllipse(doc));
            v.addElement(new PatchedEllipse(doc, true)); // <circle>
            v.addElement(new PatchedShapeNode(doc, SVGConstants.SVG_POLYGON_TAG));
            v.addElement(new PatchedShapeNode(doc, SVGConstants.SVG_POLYLINE_TAG));

            // 
            // == Text Module ======================================================
            //
            v.addElement(new PatchedText(doc));
            //// v.addElement(new TSpan(doc));

            //
            // == Font Module ======================================================
            //
            v.addElement(new PatchedFont(doc));
            v.addElement(new PatchedFontFace(doc));
            v.addElement(new PatchedGlyph(doc));
            v.addElement(new PatchedGlyph(doc, SVGConstants.SVG_MISSING_GLYPH_TAG));
            v.addElement(new PatchedHKern(doc)); 

            // 
            // == Hyperlinking Module ==============================================
            //
            v.addElement(new PatchedAnchor(doc));
           
            // 
            // == Animation Module =================================================
            //
            v.addElement(new PatchedAnimate(doc));
            v.addElement(new PatchedAnimateMotion(doc));
            v.addElement(new PatchedSet(doc));
            v.addElement(new PatchedAnimateTransform(doc));
            v.addElement(new PatchedAnimate(doc, SVGConstants.SVG_ANIMATE_COLOR_TAG));

            //
            // == SolidColor Module ================================================
            //
            v.addElement(new PatchedSolidColor(doc));

            //
            // == Gradient Module ================================================
            //
            v.addElement(new PatchedLinearGradient(doc));
            v.addElement(new PatchedRadialGradient(doc));
            v.addElement(new PatchedStop(doc));

            //
            // == Extensibility Module =========================================
            //
            v.addElement(new PatchedStrictElement(doc, 
                                           SVGConstants.SVG_FOREIGN_OBJECT_TAG,
                                           SVGConstants.SVG_NAMESPACE_URI,
                                           SVGTinyModelFactory.FOREIGN_OBJECT_REQUIRED_ATTRIBUTES,
                                           null));
            //
            // == Medial Module ================================================
            //
            // v.addElement(new Audio(doc));
            s_prototypeMap.put(doc, v);
       }

        return v;
    }    
}
