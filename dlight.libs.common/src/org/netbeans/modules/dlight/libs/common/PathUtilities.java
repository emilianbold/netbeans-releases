/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.libs.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Vladimir Kvashin
 */
public class PathUtilities {

    private PathUtilities() {
    }
    
    /** Same as the C library dirname function: given a path, return
     * its directory name. Unlike dirname, however, return null if
     * the file is in the current directory rather than ".".
     */
    public static String getDirName(String path) {
        if (path.length()>0 && (path.charAt(path.length()-1) == '\\' || path.charAt(path.length()-1) == '/')) {
            path = path.substring(0,path.length()-1);
        }
        int sep = path.lastIndexOf('/');
        if (sep == -1) {
            sep = path.lastIndexOf('\\');
        }
        if (sep != -1) {
            return path.substring(0, sep);
        }
        return null;
    }

    /** Same as the C library basename function: given a path, return
     * its filename.
     */
    public static String getBaseName(String path) {
        if (path.length()>0 && (path.charAt(path.length()-1) == '\\' || path.charAt(path.length()-1) == '/')) {
            path = path.substring(0,path.length()-1);
        }
        int sep = path.lastIndexOf('/');
        if (sep == -1) {
            sep = path.lastIndexOf('\\');
        }
        if (sep != -1) {
            return path.substring(sep + 1);
        }
        return path;
    }        

    // Implementation moved from RemoteFileSystem
    public static String normalizeUnixPath(String absPath) {
        //BZ#192265 as vkvashin stated the URI i sused to normilize the path
        //but URI is really very restrictive so let's use another way
        //will use the face that path is absolute and we have Unix like system
        //no special code for Windows
        //also as absolute path is passed to the method we will use it as an absolute
        String result = absPath;
        if (result.endsWith("/.")) {// NOI18N
            result = result.substring(0, result.length()-2);
        }
// # Remove all /./ sequences.
//    local   path=${1//\/.\//\/}
        result = result.replaceAll("[/][.][/]", "[/]"); // NOI18N

//
//    # Remove first dir/.. sequence.
//    local   npath=$(echo $path | sed -e 's;[^/][^/]*/\.\./;;')
        if (result.startsWith("..")){ // NOI18N
            result = result.replaceFirst("..", ""); // NOI18N
        }
//    # Remove remaining dir/.. sequence.
//    while [[ $npath != $path ]]
//    do
//        path=$npath
//        npath=$(echo $path | sed -e 's;[^/][^/]*/\.\./;;')
//    done
//    echo $path
        Pattern p = Pattern.compile(".*[/]([^/]+)[/][.][.].*"); // NOI18N
        Matcher m = p.matcher(result);
        if (m.matches()){
            result = result.replaceAll("[/][^/]+[/][.][.]", ""); // NOI18N
        }
        return result;

    }    
}
