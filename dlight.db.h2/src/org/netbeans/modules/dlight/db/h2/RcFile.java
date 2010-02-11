/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.db.h2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.util.Exceptions;

/**
 * A common class for reading rc files in format
 *      # comment
 *      [section1]
 *      key1=value1
 *      key2=value2
 *      ...
 * @author vk155633
 */
final class RcFile {

     static class FormatException extends Exception {
        public FormatException(String message) {
            super(message);
        }

    }

    private class Section {

        public final String name;
        private final Map<String, String> map = new TreeMap<String, String>();

        public Section(String name) throws IOException {
            this.name = name;
        }

        public synchronized  String get(String key, String defaultValue) {
            if (map.containsKey(key)) {
                return map.get(key);
            } else {
                return defaultValue;
            }
        }

        public synchronized  Collection<String> getKeys() {
            return new ArrayList<String>(map.keySet());
        }

        public synchronized boolean containsKey(String key) {
            return map.containsKey(key);
        }

         synchronized void put(String key, String value) {
            map.put(key, value);
        }
    }

    private final Map<String, Section> sections = new TreeMap<String, Section>();
    private final File file;

     synchronized String get(String section, String key, String defaultValue) {
        Section sect = sections.get(section);
        return (sect == null) ? defaultValue : sect.get(key, defaultValue);
    }

     String get(String section, String key) {
        return get(section, key, null);
    }

     void put(String sectionName, String key, String value) throws IOException {
        Section section = sections.get(sectionName);
        if (section == null){
            section = new Section(sectionName);
        }
        section.put(key, value);
        sections.put(sectionName, section);
    }

     boolean containsKey(String section, String key) {
        Section sect = sections.get(section);
        return (sect == null) ? false : sect.containsKey(key);
    }

     synchronized Collection<String> getSections() {
        List<String> result = new ArrayList<String>();
        for (Section section : sections.values()) {
            result.add(section.name);
        }
        return result;
    }

     synchronized Collection<String> getKeys(String section) {
        Section sect = sections.get(section);
        return (sect == null) ? Collections.<String>emptyList() : sect.getKeys();
    }

     RcFile(File file) throws IOException, FormatException {
        this.file = file;
        read();
    }

    private void read() throws IOException, FormatException {
        if (!file.exists()){
            return;
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String str;
        Pattern commentPattern = Pattern.compile("(#.*)|([ \t]*)"); // NOI18N
        Pattern sectionPattern = Pattern.compile("\\[(.*)\\] *"); // NOI18N
        Pattern valuePattern = Pattern.compile("([^=]+)=(.*)"); //NOI18N
        Pattern justKeyPattern = Pattern.compile("[^=]+"); //NOI18N
        Section currSection = new Section(""); // default section
        while ((str = reader.readLine()) != null) {
            if (commentPattern.matcher(str).matches()) {
                continue;
            }
            if (sectionPattern.matcher(str).matches()) {
                str = str.trim();
                String name = str.substring(1, str.length()-1);
                currSection = new Section(name);
                sections.put(name, currSection);
            } else {
                Matcher m = valuePattern.matcher(str);
                if (m.matches()) {
                    String key = m.group(1).trim();
                    String value = m.group(2).trim();
                    currSection.put(key, value);
                } else {
                    if (justKeyPattern.matcher(str).matches()) {
                        String key = str.trim();
                        String value = null;
                        currSection.put(key, value);
                    } else {
                        throw new FormatException(str);
                    }
                }
            }
        }
    }


    @Override
     public String toString() {
        return getClass().getSimpleName() + ' ' + file.getAbsolutePath();
    }

     synchronized  void dump() {
        dump(System.out);
    }

     synchronized  void dump(PrintStream ps) {
        for(Section section : sections.values()) {
            ps.printf("[%s]\n", section.name);// NOI18N
            for (String key : section.getKeys()) {
                String value = section.get(key, null);
                ps.printf("%s=%s\n", key, value);// NOI18N
            }
        }
    }

    synchronized void save(){
        try {
            dump(new PrintStream(file));
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

}
