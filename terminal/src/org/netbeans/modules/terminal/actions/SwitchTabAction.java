package org.netbeans.modules.terminal.actions;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.netbeans.modules.terminal.api.IOTerm;
import org.netbeans.modules.terminal.api.IOTopComponent;
import org.netbeans.modules.terminal.api.TerminalContainer;
import org.netbeans.modules.terminal.api.TabContentProvider;
import org.netbeans.modules.terminal.ioprovider.Terminal;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.IOSelect;

/**
 *
 * @author igromov
 */
@ActionID(id = ActionFactory.SWITCH_TAB_ACTION_ID, category = ActionFactory.CATEGORY)
@ActionRegistration(displayName = "#CTL_SwitchTab", lazy = false) //NOI18N
@ActionReferences({
    @ActionReference(path = ActionFactory.ACTIONS_PATH, name = "SwitchTabAction"), //NOI18N
})
public class SwitchTabAction extends TerminalAction {

    public SwitchTabAction() {
        this(null);
    }

    public SwitchTabAction(Terminal context) {
        super(context);

        KeyStroke[] keyStrokes = new KeyStroke[10];
        for (int i = 0; i < 10; i++) {
            keyStrokes[i] = KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, InputEvent.ALT_MASK);
        }
        putValue(ACCELERATOR_KEY, keyStrokes);

        putValue(NAME, getMessage("CTL_SwitchTab")); //NOI18N
    }

    @Override
    protected void performAction() {
        Container container = SwingUtilities.getAncestorOfClass(TabContentProvider.class, getTerminal());
        if (container != null && container instanceof TabContentProvider) {
            TabContentProvider tcp = (TabContentProvider) container;
            List<? extends Component> allTabs = tcp.getAllTabs();
            try {
                int requested = Integer.parseInt(getEvent().getActionCommand());
                requested = (requested == 0)
                        ? 9
                        : requested - 1;
                if (requested >= allTabs.size() || requested < 0) {
                    return;
                }
                if (allTabs.get(requested) instanceof Terminal) {
                    Terminal terminal = (Terminal) allTabs.get(requested);
                    tcp.select(terminal);
                }
            } catch (NumberFormatException x) {
                return;
            }
        }
    }

    // --------------------------------------------- 
    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new SwitchTabAction(actionContext.lookup(Terminal.class));
    }
}
