/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.spi.phpmodule;

import java.io.File;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.openide.util.Parameters;

/**
 * Encapsulates a PHP framework.
 *
 * <p>This class allows providing support for PHP frameworks. It can be used
 * to extend a PHP module with a PHP framework, to find out whether a PHP
 * module is already extended by a PHP framework, or to retrieve a PHP framework's
 * specific configuration files.</p>
 *
 * <p>Instances of this class are registered in the <code>{@value org.netbeans.modules.php.api.phpmodule.PhpFrameworks#FRAMEWORK_PATH}</code>
 * in the module layer.</p>
 *
 * @author Tomas Mysik
 */
public abstract class PhpFrameworkProvider {

    private final String name;
    private final String description;

    /**
     * Creates a new PHP framework with a name and description.
     *
     * @param  name the short name of this PHP framework (e.g., "Symfony"); never <code>null</code>.
     * @param  description the description of this PHP framework (e.g., "An open source framework based on the MVC pattern"); can be <code>null</code>.
     * @throws NullPointerException if the <code>name</code> parameter is <code>null</code>.
     */
    public PhpFrameworkProvider(String name, String description) {
        Parameters.notNull("name", name); // NOI18N

        this.name = name;
        this.description = description;
    }

    /**
     * Returns the name of this PHP framework.
     *
     * @return the name; never <code>null</code>.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the description of this PHP framework. Defaults to the name
     * if a <code>null</code> <code>description</code> parameter was passed to the constructor.
     *
     * @return the description; never <code>null</code>.
     */
    public final String getDescription() {
        if (description != null) {
            return description;
        }
        return getName();
    }

    /**
     * Finds out if a given PHP module has already been extended with this PHP framework.
     * <p>
     * <b>This method should be as fast as possible.</b>
     *
     * @param  phpModule the PHP module; never <code>null</code>.
     * @return <code>true</code> if the PHP module has already been extended with this framework, <code>false</code> otherwise.
     */
    public abstract boolean isInPhpModule(PhpModule phpModule);

    /**
     * Returns the configuration files belonging to this framework.
     *
     * @param  phpModule the PHP module for which the configuration files are returned; never <code>null</code>.
     * @return an array containing the configuration files; can be empty but never <code>null</code>.
     */
    public abstract File[] getConfigurationFiles(PhpModule phpModule);

    /**
     * Creates a {@link PhpModuleExtender PHP module extender} for this framework
     * and the given PHP module.
     *
     * @param  phpModule the PHP module to be extended; can be <code>null</code>, e.g., if the
     *         method is called while creating a new PHP application, in which
     *         case the module doesn't exist yet.
     * @return a new PHP module extender; can be <code>null</code> if the framework doesn't support
     *         extending (either PHP modules in general or the particular PHP module
     *         passed in the <code>phpModule</code> parameter).
     */
    public abstract PhpModuleExtender createPhpModuleExtender(PhpModule phpModule);

    /**
     * Get {@link PhpModuleProperties PHP module properties} the given PHP module. PHP framework
     * can provide default values for any property (e.g. web root).
     *
     * @param  phpModule the PHP module which properties are going to be changed
     * @return new PHP module properties
     */
    public abstract PhpModuleProperties getPhpModuleProperties(PhpModule phpModule);

    /**
     * Get a {@link PhpModuleActionsExtender PHP module actions extender} for this framework
     * and the given PHP module.
     *
     * @param  phpModule the PHP module which actions are going to be extended
     * @return a new PHP module actions extender, can be <code>null</code> if the framework doesn't support
     *         extending of actions
     * @since 1.11
     */
    public abstract PhpModuleActionsExtender getActionsExtender(PhpModule phpModule);

    /**
     * Get a {@link PhpModuleIgnoredFilesExtender PHP module ignored files extender} for this framework
     * and the given PHP module.
     *
     * @param  phpModule the PHP module which ignored files are going to be extended
     * @return PHP module ignored files extender, can be <code>null</code> if the framework doesn't need
     *         to recommend to hide any files or folders
     * @since 1.12
     */
    public abstract PhpModuleIgnoredFilesExtender getIgnoredFilesExtender(PhpModule phpModule);

    /**
     * Get a {@link FrameworkCommandSupport framework command support} for this framework
     * and the given PHP module.
     *
     * @param  phpModule the PHP module for which framework command support is to be gotten
     * @return framework command support, can be <code>null</code> if the framework doesn't support
     *         running external commands
     * @since 1.11
     */
    public abstract FrameworkCommandSupport getFrameworkCommandSupport(PhpModule phpModule);

    /**
     * Get a {@link EditorExtender editor extender} for this framework
     * and the given PHP module.
     *
     * @param  phpModule the PHP module for which editor extender is to be gotten
     * @return editor extender, can be <code>null</code> if the framework doesn't provide
     *         any additional fields/classes etx. to code completion etc.
     * @since 1.13
     */
    public abstract EditorExtender getEditorExtender(PhpModule phpModule);
}
