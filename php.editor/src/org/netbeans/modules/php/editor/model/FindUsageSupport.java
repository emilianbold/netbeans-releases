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
package org.netbeans.modules.php.editor.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.model.impl.ModelVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * @author Radek Matous
 */
public final class FindUsageSupport {
    private Set<FileObject> files;
    private ModelElement element;
    private ElementQuery.Index index;

    public static FindUsageSupport getInstance(ElementQuery.Index index, ModelElement element) {
        return new FindUsageSupport(index, element);
    }

    private FindUsageSupport(ElementQuery.Index index, ModelElement element) {
        this.element = element;
        this.files = new LinkedHashSet<FileObject>();
        this.files.add(element.getFileObject());
        String name = element.getName();
        if (name.startsWith("$")) {//NOI18N
            name = name.substring(1);
        }
        this.files.addAll(index.getLocationsForIdentifiers(name));
        this.index = index;
    }

    public Collection<TypeElement> subclasses() {
        if (element instanceof TypeElement) {
            return index.getInheritedByTypes((TypeElement) element);
        }
        return Collections.emptySet();
    }

    public Collection<TypeElement> directSubclasses() {
        if (element instanceof TypeElement) {
            return index.getDirectInheritedByTypes((TypeElement) element);
        }
        return Collections.emptySet();
    }

    @CheckForNull
    public Collection<Occurence> occurences(FileObject fileObject) {
        final Set<Occurence> retval = new TreeSet<Occurence>(new Comparator<Occurence>(){
            public int compare(Occurence o1, Occurence o2) {
                return o1.getOccurenceRange().compareTo(o2.getOccurenceRange());
            }
        });
        try {
            ParserManager.parse(Collections.singleton(Source.create(fileObject)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ParserResult parameter = (ParserResult) resultIterator.getParserResult();
                    Model model = ModelFactory.getModel(parameter);
                    ModelVisitor modelVisitor = model.getModelVisitor();
                    retval.addAll(modelVisitor.getOccurence(element));
                }
            });
        } catch (org.netbeans.modules.parsing.spi.ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return retval;
    }

    /**
     * @return the files
     */
    public Set<FileObject> inFiles() {
        return files;
    }

    /**
     * @return the element
     */
    public ModelElement elementToFind() {
        return element;
    }

}
