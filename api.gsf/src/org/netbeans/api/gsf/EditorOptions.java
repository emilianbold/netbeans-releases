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
package org.netbeans.api.gsf;

import org.openide.util.Lookup;

/**
 * Manage a set of options configurable by the user in the IDE. 
 * Language plugins can and should register their own options panels,
 * but some editor options (such as tab settings) are managed by the IDE,
 * in and these can be accessed via this class.
 * 
 * @author Tor Norbye
 */
public abstract class EditorOptions {
    public abstract int getTabSize();
    public abstract boolean getExpandTabs();
    public abstract int getSpacesPerTab();
    public abstract boolean getMatchBrackets();
    public abstract int getRightMargin();
    
    public static EditorOptions get(String mimeType) {
        EditorOptionsFactory factory = Lookup.getDefault().lookup(EditorOptionsFactory.class);
        if (factory != null) {
            return factory.get(mimeType);
        }

        return null;
    }
}
