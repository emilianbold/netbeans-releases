/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.plain.options;

import java.util.MissingResourceException;
import org.netbeans.modules.editor.options.BaseOptionsBeanInfo;
import org.openide.util.NbBundle;


/** BeanInfo for plain options
*
* @author Miloslav Metelka, Ales Novak
*/
public class PlainOptionsBeanInfo extends BaseOptionsBeanInfo {

    public PlainOptionsBeanInfo() {
        this("/org/netbeans/modules/editor/plain/resources/plainOptions"); // NOI18N
    }

    public PlainOptionsBeanInfo(String iconPrefix) {
        super(iconPrefix);
    }

    protected Class getBeanClass() {
        return PlainOptions.class;
    }

    protected String[] getPropNames() {
        return PlainOptions.PLAIN_PROP_NAMES;
    }

    /**
     * Get localized string
     */
    protected String getString(String key) {
        try {
            return NbBundle.getMessage(PlainOptionsBeanInfo.class, key);
        } catch (MissingResourceException e) {
            return super.getString(key);
        }
    }

}
