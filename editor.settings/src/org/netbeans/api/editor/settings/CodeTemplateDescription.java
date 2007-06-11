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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The definition of a code template. A code template is basically a piece of
 * code with an abbreviation associated to it. When a user types the abbreviation
 * to the editor and presses the expansion key the code associated with the
 * abbreviation gets expanded. The code can contain various parameter that the user
 * can enter during the expansion.
 * 
 * <p>The <code>CodeTemplateDescription</code>s can be obtained from
 * <code>CodeTemplateSettings</code> class that can be loaded from <code>MimeLookup</code>
 * for a particular mime type. See the example below.
 * 
 * <pre>
 * Lookup l = MimeLookup.getLookup(MimePath.parse(mimePath));
 * CodeTemplateSettings cds = l.lookup(CodeTemplateSettings.class);
 * List<CodeTemplateDescription> codeTemplates = cds.getCodeTemplateDescriptions();
 * </pre>
 * 
 * @see CodeTemplateSettings
 * @author Miloslav Metelka
 */
public final class CodeTemplateDescription {

    private final String abbreviation;
    private final String description;
    private final String parametrizedText;
    private final List<String> contexts;    

    /**
     * Creates a new code template description. It call the other constructor
     * passing <code>null</code> for the <code>contexts</code> parameter.
     * 
     * @param abbreviation The abbreviation text that expands this code template.
     * @param description The code template's display text.
     * @param parametrizedText The actual code template that will get expanded when
     *   a user writes the abbreviation in the editor.
     */
    public CodeTemplateDescription(String abbreviation, String description, String parametrizedText) {
        this(abbreviation, description, parametrizedText, null);
    }
    
    /**
     * Creates a new code template description.
     * 
     * <p>Usually clients do not need to create <code>CodeTemplateDescription</code>s
     * by themselvs. Instead they use <code>MimeLookup</code> and <code>CodeTemplateSettings</code>
     * to access code templates registered in the system.
     *
     * @param abbreviation The abbreviation text that expands this code template.
     * @param description The code template's display text.
     * @param parametrizedText The actual code template that will get expanded when
     *   a user writes the abbreviation in the editor.
     * @param contexts The list of context ids that apply for this code template.
     */
    public CodeTemplateDescription(
        String abbreviation, 
        String description, 
        String parametrizedText, 
        List<String> contexts
    ) {
        assert abbreviation != null : "The abbreviation parameter can't be null"; //NOI18N
        assert parametrizedText != null : "The parametrizedText parameter can't be null"; //NOI18N
        
        this.abbreviation = abbreviation;
        this.description = description;
        this.parametrizedText = parametrizedText;
        this.contexts = contexts == null ? 
            Collections.<String>emptyList() : 
            Collections.unmodifiableList(new ArrayList<String>(contexts));
    }
    
    /**
     * Gets the abbreviation text that triggers expansion of this code template.
     * 
     * <p>The abbreviation text should be unique among all code templates defined
     * for a one mime type so that each code template can be expanded individually.
     *
     * @return The abbreviation text that expands this code template.
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Gets textual description of this code template. It's a display text
     * that can be shown in UI such as the code completion window or Tools-Options
     * dialog.
     *
     * @return The display text for this code template or <code>null</code> if this
     *   code template has no descriptions.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the code text of this code template.
     * 
     * This is the text that will be expanded when a user types the abbreviation
     * in the editor and presses the expansion key. The text can contain parameters
     * in the form of "${...}".
     *
     * @return The code text with parameters.
     */
    public String getParametrizedText() {
        return parametrizedText;
    }

    /**
     * Gets the list of contexts that apply for this code template. The contexts
     * are simply unique identifiers used by the infrastructure to filter out
     * code templates that are not suitable for the editor context, where a user
     * types.
     * 
     * <p>The actual identifiers are defined by each particular language (mime type)
     * and can be different for different languages. The language defines contexts
     * for its constructs such as loops, methods, classes, if-else blocks, etc. and
     * than tags each code template available for that language with a context,
     * where it is meaningful to apply the template.
     * 
     * @return The contexts for this code template.
     */
    public List<String> getContexts() {
        return contexts;
    }

    public String toString() {
        return "abbrev='" + getAbbreviation() + "', parametrizedText='" + getParametrizedText() + "'"; // NOI18N
    }
    
}
