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

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of SharedLayoutData, used by LDPLayout.  Primary use case:
 * Create a panel that implements SharedLayoutData, and delegate to an instance
 * of this class.
 * Borrowed from http://imagine.dev.java.net
 * @author Tim Boudreau
 */
public final class DefaultSharedLayoutData implements SharedLayoutData {
    private final Set <LayoutDataProvider> known = new HashSet<LayoutDataProvider>();

    public void register(LayoutDataProvider p) {
        known.add (p);
        for (LayoutDataProvider d : known) {
            if (d instanceof JComponent) {
                ((JComponent) d).invalidate();
                ((JComponent) d).revalidate();
                ((JComponent) d).repaint();
            }
        }
    }
    
    public void unregister (LayoutDataProvider p) {
        known.remove (p);
        for (LayoutDataProvider d : known) {
            if (d instanceof JComponent) {
                ((JComponent) d).invalidate();
                ((JComponent) d).revalidate();
                ((JComponent) d).repaint();
            }
        }
    }

    public int xPosForColumn(int column) {
        int xpos = 0;
        for (LayoutDataProvider l : known) {
            int colpos = l.getColumnPosition(column);
            xpos = Math.max (colpos, xpos);
        }
        return xpos;
    }

    public void expanded(LayoutDataProvider p, boolean state) {
        Set <LayoutDataProvider> iter = new HashSet<LayoutDataProvider>(known);
        if (state) {
            for (LayoutDataProvider d : iter) {
                if (d != p) {
                    if (d.isExpanded()) {
                        d.doSetExpanded(false);
                    }
                } else {
                    if (d instanceof JComponent) {
                        JComponent jc = (JComponent) d;
                        jc.invalidate();
                        jc.revalidate();
                        jc.repaint();
                    }
                }
            }
        }
    }
}
