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
package org.netbeans.modules.css.visual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 * Model for stylesheets related to a file.
 *
 * TODO: I believe I cannot "get" a cached instance of CssIndex as if for
 * example classpath of the project changes, we need to get a new one.
 *
 * @author marekfukala
 */
public class DocumentViewModel implements ChangeListener {

    private FileObject file;
    private Project project;
    private CssIndex index;
    private boolean indexModified;
    
    private ChangeSupport changeSupport;
    
    /**
     * Map of stylesheet -> list of rules
     */
    private Map<FileObject, List<RuleHandle>> relatedStylesheets;

    public DocumentViewModel(FileObject file) {
        this.file = file;
        this.project = FileOwnerQuery.getOwner(file);
        if (project != null) {
            try {
                this.index = CssIndex.get(project);
                this.changeSupport = new ChangeSupport(this);
                index.addChangeListener(this);
                update();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            //no project, no related stylesheets
            relatedStylesheets = Collections.emptyMap();
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    //TODO call from a reasonable place so the instance of the model
    //can be reasonable freed.
    void dispose() {
        if (index != null) {
            index.removeChangeListener(this);
        }
        file = null;
        project = null;
        index = null;
    }

    /*
     * ChangeEvents from CssIndex.
     */
    @Override
    public void stateChanged(ChangeEvent ce) {
        //the project has been reindexed, update the map.
        indexModified = true;
        changeSupport.fireChange();
    }

    /**
     * Gets a map of stylesheet -> list of rules
     */
    public synchronized Map<FileObject, List<RuleHandle>> getFileToRulesMap() {
        if (indexModified) {
            update();
        }
        return relatedStylesheets;
    }

    private void update() {
        relatedStylesheets = new HashMap<FileObject, List<RuleHandle>>();

        DependenciesGraph dependencies = index.getDependencies(file);
        Collection<FileObject> allRelatedFiles = dependencies.getAllRelatedFiles();

        for (final FileObject related : allRelatedFiles) {
            if (isStyleSheet(related)) {
                Source source = Source.create(related);
                try {
                    ParserManager.parse(Collections.singleton(source), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/css"); //NOI18N
                            if (ri != null) {
                                final CssParserResult result = (CssParserResult) ri.getParserResult();
                                final Model model = Model.getModel(result);

                                final List<RuleHandle> rules = new ArrayList<RuleHandle>();
                                final ModelVisitor visitor = new ModelVisitor.Adapter() {
                                    @Override
                                    public void visitRule(Rule rule) {
                                        String image = model.getElementSource(rule.getSelectorsGroup()).toString();
                                        int offset = result.getSnapshot().getOriginalOffset(rule.getStartOffset());
                                        RuleHandle handle = new RuleHandle(related, rule, offset, image );
                                        rules.add(handle);
                                    }
                                };
                                model.runReadTask(new Model.ModelTask() {
                                    @Override
                                    public void run(StyleSheet styleSheet) {
                                        styleSheet.accept(visitor);
                                    }
                                });
                                relatedStylesheets.put(related, rules);
                            }
                        }
                    });
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private boolean isStyleSheet(FileObject file) {
        return "text/css".equals(file.getMIMEType()); //NOI18N
    }
}
