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

package org.netbeans.api.gsf;

/**
 * A formatter used to format items for navigation, code completion, etc.
 * Language plugins should build up HTML strings by calling logical
 * methods on this class, and suitable HTML will be constructed (using
 * whatever colors and attributes are appropriate for the different logical 
 * sections and so on). This places formatting logic within the IDE such that
 * it can be theme sensitive (and changed without replicating logic in the plugins).
 *
 * @author Tor Norbye
 */
public abstract class HtmlFormatter {
    public abstract void reset();
    public abstract void appendHtml(String html);
    public abstract void appendText(String text);
    
    public abstract void name(ElementKind kind, boolean start);
    public abstract void parameters(boolean start);
    public abstract void type(boolean start);
    public abstract void deprecated(boolean start);
    
    public abstract String getText();
}
