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
package org.netbeans.swing.layouts;

/**
 * An interface implemented by an ancestor component that allows nested panels
 * to all align their columns.  Usage:  Create a panel, set its layout to 
 * LDPLayout, and add some components to be displayed in a horizontal row.
 * Add them to a component that implements, or has an ancestor that implements,
 * SharedLayoutData.  The LDPLayout instance will access the SharedLayoutData
 * ancestor to find the column positions across all children that implement
 * LayoutDataProvider, even if they are not all children of the same component.
 * <p/>
 * To easily create a component that implements SharedLayoutData, just subclass
 * a JPanel or similar, implement SharedLayoutData and delegate to an instance
 * of DefaultSharedLayoutData, which handles memory management appropriately.
 * Borrowed from http://imagine.dev.java.net
 *
 * @author Tim Boudreau
 */
public interface SharedLayoutData {
    /**
     * The x position for this column, as agreed up on as the max position
     * across all registered components, so columns are aligned.
     * 
     * @param column The index of the column.
     * @return The X position
     */
    public int xPosForColumn (int column);
    /**
     * Register a component that should participate in layout data
     * @param p
     */
    public void register (LayoutDataProvider p);
    /**
     * Unregister a component.
     * @param p
     */
    public void unregister (LayoutDataProvider p);
    /**
     * Called when the expanded method is called 
     * @param p
     * @param state
     */
    public void expanded (LayoutDataProvider p, boolean state);
}
