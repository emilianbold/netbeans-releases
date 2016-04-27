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
package org.netbeans.api.editor.caret;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.editor.caret.NavigationFilterBypass;
import org.openide.util.Parameters;

/**
 * Describes the operation which initiated the caret navigation. 
 * <p>
 * The core implementation
 * supports operation type (token) as a key and original intended movement direction as a hint
 * for possible filtering. Future API versions may define more details.
 * </p><p>
 * This class is used in two modes: 
 * </p>
 * <ol>
 * <li>When moving a caret ({@link EditorCaret#setDot(int, org.netbeans.api.editor.caret.MoveCaretsOrigin) setDot(pos, origin)},
 * {@link EditorCaret#moveDot(int, org.netbeans.api.editor.caret.MoveCaretsOrigin) moveDot(pos, origin)}, 
 * {@link EditorCaret#moveCarets(org.netbeans.spi.editor.caret.CaretMoveHandler, org.netbeans.api.editor.caret.MoveCaretsOrigin) moveCarets(handler, origin)}),
 * as a information on the originating operation ({@link #getActionType getActionType()}) and possibly additional
 * hints that describes the intended caret move ({@link #getDirection() getDirection()}).
 * </li>
 * <li>
 * When {@link EditorCaret#setNavigationFilter(javax.swing.text.JTextComponent, org.netbeans.api.editor.caret.MoveCaretsOrigin, javax.swing.text.NavigationFilter)  registering a NavigationFilter}
 * as a filtering template. 
 * </li>
 * </ol>
 * 
 * <div class="nonnormative">
 * <p>
 * The intended usage in caret moving code (actions) is as follows:
 * </p>
 * <pre><code>
 * // Action perform method
 * editorCaret.moveCarets(new CaretMoveHandler() {
 *      &#64;Override
 *      public void moveCarets(CaretMoveContext context) {
 *          ...
 *      }
 *  }, new MoveCaretsOrigin(
 *          // The action is a raw movement command
 *          MoveCaretsOrigin.DIRECT_NAVIGATION, 
 *          // The approximate direction of the movement; can be 0.
 *          SwingConstants.NORTH)
 *  );
 * </code></pre>
 * <p>
 * If a {@link javax.swing.text.NavigationFilter} only wants to intercept certain type of moevements, it can register as follows:
 * </p>
 * <pre><code>
 * EditorCaret eCaret = .... ; // obtain EditorCaret
 * eCaret.setNavigationFilter(
 *   new NavigationFilter() {
 *          // navigation filter implementation, not important for the example
 *   }, 
 *   new MoveCaretsOrigin(MoveCaretsOrigin.DIRECT_NAVIGATION)
 * );
 * </code></pre>
 * <p>
 * If the NavigationFilter implementation wants to obtain the extended information for the caret movement,
 * it can downcast the received FilterBypass:
 * </p>
 * <pre><code>
 *  public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
 *    if (fb instanceof NavigationFilterBypass) {
 *      NavigationFilterBypass nfb = (NavigationFilterBypass)fb;
 * 
 *      // get the Origin object created by the caret-moving operation, can query the details
 *      MoveCaretsOrigin origin = nfb.getOrigin();
 * 
 *      // get the individual caret in multi-caret scenario
 *      CaretInfo info = nfb.getCaretInfo();
 * 
 *      // get the whole EditorCaret
 *      EditorCaret eCaret = nfb.getEditorCaret();
 *    }
 *  }
 * </code></pre>
 * </div>
 * @see NavigationFilterBypass
 * @since 2.9
 */
public final class MoveCaretsOrigin {
    /**
     * Actions, which are defined as moving or setting the caret. Do not user for actions
     * like search (moves caret to the found string), goto type (moves to the definition) etc.
     */
    public static final String DIRECT_NAVIGATION = "navigation.action"; // NOI18N
    
    /**
     * Undefined action type. Use this description when registering for all possible
     * caret movements.
     */
    public static final MoveCaretsOrigin DEFAULT = new MoveCaretsOrigin("default", 0); // NOI18N
    
    /**
     * Actions which must avoid caret filters. Use this special instance to indicate that the
     * infrastructure should bypass all caret filters.
     */
    public static final MoveCaretsOrigin DISABLE_FILTERS = new MoveCaretsOrigin("disable-filters", 0); // NOI18N
    
    private final String actionType;
    private final int direction;

    /**
     * Describes the origin by just the action type. 
     * @param actionType action type
     */
    public MoveCaretsOrigin(String actionType) {
        this.actionType = actionType;
        this.direction = 0;
    }

    /**
     * Specifies the origin by action type, and the overall moving direction
     * @param actionType action type
     * @param direction the intended direction of movement
     */
    public MoveCaretsOrigin(@NonNull String actionType, int direction) {
        Parameters.notNull("actionType", actionType); // NOI18N
        this.actionType = actionType;
        this.direction = direction;
    }

    /**
     * Returns the type of the action which originated the caret movement.
     * @return the action type.
     */
    @NonNull
    public String getActionType() {
        return actionType;
    }

    /**
     * Specifies the desired movement direction. Use {@link javax.swing.SwingConstants}
     * compass constants to specify the direction. 0 means the direction
     * is unspecified.
     *
     * @return The initial direction of movemnet
     */
    public int getDirection() {
        return direction;
    }
    
}
