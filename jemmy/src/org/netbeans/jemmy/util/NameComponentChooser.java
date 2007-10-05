/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
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
