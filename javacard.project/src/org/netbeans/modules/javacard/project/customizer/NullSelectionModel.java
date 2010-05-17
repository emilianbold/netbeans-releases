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
package org.netbeans.modules.javacard.project.customizer;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;

/**
 * Selection model that doesn't let you select anything.  Used
 * in the applets customizer, where we show a list of AIDs, but
 * it would be confusing if you could select one because nothing
 * is sensitive to the selection there.
 *
 * @author Tim Boudreau
 */
final class NullSelectionModel implements ListSelectionModel {
    public void setSelectionInterval(int index0, int index1) {
        //do nothing
    }

    public void addSelectionInterval(int index0, int index1) {
        //do nothing
    }

    public void removeSelectionInterval(int index0, int index1) {
        //do nothing
    }

    public int getMinSelectionIndex() {
        return -1;
    }

    public int getMaxSelectionIndex() {
        return -1;
    }

    public boolean isSelectedIndex(int index) {
        return false;
    }

    public int getAnchorSelectionIndex() {
        return -1;
    }

    public void setAnchorSelectionIndex(int index) {
        //do nothing
    }

    public int getLeadSelectionIndex() {
        return -1;
    }

    public void setLeadSelectionIndex(int index) {
        //do nothing
    }

    public void clearSelection() {
        //do nothing
    }

    public boolean isSelectionEmpty() {
        return true;
    }

    public void insertIndexInterval(int index, int length, boolean before) {
        //do nothing
    }

    public void removeIndexInterval(int index0, int index1) {
        //do nothing
    }

    public void setValueIsAdjusting(boolean valueIsAdjusting) {
        //do nothing
    }

    public boolean getValueIsAdjusting() {
        return false;
    }

    public void setSelectionMode(int selectionMode) {
        //do nothing
    }

    public int getSelectionMode() {
        return ListSelectionModel.SINGLE_SELECTION;
    }

    public void addListSelectionListener(ListSelectionListener x) {
        //do nothing
    }

    public void removeListSelectionListener(ListSelectionListener x) {
        //do nothing
    }
}
