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

package org.netbeans.modules.debugger.importd;

import java.beans.*;
import java.awt.Image;


/** Object that provides beaninfo for {@link ImportDebuggerType}.
*
* @author Jan Jancura
*/
public class ImportDebuggerTypeBeanInfo extends SimpleBeanInfo {

    /** icon */
    private static Image icon;
    /** icon32 */
    private static Image icon32;

    private static BeanDescriptor descr;
    static {
        descr = new BeanDescriptor (ImportDebuggerType.class);
        descr.setName (ImportDebugger.getLocString ("CTL_Import_Debugger_Type"));
    }

    /* gets FileSystemBeanInfo
    * @return FileSystemBeanInfo
    */
/*    public final BeanInfo[] getAdditionalBeanInfo () {
        return new BeanInfo[] {new DebuggerTypeBeanInfo ()};
    }*/

    public BeanDescriptor getBeanDescriptor () {
        return descr;
    }

    /**
    * Claim there are no icons available.  You can override
    * this if you want to provide icons for your bean.
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null) {
                icon = loadImage("/org/netbeans/modules/debugger/resources/jpdaDebugging.gif"); // NOI18N
            }
            return icon;
        } else { // 32
            if (icon32 == null) {
                icon32 = loadImage("/org/netbeans/modules/debugger/resources/jpdaDebugging32.gif"); // NOI18N
            }
            return icon32;
        }
    }
}
