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

package org.netbeans.modules.vmd.midpnb.general;

import org.netbeans.modules.vmd.api.model.ComponentProducer.Result;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.common.AcceptSuggestion;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.ResourcesCategoryCD;
import org.netbeans.modules.vmd.midp.general.FileAcceptPresenter;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGImageCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGPlayerCD;
import org.netbeans.modules.vmd.midpnb.components.svg.parsers.SVGMenuImageParser;
import org.openide.filesystems.FileObject;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormCD;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormSupport;
import org.netbeans.modules.vmd.midpnb.components.svg.parsers.SVGComponentImageParser;
import org.netbeans.modules.vmd.midpnb.components.svg.parsers.SVGFormImageParser;

/**
 *
 * @author Anton Chechel
 */
public class SVGFileAcceptPresenter extends FileAcceptPresenter {

    public SVGFileAcceptPresenter() {
        super(SVGPlayerCD.PROP_SVG_IMAGE, SVGImageCD.TYPEID, "svg"); // NOI18N
    }

    @Override
    public Result accept(Transferable transferable, AcceptSuggestion suggestion) {
        Result result = super.accept(transferable, suggestion);
        DesignComponent svgImage = result.getComponents().iterator().next();
        DesignComponent svgComponent = getComponent();
        FileObject fileObject = getNodeFileObject(transferable);
        if (fileObject == null) {
            return result;
        }

        String path = getFileClasspath(fileObject);
        svgImage.writeProperty(SVGImageCD.PROP_RESOURCE_PATH, MidpTypes.createStringValue(path));
        MidpDocumentSupport.getCategoryComponent(svgComponent.getDocument(), ResourcesCategoryCD.TYPEID).addComponent(svgImage);

        // TODO use SVGComponentImageParser.getParserByComponent. 
        // problem is that here we check for svg menu items count
        
        SVGComponentImageParser parser = getParserByComponent(svgComponent);
        parseSVGImageItems(transferable, svgComponent, parser);

        return result;
    }
    
    /**
     * the same as SVGComponentImageParser.getParserByComponent,
     * but if provided components in svg menu, this methid checks if it already contains elements
     * @param svgComponent
     * @return
     */
    protected SVGComponentImageParser getParserByComponent(DesignComponent svgComponent){
        DescriptorRegistry descrRegistry = svgComponent.getDocument().getDescriptorRegistry();
        TypeID typeID = svgComponent.getType();
        if (descrRegistry.isInHierarchy(SVGMenuCD.TYPEID, typeID)) {
            if (svgComponent.readProperty(SVGMenuCD.PROP_ELEMENTS).getArray().size() == 0) {
                return new SVGMenuImageParser();
            }
        } else if (descrRegistry.isInHierarchy(SVGFormCD.TYPEID, typeID)) {
            SVGFormSupport.removeAllSVGFormComponents(svgComponent);
            return new SVGFormImageParser();
        }
        return null;
    }

    protected void parseSVGImageItems(Transferable transferable, 
            final DesignComponent svgMenuComponent,
            SVGComponentImageParser parser) 
    {
        if (parser == null) {
            return;
        }
        
        InputStream inputStream = null;
        try {
            inputStream = getInputStream(transferable);
            if (inputStream != null) {
                parser.parse(inputStream, svgMenuComponent);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    Debug.warning(ioe);
                }
            }
        }
    }
}