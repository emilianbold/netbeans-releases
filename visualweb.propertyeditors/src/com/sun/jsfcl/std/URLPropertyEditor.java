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
package com.sun.jsfcl.std;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.io.File;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 * PropertyEditor for URLs.
 * Based on FileEditor in core.
 *
 * @author Jaroslav Tulach, David Strupl, Peter Zavadsky, Jesse Glick
 * @author Tor Norbye, Jim Davidson
 * @deprecated
 */
public class URLPropertyEditor extends PropertyEditorSupport implements PropertyEditor2,
    ExPropertyEditor { // PropertyChangeListener, ExPropertyEditor

    public static final String PROPERTY_FORM_FILE = "URLPropertyEditor-form-file"; // NOI18N
    public static final String PROPERTY_LIVE_CONTEXT = "URLPropertyEditor-live-context"; // NOI18N
    public static final String PROPERTY_PROPERTY = "URLPropertyEditor-property"; // NOI18N

    protected DesignProperty liveProperty;
    protected DesignContext liveContext;
    protected Node.Property property;
    protected File formFile;

    public void attachEnv(PropertyEnv env) {

        Object object = env.getFeatureDescriptor().getValue(PROPERTY_FORM_FILE);
        if (object instanceof File) {
            formFile = (File)object;
        } else if (object instanceof FileObject) {
            FileObject fileObject = (FileObject)object;
            formFile = FileUtil.toFile(fileObject);
        }
        object = env.getFeatureDescriptor().getValue(PROPERTY_LIVE_CONTEXT);
        if (object instanceof DesignContext) {
            liveContext = (DesignContext)object;
        }
        object = env.getFeatureDescriptor().getValue(PROPERTY_PROPERTY);
        if (object instanceof Node.Property) {
            property = (Node.Property)object;
        }
    }

    public String getAsText() {
        return (String)getValue();
    }

    public void setAsText(String str) throws IllegalArgumentException {

        if (str != null && str.trim().length() == 0) {
            str = null;
        }
        setValue(str);
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public Component getCustomEditor() {

        URLPanel panel = new URLPanel(this);
        panel.setDesignProperty(liveProperty);
        // handle case where we are called from DocumentComp
        if (formFile != null) {
            panel.setRelativeDirectory(formFile);
        }
        if (liveContext != null) {
            panel.setDesignContext(liveContext);
        }
        if (property != null) {
            panel.setProperty(property);
        }
        panel.initialize();
        return panel;
    }

    public File getFormFile() {

        return formFile;
    }

    public String getJavaInitializationString() {

        if (getValue() == null) {
            return null;
        }
        return (String)getValue();
    }

    /**
     * Specified by PropertyEditor2
     */
    public void setDesignProperty(DesignProperty prop) {

        this.liveProperty = prop;
    }

    public void setValue(Object value) {

        super.setValue(value);
    }
}
