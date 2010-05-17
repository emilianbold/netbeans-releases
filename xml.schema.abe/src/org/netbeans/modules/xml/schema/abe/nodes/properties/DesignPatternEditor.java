/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
