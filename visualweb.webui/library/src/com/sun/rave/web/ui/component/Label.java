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
package com.sun.rave.web.ui.component;

import java.beans.Beans;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.LogUtil;


/**
 * <p>Component that represents an input field label.</p>
 */

public class Label extends LabelBase {

    public static final String REQUIRED_ID = "_required";
    public static final String REQUIRED_FACET = "required";
    public static final String ERROR_ID = "_error";
    public static final String ERROR_FACET = "error";
    
    private EditableValueHolder labeledComponent = null; 

    private String element = "span"; //NOI18N
    
    private static final boolean DEBUG = false;
    
    public void setLabeledComponent(UIComponent comp) { 
        
        if(DEBUG) log("setLabeledComponent"); 
        if(comp == null) { 
            if(DEBUG) log("component is null"); 
            this.labeledComponent = null; 
        }
        else if(comp instanceof EditableValueHolder) {
            if(DEBUG) log("Component is EditableValueHolder");
            this.labeledComponent = (EditableValueHolder)comp;
            // <RAVE>
            // this.setFor(comp.getClientId(FacesContext.getCurrentInstance()));
            if (!Beans.isDesignTime())
                this.setFor(comp.getClientId(FacesContext.getCurrentInstance()));
            // </RAVE>
	    element = "label"; 
        } 
        else {
            if(DEBUG) log("Component is not an EditableValueHolder");
            if (LogUtil.infoEnabled(Label.class)) {
                FacesContext context = FacesContext.getCurrentInstance();
                
                LogUtil.info(Label.class, "Label.invalidFor",
                        new Object[] { getId(),
                                context.getViewRoot().getViewId(),
                                comp.getId() } );
            }
            
            this.labeledComponent = null;
	    element = "label"; 
        }
    }

    public EditableValueHolder getLabeledComponent() {
        
        if(DEBUG) log("getLabeledComponent for label " + String.valueOf(getText())); 
        if(labeledComponent != null) { 
            if(DEBUG) log("Found component ");
            if(DEBUG) log(((UIComponent)labeledComponent).getId());
            return labeledComponent;
        }
        if(DEBUG) log("labelled component is null, try something else");
        String id = getFor();
        
        if(DEBUG && id != null) { 
            log("\tfor attribute set to " + id);
        }
       
        if(id == null) {
            if(DEBUG) log("\tID is not set, find children ");
            setLabeledComponent(findLabeledChild());
        } 
        else {
            if(DEBUG) log("\tID found");
            if(id.indexOf(":") > -1 && !id.startsWith(":")) { 
                id = ":" + id;
            }
            setLabeledComponent(findComponent(id));
            element = "label";
        }
        return labeledComponent;
    }
    

    public String getLabeledComponentId(FacesContext context) { 
        
        String id = null; 
             
	if(labeledComponent != null) { 
            if(labeledComponent instanceof ComplexComponent) { 
                ComplexComponent compComp = (ComplexComponent)labeledComponent; 
                id = compComp.getPrimaryElementID(context);
            } 
            else { 
                UIComponent comp = ((UIComponent)labeledComponent); 
                id = comp.getClientId(context); 
            } 
	} 
        else { 
            id = getFor();   
            if(id != null && id.indexOf(":") == -1) {
                UIComponent comp = this.getParent();
                if(comp instanceof NamingContainer) {
                    id = comp.getClientId(context) + ":" + id;
                }
            }
        }
	return id; 
    } 

    private UIComponent findLabeledChild() {
        
        if(DEBUG) log("findLabeledChild");
        List kids = getChildren();
        if(DEBUG && kids.size() == 0) {
            log("No children!");
        }
        for(int i = 0; i < kids.size(); i++) {
            Object kid = kids.get(i);
            if(kid instanceof EditableValueHolder) {
                if(DEBUG) log("Found good child " + kid.toString());
                return (UIComponent)kid;
            }
        }
        if(DEBUG) log("\tReturning null...");
        return null;
    }

    public UIComponent getRequiredIcon(Theme theme, FacesContext context) { 
        
        UIComponent comp = getFacet(REQUIRED_FACET);
        if(comp == null) {
            comp = theme.getIcon(ThemeImages.LABEL_REQUIRED_ICON);
            comp.setId(getId().concat(REQUIRED_ID));
            ((Icon)comp).setBorder(0);
            //((Icon)comp).setLongDesc("TODO: Required");
            // <RAVE>
            // getFacets().put(REQUIRED_FACET, comp);
            if (!Beans.isDesignTime())
                getFacets().put(REQUIRED_FACET, comp);
            // </RAVE>
        }
        return comp;    
    }
    
    public UIComponent getErrorIcon(Theme theme, FacesContext context, 
                                    boolean valid) { 
        
        UIComponent comp = getFacet(ERROR_FACET);
        if(comp == null) {
           
            comp = theme.getIcon(ThemeImages.LABEL_INVALID_ICON);
            comp.setId(getId().concat(ERROR_ID));
            ((Icon)comp).setBorder(0);
            //((Icon)comp).setLongDesc("TODO: Invalid");
            
            
        }
        if(comp instanceof Icon) {
            if(valid) {
               ((Icon)comp).setIcon(ThemeImages.DOT);
            }
            else if(labeledComponent != null) {
               String labeledCompID = 
                   ((UIComponent)labeledComponent).getClientId(context);
               Iterator messages = context.getMessages(labeledCompID);
               FacesMessage fm = null;
               StringBuffer msgBuffer = new StringBuffer(200);
               while(messages.hasNext()) { 
                   fm = (FacesMessage)(messages.next());
                   msgBuffer.append(fm.getDetail());
                   msgBuffer.append(" "); //NOI18N
               }
               ((Icon)comp).setAlt(msgBuffer.toString());
               ((Icon)comp).setToolTip(msgBuffer.toString());
            }
        }
        return comp;
    }

    public String getElement() { 
	return element; 
    } 

    private void log(String s) { 
        System.out.println(getClass().getName() + "::" + s);
    }

    public int getLabelLevel() {

        int level = super.getLabelLevel();
        if(level < 1 || level > 3) { 
            level = 2; 
            super.setLabelLevel(level); 
        }
        return level;
    }
}
