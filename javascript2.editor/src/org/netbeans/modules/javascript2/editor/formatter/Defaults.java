/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.formatter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;

/**
 *
 * @author Petr Hejl
 */
public final class Defaults {

    private static final Map<String, String> JSON_SPECIAL_VALUES = new HashMap<String, String>();

    static {
        JSON_SPECIAL_VALUES.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS.name());
        JSON_SPECIAL_VALUES.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS.name());
    }

    public static DefaultsProvider getInstance(String mimeType) {
        if (JsTokenId.JAVASCRIPT_MIME_TYPE.equals(mimeType)) {
            return new FmtOptions.BasicDefaultsProvider();
        } else if (JsTokenId.JSON_MIME_TYPE.equals(mimeType)) {
            DefaultsProvider basic = new FmtOptions.BasicDefaultsProvider();
            return new ProxyDefaultsProvider(basic, JSON_SPECIAL_VALUES);
        }
        throw new IllegalStateException("Unsupported mime type " + mimeType);
    }

    public static class ProxyDefaultsProvider implements DefaultsProvider {

        private final DefaultsProvider provider;

        private final Map<String, String> defaults;

        public ProxyDefaultsProvider(DefaultsProvider provider, Map<String, String> defaults) {
            this.provider = provider;
            this.defaults = defaults;
        }

        @Override
        public int getDefaultAsInt(String key) {
            synchronized (defaults) {
                if (defaults.containsKey(key)) {
                    return Integer.parseInt(defaults.get(key));
                }
            }
            return provider.getDefaultAsInt(key);
        }

        @Override
        public boolean getDefaultAsBoolean(String key) {
            synchronized (defaults) {
                if (defaults.containsKey(key)) {
                    return Boolean.parseBoolean(defaults.get(key));
                }
            }
            return provider.getDefaultAsBoolean(key);
        }

        @Override
        public String getDefaultAsString(String key) {
            synchronized (defaults) {
                if (defaults.containsKey(key)) {
                    return defaults.get(key);
                }
            }
            return provider.getDefaultAsString(key);
        }

    }
}
