/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.javacard.project.libraries;

import java.awt.Image;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tim Boudreau
 */
public class LibrariesNode extends AbstractNode {

    public LibrariesNode (LibrariesModel mdl) {
        super (Children.create (new LibrariesChildren(mdl), false), Lookups.singleton(mdl));
    }

    private static class LibrariesChildren extends ChildFactory.Detachable<LibraryEntry> implements ChangeListener {
        private final LibrariesModel mdl;

        private LibrariesChildren(LibrariesModel mdl) {
            this.mdl = mdl;
        }

        @Override
        protected void addNotify() {
            mdl.addChangeListener(this);
        }

        @Override
        protected void removeNotify() {
            mdl.removeChangeListener(this);
        }

        @Override
        protected Node createNodeForKey(LibraryEntry key) {
            return new ND (key);
        }

        @Override
        protected boolean createKeys(List<LibraryEntry> toPopulate) {
            toPopulate.addAll (mdl.entries());
            return true;
        }

        public void stateChanged(ChangeEvent e) {
            refresh(true);
        }
    }

    private static final class ND extends AbstractNode {
        ND (LibraryEntry e) {
            super (Children.LEAF, Lookups.singleton(e));
            setDisplayName(e.getDisplayName());
        }

        @Override
        public Image getIcon(int type) {
            LibraryEntry e = getLookup().lookup(LibraryEntry.class);
            return e.getIcon();
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    }
}
