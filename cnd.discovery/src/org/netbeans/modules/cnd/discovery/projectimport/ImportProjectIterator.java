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

package org.netbeans.modules.cnd.discovery.projectimport;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProjectWizardPanel1.WizardStorage;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public class ImportProjectIterator implements WizardDescriptor.InstantiatingIterator {
    private WizardDescriptor wizard;
    private WizardStorage storage;
    private WizardDescriptor.Panel[] panels ;
    private int index = 0;

    public ImportProjectIterator(WizardDescriptor.Panel[] panels, WizardStorage storage){
        this.panels = panels;
        this.storage = storage;
    }

    public Set instantiate() throws IOException {
        RequestProcessor.getDefault().post(new Runnable(){
            public void run() {
                try {
                    new ImportProject(storage).create();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        return null;
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    public void uninitialize(WizardDescriptor wizard) {
    }

    public Panel current() {
        return panels[index];
    }

    public String name() {
        return null;
    }

    public boolean hasNext() {
        return index < (panels.length - 1);
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
       if ((index + 1) == panels.length) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (index == 0) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }
}
