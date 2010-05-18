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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

/*
 * Bundle.java
 *
 * Created on 15. prosinec 2003, 11:28
 */
package org.netbeans.mobility.antext;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Helper class for getting messages from resource bundle which is located in the same package as this class.
 * @author  Adam Sotona
 */
public class Bundle
{
    
    private Bundle()
    {
        //Avoid instantiation of this class
    }
    
    private static final ResourceBundle bundle = ResourceBundle.getBundle(Bundle.class.getName());
    
    /**
     * Gets message of key.
     * @param key name of message
     * @return message
     */
    static public String getMessage(final String key)
    {
        return bundle.getString(key);
    }
    
    /**
     * Gets message of key and replaces 1 argument using MessageFormat class.
     * @param key name of message
     * @param arg0 1. argument
     * @return formated message
     */
    static public String getMessage(final String key, final String arg0)
    {
        return MessageFormat.format(bundle.getString(key), new Object[] {arg0});
    }
    
    /**
     * Gets message of key and replaces 2 arguments using MessageFormat class.
     * @param key name of message
     * @param arg0 1. argument
     * @param arg1 2. argument
     * @return formated message
     */
    static public String getMessage(final String key, final String arg0, final String arg1)
    {
        return MessageFormat.format(bundle.getString(key), new Object[] {arg0, arg1});
    }
    
    /**
     * Gets message of key and replaces 3 arguments using MessageFormat class.
     * @param key name of message
     * @param arg0 1. argument
     * @param arg1 2. argument
     * @param arg2 3. argument
     * @return formated message
     */
    static public String getMessage(final String key, final String arg0, final String arg1, final String arg2)
    {
        return MessageFormat.format(bundle.getString(key), new Object[] {arg0, arg1, arg2});
    }
}
