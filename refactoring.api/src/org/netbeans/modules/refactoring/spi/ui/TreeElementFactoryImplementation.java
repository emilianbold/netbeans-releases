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

package org.netbeans.modules.refactoring.spi.ui;

/**
 * Register your own TreeElementFactoryImplementation into META-INF/services
 * if you want to build your own RefactoringPreview tree.
 * 
 * For instance Java Refactoring understand Java - specific objects e.g. 
 * Projects, Groups, Methods etc.
 * 
 * <pre>
 * public TreeElement getTreeElement(Object o) {
 * .
 * .
 * if (o instanceof SourceGroup) {
 *   return new SourceGroupTreeElement((SourceGroup)o);
 *  } else if (o instanceof SomethingFromJava) {
 *    return new SomethingFromJavaTreeElement((SomethingFromJava) o);
 *  }
 * </pre>
 * 
 * Important note. It is expected from mathematical point of view, that this method
 * is function, or even better bijection.
 * @author Jan Becicka
 */
public interface TreeElementFactoryImplementation {
    /*
     * returns TreeElement for given object if possible. Otherwise returns null.
     */
    public TreeElement getTreeElement(Object o);

    /*
     * clears internal structures
     */
    public void cleanUp();
}
