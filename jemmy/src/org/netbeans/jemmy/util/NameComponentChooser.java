/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
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
     * @param comparator Comparator for a comparision of a component name with a pattern.
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
