/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * StyleEditor.java
 *
 * Created on October 13, 2004, 12:26 PM
 */

package org.netbeans.modules.css.visual.ui;

import java.util.logging.Level;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.css2.editor.model.CssRuleContent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.netbeans.modules.css.visual.api.CssRuleContext;
import org.netbeans.modules.css.visual.ui.preview.CssPreviewable;
import org.openide.util.Exceptions;

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
            try {
                cssStyleData.modifyProperty(evt.getPropertyName(), (String) evt.getNewValue());
            } catch (BadLocationException ex) {
                Logger.getLogger("global").log(Level.WARNING, "CssModel inconsistency!", ex); //NOI18N
            }
        }
    }
}
