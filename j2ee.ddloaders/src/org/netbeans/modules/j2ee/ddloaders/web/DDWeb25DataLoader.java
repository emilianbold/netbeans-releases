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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.ddloaders.web;

/**
 * A data loader for web.xml version 2.5. Required for providing
 * a different action context than for older versions - see #85570.
 * 
 * @author Erno Mononen
 */
public class DDWeb25DataLoader extends DDDataLoader{

    private static final long serialVersionUID = 1L;

    private static final String REQUIRED_MIME = "text/x-dd-servlet2.5"; // NOI18N

    public DDWeb25DataLoader() {
        super("org.netbeans.modules.j2ee.ddloaders.web.DDDataObject");  // NOI18N
    }

    @Override
    protected String actionsContext() {
        return "Loaders/text/x-dd-web2.5/Actions/"; // NOI18N
    }

    @Override
    protected String[] getSupportedMimeTypes() {
        return new String[]{REQUIRED_MIME};
    }
}
