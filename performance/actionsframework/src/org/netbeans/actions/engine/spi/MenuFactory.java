/*
 * MenuFactory.java
 *
 * Created on January 24, 2004, 1:06 AM
 */

package org.netbeans.actions.engine.spi;

import java.util.Map;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

/** Factory for producing real menus given a container context name.  It
 * will look up the contents on the ActionProvider, and produce a menu
 * from the result.
 *
 * @author  Tim Boudreau
 */
public interface MenuFactory {
    public JMenu createMenu (String containerContext);
    public void update (String containerContext);
}
