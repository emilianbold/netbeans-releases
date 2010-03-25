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
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.awt.event.ActionEvent;
import java.io.File;
import org.openide.filesystems.FileObject;

final class RefreshSlow implements Runnable {
    private ActionEvent ref;
    private long micro;
    private long took;
    private int count;

    public RefreshSlow() {
        super();
    }

    public void run() {
        RootObj.invokeRefreshFor(this, File.listRoots());
    }

    void progress(int index, int size, FileObject obj) {
        if (ref != null) {
            ref.setSource(new Object[] { index, size, obj });
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActionEvent) {
            this.ref = (ActionEvent)obj;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    void before() {
        micro = System.currentTimeMillis();
    }

    boolean after() {
        took += (System.currentTimeMillis() - micro);
        count++;
        if (took > 10) {
            try {
                long how = count / 10;
                count = 0;
                Thread.sleep(how);
            } catch (InterruptedException ex) {
                return false;
            }
            took = 0;
        }
        return true;
    }
}
