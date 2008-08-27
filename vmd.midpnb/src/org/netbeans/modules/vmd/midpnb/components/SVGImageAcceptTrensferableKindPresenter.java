/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.vmd.midpnb.components;

import java.awt.datatransfer.Transferable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.vmd.api.model.ComponentProducer;
import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.api.model.common.DesignComponentDataFlavorSupport;
import org.netbeans.modules.vmd.midp.components.MidpAcceptTrensferableKindPresenter;
import org.netbeans.modules.vmd.midp.components.MidpProjectSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormSupport;
import org.netbeans.modules.vmd.midpnb.components.svg.parsers.SVGComponentImageParser;
import org.netbeans.modules.vmd.midpnb.components.svg.parsers.SVGFormImageParser;
import org.netbeans.modules.vmd.midpnb.components.svg.parsers.SVGMenuImageParser;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Anton Chechel
 */
public class SVGImageAcceptTrensferableKindPresenter extends MidpAcceptTrensferableKindPresenter {

    @Override
    public Result accept(Transferable transferable, AcceptSuggestion suggestion) {
        DesignComponent component = DesignComponentDataFlavorSupport.getTransferableDesignComponent(transferable);
        String propertyName = typesMap.get(component.getType());
        if (propertyName == null) {
            throw new IllegalStateException();
        }

        DesignComponent svgPlayer = getComponent();
        svgPlayer.writeProperty(propertyName, PropertyValue.createComponentReference(component));

        if (isAcceptableForComponent(svgPlayer)) {
            Set<FileObject> images = getImagesFO(svgPlayer, component);
            if (getComponent().getDocument().getDescriptorRegistry().isInHierarchy(SVGFormCD.TYPEID, getComponent().getType())) {
                SVGFormSupport.removeAllSVGFormComponents(getComponent());
            }
            for (FileObject img : images) {
                SVGFormSupport.parseSVGImageItems(img, svgPlayer);
            }
        }

        return new ComponentProducer.Result(component);
    }

    /**
     * almost duplicates 'if' clause used in SVGComponentImageParser.getParserByComponent,
     * but if provided components in svg menu, this methid checks if it already contains elements
     * @param svgComponent
     * @return
     */
    private boolean isAcceptableForComponent(DesignComponent svgComponent){
        DescriptorRegistry descrRegistry = svgComponent.getDocument().getDescriptorRegistry();
        TypeID typeID = svgComponent.getType();
        if (descrRegistry.isInHierarchy(SVGMenuCD.TYPEID, typeID)) {
            if (svgComponent.readProperty(SVGMenuCD.PROP_ELEMENTS).getArray().size() == 0) {
                return true;
            }
        } else if (descrRegistry.isInHierarchy(SVGFormCD.TYPEID, typeID)) {
            return true;
        }
        return false;
    }

    private Set<FileObject> getImagesFO(DesignComponent parentComponent, 
            DesignComponent transferableComponent)
    {
        PropertyValue propertyValue = transferableComponent.readProperty(SVGImageCD.PROP_RESOURCE_PATH);
        if (propertyValue.getKind() == PropertyValue.Kind.VALUE) {
            Map<FileObject, FileObject> images 
                    = MidpProjectSupport.getFileObjectsForRelativeResourcePath(
                            parentComponent.getDocument(), 
                            MidpTypes.getString(propertyValue));
            return images.keySet();
        }
        return Collections.EMPTY_SET;
    }
    
}