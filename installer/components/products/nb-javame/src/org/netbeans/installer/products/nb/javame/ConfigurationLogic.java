/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.products.nb.javame;

import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.helper.Text;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String MOBILITY_CLUSTER =
            "{mobility-cluster}"; // NOI18N
    private static final String ID =
            "MOB"; // NOI18N
    private static final String DISTRIBUTION_README_RESOURCE =
            "org/netbeans/installer/products/nb/javame/DISTRIBUTION.txt";
    private static final String THIRDPARTYLICENSE_RESOURCE =
            "org/netbeans/installer/products/nb/javame/THIRDPARTYLICENSE.txt";
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
            MOBILITY_CLUSTER}, ID);
    }
    public Text getDistributionReadme() {
        final String text = parseString("$R{" + DISTRIBUTION_README_RESOURCE + "}");
        return new Text(text, Text.ContentType.PLAIN_TEXT);
    }
    
    public Text getThirdPartyLicense() {
        final String text = parseString("$R{" + THIRDPARTYLICENSE_RESOURCE + "}");
        return new Text(text, Text.ContentType.PLAIN_TEXT);
    }
}
