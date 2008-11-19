/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.autoupdate.featureondemand;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class FeatureAction implements ActionListener, Runnable {

    private boolean success;
    private FileObject fo;
    private boolean isDelegateAction = false;

    public FeatureAction(FileObject fo, boolean delegate) {
        this.fo = fo;
        this.isDelegateAction = delegate;
    }

    public void actionPerformed(ActionEvent e) {
        success = false;
        RequestProcessor.Task t = RequestProcessor.getDefault().post(this, 0, Thread.NORM_PRIORITY);
        if (isDelegateAction) {
            t.waitFinished ();
        } else {
            return ;
        }
        
        if (! success) {
            return ;
        }
        
        FileObject newFile = Repository.getDefault().getDefaultFileSystem().findResource(fo.getPath());
        if (newFile == null) {
            throw new IllegalStateException("Cannot find file: " + fo.getPath());
        }
        
        Object obj = newFile.getAttribute("instanceCreate"); // NOI18N
        if (obj instanceof ActionListener) {
            ((ActionListener)obj).actionPerformed(e);
        }
    }

    public void run() {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot run in EQ!";
        URL url = FoDFileSystem.getInstance().getDelegateFileSystem(fo);
        Set<String> cnbs = Feature2LayerMapping.getInstance().getCodeName(url);
        success = ModulesInstaller.installModules(cnbs.toArray(new String[0]));
    }
}
