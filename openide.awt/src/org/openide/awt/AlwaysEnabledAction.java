package org.openide.awt;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/** Lazily initialized always enabled action
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class AlwaysEnabledAction extends AbstractAction {

    private Map map;

    public AlwaysEnabledAction(Map m) {
        super();
        this.map = m;
    }

    @Override
    public boolean isEnabled() {
        assert EventQueue.isDispatchThread();
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        assert EventQueue.isDispatchThread();
        Object listener = map.get("delegate"); // NOI18N
        if (!(listener instanceof ActionListener)) {
            throw new NullPointerException();
        }
        ((ActionListener) listener).actionPerformed(e);
    }

    @Override
    public Object getValue(String name) {
        return extractCommonAttribute(map, this, name);
    }

    static final Object extractCommonAttribute(Map fo, Action action, String name) {
        if (Action.NAME.equals(name)) {
            String actionName = (String) fo.get("displayName"); // NOI18N
            // NOI18N
            //return Actions.cutAmpersand(actionName);
            return actionName;
        }
        if (Action.MNEMONIC_KEY.equals(name)) {
            String actionName = (String) fo.get("displayName"); // NOI18N
            // NOI18N
            int position = Mnemonics.findMnemonicAmpersand(actionName);

            return position == -1 ? null : Character.valueOf(actionName.charAt(position + 1));
        }
        if (Action.SMALL_ICON.equals(name)) {
            Object icon = fo == null ? null : fo.get("iconBase"); // NOI18N
            if (icon instanceof Icon) {
                return (Icon) icon;
            }
            if (icon instanceof Image) {
                return ImageUtilities.image2Icon((Image)icon);
            }
            if (icon instanceof String) {
                return ImageUtilities.loadImage((String)icon);
            }
            if (icon instanceof URL) {
                return Toolkit.getDefaultToolkit().getImage((URL) icon);
            }
        }
        if ("iconBase".equals(name)) { // NOI18N
            return fo == null ? null : fo.get("iconBase"); // NOI18N
        }
        if ("noIconInMenu".equals(name)) { // NOI18N
            return fo == null ? null : fo.get("noIconInMenu"); // NOI18N
        }
        if (Action.ACCELERATOR_KEY.equals(name)) {
            Keymap map = Lookup.getDefault().lookup(Keymap.class);
            if (map != null) {
                KeyStroke[] arr = map.getKeyStrokesForAction(action);
                return arr.length > 0 ? arr[0] : null;
            }
        }

        return null;
    }
}
