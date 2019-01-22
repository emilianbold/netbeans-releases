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

/*
 * J2MEUnitFileIterator.java
 *
 * Created on 17 August 2007, 12:28
 *
 */

package org.netbeans.modules.mobility.j2meunit;

import java.io.IOException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;


/**
 *
 * 
 */
public class J2MEUnitFileIterator implements WizardDescriptor.InstantiatingIterator
{
    WizardDescriptor.InstantiatingIterator iterator = JavaTemplates.createJavaTemplateIterator();
    Project p;
    
    public static Object createIterator()
    {
        return new J2MEUnitFileIterator();
    }
    
    public Set/*<FileObject>*/ instantiate () throws IOException {        
        Set set=iterator.instantiate();
        ProjectClassPathExtender pcpe=(ProjectClassPathExtender) p.getLookup().lookup(ProjectClassPathExtender.class);
        if (pcpe!=null) {
            Library lib=LibraryManager.getDefault().getLibrary("JMUnit4CLDC10");
            pcpe.addLibrary(lib);
        }
        return set;
    }

    public void initialize(WizardDescriptor wizard)
    {
        p = Templates.getProject( wizard );
        iterator.initialize(wizard);
    }

    public void uninitialize(WizardDescriptor wizard)
    {
        iterator.uninitialize(wizard);
    }

    public WizardDescriptor.Panel<Object> current()
    {
        return iterator.current();
    }

    public String name()
    {
        return iterator.name();
    }

    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    public boolean hasPrevious()
    {
        return iterator.hasPrevious();
    }

    public void nextPanel()
    {
        iterator.nextPanel();
    }

    public void previousPanel()
    {
        iterator.previousPanel();
    }

    public void addChangeListener(ChangeListener l)
    {
        iterator.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l)
    {
        iterator.removeChangeListener(l);
    }
    
}
