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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.api.model;

import java.util.List;

/**
 * This class instance is returned by DTDGrammarQueryProvider.getGrammar(GrammarEnvironment env) method.
 * It is used in xml/text-edit module by GrammarManager class to obtain a list of external entities 
 * resolved during the DTD parsing. The GrammarManager then listens on these files and invalidates 
 * the resulting grammar if any of the files changes.
 *
 * @author mfukala@netbeans.org
 */
public interface ExtendedGrammarQuery extends GrammarQuery {
    
    /** @return a List of resolved entities System id-s names.*/
    public List/*<String>*/ getResolvedEntities();
    
}
