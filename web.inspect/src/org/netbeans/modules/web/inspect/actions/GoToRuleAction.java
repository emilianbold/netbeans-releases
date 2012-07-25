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
package org.netbeans.modules.web.inspect.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.modules.css.editor.api.CssCslParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.SelectorsGroup;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.inspect.CSSUtils;
import org.netbeans.modules.web.inspect.PageModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Go to rule action.
 *
 * @author Jan Stola
 */
public class GoToRuleAction extends AbstractAction {
    /** Rule to jump to. */
    private PageModel.RuleInfo rule;

    /**
     * Creates a new {@code GoToRuleAction}.
     * 
     * @param rule rule to jump to.
     */
    public GoToRuleAction(PageModel.RuleInfo rule) {
        this.rule = rule;
        String url = rule.getSourceURL();
        setEnabled(url != null && url.startsWith("file://")); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String urlTxt = rule.getSourceURL();
        try {
            URI uri = new URI(urlTxt);
            File file = new File(uri);
            file = FileUtil.normalizeFile(file);
            FileObject fob = FileUtil.toFileObject(file);
            Source source = Source.create(fob);
            ParserManager.parse(Collections.singleton(source), new GoToRuleTask(rule, fob));
        } catch (URISyntaxException ex) {
            Logger.getLogger(GoToRuleAction.class.getName()).log(Level.INFO, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(GoToRuleAction.class.getName()).log(Level.INFO, null, ex);
        }
    }

    /**
     * Task that jumps on the declaration of the given rule
     * in the specified file.
     */
    static class GoToRuleTask extends UserTask {
        /** Rule to jump to. */
        private PageModel.RuleInfo rule;
        /** File to jump into. */
        private FileObject fob;

        /**
         * Creates a new {@code GoToRuleTask}.
         * 
         * @param rule rule to jump to.
         * @param fob file to jump into.
         */
        GoToRuleTask(PageModel.RuleInfo rule, FileObject fob) {
            this.rule = rule;
            this.fob = fob;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            final String selector = normalizeSelector(rule.getSelector());
            CssCslParserResult result = (CssCslParserResult)resultIterator.getParserResult();
            final Model sourceModel = result.getModel();
            final AtomicBoolean visitorCancelled = new AtomicBoolean();
            sourceModel.runReadTask(new Model.ModelTask() {
                
                @Override
                public void run(StyleSheet styleSheet) {
                    styleSheet.accept(new ModelVisitor.Adapter() {

                        @Override
                        public void visitRule(Rule rule) {
                            if(visitorCancelled.get()) {
                                return ;
                            }
                            SelectorsGroup selectorGroup = rule.getSelectorsGroup();
                            CharSequence image = sourceModel.getElementSource(selectorGroup);
                            String selectorInFile = normalizeSelector(image.toString());
                            if (selector.equals(selectorInFile)) {
                                //found 
                                visitorCancelled.set(true);
                                final int offset = rule.getStartOffset();
                                EventQueue.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        CSSUtils.open(fob, offset);
                                    }
                                });
                            }
                        }
                        
                    }); //model visitor
                }
                
            }); //model task
            
        }

        /**
         * Returns an unspecified "normalized" version of the selector suitable
         * for {@code String} comparison with other normalized selectors.
         * 
         * @param selector selector to normalize.
         * @return "normalized" version of the selector.
         */
        private String normalizeSelector(String selector) {
            // Hack that simplifies the following cycle: adding a dummy
            // character that ensures that the last group is ended.
            // This character is removed at the end of this method.
            selector += 'A';
            String whitespaceChars = " \t\n\r\f"; // NOI18N
            String specialChars = ".>+~#:*()[]|,"; // NOI18N
            StringBuilder main = new StringBuilder();
            StringBuilder group = null;
            for (int i=0; i<selector.length(); i++) {
                char c = selector.charAt(i);
                boolean whitespace = (whitespaceChars.indexOf(c) != -1);
                boolean special = (specialChars.indexOf(c) != -1);
                if (whitespace || special) {
                    if (group == null) {
                        group = new StringBuilder();
                    }
                    if (special) {
                        group.append(c);
                    }
                } else {
                    if (group != null) {
                        if (group.length() == 0) {
                            // whitespace only group => insert single space instead
                            main.append(' ');
                        } else {
                            // group with special chars
                            main.append(group);
                        }
                        group = null;
                    }
                    main.append(c);
                }
            }
            // Removing the dummy character added at the beginning of the method
            return main.substring(0, main.length()-1).trim();
        }
        
    }

}
