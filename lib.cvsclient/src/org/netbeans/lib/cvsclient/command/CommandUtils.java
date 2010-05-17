/*****************************************************************************
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
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Thomas Singer.
 * Portions created by Robert Greig are Copyright (C) 2001.
 * All Rights Reserved.
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
 *
 * Contributor(s): Thomas Singer.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.command;

import java.util.*;

/**
 * @author  Thomas Singer
 */
public class CommandUtils {
    /**
     * Returns the directory relative to local path from the specified message.
     * This method returns null, if the specified message isn't a EXAM_DIR-
     * message.
     */
    public static String getExaminedDirectory(String message, String examDirPattern) {
        final int index = message.indexOf(examDirPattern);
        final int startIndex = index + examDirPattern.length() + 1;
        if (index < 0 || message.length() < startIndex + 1) {
            return null;
        }

        return message.substring(startIndex);
    }
    
    /**
     * for a list of string will return the string that equals the name parameter.
     * To be used everywhere you need to have only one string occupying teh memory space,
     * eg. in Builders to have the revision number strings not repeatedly in memory.
     */
    public static String findUniqueString(String name, List list) {
        if (name == null) {
            return null;
        }
        int index = list.indexOf(name);
        if (index >= 0) {
            return (String)list.get(index);
        }
        else {
            String newName = new String(name);
            list.add(newName);
            return newName;
        }
    }    
}
