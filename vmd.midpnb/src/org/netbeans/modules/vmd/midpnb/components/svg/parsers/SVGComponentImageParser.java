/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.components.svg.parsers;

import java.io.InputStream;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.midpnb.components.svg.form.SVGFormCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;

/**
 * describes class for svg image source parsing 
 * @author avk
 */
public abstract class SVGComponentImageParser {

    /**
     * parses svg image source from provided InputStream and updsates 
     * provided DesignComponent with svg image data, if necessary.
     * @param svgInputStream cvg image source InputStream 
     * @param svgComponent parent svg component to update with data retrieved 
     * from svgInputStream
     */
    public abstract void parse(InputStream svgInputStream, DesignComponent svgComponent);

    /**
     * Selects SVGComponentImageParser to parse svg source of specified component.
     * Now selects parser from fixed list. 
     * <p> 
     * Now selects parser from fixed list. It is enough for now because of 
     * limited list of component types which source should be parsed.
     * @param svgComponent
     * @return SVGComponentImageParser
     */
    public static SVGComponentImageParser getParserByComponent(final DesignComponent svgComponent) {
        final DescriptorRegistry descrRegistry = svgComponent.getDocument().getDescriptorRegistry();
        final SVGComponentImageParser[] parser = new SVGComponentImageParser[1];
        svgComponent.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                TypeID typeID = svgComponent.getType();
                if (descrRegistry.isInHierarchy(SVGMenuCD.TYPEID, typeID)) {
                    parser[0] =  new SVGMenuImageParser();
                } else if (descrRegistry.isInHierarchy(SVGFormCD.TYPEID, typeID)) {
                    parser[0] = new SVGFormImageParser();
                }
            }
        });

        return parser[0];

    }
}
