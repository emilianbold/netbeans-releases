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
package org.netbeans.modules.ruby.railsprojects.classpath;

import java.util.Collection;
import org.netbeans.modules.ruby.rubyproject.RequiredGems;
import org.netbeans.modules.ruby.rubyproject.GemRequirement;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.netbeans.modules.ruby.rubyproject.rake.RakeTask;
import org.netbeans.modules.ruby.rubyproject.spi.RakeTaskCustomizer;
import org.openide.util.NbBundle;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author Erno Mononen
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.ruby.rubyproject.spi.RakeTaskCustomizer.class)
public final class RailsGemsHelper implements RakeTaskCustomizer {

    private GemsLineConvertor convertor;

    private static final String REINDEX_GEMS_TOKEN = "--- reindex gems ---";
    private static final String RESET_GEMS_TOKEN = "--- reset gems ---";

    @Override
    public void customize(Project project, RakeTask task, RubyExecutionDescriptor taskDescriptor, boolean debug) {
        if (!"gems".equals(task.getTask())) {
            return;
        }
        RequiredGems requiredGems = null;
        for (RequiredGems each : project.getLookup().lookupAll(RequiredGems.class)) {
            if (!each.isForTests()) {
                requiredGems = each;
                break;
            }
        }
        this.convertor = new GemsLineConvertor(requiredGems, (RubyBaseProject) project);
        taskDescriptor.addOutConvertor(convertor);
        taskDescriptor.setOutProcessorFactory(new RakeGemsInputProcessorFactory(convertor.getGems(), requiredGems));
    }

    private static final class RakeGemsInputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {

        private final List<GemRequirement> gems;
        private final RequiredGems requiredGems;

        public RakeGemsInputProcessorFactory(List<GemRequirement> gems, RequiredGems requiredGems) {
            this.gems = gems;
            this.requiredGems = requiredGems;
        }

        @Override
        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return new RakeGemsInputProcessor(defaultProcessor);
        }

        private final class RakeGemsInputProcessor implements InputProcessor {

            private final InputProcessor delegate;

            public RakeGemsInputProcessor(InputProcessor delegate) {
                this.delegate = delegate;
            }

            @Override
            public void processInput(char[] chars) throws IOException {
                delegate.processInput(chars);
            }

            @Override
            public void reset() throws IOException {
                delegate.reset();
            }

            @Override
            public void close() throws IOException {
                finish();
                delegate.close();
            }

            private void finish() throws IOException {
                if (!gems.isEmpty()) {
                    delegate.processInput(("\n").toCharArray());
                    List<GemRequirement> old = requiredGems.getGemRequirements();
                    boolean addOr = false;
                    if (old == null
                            || !new HashSet<GemRequirement>(old).equals(new HashSet<GemRequirement>(gems))) {
                        delegate.processInput((REINDEX_GEMS_TOKEN + "\n").toCharArray());
                        addOr = true;
                    }
                    if (old != null) {
                        if (addOr) {
                            delegate.processInput((NbBundle.getMessage(GemsLineConvertor.class, "Or") + "\n").toCharArray());
                        } else {
                            delegate.processInput((NbBundle.getMessage(GemsLineConvertor.class, "GemsIndexed") + "\n").toCharArray());
                        }
                        delegate.processInput((RESET_GEMS_TOKEN + "\n").toCharArray());
                    }
                }//NOI18N
            }
        }
    }

    // package private for unit tests
    static final class GemsLineConvertor implements LineConvertor {

        private final List<GemRequirement> gems = new ArrayList<GemRequirement>();
        private final RequiredGems requiredGems;
        private final RubyBaseProject project;

        public GemsLineConvertor(RequiredGems requiredGems, RubyBaseProject project) {
            this.requiredGems = requiredGems;
            this.project = project;
        }

        List<GemRequirement> getGems() {
            return gems;
        }

        @Override
        public List<ConvertedLine> convert(String line) {
            GemRequirement rg = GemRequirement.parse(line);
            if (rg != null) {
                gems.add(rg);
            }
            if (line.contains(REINDEX_GEMS_TOKEN)) {
                return Collections.singletonList(
                        ConvertedLine.forText(NbBundle.getMessage(GemsLineConvertor.class, "ReindexGems"),
                        new OutputListener() {

                    @Override
                            public void outputLineSelected(OutputEvent ev) {
                            }

                    @Override
                            public void outputLineAction(OutputEvent ev) {
                                requiredGems.setRequiredGems(gems);
                                save();
                            }

                    @Override
                            public void outputLineCleared(OutputEvent ev) {
                            }
                        }));
            }
            if (line.contains(RESET_GEMS_TOKEN)) {
                return Collections.singletonList(
                        ConvertedLine.forText(NbBundle.getMessage(GemsLineConvertor.class, "ResetGems"),
                        new OutputListener() {

                    @Override
                            public void outputLineSelected(OutputEvent ev) {
                            }

                    @Override
                            public void outputLineAction(OutputEvent ev) {
                                requiredGems.setRequiredGems((String) null);
                                save();
                            }

                    @Override
                            public void outputLineCleared(OutputEvent ev) {
                            }
                        }));
            }
            return Collections.singletonList(ConvertedLine.forText(line, null));
        }

        private void save() {
            RailsProjectProperties properties = new RailsProjectProperties(project, project.getUpdateHelper(),
                    project.evaluator(), project.getReferenceHelper(), project.getGenFilesHelper());
            properties.setGemRequirements(requiredGems.getGemRequirements());
            properties.save();
        }
    }

}
