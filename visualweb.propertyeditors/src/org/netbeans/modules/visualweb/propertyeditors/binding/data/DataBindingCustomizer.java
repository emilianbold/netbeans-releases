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
package org.netbeans.modules.visualweb.propertyeditors.binding.data;

import java.awt.Component;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.impl.BasicCustomizer2;
import com.sun.rave.designtime.faces.FacesDesignProperty;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetCallback;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;
import com.sun.rave.designtime.Result;

public class DataBindingCustomizer extends BasicCustomizer2 implements BindingTargetCallback {

    private static final Bundle bundle = Bundle.getBundle(DataBindingCustomizer.class);

    public DataBindingCustomizer(String propName, Class[] panelClasses, boolean showExpr, String dialogTitle) {
        super(null, dialogTitle, null, null); //NOI18N
        this.propName = propName;
        this.panelClasses = panelClasses;
        this.showExpr = showExpr;
        // The first panel must be always Bind to dataprovider (by design)
        // It can be either BIND_OPTIONS_TO_DATAPROVIDER or BIND_VALUE_TO_DATAPROVIDER
        if (panelClasses[0] == DataBindingHelper.BIND_OPTIONS_TO_DATAPROVIDER){
            setHelpKey("projrave_ui_elements_dialogs_bindtodata_list_db");
        }else{ //BIND_VALUE_TO_DATAPROVIDER
            setHelpKey("projrave_ui_elements_dialogs_bindtodata_simple_db");
        }
    }

    public DataBindingCustomizer(String propName, Class[] panelClasses, boolean showExpr) {
        this(propName, panelClasses, showExpr, bundle.getMessage("bindToData"));
    }

    public DataBindingCustomizer(String propName, Class[] panelClasses) {
        this(propName, panelClasses, false, bundle.getMessage("bindToData"));
    }

    public void refresh() {}

    public void setNewExpressionText(String newExpr) {
        this.newExpression = newExpr;
        if(newExpression != null) {
            if (!newExpression.trim().equals(originalValueSource)){
                setModified(true);
            }else{
                setModified(false);
            }
        }else if (originalValueSource != null){
            setModified(true);
        }else{
            setModified(false);
        }
    }

    String propName;
    DesignProperty prop;
    FacesDesignProperty fprop;
    String newExpression = null;
    Class[] panelClasses = null;
    boolean showExpr = true;

    Object originalValue;
    String originalValueSource;


    protected TabbedDataBindingPanel tabbedPanel = null;
    public Component getCustomizerPanel(DesignBean designBean) {
        setDisplayName(bundle.getMessage("bindToData") + " - " + designBean.getInstanceName());
        this.designBean = designBean;
        this.prop = designBean.getProperty(propName);
        if (prop instanceof FacesDesignProperty) {
            fprop = (FacesDesignProperty)prop;
            if (fprop.isBound() && fprop.getValueBinding() != null) {
                this.newExpression = fprop.getValueBinding().getExpressionString();
            }
        }
        if(prop instanceof FacesDesignProperty){
          FacesDesignProperty facesProp =  (FacesDesignProperty) prop;
          if (facesProp.isBound()){
              originalValueSource = facesProp.getValueSource();
              if(originalValueSource != null) originalValueSource = originalValueSource.trim();
          }else{
             originalValue = facesProp.getValue();
          }
        }
        tabbedPanel = new TabbedDataBindingPanel(this, prop, panelClasses, showExpr); //NOI18N
        return tabbedPanel;
    }

    public boolean isApplyCapable(){
        return true;
    }

    public Result applyChanges() {
        if (isModified()) {
            // Do not clear the original values or unset the property
            // if the new value binding is null or empty - Winston
            if (newExpression == null || newExpression.trim().equals("")){
                if(originalValueSource != originalValue){
                    prop.unset();
                }else{
                    prop.setValue(originalValue);
                }
            }else{
               prop.setValueSource(newExpression); 
            }
        }
        return super.applyChanges();
    }
}
