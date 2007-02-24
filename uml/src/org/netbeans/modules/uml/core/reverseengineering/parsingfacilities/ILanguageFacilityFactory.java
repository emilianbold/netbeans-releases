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

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IAnalysisFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IColorFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IDiscoveryFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFormatFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IValidationFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IVisualizationFacility;

/**
 */
public interface ILanguageFacilityFactory
{
    public IUMLParser getUMLParser();

    public IAnalysisFacility getAnalysisFacility();

    public IValidationFacility getValidationFacility();

    public IVisualizationFacility getVisualizationFacility();

    public IDiscoveryFacility getDiscoveryFacility();

    public IFormatFacility getFormatFacility();

    public IColorFacility getColorFacility();

    public IFacility retrieveFacility(String facilityName);
}