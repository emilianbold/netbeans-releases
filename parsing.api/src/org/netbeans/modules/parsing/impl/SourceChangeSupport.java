/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl;

import java.util.Objects;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.parsing.implspi.SourceControl;
import org.netbeans.modules.parsing.spi.Parser;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 * Support to watch file changes, extracted from original org.netbeans.modules.parsing.impl.events.EventSupport.
 * The rest of event support was moved to parsing.nb module as it relies on DataSystems and Editors.
 * 
 * @author sdedic
 */
public final class SourceChangeSupport extends FileChangeAdapter implements ChangeListener {
    private final SourceControl control;
    private final FileObject file;

    public SourceChangeSupport(SourceControl control, FileObject file) {
        this.control = control;
        this.file = file;
    }
    
    public void activate(Parser parser) {
        if (file == null) {
            return;
        }
        if (parser != null) {
            if (file != null || (control.getSource().getDocument(false)) != null) {
                parser.addChangeListener(this);
            }
        }
        file.addFileChangeListener(FileUtil.weakFileChangeListener(this, file));
    }
    
    @Override
    public void fileChanged(final FileEvent fe) {
        // FIXME -- MIME type may changed even though the file only changed content.
        // For example XML files' MIME type depends on their XMLNS declaration 
        control.sourceChanged(false);
        Utilities.revalidate(control.getSource());
    }        

    @Override
    public void fileRenamed(final FileRenameEvent fe) {
        final String oldExt = fe.getExt();
        final String newExt = fe.getFile().getExt();
        control.sourceChanged(!Objects.equals(oldExt, newExt));
        Utilities.revalidate(control.getSource());
    }
    
    @Override
    public void stateChanged(final ChangeEvent e) {
        control.sourceChanged(false);
        Utilities.revalidate(control.getSource());
    }
}
