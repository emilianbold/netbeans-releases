/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.spi.editor.caret;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.caret.EditorCaret;
import org.netbeans.api.editor.caret.MoveCaretsOrigin;
import org.openide.util.Parameters;

/**
 * Boilerplate {@link NavigationFilter}, which supports chaining of filters
 * on an JTextComponent. The implementation should call super methods to 
 * allow lower-precedence filters to react. If the implementation desires to
 * disable the filters and take the movement action directly, it can still use
 * the {@link FilterBypass} instance passed.
 * <p/>
 * There are helper {@link #register} and {@link #unregister} methods which 
 * ensure the chain of filters is correctly maintained. After registering, methods
 * implemented by this class will delegate to the remembered formerly-toplevel filter.
 * Implementor of this class may safely call super.* methods to delegate to filters
 * further in the chain.
 * <p/>
 * 
 * @author sdedic
 */
public abstract class CascadingNavigationFilter extends NavigationFilter {
    private NavigationFilter    previous;
    private EditorCaret         owner;

    /**
     * Returns the next filter in the chain. This class' implementations of NavigationFilter
     * API methods delegate to that filter, if non-null. Results after this Filter is
     * unregistered (removed from the NavigationFilter) chain
     * are undefined.
     * 
     * @return next NavigationFilter.
     */
    protected final NavigationFilter getNextFilter() {
        return previous;
    }
    
    @Override
    public int getNextVisualPositionFrom(JTextComponent text, int pos, Position.Bias bias, int direction, Position.Bias[] biasRet) throws BadLocationException {
        return previous != null ?
                previous.getNextVisualPositionFrom(text, pos, bias, direction, biasRet) :
                super.getNextVisualPositionFrom(text, pos, bias, direction, biasRet);
    }

    @Override
    public void moveDot(FilterBypass fb, int dot, Position.Bias bias) {
        if (previous != null) {
            previous.moveDot(fb, dot, bias);
        } else {
            super.moveDot(fb, dot, bias);
        }
    }

    @Override
    public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
        if (previous != null) {
            previous.setDot(fb, dot, bias);
        } else {
            super.setDot(fb, dot, bias);
        }
    }

    /**
     * Removes this NavigationFilter from the chain; preceding filter will
     * be connected to the following one, so the chain will not be broken.
     * @param orig the operation specifier, must not be null, use {@link EditorCaret.MoveCaretsOrigin#DEFAULT}
     * for the default placement.
     */
    public final void unregister(@NonNull MoveCaretsOrigin orig) {
        Parameters.notNull("orig", orig);
        NavigationFilter f = owner.getNavigationFilter(orig);
        CascadingNavigationFilter next = null;
        
        while (f instanceof CascadingNavigationFilter && f != this) {
            next = (CascadingNavigationFilter)f;
            f = next.getNextFilter();
        }
        if (f != this) {
            return;
        }
        if (next == null) {
            owner.setNavigationFilter(orig, previous);
        } else {
            next.previous = previous;
        }
        // reset state
        this.owner = null;
        this.previous = null;
    }

    /**
     * Registers this Filter into the NavigationFilter chain. This filter will
     * be placed on top of the filter's chain and the formerly-toplevel filter will
     * be remembered for delegation.
     * <p/>
     * It is not permitted to register with more carets; make multiple instances of
     * the filter for that case.
     * 
     * @param caret where this Filter should be registered.
     * @param origin operation specifier
     */
    public final void register(
            @NonNull EditorCaret caret,
            @NonNull MoveCaretsOrigin origin) {
        Parameters.notNull("caret", caret);
        Parameters.notNull("origin", origin);
        if (owner != null) {
            throw new IllegalStateException();
        }
        NavigationFilter prev = caret.getNavigationFilter(origin);
        caret.setNavigationFilter(origin, this);
        setOwnerAndPrevious(caret, prev);
    }
    
    public void setOwnerAndPrevious(EditorCaret ec, NavigationFilter prev) {
        if (this.owner != null) {
            throw new IllegalStateException("Can be registered only once");
        }
        this.owner = ec;
        this.previous = prev;
    }
}
