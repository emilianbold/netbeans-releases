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

package org.netbeans.api.editor.settings;

import java.util.Collection;
import javax.swing.KeyStroke;

/**
 * The list of available templates. Instances of this class should be retrieved
 * from <code>MimeLookup</code>.
 * 
 * 
 * <p><font color="red">This class must NOT be extended by any API clients.</font>
 *
 * @author Martin Roskanin
 */
public abstract class CodeTemplateSettings {

    /**
     * Construction prohibited for API clients.
     */
    public CodeTemplateSettings() {
        // Control instantiation of the allowed subclass only
        if (!"org.netbeans.modules.editor.settings.storage.codetemplates.CodeTemplateSettingsImpl$Immutable".equals(getClass().getName())) { // NOI18N
            throw new IllegalStateException("Instantiation prohibited."); // NOI18N
        }
    }
    
    /**
     * Gets the list of code template descriptions.
     *
     * @return An unmodifiable list of code template descriptions.
     */
    public abstract Collection<CodeTemplateDescription> getCodeTemplateDescriptions();
    
    /**
     * Gets the keystroke that expands the code templates abbreviations.
     *
     * @return A keystroke that expands code template abbreviations to
     *   its code text.
     */
    public abstract KeyStroke getExpandKey();
    
}
