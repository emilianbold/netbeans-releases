/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository.translator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.util.Utilities;

/**
 *
 * @author vk155633
 */
public class IndexConverter {

    private static class Rule {

        public final String from;
        public final String to;

        private Rule(String first, String second) {
            this.from = first;
            this.to = second;
        }
    }
    
    private static class Rules implements Iterable<Rule> {

        private List<Rule> list = new ArrayList<Rule>();
        
        private void addRule(String first, String second) {
            first = first.endsWith("/") ? first : first + '/'; //NOI18N
            second = second.endsWith("/") ? second : second + '/'; //NOI18N
            list.add(new Rule(first, second));
            if (Utilities.isWindows()) {
                list.add(new Rule(first.replace('/', '\\'), second.replace('/', '\\')));
            }            
        }

        @Override
        public Iterator<Rule> iterator() {
            return list.iterator();
        }

        private boolean isEmpty() {
            return list.isEmpty();
        }
    }

    private Rules rules = new Rules();

    IndexConverter(String oldPath, String newPath) {
        if (!oldPath.endsWith(newPath)) {
            oldPath = oldPath.replace('\\', '/');
            newPath = newPath.replace('\\', '/');
            String[] oldParts = oldPath.split("/"); // NOI18N
            String[] newParts = newPath.split("/"); // NOI18N
            int oldIdx = oldParts.length - 1;
            int newIdx = newParts.length - 1;
            while (newIdx > 0 && oldIdx > 0) {
                if (!oldParts[oldIdx].equals(newParts[newIdx])) {
                    break;
                }
                oldIdx--;
                newIdx--;
            }
            String from;
            {
                StringBuilder sb = Utilities.isWindows() ? new StringBuilder() : new StringBuilder("/"); // NOI18N
                for (int i = 0; i <= oldIdx; i++) {
                    if (sb.length() > 0 &&  !endsWith(sb, '/')) {
                        sb.append('/');
                    }
                    sb.append(oldParts[i]);
                }
                from = sb.toString();
            }
            String to;
            {
                StringBuilder sb = Utilities.isWindows() ? new StringBuilder() : new StringBuilder("/"); // NOI18N
                for (int i = 0; i <= newIdx; i++) {
                    if (sb.length() > 0 &&  !endsWith(sb, '/')) {
                        sb.append('/');
                    }
                    sb.append(newParts[i]);
                }
                to = sb.toString();
            }
            rules.addRule(from, to);
        }

        String option = System.getProperty("cnd.repository.translation");
        if (option != null) {
            String[] parts = option.split(":"); // NOI18N
            if (parts.length > 0) {
                for (String pair : parts) {
                    String[] t = pair.split("="); // NOI18N
                    if (t.length != 2) {
                        System.err.printf("Incorrect option: %s\n", option); //NOI18N
                        return;
                    }
                    String from = t[0];
                    String to = t[1];
                    rules.addRule(from, to);
                }
            } else {
                System.err.printf("Incorrect option: %s\n", option); //NOI18N
            }
        }
    }

    private boolean endsWith(StringBuilder sb, char c) {
        if (sb != null && sb.length() > 0 ) {
            return sb.charAt(sb.length() - 1) == c;
        }
        return false;
    }

    public boolean needsConversion() {
        return ! rules.isEmpty();
    }

    public CharSequence convert(CharSequence path) {
        if (path != null) {
            for (Rule pair : rules) {
                String toFind = pair.from;
                if (startsWith(path, toFind)) {
                    String toReplace = pair.to;
                    String subst = toReplace + path.subSequence(toFind.length(), path.length());
                    //return CharSequences.create(subst);
                    return FilePathCache.getManager().getString(subst);
                }
            }
        }
        return path;
    }
    
    private boolean startsWith(CharSequence path, CharSequence prefix) {
        if (Utilities.isWindows()) {
            return CharSequenceUtils.startsWithIgnoreCase(path, prefix);
        } else {
            return CharSequenceUtils.startsWith(path, prefix);
        }
    }
}
