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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.refactoring;

import java.beans.BeanInfo;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.bpel.refactoring.Util;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation;
import org.netbeans.modules.xml.xam.Component;
import org.openide.filesystems.FileObject;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.03.16
 */
public class Tree implements TreeElementFactoryImplementation {

    public TreeElement getTreeElement(Object o) {
/*
        TreeElement result = null;
        if (o instanceof RefactoringElement) {
System.out.println();
System.out.println("!!!!!!!: RefactoringElement");
System.out.println();
            Component comp = ((RefactoringElement)o).getLookup().lookup(Component.class);

            if (comp != null) {
                FileObject fo = ((RefactoringElement) o).getParentFile();
                result = new Element((RefactoringElement) o);
             } 
        } 
        else */
        if (o instanceof Component) {
//System.out.println();
//System.out.println("!!!!!!!: Component " + o);
//System.out.println();
            return new Element((Component) o);
        }
        return null;
    }

    public void cleanUp() {}
}
