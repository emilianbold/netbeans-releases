/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.wizards;

/*
 * WizardIterator.java
 *
 * Created on April 10, 2002, 1:51 PM
 */

import java.io.IOException;
import java.util.ArrayList;
import java.lang.reflect.Modifier;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.src.Type;
import org.openide.src.Identifier;
import org.openide.src.ClassElement;
import org.openide.src.MethodElement;
import org.openide.src.SourceException;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.TemplateWizard;

import org.netbeans.modules.java.JavaDataObject;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public abstract class WizardIterator implements TemplateWizard.Iterator {
    
    protected transient WizardDescriptor.Panel[] panels;
    protected transient String[] names;
    protected transient int current = 0;
    protected transient TemplateWizard wizard;
    
    public void addChangeListener(javax.swing.event.ChangeListener changeListener) {
    }
    
    public org.openide.WizardDescriptor.Panel current() {
        return panels[current];
    }
    
    public boolean hasNext() {
        return (current+1)<panels.length;
    }
    
    public boolean hasPrevious() {
        return current>0;
    }
    
    public String name() {
        return names[current];
    }
    
    public void nextPanel() {
        current++;
    }
    
    public void previousPanel() {
        current--;
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
    }
    
    public void uninitialize(TemplateWizard wizard) {
        panels=null;
        names=null;
    }
    
    private static MethodElement[] getTemplateMethods(JavaDataObject source) {
        ClassElement clel = source.getSource().getClass(Identifier.create(source.getName()));
        MethodElement[] methods = clel.getMethods();
        ArrayList templates = new ArrayList();
        for (int i=0; i<methods.length; i++)
            if ((methods[i].getName().getName().startsWith("test"))&&
                (methods[i].getModifiers()==Modifier.PUBLIC)&&
                (methods[i].getParameters().length==0)&&
                (methods[i].getReturn().equals(Type.VOID)))
                templates.add(methods[i]);
        return (MethodElement[])templates.toArray(new MethodElement[templates.size()]);
    }
            
    private static void transformTemplateMethods(JavaDataObject source, String[] newNames,  int[] indexes, MethodElement[] templates) throws SourceException, IOException {
        ClassElement clel = source.getSource().getClass(Identifier.create(source.getName()));
        clel.removeMethods(getTemplateMethods(source));
        for (int i=0; i<newNames.length; i++) {
            clel.addMethod(templates[indexes[i]]);
            clel.getMethod(templates[indexes[i]].getName(), null).setName(Identifier.create(newNames[i]));
        }
        String className=source.getName();
        StringBuffer suite = new StringBuffer();
        suite.append("\n        TestSuite suite = new NbTestSuite();\n");
        for (int i=0; i<newNames.length; i++) {
            suite.append("        suite.addTest(new ");
            suite.append(className);
            suite.append("(\"");
            suite.append(newNames[i]);
            suite.append("\"));\n");
        }
        suite.append("        return suite;\n");
        clel.getMethod(Identifier.create("suite"), null).setBody(suite.toString());
        ((SaveCookie)source.getCookie(SaveCookie.class)).save();
    }
    
}
