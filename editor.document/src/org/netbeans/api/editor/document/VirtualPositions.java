/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.api.editor.document;

import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.editor.lib2.document.VirtualPos;
import org.openide.util.Parameters;

/**
 * A virtual position is a regular position togehter with a number of extra virtual columns.
 * That allows for positions behind line's last character.
 * Virtual position points at newline character of a particular line and {@link #extraColumns(Position)}
 * give an additional shift information.
 *
 * @author Miloslav Metelka
 */
public final class VirtualPositions {

    private VirtualPositions() {
        // No instances
    }
    
    /**
     * Produce an immutable virtual position.
     * The returned position acts like the original position and it does not handle in any way
     * any subsequent document modifications.
     *
     * @param pos non-null position. If this is already a virtual position its extraColumns
     *  get added to the extraColumns parameter passed to this method.
     * @param extraColumns >= 0 number of extra columns added to the position.
     *  For 0 the original position gets returned. Negative value throws an IllegalArgumentException.
     * @return virtual position whose {@link Position#getOffset()} returns the same value
     *  like the pos parameter.
     */
    public static Position create(@NonNull Position pos, int extraColumns) {
        Parameters.notNull("pos", pos);   //NOI18N
        if (extraColumns > 0) {
            if (pos.getClass() == VirtualPos.class) {
                return new VirtualPos((VirtualPos)pos, extraColumns);
            } else {
                return new VirtualPos(pos, extraColumns);
            }
        } else if (extraColumns == 0) {
            return pos;
        } else {
            throw new IllegalArgumentException("extraColumns=" + extraColumns + " < 0");
        }
    }

    /**
     * Return extra columns of a passed virtual position or zero for regular positions.
     * @param pos non-null position.
     * @return >=0 extra columns count or zero for regular positions.
     */
    public static int extraColumns(@NonNull Position pos) {
        Parameters.notNull("pos", pos);   //NOI18N
        return (pos.getClass() == VirtualPos.class)
                ? ((VirtualPos)pos).extraColumns()
                : 0;
    }
    
}
