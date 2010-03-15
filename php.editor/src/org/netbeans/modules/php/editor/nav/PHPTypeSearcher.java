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

package org.netbeans.modules.php.editor.nav;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Icon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.php.editor.PHPCompletionItem;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind.Prefix;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Radek Matous, Jan Lahoda
 */
public class PHPTypeSearcher implements IndexSearcher {
    //TODO: no supported: came cases, regular expressions in queries (needs improve PHPIndex methods)
    public Set<? extends Descriptor> getSymbols(Project project, String textForQuery, Kind kind, Helper helper) {
        // XXX: use PHP specific path ids
        EnumSet<Kind> regexpKinds = EnumSet.of(Kind.CAMEL_CASE, Kind.CASE_INSENSITIVE_CAMEL_CASE,  Kind.CASE_INSENSITIVE_REGEXP);
        // PHP isn't Java so we may need to overrule the chosen kind
        // in case the query looks like it may be a camel-case/wildcard pattern
        // fix for #167687
        if ((kind == Kind.CASE_INSENSITIVE_PREFIX || kind == Kind.PREFIX) && isCamelCasePattern(textForQuery)) {
            kind = Kind.CAMEL_CASE;
        }
        final Collection<FileObject> findRoots = QuerySupport.findRoots(project,
                Collections.singleton(PhpSourcePath.SOURCE_CP),
                Collections.singleton(PhpSourcePath.BOOT_CP),
                Collections.<String>emptySet());
        final Index index = ElementQueryFactory.getIndexQuery(QuerySupportFactory.get(findRoots));


        Set<PHPTypeDescriptor> result = new HashSet<PHPTypeDescriptor>();
        if (index != null && textForQuery.trim().length() > 0) {
            final boolean isVariable = textForQuery.startsWith("$");//NOI18N
            String query = prepareIdxQuery(textForQuery, regexpKinds, kind);
            final Kind useKind = kind.equals(Kind.EXACT) ? Kind.EXACT : Kind.CASE_INSENSITIVE_PREFIX;//NOI18N
            if (!kind.equals(Kind.EXACT)) {//NOI18N
                query = query.toLowerCase();
            }
            final NameKind prefix = NameKind.create(query, useKind);
            if (!isVariable) {
                for (PhpElement indexedElement : index.getTopLevelElements(prefix)) {
                    result.add(new PHPTypeDescriptor(indexedElement, helper));
                }
                for (PhpElement indexedElement : index.getMethods(prefix)) {
                    result.add(new PHPTypeDescriptor(indexedElement, helper));
                }
            }
            for (PhpElement indexedElement : index.getTopLevelVariables(prefix)) {
                result.add(new PHPTypeDescriptor(indexedElement, helper));
            }
            for (PhpElement indexedElement : index.getFields(prefix)) {
                result.add(new PHPTypeDescriptor(indexedElement, helper));
            }
            for (PhpElement indexedElement : index.getTypeConstants(prefix)) {
                result.add(new PHPTypeDescriptor(indexedElement, helper));
            }
        }
        if (regexpKinds.contains(kind)) {
            //handles wildcards and camelCases
            Set<PHPTypeDescriptor> originalResult = result;
            result = new HashSet<PHPTypeDescriptor>();
            Pattern pattern = queryToPattern(textForQuery);
            for (PHPTypeDescriptor typeDescriptor : originalResult) {
                String typeName = typeDescriptor.getElement().getName();
                if (pattern.matcher(typeName).matches()) {
                    result.add(typeDescriptor);
                }
            }
        }

        return result;
    }

    public Set<? extends Descriptor> getTypes(Project project, String textForQuery, Kind kind, Helper helper) {
        // XXX: use PHP specific path ids
        EnumSet<Kind> regexpKinds = EnumSet.of(Kind.CAMEL_CASE, Kind.CASE_INSENSITIVE_CAMEL_CASE,  Kind.CASE_INSENSITIVE_REGEXP);
        // PHP isn't Java so we may need to overrule the chosen kind
        // in case the query looks like it may be a camel-case/wildcard pattern
        // fix for #167687
        if ((kind == Kind.CASE_INSENSITIVE_PREFIX || kind == Kind.PREFIX) && isCamelCasePattern(textForQuery)) {
            kind = Kind.CAMEL_CASE;
        }

        final Collection<FileObject> findRoots = QuerySupport.findRoots(project,
                Collections.singleton(PhpSourcePath.SOURCE_CP),
                Collections.singleton(PhpSourcePath.BOOT_CP),
                Collections.<String>emptySet());
        final Index index = ElementQueryFactory.getIndexQuery(QuerySupportFactory.get(findRoots));

        Set<PHPTypeDescriptor> result = new HashSet<PHPTypeDescriptor>();
        QualifiedName queryName = QualifiedName.create(textForQuery);
        QualifiedNameKind qnk = queryName.getKind();
        if (index != null) {
            String query = qnk.isUnqualified() ? prepareIdxQuery(textForQuery, regexpKinds, kind).toLowerCase() : textForQuery;
            Prefix prefix = NameKind.prefix(QualifiedName.create(query));
            for (PhpElement indexedElement : index.getClasses(prefix)) {
                result.add(new PHPTypeDescriptor(indexedElement, helper));
            }
            for (PhpElement indexedElement : index.getInterfaces(prefix)) {
                result.add(new PHPTypeDescriptor(indexedElement, helper));
            }
        }
        if (qnk.isUnqualified() && regexpKinds.contains(kind)) {
            //handles wildcards and camelCases
            Set<PHPTypeDescriptor> originalResult = result;
            result = new HashSet<PHPTypeDescriptor>();
            Pattern pattern = queryToPattern(textForQuery);
            for (PHPTypeDescriptor typeDescriptor : originalResult) {
                String typeName = typeDescriptor.getElement().getName();
                if (pattern.matcher(typeName).matches()) {
                    result.add(typeDescriptor);
                }
            }
        }
        return result;
    }

    private static class PHPTypeDescriptor extends Descriptor {
        private final PhpElement element;
        private final PhpElement enclosingClass;
        private String projectName;
        private Icon projectIcon;
        private final Helper helper;

        public PHPTypeDescriptor(PhpElement element, Helper helper) {
            this(element, null, helper);
        }

        public PHPTypeDescriptor(PhpElement element, PhpElement enclosingClass, Helper helper) {
            this.element = element;
            this.enclosingClass = enclosingClass;
            this.helper = helper;
        }

        public Icon getIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            if (element instanceof InterfaceElement) {
                return PHPCompletionItem.getInterfaceIcon();
            }
            return helper.getIcon(element);
        }

        public String getTypeName() {
            return element.getName();
        }

        public String getProjectName() {
            if (projectName == null) {
                initProjectInfo();
            }
            return projectName;
        }

        private void initProjectInfo() {
            FileObject fo = element.getFileObject();
            if (fo != null) {
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null) {
                    ProjectInformation pi = ProjectUtils.getInformation(p);
                    projectName = pi.getDisplayName();
                    projectIcon = pi.getIcon();
                }
            }
            
            if (projectName == null) {
                projectName = "";
            }
        }

        public Icon getProjectIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            return projectIcon;
        }

        public FileObject getFileObject() {
            return element.getFileObject();
        }

        public void open() {
            FileObject fileObject = element.getFileObject();
            if (fileObject != null) {
                GsfUtilities.open(fileObject, element.getOffset(), element.getName());
            } else {
                Logger logger = Logger.getLogger(PHPTypeSearcher.class.getName());
                logger.log(Level.INFO,String.format("%s: cannot find %s", //NOI18N
                        PHPTypeSearcher.class.getName(), element.getFilenameUrl()));
            }
        }

        public String getContextName() {
            StringBuilder sb = new StringBuilder();
            boolean s = false;
            if (element instanceof FullyQualifiedElement) {
                FullyQualifiedElement fqnElement = (FullyQualifiedElement) element;
                if (!fqnElement.getNamespaceName().isDefaultNamespace()) {
                    if (element instanceof TypeElement) {
                        sb.append(fqnElement.getFullyQualifiedName());
                    } else {
                        sb.append(fqnElement.getNamespaceName());
                    }
                    s = true;
                }
            } else {
                if (enclosingClass != null) {
                    if ((enclosingClass instanceof FullyQualifiedElement) &&
                            (!((FullyQualifiedElement)enclosingClass).getNamespaceName().isDefaultNamespace())) {
                        sb.append(((FullyQualifiedElement) enclosingClass).getFullyQualifiedName().toString());
                    } else {
                        sb.append(enclosingClass.getName());
                    }
                    s = true;
                }
            }
            FileObject file = getFileObject();
            if (file != null) {
                if (s) {
                    sb.append(" in ");
                }
                sb.append(FileUtil.getFileDisplayName(file));
            }
            if (sb.length() > 0) {
                return sb.toString();
            }
            return null;
        }

        public ElementHandle getElement() {
            return element;
        }

        public int getOffset() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getSimpleName() {
            return element.getName();
        }

        public String getOuterName() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PHPTypeDescriptor other = (PHPTypeDescriptor) obj;
            if (this.element != other.element && (this.element == null || !this.element.equals(other.element))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + (this.element != null ? this.element.hashCode() : 0);
            return hash;
        }
    }
    
    private static Pattern queryToPattern(String query) {
        StringBuilder sb = new StringBuilder();
        char[] chars = query.toCharArray();
        boolean incamel = false;
        if (!query.startsWith("$")) {
            sb.append("[$]*");//NOI18N
        }
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '?') {//NOI18N
                sb.append('.');//NOI18N
            } else if (chars[i] == '*') {
                sb.append(".*");//NOI18N
            } else if (Character.isUpperCase(chars[i])) {
                if (incamel) {
                    sb.append("[a-z0-9_]*");//NOI18N
                }
                sb.append(chars[i]);
                incamel = true;
            } else if (i == 0 && chars[i] == '$') {
                sb.append('\\').append(chars[i]);//NOI18N
            }else {
                sb.append(chars[i]);
            }
        }
        sb.append(".*");//NOI18N
        String patternString = sb.toString();
        patternString = patternString.replaceAll(Pattern.quote(".."), ".");//NOI18N
        return Pattern.compile(patternString);
    }

    private String prepareIdxQuery(String textForQuery, EnumSet<Kind> regexpKinds, Kind kind) {
        String query = textForQuery.toLowerCase();
        if (regexpKinds.contains(kind)) {
            final char charAt = textForQuery.charAt(0);
            final int length = textForQuery.length();
            if (Character.isLetter(charAt) && length > 0) {
                query = query.substring(0, 1);//NOI18N
            } else if (charAt == '$' && length > 1) {
                query = query.substring(0, 1);//NOI18N
            }else {
                query = "";//NOI18N
            }
        }
        return query;
    }
    
    private static boolean isCamelCasePattern(String query) {
        char[] chars = query.toCharArray();
        for (char c : chars) {
            if (c == '*' || c == '?' || Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }
}
