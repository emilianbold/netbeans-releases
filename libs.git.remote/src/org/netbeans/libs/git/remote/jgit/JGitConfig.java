/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.libs.git.remote.jgit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * @author alsimon
 */
public class JGitConfig {
    public static final String CONFIG_CORE_SECTION = "core";
    public static final String CONFIG_KEY_FILEMODE = "filemode";
    public static final String CONFIG_KEY_AUTOCRLF = "autocrlf";
    public static final String CONFIG_BRANCH_SECTION = "branch";
    public static final String CONFIG_KEY_AUTOSETUPMERGE = "autosetupmerge";
    public static final String CONFIG_KEY_REMOTE = "remote";
    public static final String CONFIG_KEY_MERGE = "merge";
    
    private final TreeMap<SectionKey, TreeMap<String,String>> map = new TreeMap<>();
    private final VCSFileProxy location;
    
    public JGitConfig(VCSFileProxy location) {
        this.location = location;
    }
    
    public void load() {
    }

    public void save() {
    }

    public void setString(String section, String subsection, String key, String value) {
        SectionKey storageKey = new SectionKey(section, subsection);
        TreeMap<String, String> storage = map.get(storageKey);
        if (storage == null) {
            storage = new TreeMap<>();
            map.put(storageKey, storage);
        }
        storage.put(key, value);
    }

    public String getString(String section, String subsection, String key) {
        SectionKey storageKey = new SectionKey(section, subsection);
        TreeMap<String, String> storage = map.get(storageKey);
        if (storage == null) {
            return null;
        }
        return storage.get(key);
    }

    public void setBoolean(String section, String subsection, String key, boolean b) {
        SectionKey storageKey = new SectionKey(section, subsection);
        TreeMap<String, String> storage = map.get(storageKey);
        if (storage == null) {
            storage = new TreeMap<>();
            map.put(storageKey, storage);
        }
        storage.put(key, Boolean.toString(b));
    }

    public boolean getBoolean(String section, String subsection, String key, boolean b) {
        SectionKey storageKey = new SectionKey(section, subsection);
        TreeMap<String, String> storage = map.get(storageKey);
        if (storage == null) {
            return false;
        }
        String val = storage.get(key);
        return Boolean.toString(true).equals(val);
    }


    public Collection<String> getSubsections(String section) {
        List<String> res = new ArrayList<>();
        for(Map.Entry<SectionKey, TreeMap<String,String>> entry : map.entrySet()) {
            SectionKey key = entry.getKey();
            if (section.equals(key.section)) {
                if (key.subSection != null) {
                    res.add(key.subSection);
                }
            }
        }
        return res;
    }


    public Collection<String> getSections() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    private static final class SectionKey implements Comparable<SectionKey>{
        private final String section;
        private final String subSection;
        
        private SectionKey(String section, String subSection) {
            this.section = section;
            this.subSection = subSection;
        }

        @Override
        public int compareTo(SectionKey o) {
            int res = section.compareTo(o.section);
            if (res == 0) {
                if (subSection == null && o.subSection == null) {
                    return 0;
                } else if (subSection == null) {
                    return -1;
                } else if (o.subSection == null) {
                    return 1;
                } else {
                    return subSection.compareTo(o.subSection);
                }
            }
            return res;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SectionKey other = (SectionKey) obj;
            if (!Objects.equals(this.section, other.section)) {
                return false;
            }
            if (!Objects.equals(this.subSection, other.subSection)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.section);
            hash = 83 * hash + Objects.hashCode(this.subSection);
            return hash;
        }

    }
    
}
