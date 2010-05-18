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
package com.sun.rave.web.ui.component;

import com.sun.rave.web.ui.util.ComponentUtilities;
import java.beans.Beans;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;

/**
 *
 * @author avk
 */
public class Field extends FieldBase implements ComplexComponent {

    public static final String READONLY_ID = "_readOnly"; //NOI18N
    public static final String LABEL_ID = "_label"; //NOI18N
    public static final String INPUT_ID = "_field"; //NOI18N
    public static final String READONLY_FACET = "readOnly"; //NOI18N
    public static final String LABEL_FACET = "label";

    private static final boolean DEBUG = false;

    /** Creates a new instance of FieldBase */
    public Field() {
    }
    // Labels
    public UIComponent getLabelComponent(FacesContext context, String style) {
        
        if(DEBUG) log("getReadOnlyComponent()");
        
        // Check if the page author has defined a label facet
        UIComponent labelComponent = getFacet(LABEL_FACET); //NOI18N
        
        // If the page author has not defined a label facet,
        // check if the page author specified a label.
        if(labelComponent == null) {
            String label = getLabel();
            if(label != null && label.length() > 0) {
                labelComponent = createLabel(label, style, context); //NOI18N\
            }
        } else if(DEBUG) {
            log("\tFound facet."); //NOI18N
        }
        
        return labelComponent;
    }
    
    // Readonly value
    public UIComponent getReadOnlyComponent(FacesContext context) {
        
        if(DEBUG) log("getListLabelComponent()");
        
        String id = getId();
        
        // Check if the page author has defined a label facet
        UIComponent textComponent = getFacet(READONLY_FACET); //NOI18N
        
        // If the page author has not defined a label facet,
        // check if the page author specified a label.
        if(textComponent == null) {
            textComponent = createText(getReadOnlyValueString(context)); //NOI18N
            //<RAVE>
        } else {
            if(DEBUG) {
                log("\tFound facet."); //NOI18N
            }
            //CR 6391493
            //The readonly component was cached. Make sure its value (text) property is up to date.
            if (textComponent instanceof UIOutput) {
                UIOutput outputComponent = (UIOutput)textComponent;
                outputComponent.setValue(getReadOnlyValueString(context));
            }
            //</RAVE>
        }
        
        return textComponent;
    }
    
    private UIComponent createLabel(String labelString, String style, FacesContext context) {
        
        if(DEBUG) log("createLabel()");
        
        // If we find a label, define a component and add it to the
        // children, unless it has been added in a previous cycle
        // (the component is being redisplayed).
        
        if(labelString == null || labelString.length() < 1) {
            if(DEBUG) log("\tNo label");
            return null;
        } else if(DEBUG) {
            log("\tLabel is " + labelString);  //NOI18N
        }
        
        Label label = new Label();
        label.setId(getId().concat(LABEL_ID));
        label.setLabelLevel(getLabelLevel());
        label.setStyleClass(style);
        label.setText(labelString);
        label.setLabeledComponent(this);
        // <RAVE>
        // this.getFacets().put(LABEL_FACET, label);
        if (!Beans.isDesignTime())
            this.getFacets().put(LABEL_FACET, label);
        // </RAVE>
        return label;
    }
    
    private UIComponent createText(String string) {
        
        if(DEBUG) log("createText()");
        
        // If we find a label, define a component and add it to the
        // children, unless it has been added in a previous cycle
        // (the component is being redisplayed).
        
        if(string == null || string.length() < 1) {
            // TODO - maybe print a default?
            string = new String();
        }
        StaticText text = new StaticText();
        text.setText(string);
        text.setId(getId().concat(READONLY_ID));
        // <RAVE>
        // this.getFacets().put(READONLY_FACET, text);
        if (!Beans.isDesignTime())
            this.getFacets().put(READONLY_FACET, text);
        // </RAVE>
        return text;
    }
    
    /**
     * Log an error - only used during development time.
     */
    protected void log(String s) {
        System.out.println(this.getClass().getName() + "::" + s); //NOI18N
    }
    
    // <RAVE>
    // Merged INF http://inf.central/inf/integrationReport.jsp?id=82661 from braveheart.
    // It fixes bug 6349156: File upload label's "for" attribute value is incorrect
    
    /** 
     * Retrieves the DOM ID for the HTML input element. To be used by 
     * Label component as a value for the "for" attribute. 
     */
    public String getPrimaryElementID(FacesContext context) {
     
        // Check for a public facet
	String clntId = this.getClientId(context);
	UIComponent facet = getFacet(LABEL_FACET);
	if (facet != null) {
	    return clntId.concat(this.INPUT_ID);
	}
	// Need to check for the private facet as well. Note that 
        // this is not ideal - unless getLabelComponent has been invoked
        // first, the private facet will be null. (Is there a reason 
        // we can't just invoke getLabelComponent instead of getFacet?). 
        
	// Pass "false" since we don't want to get null if the id's 
	// don't match. We don't care at this point, in fact they
	// should match because getLabelComponent must have been
	// called in order for this to even work.
	//
	facet = ComponentUtilities.getPrivateFacet(this, LABEL_FACET, false);
	if (facet == null) {
	    return getClientId(context); 
	}
	return this.getClientId(context).concat(this.INPUT_ID);
    }
    // <RAVE>
       
    public int getColumns() {
        
        int columns = super.getColumns();
        if(columns < 1) {
            columns = 20;
            super.setColumns(20);
        }
        return columns;
    }
    
    // <RAVE>
    public void setText(Object text) {
        if (this.isReadOnly()) {
            UIComponent facet = (UIComponent) getFacets().get(READONLY_FACET);
            if (facet == null || !(facet instanceof StaticText)) {
                StaticText staticText = new StaticText();
                staticText.setId(getId().concat(READONLY_ID));
                getFacets().put(READONLY_FACET, staticText);
                facet = staticText;
            }
            ((StaticText) facet).setText(text);
        }
        super.setText(text);
    }
    // </RAVE>
    
}
