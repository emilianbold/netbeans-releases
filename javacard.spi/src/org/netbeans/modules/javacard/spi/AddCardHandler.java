/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.spi;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.javacard.common.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.common.Utils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 * Interface which can be registered in the system filesystem, to allow
 * the user to define a device on a Java Card platform.  All platforms should,
 * if possible, create a default device when they are created, as it may
 * not be obvious to the user that they need to do this.
 * <p/>
 * Instances of this class are registered by <i>Platform Kind</i>.  Platform
 * kind is an ad-hoc string provided from JavacardPlatform.getPlatformKind().
 * For each platform kind there is a directory in the system filesystem, where
 * instances of AddDeviceHandler can be registered.  If more than one is present
 * for a given card kind, then the Add Device popup menu item will be a submenu
 * allowing the user to create multiple kinds of card.  In this way, the simplest
 * kind of alternate device integration can simply "hang" its own card definitions
 * off of the default instance of the Java Card RI, which can hijack Ant
 * deployment any way they want using an AntTargetInterceptor - without having
 * to implement JavacardPlatform and provide the Java Card API jars themselves.
 * <p/>
 * To add an AddDeviceHandler for a platform, place a <code>.instance</code> file or similar,
 * which can create an AddDeviceHandler subclass in the system filesystem (via
 * the module layer.xml file), in the folder
 * <pre>
 * org-netbeans-modules-javacard-spi/kinds/$KIND/
 * </pre>
 * (replacing <code>$KIND</code> with the actual value returned by
 * <code>JavacardPlatform.getKind()</code>).  For example, the path to register
 * your own factory for Card instances that run against the Java Card RI,
 * you would add a <code>.instance</code> file in
 * <pre>
 * org-netbeans-modules-javacard-spi/kinds/RI/
 * </pre>
 * <p/>
 * Custom JavacardPlatform implementations should call <code>createAddDeviceAction()</code>
 * to include any registered AddDeviceHandlers in the list of popup menu actions
 * for their Nodes.
 * 
 * @author Tim Boudreau
 */
public abstract class AddCardHandler {

    private final String name;

    protected AddCardHandler(String name) {
        this.name = name;
    }

    /**
     * Show a dialog that will allow the user to set up a new card for this
     * platform.  If the user completes the dialog successfully, this should
     * result in writing out a new file representing a Card, which will
     * appear as a child card of the platform in question.
     * <p/>
     * How exactly this happens depends on how the platform in question finds
     * its Cards.  In the case of the Java Card RI, it means writing a
     * properties-format file into the directory for Cards for the RI (this
     * can be located by calling
     * <pre>
     * FileObject targetFolder = org.netbeans.modules.javacard.common.Utils.
     *      sfsFolderForDeviceConfigsForPlatformNamed(platform.getSystemId(), true)
     * </pre>
     * ), and giving it a file extension that will result it the DataObject for
     * that file having an instance of Card in its Lookup (you can use the
     * same file extension, <code>.jcard</code> that the RI uses if your card's
     * behavior is compatible with the RI's cards;  otherwise, define your
     * own File Type and let your DataObject create the Card instance however
     * you want - if you need the card definition included in the Ant build
     * environment, you will probably want to use properties file format with
     * whatever custom extension you want).
     * <p/>
     * This method will only ever be called on the AWT event dispatch thread,
     * and it is safe to construct Swing components inside this method.
     *
     * @param platformDob The DataObject for the platform.
     * @param target The actual platform
     * @param callback A callback which will be passed the newly created card,
     * if any is created.  This is used to, for example, change the selected
     * Card to the newly created one in the card management dialog.  May be
     * null.
     * @return A Card instance, if the dialog is completed successfully
     */
    public abstract Card showNewDeviceDialog(DataObject platformDob, JavacardPlatform target, CardCreatedCallback callback, FileObject targetFolder);

    /**
     * Determine if this AddDeviceHandler is available on this platform.  The
     * default implementation simply returns
     * <code>target.isValid()</code>.  Implementations that need to check other
     * aspects of the platform should override this method.
     *
     * @param target The platform
     * @return Whether or not this AddDeviceHandler can work with this platform
     */
    public boolean isEnabled(JavacardPlatform target) {
        return target.isValid();
    }

    /**
     * Get the localized name of this kind of card, which may be used in a
     * popup menu, e.g. <b>Add [getName()] Card</b>.
     * @return A localized name for the kind of card this handler can create
     */
    public final String getName() {
        return name;
    }

    public static final Lookup.Result<? extends AddCardHandler> handlersFor(JavacardPlatform platform) {
        String path = CommonSystemFilesystemPaths.SFS_ADD_HANDLER_REGISTRATION_ROOT + platform.getPlatformKind();
        return Lookups.forPath(path).lookupResult(AddCardHandler.class);
    }

    /**
     * Create an Action which will provide one or more New Card menu items,
     * based on the AddDeviceHandlers registered in the system filesystem.
     * @param pdo The DataObject representing the platform
     * @param platform The platform object itself
     * @return An action, or null if no AddDeviceHandlers are registered for this
     * platform kind.
     */
    public static Action createAddDeviceAction(DataObject pdo, JavacardPlatform platform, CardCreatedCallback callback) {
        Collection <? extends Lookup.Item<? extends AddCardHandler>> c = handlersFor(platform).allItems();
        int ct = c.size();
        if (ct == 0) {
            return null;
        } else if (ct == 1) {
            return new SingleHandlerAction(platform, pdo, c.iterator().next().getInstance(), callback);
        } else {
            return new AddAction (pdo, platform, callback);
        }
    }

    private static final class AddAction extends AbstractAction implements Presenter.Popup {

        private final JavacardPlatform platform;
        private final DataObject pdo;
        private final CardCreatedCallback callback;

        private AddAction(DataObject pdo, JavacardPlatform platform, CardCreatedCallback callback) {
            this.pdo = pdo;
            this.platform = platform;
            this.callback = callback;
            putValue (NAME, NbBundle.getMessage(AddAction.class, "SUBMENU_NEW_CARD")); //NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            throw new AssertionError("Not supported.");
        }

        public JMenuItem getPopupPresenter() {
            JMenu menu = new JMenu(this);
            for (AddCardHandler handler : handlersFor(platform).allInstances()) {
                menu.add(new SingleHandlerAction(platform, pdo, handler, callback));
            }
            return menu;
        }
    }

    private static class SingleHandlerAction extends AbstractAction {
        private final JavacardPlatform platform;
        private final DataObject pdo;
        private final AddCardHandler handler;
        private final CardCreatedCallback callback;

        public SingleHandlerAction(JavacardPlatform platform, DataObject pdo, AddCardHandler handler, CardCreatedCallback callback) {
            this.platform = platform;
            this.callback = callback;
            this.pdo = pdo;
            this.handler = handler;
            putValue (NAME, NbBundle.getMessage(SingleHandlerAction.class, "ACTION_ADD", handler.getName())); //NOI18N
        }


        public void actionPerformed(ActionEvent e) {
            FileObject targetFolder = Utils.sfsFolderForDeviceConfigsForPlatformNamed(platform.getSystemName(), true);
            Card card = handler.showNewDeviceDialog(pdo, platform, callback, targetFolder);
        }
    }

    /**
     * Callback interface which can be passed into createAddDeviceAction(),
     *
     */
    public interface CardCreatedCallback {
        public void onCardCreated (Card card, FileObject file);
    }
}
