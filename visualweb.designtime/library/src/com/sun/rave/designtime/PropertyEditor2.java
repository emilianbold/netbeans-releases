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

package com.sun.rave.designtime;

import java.beans.PropertyEditor;

/**
 * <p>This interface extends the standard PropertyEditor interface and adds a hook to give the
 * implementor access to the design-time context of the property being edited via the DesignProperty
 * interface.  This is useful if a PropertyEditor author wishes to display a list of instances
 * within scope, or wishes to drill-in to the object that this property is being set on.</p>
 *
 * <p>NOTE: It is important to only use the passed-in DesignProperty for context purposes.  This
 * DesignProperty should not be directly manipulated, or the IDE could get into a recursive loop.</p>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 */
public interface PropertyEditor2 extends PropertyEditor {

    /**
     * <p>When the PropetyEditor is being invoked, the matching DesignProperty will be passed in for
     * context.  This can be used to dig into the DesignBean being edited and its surrounding context.
     * </p>
     *
     * <p>NOTE: It is important to only use the passed-in DesignProperty for context purposes.  This
     * DesignProperty should not be directly manipulated, or the IDE could get into a recursive loop.
     * </p>
     *
     * @param prop The DesignProperty currently being edited by this PropertyEditor2 - this may be
     *        used for context purposes only, and should not be used to manipulate the property.
     */
    public void setDesignProperty(DesignProperty prop);
}
