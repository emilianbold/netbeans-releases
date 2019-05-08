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
package org.netbeans.modules.cnd.discovery.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.util.ChangeSupport;

/**
 *
 */
public class SourcesVisibilityQuery implements ChangeListener {

    private final ChangeSupport cs = new ChangeSupport(this);
    private static final SourcesVisibilityQuery INSTANCE = new SourcesVisibilityQuery();
    private Pattern acceptedFilesPattern = null;
    private static final String DEFAULT_IGNORE_BYNARY_PATTERN = ".*\\.(il|o|a|dll|dylib|lib|lo|la|Po|Plo|so(\\.[0-9]*)*)$"; // NOI18N
    private final Pattern ignoredFilesPattern = Pattern.compile(DEFAULT_IGNORE_BYNARY_PATTERN);
    
    /** Default instance for lookup. */
    private SourcesVisibilityQuery() {
        MIMEExtensions.get(MIMENames.C_MIME_TYPE).addChangeListener(this);
        MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).addChangeListener(this);
        MIMEExtensions.get(MIMENames.FORTRAN_MIME_TYPE).addChangeListener(this);
    }

    public static SourcesVisibilityQuery getDefault() {
        return INSTANCE;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        acceptedFilesPattern = null; // This will reset filter
        cs.fireChange();
    }

    boolean isVisible(final String fileName) {
        Pattern pattern = getAcceptedFilesPattern();
        return (pattern != null) ? pattern.matcher(fileName).find() : true;
    }

    public boolean isIgnored(String fileName) {
        return ignoredFilesPattern.matcher(fileName).find();
    }

    /**
     * Add a listener to changes.
     * @param l a listener to add
     */
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    private List<Collection<String>> getAcceptedFilesExtensions() {
        List<Collection<String>> suffixes = new ArrayList<>();
        suffixes.add(MIMEExtensions.get(MIMENames.C_MIME_TYPE).getValues());
        suffixes.add(MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).getValues());
        suffixes.add(MIMEExtensions.get(MIMENames.FORTRAN_MIME_TYPE).getValues());
        return suffixes;
    }

    private Pattern getAcceptedFilesPattern() {
        if (acceptedFilesPattern == null) {
            List<Collection<String>> acceptedFileExtensions = getAcceptedFilesExtensions();
            StringBuilder pat = new StringBuilder();
            for (Collection<String> col : acceptedFileExtensions) {
                for (String s : col) {
                    if (pat.length() > 0) {
                        pat.append('|');
                    }
                    if (s.indexOf('+') >= 0) {
                        s = s.replace("+", "\\+"); // NOI18N
                    }
                    pat.append(s);
                }
                String ignoredFiles = ".*\\.(" + pat.toString() + ")$"; //NOI18N;
                acceptedFilesPattern = Pattern.compile(ignoredFiles);
            }
        }
        return acceptedFilesPattern;
    }
}
