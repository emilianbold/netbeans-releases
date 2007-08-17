/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 * @author Lukas Waldmann
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
