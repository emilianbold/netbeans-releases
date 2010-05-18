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
package org.netbeans.modules.visualweb.faces.dt;

import com.sun.rave.designtime.CheckedDisplayAction;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.impl.BasicDisplayAction;
import org.netbeans.modules.visualweb.faces.dt.component.html.HtmlFormDesignInfo;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;
import javax.faces.component.NamingContainer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectManyCheckbox;
import javax.faces.component.html.HtmlSelectOneRadio;

public class AutoSubmitOnChangeCheckedAction extends BasicDisplayAction implements
    CheckedDisplayAction {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(
        AutoSubmitOnChangeCheckedAction.class);

    private static final Pattern thisDotFormDotSubmitPattern = Pattern.compile(
            "this\\s*\\.\\s*form\\s*\\.\\s*submit\\s*\\(\\s*\\)\\s*;?"); //NOI18N
    
    private static final String EE5_TIMEOUT_FUNCTION = "webui.suntheme.common.timeoutSubmitForm"; //NOI18N
    private static final String EE4_TIMEOUT_FUNCTION = "common_timeoutSubmitForm"; //NOI18N
    
    private static final String EE4_PAGE = "com.sun.rave.web.ui.component.Page";   //NOI18N
    private static final String EE5_PAGE = "com.sun.webui.jsf.component.Page";   //NOI18N
    
    private static final String SUBMIT_PATTERN_SUFFIX = "\\s*\\(\\s*this\\s*\\.\\s*form\\s*,\\s*'\\S+'\\s*\\)\\s*;?"; //NOI18N
    
    private String timeoutFunction;

    private Pattern submitPattern;

    protected DesignBean bean;
    public AutoSubmitOnChangeCheckedAction(DesignBean bean) {
        super(bundle.getMessage("autoSubmit")); //NOI18N
        this.bean = bean;
        //default to ee4 value:
        this.timeoutFunction = EE5_PAGE.equals(getPageClassName()) ? EE5_TIMEOUT_FUNCTION : EE4_TIMEOUT_FUNCTION;
        this.submitPattern = Pattern.compile(this.timeoutFunction + SUBMIT_PATTERN_SUFFIX);
    }

    public void setAutoSubmit(boolean autoSubmit) {
        //remove any submit script from onchange if submit property is onclick
        cleanOnchangeIfAppropriate();
        DesignProperty prop = getSubmitProperty();
        if (prop != null) {
            String value = (String)prop.getValue();
            if (value == null) value = "";	//NOI18N
            //get rid of "this.form.submit()" if it's there
            value = thisDotFormDotSubmitPattern.matcher(value).replaceFirst(""); //NOI18N
            Matcher m = submitPattern.matcher(value);
            if (autoSubmit) {
                //if submitPattern is not found, append it
                if (!m.find()) {
                    prop.setValue(getSubmitScript(value));
                }
            }
            else {
                //erase the submit pattern
                String newValue = m.replaceFirst("");
                if (newValue.length() < 1) {
                    prop.unset();
                }
                else {
                    prop.setValue(newValue); //NOI18N
                }
            }
            if (getPageClassName() != null) {
                return;	//don't touch immediate property if this is a recognized ee4 or ee5 page
            }
            prop = bean.getProperty("immediate"); //NOI18N
            if (prop != null) {
                if (autoSubmit) {
                    prop.setValue(Boolean.TRUE);
                } else {
                    prop.unset();
                }
            }
        }
    }

    public boolean isAutoSubmit() {
        DesignProperty property = getSubmitProperty();
        if (property == null)
            return false;
        String value = (String) property.getValue();
        if(value == null)
            return false;
        return submitPattern.matcher(value).find() ||
               thisDotFormDotSubmitPattern.matcher(value).find();
    }

    public void toggleAutoSubmit() {
        //remove any submit script from onchange if submit property is onclick
        cleanOnchangeIfAppropriate();
        DesignProperty prop = getSubmitProperty();
        if (prop != null) {
            boolean isON = isAutoSubmit();
            String value = (String)prop.getValue();
            if (value == null || value.length() == 0) {
                // If no property value, set it
                prop.setValue(getSubmitScript(null));
            } else {
                //get rid of "this.form.submit()" if it's there
                value = thisDotFormDotSubmitPattern.matcher(value).replaceFirst(""); //NOI18N
                if (isON) {
                    // If property value contains the onSubmit script, remove it
                    prop.setValue(submitPattern.matcher(value).replaceFirst("")); //NOI18N
                } else {
                    // Otherwise, append the onSubmit script
                    prop.setValue(getSubmitScript(value));
                }
            }
            if (getPageClassName() != null) {
                return;	//don't touch immediate property if this is a recognized ee4 or ee5 page
            }
            prop = bean.getProperty("immediate"); //NOI18N
            if (prop != null) {
                if (isON) {
                    prop.unset();
                } else {
                    prop.setValue(Boolean.TRUE);
                }
            }
        }
    }

    public boolean isChecked() {
        return isAutoSubmit();
    }

    public Result invoke() {
        toggleAutoSubmit();
        return Result.SUCCESS;
    }

    /**
     * Returns the <code>onchange</code> property for all components except
     * checkbox and radio button types, for which <code>onclick</code> is 
     * returned. Special casing for these components needed by Internet 
     * Explorer.
     */
    DesignProperty getSubmitProperty() {
        return bean.getProperty(getSubmitPropertyName());
    }

    private String getSubmitPropertyName() {
        Object beanInstance = bean.getInstance();
        if (beanInstance instanceof HtmlSelectBooleanCheckbox || 
		beanInstance instanceof HtmlSelectManyCheckbox || 
		beanInstance instanceof HtmlSelectOneRadio)
            return "onclick"; //NOI18N
        else
            return "onchange"; //NOI18N
    }
    
    String getSubmitScript(String previousScript) {
        StringBuffer buffer = new StringBuffer();
        if (previousScript != null) {
            buffer.append(previousScript);
            if (!Pattern.compile(";\\s*$").matcher(previousScript).find()) {
                buffer.append(';');
            }
            if (!Pattern.compile("\\s+$").matcher(buffer.toString()).find()) {
                buffer.append(' ');
            }
        }
        String id = HtmlFormDesignInfo.getFullyQualifiedId(bean);
        if (id == null) {
            id = bean.getInstanceName();
        }
        else if (id.startsWith(String.valueOf(NamingContainer.SEPARATOR_CHAR)) && id.length() > 1) {
            //fully qualified id (starting with ":") could look intimidating to users. so just chop off leading ":"
            id = id.substring(1, id.length());
        }
        buffer.append(this.timeoutFunction);
        buffer.append("(this.form, '");
        buffer.append(id);
        buffer.append("');");
        return buffer.toString();
    }

    //in old pages, onchange might have been used for radiobutton and checkbox types
    //if this is an old page and the correct submit property is onclick,
    //then clean up onchange
    private void cleanOnchangeIfAppropriate() {     
        if (getPageClassName() == null && "onclick".equals(getSubmitPropertyName())) { //NOI18N
            //remove any onsubmit script from onchange handler
            DesignProperty onchangeProp = bean.getProperty("onchange"); //NOI18N
            if (onchangeProp != null) {
                String onchangeValue = (String)onchangeProp.getValue();
                if (onchangeValue != null) {
                    onchangeValue = thisDotFormDotSubmitPattern.matcher(onchangeValue).replaceFirst(""); //NOI18N
                    onchangeProp.setValue(submitPattern.matcher(onchangeValue).replaceFirst("")); //NOI18N
                }
            }
        }
    }

    private String getPageClassName() {
        DesignBean testBean = bean;
        while (testBean != null) {
            Object instance = testBean.getInstance();
            if (instance != null) {
                String className = instance.getClass().getName();
                if (EE4_PAGE.equals(className) || EE5_PAGE.equals(className)) {
                    return className;
                }
            }
            testBean = testBean.getBeanParent();
        }
        return null;
    }
}
