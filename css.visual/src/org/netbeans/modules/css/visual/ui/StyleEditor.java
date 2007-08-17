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
 * StyleEditor.java
 *
 * Created on October 13, 2004, 12:26 PM
 */

package org.netbeans.modules.css.visual.ui;

import org.netbeans.modules.css.model.CssRuleContent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.netbeans.modules.css.visual.api.CssRuleContext;
import org.netbeans.modules.css.visual.ui.preview.CssPreviewable;

/**
 * Super class for all Style editors
 * @author  Winston Prakash
 * @version 1.0
 */
abstract public class StyleEditor extends JPanel {

    private PropertyChangeSupport cssPropertyChangeSupport;

    CssPropertyChangeListener cssPropertyChangeListener = new CssPropertyChangeListener();

    boolean listenerAdded = false;

    private CssRuleContext content;
    
    /** Called by StyleBuilderPanel to set the UI panel property values. */
    public void setContent(CssRuleContext content) {
        this.content = content;
        setCssPropertyValues(content.selectedRule().ruleContent());
    }
    
    protected CssRuleContext content() {
        return content;
    }
    
    /**
     * Overriden by the subclasses
     * - Remove the property change listener
     * - Set the values from CSS data to GUI elements
     * - Set back the CSS property change listener
     */
    abstract protected void setCssPropertyValues(CssRuleContent styleData);

    PropertyChangeSupport cssPropertyChangeSupport() {
        if(cssPropertyChangeSupport == null) {
            cssPropertyChangeSupport =  new PropertyChangeSupport(this);
        }
        return cssPropertyChangeSupport;
    }
    
    /**
     * Set the CSS property change listener
     */
    public void setCssPropertyChangeListener(CssRuleContent styleData){
        // We don't want the property change listener added more than
        // once accidently
        synchronized(StyleEditor.class){
            if (!listenerAdded){
                listenerAdded = true;
                cssPropertyChangeListener.setCssStyleData(styleData);
                cssPropertyChangeSupport().addPropertyChangeListener(cssPropertyChangeListener);
            }
        }
    }

    /**
     * Remove the CSS property change listener
     */
    public void removeCssPropertyChangeListener(){
        synchronized(StyleEditor.class){
            if (listenerAdded){
                listenerAdded = false;
                cssPropertyChangeSupport().removePropertyChangeListener(cssPropertyChangeListener);
            }
        }
    }


    /**
     * Holds value of property displayName.
     */
    private String displayName;

    /**
     * Holds value of property icon.
     */
    private Icon icon;

    /**
     * Getter for property displayName.
     * @return Value of property displayName.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Setter for property displayName.
     * @param displayName New value of property displayName.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Getter for property icon.
     * @return Value of property icon.
     */
    public Icon getIcon() {
        return this.icon;
    }

    /**
     * Setter for property icon.
     * @param icon New value of property icon.
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    static class CssPropertyChangeListener implements PropertyChangeListener{
        CssRuleContent cssStyleData;

        public CssPropertyChangeListener(){
        }

        public CssPropertyChangeListener(CssRuleContent styleData){
            cssStyleData = styleData;
        }

        public void setCssStyleData(CssRuleContent styleData){
            cssStyleData = styleData;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            cssStyleData.modifyProperty(evt.getPropertyName(), (String)evt.getNewValue());
        }
    }
}
