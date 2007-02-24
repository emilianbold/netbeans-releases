/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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