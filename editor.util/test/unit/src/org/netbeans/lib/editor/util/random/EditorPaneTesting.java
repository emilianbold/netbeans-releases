/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.lib.editor.util.random;

import java.awt.event.ActionEvent;
import java.util.Random;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.random.RandomTestContainer.Context;

public class EditorPaneTesting {

    /**
     * Name of op that types a single char into editor pane.
     */
    public static final String TYPE_CHAR = "editor-pane-type-char";

    /**
     * Name of op that groups actions for moving caret left/right/up/down.
     */
    public static final String MOVE = "editor-pane-move";

    /**
     * Name of op that groups actions for doing selection with caret left/right/up/down.
     */
    public static final String SELECT = "editor-pane-select";

    /**
     * Name of op that sets a random offset in document.
     */
    public static final String SET_CARET_OFFSET = "editor-pane-set-caret-offset";

    /**
     * Prefix for action ops e.g. <code>ACTION + DefaultEditorKit.insertBreakAction</code>
     * is an action for inserting newlines.
     * <br/>
     * For example:<code>
     *   container.addOp(new ActionOp(
     *           round.setRatio(EditorPaneTesting.TYPE_CHAR, 10);
     * </code>
     */
    static final String ACTION_PREFIX = "editor-pane-action-";

    public static RandomTestContainer initContainer(RandomTestContainer container, final EditorKit kit) throws Exception {
        if (container == null) {
            container = new RandomTestContainer();
        }
        final RandomTestContainer validContainer = container;
        JEditorPane pane = getEditorPane(container);
        if (pane == null) {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    validContainer.putProperty(JEditorPane.class, new JEditorPane());
                }
            });
            pane = container.getInstance(JEditorPane.class);
        }
        final JEditorPane validPane = pane;
        // Use a test frame and scroll pane
        JFrame frame = container.getInstanceOrNull(JFrame.class);
        if (frame == null) {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = new JFrame("Test Frame");
                    validContainer.putProperty(JFrame.class, frame);
                    frame.getContentPane().add(new JScrollPane(validPane));
                    frame.pack();
//                    frame.setSize(200, 100);
                    frame.setVisible(true); // Could it be rendered just to a memory buffer??
                    validContainer.putProperty(JFrame.class, frame);
                }
            });
            frame = validContainer.getInstance(JFrame.class);
        }
        
        if (kit != null) {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    validContainer.getInstance(JEditorPane.class).setEditorKit(kit);
                }
            });
        }

        container.addOp(new TypeCharOp());
        addActionOp(container, DefaultEditorKit.insertBreakAction);
        addActionOp(container, DefaultEditorKit.insertTabAction);
        addActionOp(container, DefaultEditorKit.deleteNextCharAction);
        addActionOp(container, DefaultEditorKit.deletePrevCharAction);
        container.addOp(new MoveOrSelectOp(MOVE));
        container.addOp(new MoveOrSelectOp(SELECT));
        container.addOp(new SetCaretOffsetOp());
        return container;
    }

    public static JEditorPane getEditorPane(PropertyProvider provider) {
        return provider.getInstanceOrNull(JEditorPane.class);
    }

    /**
     * Add tested action operation to container.
     *
     * @param container non-null container.
     * @param actionName non-null tested action name.
     */
    public static void addActionOp(RandomTestContainer container, String actionName) {
        container.addOp(new ActionOp(actionName));
    }

    public static void setActionRatio(RandomTestContainer.Round round, String actionName, double ratio) {
        round.setRatio(ACTION_PREFIX + actionName, ratio);
    }

    public static void performAction(Context context, JEditorPane pane, String actionName) throws Exception {
        performAction(context, pane, actionName, null);
    }

    public static void performAction(Context context, final JEditorPane pane,
            final String actionName, final ActionEvent evt) throws Exception
    {
        performAction(context, pane, actionName, evt, true);
    }

    /*private*/ static void performAction(Context context, final JEditorPane pane,
            final String actionName, ActionEvent evt, boolean logOpEnabled) throws Exception
    {
        if (logOpEnabled && context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append(" runAction(").append(actionName).append(")\n");
            context.logOp(sb);
        }
        if (evt == null) {
            evt = new ActionEvent(pane, 0, "");
        }
        final ActionEvent validEvt = evt;
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Action action = getAction(pane, actionName);
                action.actionPerformed(validEvt);
            }
        });
    }

    private static Action getAction(JEditorPane pane, String actionName) {
        Action action = pane.getActionMap().get(actionName);
        if (action == null) {
            throw new IllegalStateException("Action \"" + actionName + "\" not found in pane=" + pane); // NOI18N
        }
        return action;
    }

    /**
     * Move/select caret left/right/up/down by directions WEST/EAST/NORTH/SOUTH from SwingConstants.
     *
     * @param context
     * @param direction
     * @throws Exception
     */
    public static void moveOrSelect(Context context, int direction, boolean select) throws Exception {
        String actionName;
        String directionName;
        switch (direction) {
            case SwingConstants.WEST:
                actionName = select ? DefaultEditorKit.selectionBackwardAction : DefaultEditorKit.backwardAction;
                directionName = "left";
                break;
            case SwingConstants.EAST:
                actionName = select ? DefaultEditorKit.selectionForwardAction : DefaultEditorKit.forwardAction;
                directionName = "right";
                break;
            case SwingConstants.NORTH:
                actionName = select ? DefaultEditorKit.selectionUpAction : DefaultEditorKit.upAction;
                directionName = "up";
                break;
            case SwingConstants.SOUTH:
                actionName = select ? DefaultEditorKit.selectionDownAction : DefaultEditorKit.downAction;
                directionName = "down";
                break;
            default:
                throw new IllegalStateException("Invalid direction=" + direction); // NOI18N
        }
        if (context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append(select ? "Selection" : "Cursor").append(' ').append(directionName).append("\n");
            context.logOp(sb);
        }
        JEditorPane pane = context.getInstance(JEditorPane.class);
        performAction(context, pane, actionName, null, false);
    }

    public static void setCaretOffset(Context context, int offset) throws Exception {
        JEditorPane pane = context.getInstance(JEditorPane.class);
        if (context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append("SET_CARET_OFFSET: ").append(pane.getCaretPosition()).append(" => ").append(offset).append("\n");
            context.logOp(sb);
        }
        pane.setCaretPosition(offset);
    }

    public static void typeChar(Context context, char ch) throws Exception {
        JEditorPane pane = context.getInstance(JEditorPane.class);
        if (ch == '\n') { // Insert break
            performAction(context, pane, DefaultEditorKit.insertBreakAction, null, true);
        } else if (ch == '\t') { // Insert TAB
            performAction(context, pane, DefaultEditorKit.insertTabAction, null, true);
        } else { // default key typed action
            if (context.isLogOp()) {
                StringBuilder sb = context.logOpBuilder();
                sb.append(" typeChar('").append(CharSequenceUtilities.debugChar(ch)).append("')\n");
                context.logOp(sb);
            }
            performAction(context, pane, DefaultEditorKit.defaultKeyTypedAction,
                    new ActionEvent(pane, 0, String.valueOf(ch)), false);
        }
    }

    public static void threadSleep(Context context, int timeMillis) throws Exception {
        if (context.isLogOp()) {
            StringBuilder sb = context.logOpBuilder();
            sb.append("SLEEP: ").append(timeMillis).append(" ms\n");
            context.logOp(sb);
        }
        Thread.sleep(timeMillis);
    }

    private static final class MoveOrSelectOp extends RandomTestContainer.Op {

        public MoveOrSelectOp(String name) {
            super(name);
        }

        @Override
        protected void run(Context context) throws Exception {
            Random random = context.container().random();
            int actionIndex = random.nextInt(4);
            boolean select = random.nextBoolean();
            int direction;
            switch (actionIndex) {
                case 0:
                    direction = SwingConstants.WEST;
                    break;
                case 1:
                    direction = SwingConstants.EAST;
                    break;
                case 2:
                    direction = SwingConstants.NORTH;
                    break;
                case 3:
                    direction = SwingConstants.SOUTH;
                    break;
                default:
                    throw new IllegalStateException("Invalid actionIndex=" + actionIndex); // NOI18N
            }
            moveOrSelect(context, direction, select);
        }

    }

    private static final class ActionOp extends RandomTestContainer.Op {

        private String actionName;

        public ActionOp(String actionName) {
            super(ACTION_PREFIX + actionName);
            this.actionName = actionName;
        }

        @Override
        protected void run(Context context) throws Exception {
            performAction(context, context.getInstance(JEditorPane.class), actionName, null, true);
        }

    }

    private static final class TypeCharOp extends RandomTestContainer.Op {

        TypeCharOp() {
            super(TYPE_CHAR);
        }

        @Override
        protected void run(Context context) throws Exception {
            Random random = context.container().random();
            RandomText randomText = context.getInstance(RandomText.class);
            char ch = randomText.randomChar(random);
            typeChar(context, ch);
        }

    }

    private static final class SetCaretOffsetOp extends RandomTestContainer.Op {

        SetCaretOffsetOp() {
            super(SET_CARET_OFFSET);
        }

        @Override
        protected void run(Context context) throws Exception {
            Random random = context.container().random();
            Document doc = DocumentTesting.getDocument(context);
            int offset = random.nextInt(doc.getLength() + 1);
            setCaretOffset(context, offset);
        }

    }

}
