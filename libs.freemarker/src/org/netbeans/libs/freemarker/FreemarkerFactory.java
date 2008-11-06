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
 * The Original Software is scripting.dev.java.net. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc.
 *
 * Portions Copyrighted 2006 Sun Microsystems, Inc.
 */

/*
 * @author A. Sundararajan
 */
package org.netbeans.libs.freemarker;

import javax.script.*;
import java.util.*;

@org.openide.util.lookup.ServiceProvider(service=javax.script.ScriptEngineFactory.class)
public class FreemarkerFactory implements ScriptEngineFactory {
    public String getEngineName() { 
        return "freemarker";
    }

    public String getEngineVersion() {
        return "2.3.8";
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public String getLanguageName() {
        return "freemarker";
    }

    public String getLanguageVersion() {
        return "2.3.8";
    }

    public String getMethodCallSyntax(String obj, String m, String... args) {
        StringBuffer buf = new StringBuffer();
        buf.append("${");
        buf.append(obj);
        buf.append(".");
        buf.append(m);
        buf.append("(");
        if (args.length != 0) {
            int i = 0;
            for (; i < args.length - 1; i++) {
                buf.append("$" + args[i]);
                buf.append(", ");
            }
            buf.append("$" + args[i]);
        }        
        buf.append(")}");
        return buf.toString();
    }

    public List<String> getMimeTypes() {
        return mimeTypes;
    }

    public List<String> getNames() {
        return names;
    }

    public String getOutputStatement(String toDisplay) {
        StringBuffer buf = new StringBuffer();
        int len = toDisplay.length();
        buf.append("${context.getWriter().write(\"");
        for (int i = 0; i < len; i++) {
            char ch = toDisplay.charAt(i);
            switch (ch) {
            case '"':
                buf.append("\\\"");
                break;
            case '\\':
                buf.append("\\\\");
                break;
            default:
                buf.append(ch);
                break;
            }
        }
        buf.append("\")}");
        return buf.toString();
    }

    public String getParameter(String key) {
        if (key.equals(ScriptEngine.NAME)) {
            return getLanguageName();
        } else if (key.equals(ScriptEngine.ENGINE)) {
            return getEngineName();
        } else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
            return getEngineVersion();
        } else if (key.equals(ScriptEngine.LANGUAGE)) {
            return getLanguageName();
        } else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
            return getLanguageVersion();
        } else if (key.equals("THREADING")) {
            return "MULTITHREADED";
        } else {
            return null;
        }
    } 

    public String getProgram(String... statements) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < statements.length; i++) {
            buf.append(statements[i]);
            buf.append("\n");
        }
        return buf.toString();
    }

    public ScriptEngine getScriptEngine() {
        return new FreemarkerEngine(this);
    }

    private static List<String> names;
    private static List<String> extensions;
    private static List<String> mimeTypes;
    static {
        names = new ArrayList<String>(2);
        names.add("FreeMarker");
        names.add("freemarker");
        names = Collections.unmodifiableList(names);
        extensions = new ArrayList<String>(1);
        extensions.add("fm");
        extensions = Collections.unmodifiableList(extensions);
        mimeTypes = new ArrayList<String>(0);
        mimeTypes = Collections.unmodifiableList(mimeTypes);
    }
}
