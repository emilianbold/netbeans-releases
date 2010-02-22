/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.project;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;

/**
 *
 * @author Tim Boudreau
 */
final class JCStoreGroup extends StoreGroup {

    private final Map<String, ExtDocument> models = new HashMap<String, ExtDocument>();
    private DocumentListener documentListener = new DocumentListener() {

        @Override
        public void insertUpdate(DocumentEvent e) {
            documentModified(e.getDocument());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            documentModified(e.getDocument());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            documentModified(e.getDocument());
        }
    };
    private final Set<String> modified = new HashSet<String>();

    private void documentModified(Document d) {
        for (Map.Entry<String, ExtDocument> en : models.entrySet()) {
            String key = en.getKey();
            Document val = en.getValue();
            if (d == val) {
                modified.add(key);
                break;
            }
        }
    }

    private void checkModelDoesNotExist(String propertyName) {
        if (models.get(propertyName) != null) {
            throw new IllegalArgumentException("Model for property " + //NOI18N
                    propertyName + "already exists."); //NOI18N
        }
    }
    static final Pattern NESTED_PROP_PATTERN = Pattern.compile("\\$\\{(.*?)\\}"); //NOI18N

    /**
     * Take the given key and value, and convert ${propname} delimited
     * properties to their resolved value
     * @param key The property key being searched for, to avoid recursive
     * invocation
     * @param value The raw value, including delimited strings
     * @return The dereferenced value
     */
    private String resolve(String key, String value, PropertyEvaluator eval) {
        if (value != null && value.equals("${" + key + "}")) { //NOI18N
            return key;
        }
        StringBuilder sb = new StringBuilder(value);
        Matcher m = NESTED_PROP_PATTERN.matcher(sb);
        while (m.find()) {
            String resolveKey = m.group(1);
            if (!resolveKey.equals(key)) {
                String replacement = eval.evaluate(resolveKey);
                if (replacement != null) {
                    int start = m.start();
                    int end = m.end();
                    sb.replace(start, end, replacement);
                    m = NESTED_PROP_PATTERN.matcher(sb);
                }
            }
        }
        return sb.toString().trim();
    }

    public Document createResolvingDocument(PropertyEvaluator evaluator, String propertyName, String... substitutions) {

        checkModelDoesNotExist(propertyName);

        String value = evaluator.getProperty(propertyName);
        if (value == null) {
            value = ""; // NOI18N
        } else {
        }
        value = resolve(propertyName, value, evaluator);
        value = value.replace(File.separatorChar, '/');

        try {
            ExtDocument d = new ExtDocument(evaluator, substitutions);
            d.remove(0, d.getLength());
            d.insertString(0, value, null);
            d.addDocumentListener(documentListener);
            models.put(propertyName, d);
            return d;
        } catch (BadLocationException e) {
            assert false : "Bad location exception from new document."; // NOI18N
            return new PlainDocument();
        }
    }

    @Override
    public void store(EditableProperties editableProperties) {
        super.store(editableProperties);
        for (String name : modified) {
            ExtDocument doc = models.get(name);
            doc.store(editableProperties, name);
        }
    }

    private static final class ExtDocument extends PlainDocument {

        private final String[] propNames;
        private final PropertyEvaluator eval;

        ExtDocument(PropertyEvaluator eval, String... propNames) {
            this.propNames = propNames;
            this.eval = eval;
        }

        void store(EditableProperties props, String name) {
            String txt;
            try {
                txt = getText(0, getLength());
                for (String key : propNames) {
                    String val = eval.getProperty(key);
                    if (txt.startsWith(val)) {
                        txt = "${" + key + "}" + txt.substring(val.length()); //NOI18N
                    }
                }
                txt = txt.replace('\\', '/');
            } catch (BadLocationException e) {
                txt = ""; // NOI18N
            }
            props.setProperty(name, txt);
        }
    }
}
