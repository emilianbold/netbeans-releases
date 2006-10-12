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

package org.netbeans.modules.web.core.jsploader.api;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;

/**
 *
 * @author Petr Pisl
 */

/**
 * Defines cookie which supports parsing of jsp file.
 * Note: Do not implement this cookie, use the factory only.
 * (The same contract like for window system API interfaces, you as
 * provider can later add methods to it, the client is not implementor).
 */
public interface TagLibParseCookie extends org.openide.nodes.Node.Cookie {
    public JspParserAPI.JspOpenInfo getCachedOpenInfo(boolean preferCurrent, boolean useEditor);
}

