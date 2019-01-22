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
 * SvgAnimatorHelper.java
 *
 * Created on February 23, 2006, 5:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.microedition.svg;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Ticker;
import javax.microedition.m2g.SVGAnimator;
import javax.microedition.m2g.SVGEventListener;
import javax.microedition.m2g.SVGImage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimationElement;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRGBColor;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * This is wrapper class, which exposes some of the setters avilable from
 * SVGAnimator owned canvas to this wrapper, so it can be used as a component
 * in VisualDesigner. It also adds many utility methods which can be used
 * to easily manipulate the underlying document of the animated SVG image.
 * <p>
 * In this version this class simply extends SVGPlayer and does not define any 
 * methods/fields. It is kept here only for backward compatibility reasons
 * 
 *
 * @deprecated This class has been deprected with 6.0 version of NetBeans. Please
 * use {@link SVGPlayer} instead of it.
 */
public class SVGAnimatorWrapper extends SVGPlayer {
    
    /** 
     * Creates a new instance of SvgAnimatorHelper. It requires SVGImage to be animated
     * and display.
     *
     * <p/> Please note, supplied SVGImage shouldn't be reused in other SVGAnimator.
     *
     * @deprecated This class has been deprected with 6.0 version of NetBeans. Please
     * use {@link SVGPlayer} instead of it.
     */
    public SVGAnimatorWrapper(SVGImage svgImage, Display display) throws IllegalArgumentException {
        super(svgImage,display);
    }
    
}
