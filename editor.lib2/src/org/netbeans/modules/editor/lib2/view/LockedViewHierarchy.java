/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.lib2.view;

import java.util.concurrent.Callable;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.editor.lib2.view.ViewHierarchyImpl;

/**
 * Locked view hierarchy as result of {@link ViewHierarchy#lock() }.
 * <br/>
 * Underlying document of the view hierarchy's text component must be
 * read-locked to guarantee stability of offsets passed to methods of this class.
 * <br/>
 * If editor view hierarchy is not installed into text component
 * (text component's root view is not an instance of DocumentView)
 * the methods return default values as described in their documentation.
 * 
 * @author Miloslav Metelka
 */
public final class LockedViewHierarchy {
    
    private final ViewHierarchyImpl impl;
    
    LockedViewHierarchy(ViewHierarchyImpl impl) {
        this.impl = impl;
    }

    public void unlock() {
        impl.unlock(this);
    }

    /**
     * Get text component that this view hierarchy is associated with.
     * <br/>
     * 
     * @return non-null text component.
     */
    public @NonNull JTextComponent getTextComponent() {
        return impl.textComponent();
    }
    
    /**
     * Get y coordinate of a visual row that corresponds to given offset.
     * <br/>
     * This method can only be run within transaction {@link #runTransaction(java.util.concurrent.Callable)}.
     * <br/>
     * Underlying document of the view hierarchy's text component should be read-locked
     * to guarantee stability of passed offset.
     * <br/>
     * If editor view hierarchy is not installed into text component this method
     * delegates to {@link JTextComponent#modelToView(int) }.
     *
     * @param offset
     * @return y
     */
    public double modelToY(int offset) {
        return impl.modelToY(this, offset);
    }
    
    /**
     * Multi-offset variant of {@link #modelToY(int)} with improved efficiency for sorted offsets.
     *
     * @param offsets array of offsets to be translated to y coordinates. More efficiency
     *  is achieved if the offsets are sorted from lowest to highest (at least partially).
     * @return array of y-coordinates (with the same cardinality as offsets array).
     */
    public double[] modelToY(int[] offsets) {
        return impl.modelToY(this, offsets);
    }
    
    /**
     * Get height of a visual row of text.
     * <br/>
     * For wrapped lines (containing multiple visual rows) this is height of a single visual row.
     * <br/>
     * Current editor view hierarchy implementation uses uniform row height for all the rows.
     * <br/>
     * This method can only be run within transaction {@link #runTransaction(java.util.concurrent.Callable)}.
     * 
     * @return height of a visual row.
     */
    public float getDefaultRowHeight() {
        return impl.getDefaultRowHeight(this);
    }

}
