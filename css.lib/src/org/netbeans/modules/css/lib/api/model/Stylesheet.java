/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.css.lib.api.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.lib.nbparser.CssParser;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

/**
 * A domain object model representing CSS file backed up by 
 * instance of {@link org.netbeans.modules.parsing.api.Snapshot;}.
 *
 * @author mfukala@netbeans.org
 */
public final class Stylesheet {

    private static final Logger LOGGER = Logger.getLogger(Stylesheet.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    private final List<Rule> rules = new ArrayList<Rule>(10);
    /* package private for unit tests only */ final Collection<String> imported_files = new ArrayList<String>();
    private final List<Namespace> namespaces = new ArrayList<Namespace>();
    private Snapshot snapshot;
    private FileObject fileObject;

    public static Stylesheet create(CssParserResult result) {
        return new Stylesheet(result.getSnapshot(), result.getParseTree());
    }

    private Stylesheet(Snapshot snapshot, Node root) {
        this.snapshot = snapshot;
        this.fileObject = snapshot.getSource().getFileObject();
        //check for null which may happen if the source is severely broken
        //if it happens, the model contains just empty list of rules
        if (root != null) {
            updateModel(snapshot, root);
        }
    }

    /** @return List of {@link CssRule}s or null if the document hasn't been parsed yet. */
    public List<Rule> rules() {
        return rules;
    }

    /**
     * @return a list of declared namespaces
     */
    public List<Namespace> getNamespaces() {
        return namespaces;
    }

    private Collection<FileObject> getImportedFiles() {
        FileObject sourceFile = snapshot.getSource().getFileObject();
        if (sourceFile == null) {
            //this quite unlikely situation but possible. 
            //in such case we cannot properly resolve the links so 
            //just return empty list
            return Collections.emptyList();
        }

        FileObject baseFolder = sourceFile.getParent();
        Collection<FileObject> files = new ArrayList<FileObject>();
        for (String fileNamePath : imported_files) {
            FileObject file = baseFolder.getFileObject(fileNamePath);
            if (file != null) {
                files.add(file);
            }
        }
        return files;
    }

    public Collection<Stylesheet> getImportedFileModels() {
        Collection<Stylesheet> models = new HashSet<Stylesheet>();
        processModels(models, this);
        return models;
    }

    private void processModels(final Collection<Stylesheet> models, Stylesheet model) {
        for (FileObject importedFile : model.getImportedFiles()) {
            if (importedFile.isValid() && importedFile.getMIMEType().equals("text/x-css")) {
                try {
                    Stylesheet created = CssParser.parse(snapshot).getModel();
                    if (created != null) {
                        if (models.add(created)) {
                            processModels(models, created);
                        }
                    }
                } catch (ParseException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }
        }
    }

    /** Finds a rule on the given offset.
     * 
     * @return Instance of {@link CssRule} for the given offset or null if the 
     * offset falls into a space where there is no css rule.
     * @param offset within the model's document
     */
    public Rule ruleForOffset(int offset) {
        synchronized (rules) {
            if (rules != null) {
                for (Rule rule : rules()) {
                    if (rule.getRuleNameOffset() <= offset && rule.getRuleCloseBracketOffset() >= offset) {
                        return rule;
                    }
                }
            }
            return null;
        }
    }

    private synchronized void updateModel(final Snapshot snapshot, Node root) {
        synchronized (rules) {
            NodeVisitor styleRuleVisitor = new NodeVisitor() {

                @Override
                public boolean visit(Node node) {
                    switch (node.type()) {
                        case ruleSet:
                            rules.add(new Rule(snapshot, node));
                            break;
                        case imports:
                            Node importedFile = NodeUtil.query(node, "resourceIdentifier"); //NOI18N
                            if (importedFile != null) {
                                imported_files.add(WebUtils.unquotedValue(importedFile.image()));
                            }
                            break;
                        case namespace:
                            namespaces.add(new Namespace(snapshot, node));
                            break;

                    }
                    return false;
                }
            };
            styleRuleVisitor.visitChildren(root);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Stylesheet other = (Stylesheet) obj;
        if (this.fileObject != other.fileObject && (this.fileObject == null || !this.fileObject.equals(other.fileObject))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.fileObject != null ? this.fileObject.hashCode() : 0);
        return hash;
    }
}
