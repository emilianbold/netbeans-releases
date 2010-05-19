/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.maven;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputProcessorFactory;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Browses a Jetty server that has been started.
 */
@ServiceProvider(service=OutputProcessorFactory.class)
public class JettyOutputProcessorFactory implements OutputProcessorFactory {

    public Set<OutputProcessor> createProcessorsSet(Project project) {
        return Collections.<OutputProcessor>singleton(new JettyOutputProcessor());
    }

    private static final class JettyOutputProcessor implements OutputProcessor {

        public String[] getRegisteredOutputSequences() {
            return new String[] {"mojo-execute#jetty:run", "mojo-execute#hpi:run"}; // NOI18N
        }

        private static final Pattern LINE = Pattern.compile(".*Started SelectChannelConnector @ 0[.]0[.]0[.]0:(\\d+)"); // NOI18N

        public void processLine(String line, OutputVisitor visitor) {
            Matcher m = LINE.matcher(line);
            if (m.matches()) {
                try {
                    URLDisplayer.getDefault().showURL(new URL("http://localhost:" + m.group(1) + "/"));
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        public void sequenceStart(String sequenceId, OutputVisitor visitor) {}

        public void sequenceEnd(String sequenceId, OutputVisitor visitor) {}

        public void sequenceFail(String sequenceId, OutputVisitor visitor) {}

    }

}
