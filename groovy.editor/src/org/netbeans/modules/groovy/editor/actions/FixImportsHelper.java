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
package org.netbeans.modules.groovy.editor.actions;

import java.util.MissingResourceException;
import javax.swing.text.BadLocationException;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import java.util.List;
import org.netbeans.api.java.source.ClasspathInfo;
import java.util.ArrayList;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import java.util.Set;
import java.util.EnumSet;
import javax.lang.model.element.TypeElement;
import javax.swing.Icon;
import org.netbeans.editor.BaseDocument;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.groovy.editor.api.NbUtilities;
import org.netbeans.modules.gsf.api.EditList;

/**
 *
 * @author schmidtm
 */
public class FixImportsHelper {

    private final Logger LOG = Logger.getLogger(FixImportsHelper.class.getName());

    public class ImportCandidate {

        String name;
        String fqnName;
        Icon icon;
        int importantsLevel;

        public ImportCandidate(String name, String fqnName, Icon icon, int importantsLevel) {
            this.name = name;
            this.fqnName = fqnName;
            this.icon = icon;
            this.importantsLevel = importantsLevel;
        }

        public String getName() {
            return name;
        }

        public void setName(String Name) {
            this.name = Name;
        }

        public String getFqnName() {
            return fqnName;
        }

        public void setFqnName(String fqnName) {
            this.fqnName = fqnName;
        }

        public Icon getIcon() {
            return icon;
        }

        public void setIcon(Icon icon) {
            this.icon = icon;
        }

        public int getImportantsLevel() {
            return importantsLevel;
        }

        public void setImportantsLevel(int importantsLevel) {
            this.importantsLevel = importantsLevel;
        }
    }

    public List<ImportCandidate> getImportCandidate(FileObject fo, String missingClass) {
        LOG.log(Level.FINEST, "Looking for class: " + missingClass);

        List<ImportCandidate> result = new ArrayList<ImportCandidate>();
        
        ClasspathInfo pathInfo = NbUtilities.getClasspathInfoForFileObject(fo);
        
        if(pathInfo == null){
            LOG.log(Level.FINEST, "Problem getting ClasspathInfo");
            return result;
        }

        Set<org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>> typeNames;

        typeNames = pathInfo.getClassIndex().getDeclaredTypes(missingClass, NameKind.SIMPLE_NAME,
                EnumSet.allOf(ClassIndex.SearchScope.class));

        for (org.netbeans.api.java.source.ElementHandle<TypeElement> typeName : typeNames) {
            javax.lang.model.element.ElementKind ek = typeName.getKind();

            if (ek == javax.lang.model.element.ElementKind.CLASS ||
                    ek == javax.lang.model.element.ElementKind.INTERFACE) {
                String fqnName = typeName.getQualifiedName();
                LOG.log(Level.FINEST, "Found     : " + fqnName);

                Icon icon = ElementIcons.getElementIcon(ek, null);
                int level = getImportanceLevel(fqnName);

                ImportCandidate candidate = new ImportCandidate(missingClass, fqnName, icon, level);
                result.add(candidate);
            }

        }

        return result;

    }

    public static int getImportanceLevel(String fqn) {
        int weight = 50;
        if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) // NOI18N
        {
            weight -= 10;
        } else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) // NOI18N
        {
            weight += 10;
        } else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) // NOI18N
        {
            weight += 20;
        } else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) // NOI18N
        {
            weight += 30;
        }
        return weight;
    }

    public static String getMissingClassName(String errorMessage) {
        String ERR_PREFIX = "unable to resolve class "; // NOI18N
        String missingClass = null;

        if (errorMessage.startsWith(ERR_PREFIX)) {

            missingClass = errorMessage.substring(ERR_PREFIX.length());
            int idx = missingClass.indexOf(" ");

            if (idx != -1) {
                return missingClass.substring(0, idx);
            }
        }

        return missingClass;
    }

    int getImportPosition(BaseDocument doc) {
        TokenSequence<?> ts = LexUtilities.getGroovyTokenSequence(doc, 1);

        // LOG.setLevel(Level.FINEST);

        int importEnd = -1;
        int packageOffset = -1;

        while (ts.moveNext()) {
            Token t = ts.token();
            int offset = ts.offset();

            if (t.id() == GroovyTokenId.LITERAL_import) {
                LOG.log(Level.FINEST, "GroovyTokenId.LITERAL_import found");
                importEnd = offset;
            } else if (t.id() == GroovyTokenId.LITERAL_package) {
                LOG.log(Level.FINEST, "GroovyTokenId.LITERAL_package found");
                packageOffset = offset;
            }
        }

        int useOffset = 0;

        // sanity check: package *before* import
        if (importEnd != -1 && packageOffset > importEnd) {
            LOG.log(Level.FINEST, "packageOffset > importEnd");
            return -1;
        }

        // nothing set:
        if (importEnd == -1 && packageOffset == -1) {
            // place imports in the first line
            LOG.log(Level.FINEST, "importEnd == -1 && packageOffset == -1");
            return 0;

        } // only package set:
        else if (importEnd == -1 && packageOffset != -1) {
            // place imports behind package statement
            LOG.log(Level.FINEST, "importEnd == -1 && packageOffset != -1");
            useOffset = packageOffset;
        } // only imports set:
        else if (importEnd != -1 && packageOffset == -1) {
            // place imports after the last import statement
            LOG.log(Level.FINEST, "importEnd != -1 && packageOffset == -1");
            useOffset = importEnd;
        } // both package & import set:
        else if (importEnd != -1 && packageOffset != -1) {
            // place imports right after the last import statement
            LOG.log(Level.FINEST, "importEnd != -1 && packageOffset != -1");
            useOffset = importEnd;

        }

        int lineOffset = 0;

        try {
            lineOffset = Utilities.getLineOffset(doc, useOffset);
        } catch (BadLocationException ex) {
            LOG.log(Level.FINEST, "BadLocationException for offset : {0}", useOffset);
            LOG.log(Level.FINEST, "BadLocationException : {0}", ex.getMessage());
            return -1;
        }

        return Utilities.getRowStartFromLineOffset(doc, lineOffset + 1);

    }

    public void doImport(FileObject fo, String fqnName) throws MissingResourceException {
        int firstFreePosition = 0;
        BaseDocument baseDoc = AstUtilities.getBaseDocument(fo, true);

        firstFreePosition = getImportPosition(baseDoc);

        if (firstFreePosition != -1) {
            if (baseDoc == null) {
                return;
            }

            EditList edits = new EditList(baseDoc);
            LOG.log(Level.FINEST, "Importing here: " + firstFreePosition);

            edits.replace(firstFreePosition, 0, "import " + fqnName + "\n", false, 0);
            edits.apply();
        }
    }
}
