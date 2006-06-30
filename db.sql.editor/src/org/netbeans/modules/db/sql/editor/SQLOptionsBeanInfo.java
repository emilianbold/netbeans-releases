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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.editor;

import java.util.MissingResourceException;
import org.netbeans.modules.editor.options.BaseOptionsBeanInfo;
import org.netbeans.modules.editor.options.OptionSupport;
import org.openide.util.NbBundle;


/**
 * BeanInfo for sql options
 *
 * @author Jesse Beaumont based on code by Miloslav Metelka
 */
public class SQLOptionsBeanInfo extends BaseOptionsBeanInfo {

    /**
     * Constructor. The parameter in the superclass constructor is the
     * icon prefix. Therefore the files sqlOptions.gif and sqlOptions32.gif
     * are used by this.
     */
    public SQLOptionsBeanInfo() {
        super("/org/netbeans/modules/db/sql/editor/resources/sqlOptions"); // NOI18N
    }

    /*
     * Gets the property names after mergin it with the set of properties
     * available from the BaseOptions from the editor module
     */
    protected String[] getPropNames() {
        return OptionSupport.mergeStringArrays(
                super.getPropNames(), 
                SQLOptions.SQL_PROP_NAMES);
    }

    /**
     * Get the class described by this bean info
     */
    protected Class getBeanClass() {
        return SQLOptions.class;
    }
    
    /**
     * Look up a resource bundle message, if it is not found locally defer to 
     * the super implementation
     */
    protected String getString(String key) {
        try {
            return NbBundle.getMessage(SQLOptionsBeanInfo.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }
}
