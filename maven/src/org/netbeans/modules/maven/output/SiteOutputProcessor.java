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

package org.netbeans.modules.maven.output;

import java.io.File;
import java.net.URL;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.output.OutputProcessor;
import org.netbeans.modules.maven.api.output.OutputVisitor;
import org.netbeans.api.project.Project;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 *
 * @author mkleint
 */
public class SiteOutputProcessor implements OutputProcessor {
    
    private static final String[] SITEGOALS = new String[] {
        "mojo-execute#site:site" //NOI18N
    };
    private Project project;
    
    /** Creates a new instance of SiteOutputProcessor */
    public SiteOutputProcessor(Project prj) {
        this.project = prj;
    }
    
    public String[] getRegisteredOutputSequences() {
        return SITEGOALS;
    }
    
    public void processLine(String line, OutputVisitor visitor) {
    }
    
    public void sequenceStart(String sequenceId, OutputVisitor visitor) {
    }
    
    public void sequenceEnd(String sequenceId, OutputVisitor visitor) {
        visitor.setLine("     View Generated Project Site"); //NOI18N shows up in maven output.
        visitor.setOutputListener(new Listener(project), false);
    }
    
    public void sequenceFail(String sequenceId, OutputVisitor visitor) {
    }
    
    private static class Listener implements OutputListener {
        private File root;
        private Listener(Project prj) {
            File fl = FileUtil.toFile(prj.getProjectDirectory());
            root = new File(fl, "target" + File.separator + "site"); //NOI18N
        }
        public void outputLineSelected(OutputEvent arg0) {
            
        }
        
        public void outputLineAction(OutputEvent arg0) {
            File site = FileUtil.normalizeFile(root);
            FileObject fo = FileUtilities.toFileObject(site);
            if (fo != null) {
                FileObject index = fo.getFileObject("index.html"); //NOI18N
                if (index != null) {
                    URL link = URLMapper.findURL(index, URLMapper.EXTERNAL);
                    HtmlBrowser.URLDisplayer.getDefault().showURL(link);
                }
            }
        }
        
        public void outputLineCleared(OutputEvent arg0) {
        }
    }
}
