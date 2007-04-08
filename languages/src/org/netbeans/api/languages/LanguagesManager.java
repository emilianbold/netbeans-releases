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
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.api.languages;

/**
 *
 * @author Jan Jancura
 */
public abstract class LanguagesManager {

    public static final LanguagesManager get () {
        return org.netbeans.modules.languages.LanguagesManager.getDefault ();
    }
    
    public abstract Language getLanguage (String mimeType) throws LanguageDefinitionNotFoundException;
  
    {
        if (!getClass ().getName ().equals ("org.netbeans.modules.languages.LanguagesManager"))
            throw new IllegalArgumentException ();
    }
}
