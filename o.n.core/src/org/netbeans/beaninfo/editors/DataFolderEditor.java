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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.text.MessageFormat;
import org.netbeans.core.UIExceptions;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Strupl
 * @version
 */
public class DataFolderEditor extends PropertyEditorSupport implements ExPropertyEditor {

    /** Creates new DataFolderEditor */
    public DataFolderEditor() {
    }

    private DataFolderPanel dfPanel;

    PropertyEnv env;

    /**
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    @Override
    public String getAsText() {
        DataFolder df = (DataFolder)getValue ();
        String result;
        if (df == null) {
            result = getString ("LAB_DefaultDataFolder"); //NOI18N
        } else {
            result = df.getName();
        }
        if (result.length() ==0) {
            result = File.pathSeparator;
        }
        return result;
    }

    /** Set the property value by parsing a given String.  May raise
    * java.lang.IllegalArgumentException if either the String is
    * badly formatted or if this kind of property can't be expressed
    * as text.
    * @param text  The string to be parsed.
    */
    @Override
    public void setAsText(String text) {
        if (text==null || 
            "".equals(text) || 
            text.equals(getString ("LAB_DefaultDataFolder")) || 
            File.pathSeparator.equals(text)) {
            //XXX Mysterious why a real implementation of setAsText is not here
            setValue(null);
        } else {
            //Reasonable to assume any exceptions from core/jdk editors are legit
            IllegalArgumentException iae = new IllegalArgumentException ();
            String msg = MessageFormat.format(
                NbBundle.getMessage(
                    DataFolderEditor.class, "FMT_DF_UNKNOWN"), new Object[] {text}); //NOI18N
            UIExceptions.annotateUser(iae, iae.getMessage(), msg, null,
                                     new java.util.Date());
            throw iae;
        }        
    }

    @Override
    public boolean supportsCustomEditor () {
        return true;
    }

    @Override
    public java.awt.Component getCustomEditor () {
        dfPanel = getDFPanel();
        Object val = getValue();
        if (val instanceof DataFolder) {
            dfPanel.setTargetFolder((DataFolder)val);
        }
        return dfPanel;
    }

    /** Calls super.setValue(newValue) and then updates
     * the look of the associated DataFolderPanel by
     * providing appropriate node to display.
     */
    @Override
    public void setValue(Object newValue) {
        Object oldValue = getValue();
        super.setValue(newValue);
        DataFolderPanel dfp = getDFPanel();
        if ((newValue != oldValue)&&(dfp != null) && (newValue instanceof DataFolder)){
            dfp.setTargetFolder((DataFolder)newValue);
        }

    }

    /** This method is called from DataFolderPanel, so it is similar to
     * setValue but does not call DataFolderPanel.setTargetFolder()
     */
    void setDataFolder(DataFolder newDf) {
        super.setValue(newDf);
    }
    
    DataFolderPanel getDFPanel() {
        if (dfPanel == null) {
            dfPanel = new DataFolderPanel(this);
        }
        return dfPanel;
    }

    private static String getString (String s) {
        return org.openide.util.NbBundle.getBundle (DataFolderEditor.class).getString (s);
    }

    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }
}
