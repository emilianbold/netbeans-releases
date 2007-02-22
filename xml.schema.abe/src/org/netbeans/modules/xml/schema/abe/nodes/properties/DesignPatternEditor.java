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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * DesignPatternEditor.java
 *
 */

package org.netbeans.modules.xml.schema.abe.nodes.properties;

import java.beans.PropertyEditorSupport;
import org.netbeans.modules.xml.axi.SchemaGenerator;
import org.netbeans.modules.xml.schema.abe.wizard.SchemaTransformPatternSelectionUI;
import org.netbeans.modules.xml.schema.model.Form;
import org.openide.util.NbBundle;

/**
 *
 * @author Ayub Khan
 * "russianDoll", "salamiSlice", "venitianBlind", "gardenofEden", Empty (Default for Schema)
 *
 */
public class DesignPatternEditor  extends PropertyEditorSupport{

    /**
     * Creates a new instance of FormPropertyEditor
     */
    public DesignPatternEditor() {
    }

    public String[] getTags() {
        return new String[] {getAsText(), 
			NbBundle.getMessage(DesignPatternEditor.class,getEmptyLabel())};
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        if (text.equals(NbBundle.getMessage(DesignPatternEditor.class,getEmptyLabel()))){
            setValue(null);
        } else if (text.equals(NbBundle.getMessage(SchemaTransformPatternSelectionUI.class,"LBL_SchemaTransform_russianDoll"))){
            setValue(SchemaGenerator.Pattern.RUSSIAN_DOLL);
        } else if (text.equals(NbBundle.getMessage(SchemaTransformPatternSelectionUI.class,"LBL_SchemaTransform_salamiSlice"))){
            setValue(SchemaGenerator.Pattern.SALAMI_SLICE);
        } else if (text.equals(NbBundle.getMessage(SchemaTransformPatternSelectionUI.class,"LBL_SchemaTransform_venetianBlind"))){
            setValue(SchemaGenerator.Pattern.VENITIAN_BLIND);
        } else if (text.equals(NbBundle.getMessage(SchemaTransformPatternSelectionUI.class,"LBL_SchemaTransform_gardenOfEden"))){
            setValue(SchemaGenerator.Pattern.GARDEN_OF_EDEN);
        }		
    }
    
    public String getAsText() {
        Object val = getValue();
        if (val instanceof SchemaGenerator.Pattern){
            if (SchemaGenerator.Pattern.RUSSIAN_DOLL.equals(val)) {
                return NbBundle.getMessage(SchemaTransformPatternSelectionUI.class,"LBL_SchemaTransform_russianDoll");
            } else if (SchemaGenerator.Pattern.SALAMI_SLICE.equals(val)) {
                return NbBundle.getMessage(SchemaTransformPatternSelectionUI.class,"LBL_SchemaTransform_salamiSlice");
            } else if (SchemaGenerator.Pattern.VENITIAN_BLIND.equals(val)) {
                return NbBundle.getMessage(SchemaTransformPatternSelectionUI.class,"LBL_SchemaTransform_venetianBlind");
            } else if (SchemaGenerator.Pattern.GARDEN_OF_EDEN.equals(val)) {
                return NbBundle.getMessage(SchemaTransformPatternSelectionUI.class,"LBL_SchemaTransform_gardenOfEden");				
            }
        }
        // TODO how to display invalid values?
        return NbBundle.getMessage(DesignPatternEditor.class,getEmptyLabel());
    }
    
    protected String getEmptyLabel() {
        return "LBL_SchemaTransform_Select_DesignPattern";
    }
}
