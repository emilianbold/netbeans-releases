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
package org.netbeans.modules.mercurial.ui.add;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.awt.event.ActionEvent;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Add action for mercurial: 
 * hg add - add the specified files on the next commit
 * 
 * @author John Rice
 */
public class AddAction extends AbstractAction {
    
    private final VCSContext context;

    public AddAction(String name, VCSContext context) {
        this.context = context;
        putValue(Action.NAME, name);
    }
    
    public boolean isEnabled() {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();        
        
        if(cache.listFiles(context, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY).length != 0)
            return true;

        return false;
    } 
    
    public void actionPerformed(ActionEvent ev) {     
        if(!Mercurial.getInstance().isGoodVersionAndNotify()) return;
        Mercurial hg = Mercurial.getInstance();
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if (files == null || files.length == 0) return;

        FileStatusCache cache = hg.getFileStatusCache();        
        
        File root = hg.getTopmostManagedParent(files[0]);
        List<File> addFiles = new ArrayList<File>();
        
        for (File file : files) {
            if (!file.isDirectory() && (cache.getStatus(file).getStatus() & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0){
                addFiles.add(file);
            }
        }
        if (addFiles.size() == 0) return;
        
        try {
            HgCommand.doAdd(root, addFiles);
        } catch (HgException ex) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
            DialogDisplayer.getDefault().notifyLater(e);
        }
        // hg.versionedFilesChanged();
        for (File file : addFiles){
            cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }
}
