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


package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IAnalysisFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IColorFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IDiscoveryFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFormatFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IValidationFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IVisualizationFacility;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

/**
 */
public class LanguageFacilityFactory implements ILanguageFacilityFactory
{
    /**
     * Retrieves the UMLParser faciltiy from the faciltity manager.
     *
     * @param pVal [in] The UMLParser.
     */
    public IUMLParser getUMLParser()
    {
        IFacility fac = retrieveFacility("Parsing.UMLParser");
        return fac instanceof IUMLParser? (IUMLParser) fac : null;
    }

    /**
     * Retrieves the AnalysisFacility faciltiy from the faciltity manager.
     *
     * @param pVal [in] The UMLParser.
     */
    public IAnalysisFacility getAnalysisFacility()
    {
        // C++ code does the same
        return null;
    }

    /**
     * Retrieves the ValidationFacility faciltiy from the faciltity manager.
     *
     * @param pVal [in] The UMLParser.
     */
    public IValidationFacility getValidationFacility()
    {
        // C++ code does the same
        return null;
    }

    /**
     * Retrieves the VisualizationFacility faciltiy from the faciltity manager.
     *
     * @param pVal [in] The UMLParser.
     */
    public IVisualizationFacility getVisualizationFacility()
    {
        // C++ code does the same
        return null;
    }

    /**
     * Retrieves the DiscoveryFacility faciltiy from the faciltity manager.
     *
     * @param pVal [in] The UMLParser.
     */
    public IDiscoveryFacility getDiscoveryFacility()
    {
        // C++ code does the same
        return null;
    }

    /**
     * Retrieves the FormatFacility faciltiy from the faciltity manager.
     *
     * @param pVal [in] The UMLParser.
     */
    public IFormatFacility getFormatFacility()
    {
        // C++ code does the same
        return null;
    }

    /**
     * Retrieves the ColorFacility faciltiy from the faciltity manager.
     *
     * @param pVal [in] The UMLParser.
     */
    public IColorFacility getColorFacility()
    {
        // C++ code does the same
        return null;
    }

    /**
     * Retrieves a faciltiy from the faciltity manager.
     *
     * @param **pVal [in] The UMLParser.
     */
    public IFacility retrieveFacility(String facilityName)
    {
        // I must retrieve the Facility Manager from the Core product.
        ICoreProduct product = ProductRetriever.retrieveProduct();
        if (product != null)
        {
            IFacilityManager man = product.getFacilityManager();
            if (man != null)
                return man.retrieveFacility(facilityName);
        }
        return null;
    }
}
