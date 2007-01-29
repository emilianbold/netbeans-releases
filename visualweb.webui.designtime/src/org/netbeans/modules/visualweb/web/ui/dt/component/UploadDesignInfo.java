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
package org.netbeans.modules.visualweb.web.ui.dt.component;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.web.ui.component.Upload;
import javax.faces.convert.Converter;

/**
 * Design time behavior for a <code>Upload</code> AKA <code>File Upload</code>
 * component.
 *
 * @author Edwin Goei
 */
public class UploadDesignInfo extends EditableValueHolderDesignInfo {

    public UploadDesignInfo() {
        super(Upload.class);
    }

    public boolean acceptLink(DesignBean targetBean, DesignBean sourceBean, Class sourceClass) {
        if (Converter.class.isAssignableFrom(sourceClass))
            return false;
        return super.acceptLink(targetBean, sourceBean, sourceClass);
    }

    public boolean acceptChild(DesignBean parentBean, DesignBean childBean, Class childClass) {
        if (Converter.class.isAssignableFrom(childClass))
            return false;
        return super.acceptChild(parentBean, childBean, childClass);
    }
}
