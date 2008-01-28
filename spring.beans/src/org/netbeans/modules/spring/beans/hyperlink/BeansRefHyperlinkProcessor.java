/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.spring.beans.hyperlink;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.Location;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class BeansRefHyperlinkProcessor implements HyperlinkProcessor {

    // XXX what is this for?
    private boolean showExternal;

    public BeansRefHyperlinkProcessor(boolean showExternal) {
        this.showExternal = showExternal;
    }

    public void process(HyperlinkEnv env) {
        FileObject fileObject = NbEditorUtilities.getFileObject(env.getDocument());
        if (fileObject == null) {
            return;
        }
        SpringConfigModel model = SpringConfigModel.forFileObject(fileObject);
        final String beanName = env.getValueString();
        final File[] file = { null };
        final int[] offset = { -1 };
        try {
            model.runReadAction(new Action<SpringBeans>() {
                public void run(SpringBeans beans) {
                    SpringBean bean = beans.findBean(beanName);
                    if (bean == null) {
                        return;
                    }
                    Location location = bean.getLocation();
                    if (location == null) {
                        return;
                    }
                    file[0] = location.getFile();
                    offset[0] = location.getOffset();
                }
            });
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        if (file[0] != null) {
            SpringXMLConfigEditorUtils.openFile(file[0], offset[0]);
        }
    }
}
