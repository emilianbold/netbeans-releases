/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 *
 * @author tomslot
 */
public class PredefinedSymbols {
    public static final String MIXED_TYPE = "mixed"; //NOI18N

    // see http://www.php.net/manual/en/reserved.variables.php
    public static final Collection<String> SUPERGLOBALS = new TreeSet<String>(Arrays.asList(
            "GLOBALS", "_SERVER", "_GET", "_POST", "_FILES", //NOI18N
            "_COOKIE", "_SESSION", "_REQUEST", "_ENV", "php_errormsg", //NOI18N
            "HTTP_RAW_POST_DATA", "http_response_header", "argc", "argv")); //NOI18N

    public  static final Map<String,IndexedFunction> MAGIC_METHODS = new HashMap<String, IndexedFunction>();

    public static IndexedFunction createMagicFunction(String fncName, String arguments, int flags) {
        IndexedFunction ifnc = new MagicIndexedFunction(fncName, 
                NbBundle.getMessage(PredefinedSymbols.class, "MagicMethod"), //NOI18N
                null, null, arguments, 0, flags, ElementKind.METHOD);
        ifnc.setOptionalArgs(new int[0]);
        return ifnc;
    }
    static class MagicIndexedFunction extends IndexedFunction {
        public MagicIndexedFunction(String name, String in, PHPIndex index, String fileURL, String arguments, int offset, int flags, ElementKind kind) {
            super(name, in, index, fileURL, arguments, offset, flags, kind);
        }
    }

    static {
        IndexedFunction[] ifunctions = new IndexedFunction[] {
            createMagicFunction("__callStatic", "$name, $arguments", Modifier.PUBLIC | Modifier.STATIC),//NOI18N
            createMagicFunction("__set_state", "$array", Modifier.PUBLIC | Modifier.STATIC),//NOI18N
            createMagicFunction("__call", "$name, $arguments", Modifier.PUBLIC),//NOI18N
            createMagicFunction("__clone", "", Modifier.PUBLIC),//NOI18N
            createMagicFunction("__construct", "", Modifier.PUBLIC),//NOI18N
            createMagicFunction("__destruct", "", Modifier.PUBLIC),//NOI18N
            createMagicFunction("__get", "$name", Modifier.PUBLIC),//NOI18N
            createMagicFunction("__set", "$name, $value", Modifier.PUBLIC),//NOI18N
            createMagicFunction("__isset", "$name", Modifier.PUBLIC),//NOI18N
            createMagicFunction("__unset", "$name", Modifier.PUBLIC),//NOI18N
            createMagicFunction("__sleep", "", Modifier.PUBLIC),//NOI18N
            createMagicFunction("__wakeup", "", Modifier.PUBLIC),//NOI18N
            createMagicFunction("__toString", "", Modifier.PUBLIC)//NOI18N
        };
        for (IndexedFunction ifunc : ifunctions) {
            MAGIC_METHODS.put(ifunc.getName(),ifunc);
        }
    }
    public static final List<String> SERVER_ENTRY_CONSTANTS =
            Arrays.asList(new String[]{
                "PHP_SELF",
                "GATEWAY_INTERFACE",
                "SERVER_ADDR",
                "SERVER_NAME",
                "SERVER_SOFTWARE",
                "SERVER_PROTOCOL",
                "REQUEST_METHOD",
                "QUERY_STRING",
                "DOCUMENT_ROOT",
                "HTTP_ACCEPT",
                "HTTP_ACCEPT_CHARSET",
                "HTTP_ACCEPT_ENCODING",
                "HTTP_ACCEPT_LANGUAGE",
                "HTTP_CONNECTION",
                "HTTP_HOST",
                "HTTP_REFERER",
                "HTTP_USER_AGENT",
                "HTTPS",
                "REMOTE_ADDR",
                "REMOTE_HOST",
                "REMOTE_PORT",
                "SCRIPT_FILENAME",
                "SERVER_ADMIN",
                "SERVER_PORT",
                "SERVER_SIGNATURE",
                "PATH_TRANSLATED",
                "SCRIPT_NAME",
                "REQUEST_URI",
                "PHP_AUTH_DIGEST",
                "PHP_AUTH_USER",
                "PHP_AUTH_PW",
                "AUTH_TYPE"
            });


    public static enum VariableKind {
        STANDARD,
        THIS,
        SELF,
        PARENT
    };

    private static String docURLBase;

    private static void initDoc() {
        File file = InstalledFileLocator.getDefault().locate("docs/predefined_vars.zip", null, true); //NoI18N
        if (file != null) {
            try {
                URL urll = file.toURL();
                urll = FileUtil.getArchiveRoot(urll);
                docURLBase = urll.toString();
            } catch (java.net.MalformedURLException e) {
                // nothing to do
                }
        }
    }

    public static boolean isSuperGlobalName(String name){
        return SUPERGLOBALS.contains(name);
    }

    public static String getDocumentation(String name) {
        if (docURLBase == null) {
            initDoc();
        }

        String resPath = String.format("%s%s.desc", docURLBase, name); //NOI18N

        try {
            URL url = new URL(resPath);
            InputStream is = url.openStream();
            byte buffer[] = new byte[1000];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int count = 0;
            do {
                count = is.read(buffer);
                if (count > 0) {
                    baos.write(buffer, 0, count);
                }
            } while (count > 0);

            is.close();
            String text = baos.toString();
            baos.close();
            return text;
        } catch (java.io.IOException e) {
            return null;
        }
    }
}
