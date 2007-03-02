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
package org.netbeans.modules.visualweb.propertyeditors;

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
public class StandardUrlPropertyEditor extends PropertyEditorSupport implements PropertyEditor2,
    ExPropertyEditor {

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

        StandardUrlPanel panel = new StandardUrlPanel();
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
