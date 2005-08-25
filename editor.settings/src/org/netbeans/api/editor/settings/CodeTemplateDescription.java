/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.editor.settings;

import org.netbeans.api.editor.settings.CodeTemplateDescription;

/**
 * Description of the code template includes abbreviation name,
 * description and parametrized text.
 * <br/>
 * The descriptions are provided from
 * {@link CodeTemplateSettings#getCodeTemplateDescriptions()}.
 *
 * @author Miloslav Metelka
 */
public final class CodeTemplateDescription {
    
    private final String abbreviation;
    
    private final String description;
    
    private final String parametrizedText;
    
    /**
     * Construct code template description.
     * It can be constructed e.g. from the templates stored in an xml format.
     *
     * @param abbreviation non-null abbreviation.
     * @param description non-null description.
     * @param parametrized non-null parametrized text.
     */
    public CodeTemplateDescription(String abbreviation, String description, String parametrizedText) {
        assert (abbreviation != null);
        assert (description != null);
        assert (parametrizedText != null);
        this.abbreviation = abbreviation;
        this.description = description;
        this.parametrizedText = parametrizedText;
    }
    
    /**
     * Get abbreviation that triggers expansion of this code template.
     * <br>
     * It should be unique among code templates for a single mime-type
     * so that each code template can be expanded individually.
     *
     * @return non-null abbreviation that expands to this template.
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Get textual description of this code template.
     * <br>
     * It's being displayed e.g. in the code completion window.
     *
     * @return non-null description text.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the parametrized text of this code template.
     * <br>
     * The parameters have form "${...}" and there can be arbitrary
     * number of them.
     *
     * @return non-null parametrized text.
     */
    public String getParametrizedText() {
        return parametrizedText;
    }

    public String toString() {
        return "abbrev='" + getAbbreviation() + "', parametrizedText='" + getParametrizedText() + "'"; // NOI18N
    }
    
}
