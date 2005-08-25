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

package org.netbeans.lib.editor.codetemplates.api;

import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.codetemplates.CodeTemplateManagerOperation;
import org.netbeans.lib.editor.codetemplates.CodeTemplateSpiPackageAccessor;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateInsertRequest;

/**
 * Code template is represented by a parametrized text
 * that, after pre-processing, can be pasted into a given
 * text component.
 * <br/>
 * Code template instances are either persistent (can be retrieved by
 * {@link CodeTemplateManager#getCodeTemplates()})
 * or temporary code templates that can be created
 * by {@link CodeTemplateManager#createTemporary(String)}.
 * 
 * @author Miloslav Metelka
 */
public final class CodeTemplate {
    
    private final CodeTemplateManagerOperation managerOperation;
    
    private final String abbreviation;
    
    private final String description;
    
    private final String parametrizedText;
    
    CodeTemplate(CodeTemplateManagerOperation managerOperation,
    String abbreviation, String description, String parametrizedText) {
        
        assert (managerOperation != null);
        if (abbreviation == null) {
            throw new NullPointerException("abbreviation cannot be null"); // NOI18N
        }
        if (description == null) {
            throw new NullPointerException("description cannot be null"); // NOI18N
        }
        if (parametrizedText == null) {
            throw new NullPointerException("parametrizedText cannot be null"); // NOI18N
        }

        this.managerOperation = managerOperation;
        this.abbreviation = abbreviation;
        this.description = description;
        this.parametrizedText = parametrizedText;
    }

    /**
     * Insert this code template into the given text component
     * at the caret position.
     *
     * @param component non-null text component.
     */
    public void insert(JTextComponent component) {
        managerOperation.insert(this, component);
    }

    /**
     * Get abbreviation that triggers expansion of this code template.
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
