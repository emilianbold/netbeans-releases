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

package org.netbeans.modules.j2ee.ddloaders.ejb;

/** 
 * Recognizes ejb-jar.xml for Java EE 5, ejb-jar with version number 3.0. Needed for 
 * providing a different set of actions than for older versions of ejb-jar.xml.
 * See #76967.
 *
 * @author Erno Mononen
 */
public class EjbJar30DataLoader extends EjbJarDataLoader{
    
    private static final long serialVersionUID = 1L;
    private static final String REQUIRED_MIME_PREFIX_3 = "text/x-dd-ejbjar3.0"; // NOI18N


    public EjbJar30DataLoader () {
        super ("org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarMultiViewDataObject");  // NOI18N
    }

    protected String actionsContext() {
        return "Loaders/text/x-dd-ejbjar3.0/Actions/"; // NOI18N
    }
    
    protected String[] getSupportedMimeTypes(){
        return new String[]{REQUIRED_MIME_PREFIX_3};
    }

}
