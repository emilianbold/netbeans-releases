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

/**
 * <P>A BeanCreateInfo describes an item on a Palette that will create a bean in a visual designer.
 * This includes a display name, description, icon, etc.  There is also (most importantly) a hook
 * to programmatically manipulate the newly created bean immediately after is has been created.
 * This is useful for setting a default state for the newly created bean.</P>
 *
 * <P>If the specified JavaBean has an associated DesignInfo, the DesignInfo's 'beanCreated'
 * method will be called before the BeanCreateInfo's 'beanCreated' method is called.  This gives the
 * DesignInfo the "first crack", but it gives the BeanCreateInfo the "last word".</P>
 *
 * <P><B>IMPLEMENTED BY THE COMPONENT AUTHOR</B> - This interface is designed to be implemented by
 * the component (bean) author.  The BasicBeanCreateInfo class can be used for convenience.</P>
 *
 * @author Joe Nuxoll
 * @version 1.0
 * @see com.sun.rave.designtime.impl.BasicBeanCreateInfo
 */
public interface BeanCreateInfo extends DisplayItem {

    /**
     * Returns the class name of the new JavaBean to create when this BeanCreateInfo is invoked in
     * a visual designer.
     *
     * @return The String fully qualified class name for the JavaBean to create.
     */
    public String getBeanClassName();

    /**
     * <p>A hook that gets called after this JavaBean gets created initially.  This is useful for a
     * component author to setup an initial state for their JavaBean when it is first created.  Note
     * that this method is only called one time after the JavaBeans are initially created from the
     * palette.  This is *not* a hook that is called each time the project is reopened.</p>

     * <p>NOTE: If the specified bean has an associated DesignInfo class - it will have "first
     * crack" at modifying the initial state of the bean.  This method will be called after the
     * DesignInfo one is called.</p>
     *
     * @return A standard Result object, indicating success or failure - and optionally including
     *         messages for the user.
     */
    public Result beanCreatedSetup(DesignBean designBean);
}
