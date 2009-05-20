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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.microedition.m2g.SVGImage;
import org.netbeans.modules.mobility.svgcore.util.Util;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.PropertyValue.Kind;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGListCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGListElementEventSourceCD;
import org.w3c.dom.svg.SVGLocatableElement;
import org.w3c.dom.svg.SVGRect;

/**
 *
 * @author akorostelev
 */
public class SVGListDisplayPresenter extends UpdatableSVGComponentDisplayPresenter {

    private static final String CONTENT = "content";          // NOI18N
    private static final String HIDDEN_TEXT = "hidden_text";      // NOI18N
    private static final String BOUNDS = "bound";            // NOI18N
    private static final String CONTENT_SUFFIX = DASH + CONTENT;
    private static final String BOUNDS_SUFIX = DASH + BOUNDS;
    private static final String HIDDEN_TEXT_SUFFIX = DASH + HIDDEN_TEXT;
    protected static final String TRAIT_FONT_SIZE = "font-size";        // NOI18N
    protected static final String TRAIT_FONT_FAMILY = "font-family";      // NOI18N
    protected static final String TEXT = "text";             // NOI18N
    public static final String METADATA_METADATA = "text";           // NOI18N
    public static final String METADATA_DISPLAY = "display";        // NOI18N
    public static final String METADATA_NONE = "none";           // NOI18N

    @Override
    protected void reloadSVGComponent(SVGImage svgImage, DesignComponent svgComponent, String componentId) {

        SVGLocatableElement myHiddenText = (SVGLocatableElement) Util.getElementById(
                svgImage, componentId + HIDDEN_TEXT_SUFFIX);
        SVGLocatableElement myBounds = (SVGLocatableElement) Util.getElementById(
                svgImage, componentId + BOUNDS_SUFIX);
        SVGLocatableElement myContent = (SVGLocatableElement) Util.getElementById(
                svgImage, componentId + CONTENT_SUFFIX);
        if (myHiddenText == null || myBounds == null) {
            return;
        }
        float itemHeight = myHiddenText.getFloatTrait(TRAIT_FONT_SIZE);
        int listCapacity = (int) (myBounds.getBBox().getHeight() / itemHeight);
        SVGListCellRenderer renderer = new SVGListCellRenderer(svgImage.getDocument(), itemHeight, myHiddenText, myBounds, myContent);

        List<String> items = getListModelElements(svgComponent);
        renderList(items, renderer, listCapacity);
    }

    private Vector<SVGLocatableElement> renderList(List<String> items, SVGListCellRenderer renderer, int listCapacity) {
        renderer.clearContent();

        Vector<SVGLocatableElement> vector = new Vector<SVGLocatableElement>();
        int itemsCount = items.size();
        if (itemsCount == 0) {
            return vector;
        }
        int i = 0;
        do {
            SVGLocatableElement comp = renderer.getCellRendererComponent(items.get(i), i);
            vector.addElement(comp);
            i++;
        } while (i < Math.min(listCapacity, itemsCount));
        return vector;
    }

    public SVGRect getBounds(SVGLocatableElement element) {
        if (element == null) {
            return null;
        }
        SVGRect rect = element.getScreenBBox();
        return rect;
    }

    private List<String> getListModelElements(DesignComponent svgComponent) {
        List<String> itemsList = new ArrayList<String>();
        if (SVGListCD.TYPEID != svgComponent.getType()) {
            return itemsList;
        }
        PropertyValue model = svgComponent.readProperty(SVGListCD.PROP_ELEMENTS);
        if (model == null) {
            return itemsList;
        }
        if (model.getKind().equals(Kind.USERCODE)) {
            itemsList.add(USERCODE);
        } else {
            List<PropertyValue> propsList = model.getArray();
            if (propsList == null || propsList.isEmpty()) {
                return itemsList;
            }

            for (PropertyValue propertyValue : propsList) {
                PropertyValue stringValue = propertyValue.getComponent().
                        readProperty(SVGListElementEventSourceCD.PROP_STRING);
                itemsList.add((String) stringValue.getPrimitiveValue());
            }
        }
        return itemsList;
    }
}
