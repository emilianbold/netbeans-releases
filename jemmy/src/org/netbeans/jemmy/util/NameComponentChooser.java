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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.util;

import java.awt.Component;

import org.netbeans.jemmy.ComponentChooser;

import org.netbeans.jemmy.operators.Operator;

/**
 *
 * Specifies criteria for component lookup basing on component name.
 *
 * By default uses new Operator.DefaultStringComparator(true, true) compa
 *
 * @author Nathan Paris (Nathan_Paris@adp.com)
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *
 */
public class NameComponentChooser implements ComponentChooser {
    private String name;
    private Operator.StringComparator comparator;

    /**
     * Creates an instance to search for a component by name.
     * @param name Expecten component name pattern.
     * @param comparator Comparator for a comparision of a component name with a pattern.
     */
    public NameComponentChooser(String name, Operator.StringComparator comparator) {
        this.name = name;
        this.comparator = comparator;
    }

    /**
     * Creates an instance to search for a component by name using exact comparision.
     * @param name Expecten component name pattern.
     */
    public NameComponentChooser(String name) {
        this(name, new Operator.DefaultStringComparator(true, true));
    }

    public boolean checkComponent(Component component) {
        return(comparator.equals(component.getName(), name));
    }

    public String getDescription() {
        return("Component having \"" + name + "\" name.");
    }
}
