/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.modules;

import org.openide.util.Exceptions;
import org.openide.util.SharedClassObject;

/**
* Provides hooks for a custom module that may be inserted into NetBeans.
* If needed this class should be extended by the main class of a module.
*
* <p>Simple modules will likely not need a main class--just a few entries in the manifest file.
* Even modules with a main class need not do anything in it that is already covered by manifest entries;
* only additional special functionality need be handled here.
*
* <p>Specify this class in the manifest file with <code>OpenIDE-Module-Install</code>.
*
* <p>Modules wishing to keep state associated with the installation of the module
* may do so by implementing not only this class but also {@link java.io.Externalizable}.
* In this case, they are responsible for reading and writing their own state
* properly (probably using {@link java.io.ObjectOutput#writeObject} and {@link java.io.ObjectInput#readObject}).
* Note that state which is logically connected to the user's configuration of the module on
* a possibly project-specific basis should <em>not</em> be stored this way, but rather
* using a system option. (Even if this information is not to be displayed, it should
* still be stored as hidden properties of the system option, so as to be switched properly
* during project switches.)
 * <strong>Storing externalizable state in a <code>ModuleInstall</code> is deprecated.</strong>
* @author Petr Hamernik, Jaroslav Tulach, Jesse Glick
*/
public class ModuleInstall extends SharedClassObject {
    private static final long serialVersionUID = -5615399519545301432L;

    /** Called when a module is being considered for loading.
     * (This would be before {@link #installed}, {@link #restored},
     * or {@link #updated} are called.) If something is critically
     * wrong with the module (missing ad-hoc dependency, missing
     * license key, etc.) then <code>IllegalStateException</code>
     * may be thrown to prevent it from being loaded (preferably
     * with a {@linkplain Exceptions#attachLocalizedMessage localized annotation}). The default implementation
     * does nothing. The module cannot assume much about when this
     * method will be called; specifically it cannot rely on layers
     * or manifest sections to be ready, nor for the module's classloader
     * to exist in the system class loader (so if loading bundles, icons,
     * and so on, specifically pass in the class loader of the install
     * class rather than relying on the default modules class loader).
     * @since 1.24
     */
    public void validate() throws IllegalStateException {
    }

    /**
     * Called when the module is first installed.
     * Should perform whatever setup functions are required.
     * The default implementation calls restored.
     * <p>Typically, would do one-off functions, and then also call {@link #restored}.
     * @deprecated Better to check specific aspects of the module's installation.
     *             For example, a globally installed module might be used in several
     *             user directories. Only the module itself can know whether its
     *             special installation tasks apply to some part of the global installation,
     *             or whether they apply to the module's usage in the current user directory.
     *             For this reason, implementing this method cannot be guaranteed
     *             to have useful effects.
    */
    public void installed() {
        restored();
    }

    /**
     * Called when an already-installed module is restored (during startup).
     * Should perform whatever initializations are required.
     * <p>Note that it is possible for module code to be run before this method
     * is called, and that code must be ready nonetheless. For example, data loaders
     * might be asked to recognize a file before the module is "restored". For this
     * reason, but more importantly for general performance reasons, modules should
     * avoid doing anything here that is not strictly necessary - often by moving
     * initialization code into the place where the initialization is actually first
     * required (if ever). This method should serve as a place for tasks that must
     * be run once during every startup, and that cannot reasonably be put elsewhere.
     * <p>Basic programmatic services are available to the module at this stage -
     * for example, its class loader is ready for general use, any objects registered
     * declaratively to lookup (e.g. system options or services) are ready to be
     * queried, and so on.
     */
    public void restored() {
    }

    /**
     * Called when the module is loaded and the version is higher than
     * by the previous load
     * The default implementation calls {@link #restored}.
     * @param release The major release number of the <B>old</B> module code name or -1 if not specified.
     * @param specVersion The specification version of the this <B>old</B> module.
     * @deprecated Better to check specific aspects of the module's installation.
     *             For example, a globally installed module might be used in several
     *             user directories. Only the module itself can know whether its
     *             special installation tasks apply to some part of the global installation,
     *             or whether they apply to the module's usage in the current user directory.
     *             For this reason, implementing this method cannot be guaranteed
     *             to have useful effects.
    */
    public void updated(int release, String specVersion) {
        restored();
    }

    /**
     * Called when the module is disabled or uninstalled (from a running NetBeans instance).
     * Should remove whatever functionality that it had registered.
     * @deprecated In practice there is no way to ensure that this method will really be called.
     *             The module might simply be deleted or disabled while the IDE is not running.
     *             <span class="nonnormative">(In fact this is always the case in NetBeans 6.0;
     *             the Plugin Manager only uninstalls or disables modules between restarts.)</span>
    */
    public void uninstalled() {
    }

    /**
     * Called when NetBeans is about to exit. The default implementation returns <code>true</code>.
     * The module may cancel the exit if it is not prepared to be shut down.
    * @return <code>true</code> if it is ok to exit
    */
    public boolean closing() {
        return true;
    }

    /**
     * Called when all modules agreed with closing and NetBeans will be closed.
    */
    public void close() {
    }

    @Override
    protected boolean clearSharedData() {
        return false;
    }
}
