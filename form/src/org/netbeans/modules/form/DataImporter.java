/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form;

import java.util.concurrent.*;

/**
 * Provider of component with data (for example collection of JPA entities).
 *
 * @author Jan Stola
 */
public interface DataImporter {

    /**
     * Imports the data. Usually opens a dialog allowing the user to customize
     * the data to import.
     * 
     * @param form form to import the data into.
     * @return the component encapsulating the imported data.
     */
    Future<RADComponent> importData(FormModel form);

}
