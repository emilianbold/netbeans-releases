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

package org.netbeans.modules.cnd.settings;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 *  Bean info for CppSettings
 */
public class CppSettingsBeanInfo extends SimpleBeanInfo {

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bdesc = new BeanDescriptor(CppSettings.class);
        bdesc.setDisplayName(CppSettings.getString("OPTION_CPP_SETTINGS_NAME")); // NOI18N
        bdesc.setShortDescription(CppSettings.getString("HINT_CPP_SETTINGS_NAME")); // NOI18N
        return bdesc;
    }

    /**
     *  Descriptor of valid properties.
     *
     *  @return array of properties
     */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {

	int i = 0;
	PropertyDescriptor[] desc = null;
	try {
            desc = new PropertyDescriptor[] {
		new PropertyDescriptor(CppSettings.PROP_REPLACEABLE_STRINGS_TABLE, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_FREE_FORMAT_FORTRAN, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_PARSING_DELAY, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_FORTRAN_ENABLED, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_MAKE_NAME, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_MAKE_PATH, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_GDB_NAME, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_GDB_PATH, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_COMPILER_SET_NAME, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_COMPILER_SET_DIRECTORIES, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_C_COMPILER_NAME, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_CPP_COMPILER_NAME, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_FORTRAN_COMPILER_NAME, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_GDB_REQUIRED, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_C_REQUIRED, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_CPP_REQUIRED, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_FORTRAN_REQUIRED, CppSettings.class),
		new PropertyDescriptor(CppSettings.PROP_ARRAY_REPEAT_THRESHOLD, CppSettings.class),
	    };

	    desc[i].setDisplayName(CppSettings.getString( "PROP_REPLACEABLE_STRINGS"));	    //NOI18N
	    desc[i++].setShortDescription(CppSettings.getString( "HINT_REPLACEABLE_STRINGS"));	    //NOI18N
	    desc[i].setDisplayName(CppSettings.getString( "PROP_FREE_FORMAT_FORTRAN"));	    //NOI18N
 	    desc[i++].setShortDescription(CppSettings.getString( "HINT_FREE_FORMAT_FORTRAN"));	    //NOI18N
	    desc[i].setDisplayName(CppSettings.getString( "PROP_AUTO_PARSING_DELAY"));	    //NOI18N
 	    desc[i++].setShortDescription(CppSettings.getString( "HINT_AUTO_PARSING_DELAY"));	    //NOI18N
            if (!Boolean.getBoolean("cnd.debug.showHiddenProperties")) {  // NOI18N
                desc[i++].setHidden(true); // PROP_FORTRAN_ENABLED
                desc[i++].setHidden(true); // PROP_MAKE_NAME
                desc[i++].setHidden(true); // PROP_MAKE_PATH
                desc[i++].setHidden(true); // PROP_GDB_NAME
                desc[i++].setHidden(true); // PROP_GDB_PATH
                desc[i++].setHidden(true); // PROP_COMPILER_SET_NAME
                desc[i++].setHidden(true); // PROP_COMPILER_SET_DIRECTORIES
                desc[i++].setHidden(true); // PROP_C_COMPILER_NAME
                desc[i++].setHidden(true); // PROP_CPP_COMPILER_NAME
                desc[i++].setHidden(true); // PROP_FORTRAN_COMPILER_NAME
                desc[i++].setHidden(true); // PROP_GDB_REQUIRED
                desc[i++].setHidden(true); // PROP_C_REQUIRED
                desc[i++].setHidden(true); // PROP_CPP_REQUIRED
                desc[i++].setHidden(true); // PROP_FORTRAN_REQUIRED
            }
	} catch (IntrospectionException ex) {
	    throw new InternalError();
	}
	return desc;
    }
    
    /*
     *  There currently are no icons for CCF. This is just a place holder.
     */
    @Override
    public Image getIcon(int type) {
	// XXX this icon is wrong
	return ImageUtilities.loadImage("org/netbeans/modules/cnd/loaders/CCSrcIcon.gif"); //NOI18N
    }
}
