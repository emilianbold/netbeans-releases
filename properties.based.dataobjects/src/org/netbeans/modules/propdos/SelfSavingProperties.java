/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.propdos;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileSystem;

/**
 * Properties object what has the ability to auto-save on a time delay after
 * any modification.  Calls to put() will fire property changes.  Property
 * changes are fired on the event queue, only *after* the changes have
 * been written to disk.
 *
 * @author Tim Boudreau
 */
abstract class SelfSavingProperties extends AntStyleResolvingProperties {

    //A properties subclass that writes itself to disk on a timer after
    //it is modified.  Customizer UI does not have to explicitly save it.
    protected final DataObject dob;
//    static { PropertiesBasedDataObject.LOGGER.setLevel(Level.ALL); }

    SelfSavingProperties(DataObject ob) {
        this.dob = ob;
    }

    protected abstract void onWriteCompleted() throws IOException;
    
    @Override
    public String toString() {
        return dob.getPrimaryFile().getPath() + "[" + super.toString() + "]" ; //NOI18N
    }

    @Override
    void onChangeOccurred() throws IOException {
        if (PropertiesBasedDataObject.LOGGER.isLoggable(Level.FINEST)) {
            PropertiesBasedDataObject.LOGGER.log(Level.FINEST,
                    "Begin write of " + //NOI18N
                    SelfSavingProperties.this.dob.getPrimaryFile().getPath(),
                    new Exception());
        }
        write();
        if (PropertiesBasedDataObject.LOGGER.isLoggable(Level.FINEST)) {
            PropertiesBasedDataObject.LOGGER.log(Level.FINEST,
                    "Successful write of " + //NOI18N
                    SelfSavingProperties.this.dob.getPrimaryFile().getPath()
                    + " invoking onWriteCompleted() in "
                    + getClass().getName()); //NOI18N
        }
        onWriteCompleted();
    }
    volatile boolean writing;

    private void write() throws IOException {
        //Method may be reentered while another thread is still writing
        if (writing || !dob.isValid()) {
            return;
        }
        final FileObject fo = dob.getPrimaryFile();
        fo.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {

            public void run() throws IOException {
                writing = true;
                FileLock lock = fo.lock();
                OutputStream out = fo.getOutputStream(lock);
                try {
                    SelfSavingProperties.this.store(out, ""); //NOI18N
                } catch (FileAlreadyLockedException locked) {
                    if (PropertiesBasedDataObject.LOGGER.isLoggable(Level.WARNING)) {
                        PropertiesBasedDataObject.LOGGER.log(Level.WARNING,
                                "Could not write " + dob.getPrimaryFile().getPath() + //NOI18N
                                " - already locked", locked); //NOI18N
                    }
                } finally {
                    writing = false;
                    out.close();
                    lock.releaseLock();
                }
            }
        });
    }
}
