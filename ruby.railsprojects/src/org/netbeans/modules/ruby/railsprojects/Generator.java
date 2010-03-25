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
package org.netbeans.modules.ruby.railsprojects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;


/**
 * @todo PLUGIN has a --with-generator flag - how do I support that?
 *
 * @author Tor Norbye
 */
public class Generator {
    public static Generator NONE = new Generator("NONE", null, 0); //NO18N
    public static Generator CONTROLLER = new Generator("controller", null, null, "Views", null, 1); // NOI18N
    public static Generator INTEGRATION_TEST = new Generator("integration_test", null, 1); // NOI18N
    public static Generator MAILER = new Generator("mailer", null, null, "Views", null, 1); // NOI18N
    public static Generator MIGRATION = new Generator("migration", null, 1); // NOI18N
    public static Generator MODEL = new Generator("model", null, 1); // NOI18N
    public static Generator METAL = new Generator("metal", null, 1); // NOI18N
    public static Generator PLUGIN = new Generator("plugin", null, 1); // NOI18N
    public static Generator SCAFFOLD_ONE =
        new Generator("scaffold", null, "ModelName", "ScaffControllerName", "ScaffoldActions", 1); // NOI18N
    public static Generator SCAFFOLD_TWO =
        new Generator("scaffold", null, "ModelName", "ScaffoldAttrs", null, 1); // NOI18N
    public static Generator SESSION_MIGRATION = new Generator("session_migration", null, 1); // NOI18N
    public static Generator WEB_SERVICE =
        new Generator("web_service", null, null, "ApiMethods", null, 1); // NOI18N

    private final String name;
    private FileObject location;
    private final String nameKey;
    private final String arg1Key;
    private final String arg2Key;
    private final int argsRequired;

    /**
     * Creates a new Generator.
     *
     * @param name the name of the generator; must not be <code>null</code>.
     * @param location the location of the generator; may be <code>null</code>.
     * @param argsRequired the number of arguments required by the generator.
     */
    public Generator(String name, FileObject location, int argsRequired) {
        // Unknown meaning of the first argument
        this(name, location, "Arguments", null, null, argsRequired); //NOI18N
    }

    private Generator(String name, FileObject location, String nameKey, String arg1Key,
        String arg2Key, int argsRequired) {
        Parameters.notNull("name", name);
        this.argsRequired = argsRequired;
        this.name = name;
        this.location = location;
        this.nameKey = nameKey;
        this.arg1Key = arg1Key;
        this.arg2Key = arg2Key;
    }
    
    /**
     * Add in the "known" or builtin generators.
     * 
     * @param foundBuiltin the builtin generators found in the rails installation
     *  of the project.
     */
    static List<Generator> getBuiltinGenerators(String railsVersion, List<Generator> foundBuiltin) {
        boolean isRailsOne = railsVersion != null && railsVersion.startsWith("1."); // NOI18N
        List<Generator> list = new ArrayList<Generator>();
        list.add(CONTROLLER);
        list.add(INTEGRATION_TEST);
        list.add(MODEL);
        list.add(MIGRATION);
        list.add(MAILER);
        list.add(PLUGIN);
        if (isRailsOne) {
            list.add(SCAFFOLD_ONE);
        } else {
            list.add(SCAFFOLD_TWO);
        }
        list.add(SESSION_MIGRATION);
        if (isRailsOne) {
            list.add(WEB_SERVICE);
            // TODO - missing scaffold_resource!
        }
        
        return configureBuiltins(list, foundBuiltin);
    }

    private static List<Generator> configureBuiltins(List<Generator> builtin, List<Generator> foundBuiltin) {
        // sets the location for built-in generators so that the usage file is found, not pretty
        // but other approaches
        // would require rather extensive changes.
        for (Generator found : foundBuiltin) {
            boolean match = false;
            for (Generator preConfigured : builtin) {
                if (found.name.equals(preConfigured.name)) {
                    preConfigured.location = found.location;
                    match = true;
                }
            }
            if (!match) {
                builtin.add(found);
            }
        }
        return builtin;
    }


    public int getArgsRequired() {
        return argsRequired;
    }

    public String getName() {
        return name;
    }

    public FileObject getDir() {
        return location;
    }

    public String getUsage(Project project) {
        if (this == NONE) {
            return null;
        }

        File generatorDir = null;

        RubyPlatform platform = RubyPlatform.platformFor(project);
        if (location == null) {
            // Lazy evaluation for the builtin generators
            // Generator dir
            File gemLocation = null;

            FileObject railsInstall = project.getProjectDirectory().getFileObject("vendor/rails/railties"); // NOI18N
            if (railsInstall != null) {
                gemLocation = FileUtil.toFile(railsInstall);
            } else {
                String version = platform.getGemManager().getLatestVersion("rails"); // NOI18N
                if (version != null) {
                    gemLocation =
                        new File(platform.getGemManager().getGemHome() + File.separator +
                            "gems" + File.separator + "rails" + "-" + version); // NOI18N
                } else if (!Utilities.isWindows()) {
                    // XXX This is suspicious
                    File rubyHome = platform.getHome();
                    if (rubyHome != null) {
                        File railsDir = new File(rubyHome, "/share/rails/railties"); // NOI18N
                        if (railsDir.exists()) {
                            gemLocation = railsDir;
                        }
                    }
                }
            }

            if (gemLocation != null) {
                //Example: jruby-0.9.2/lib/ruby/gems/1.8/gems/rails-1.1.6/lib/rails_generator/generators/components       
                generatorDir = new File(gemLocation,
                        "lib" + File.separator + // NOI18N
                        "rails_generator" + File.separator + "generators" + File.separator + // NOI18N
                        "components" + File.separator + name); // NOI18N
            }
        } else {
            generatorDir = FileUtil.toFile(location);
        }

        File usageFile = findUsageFile(generatorDir);
        return usageFile != null ? RailsProjectUtil.asText(usageFile) : null;
    }

    private File findUsageFile(File generatorDir) {
        final String[] filesToTry = {"USAGE",
            // At least the "resource" generator on railties seems to live
            // in the "wrong" place; check the additional location
            "templates" + File.separator + "USAGE",
            // e.g. haml_scaffold doesn't ship with a usage file, try README.rdoc instead
            "README.rdoc",
            // finally try just README
            "README"
        };

        List<File> dirsToTry = new ArrayList<File>(3);
        dirsToTry.add(generatorDir);

        // look at parents as well for usage/readme. a typical structure of
        // gem generators is <gem_home>/my-gem-generator/generators/my-gem-generator.rb,
        // so we're looking for <gem_home>/my-gem-generator/generators/README and
        // <gem_home>/my-gem-generator/README etc.
        File parent = generatorDir.getParentFile();
        if (parent.exists() && "generators".equals(parent.getName())) {
            dirsToTry.add(parent);
            File grandParent = parent.getParentFile();
            if (grandParent.exists()) {
                dirsToTry.add(grandParent);
            }
        }

        for (File dir : dirsToTry) {
            for (String file : filesToTry) {
                File result = new File(dir, file);
                if (result.exists()) {
                    return result;
                }
            }
        }
        return null;
    }

    String getNameLabel() {
        if (nameKey != null) {
            return NbBundle.getMessage(Generator.class, nameKey);
        } else {
            return NbBundle.getMessage(Generator.class, "Name");
        }
    }

    String getArg1Label() {
        if (arg1Key != null) {
            return NbBundle.getMessage(Generator.class, arg1Key);
        } else {
            return null;
        }
    }

    String getArg2Label() {
        if (arg2Key != null) {
            return NbBundle.getMessage(Generator.class, arg2Key);
        } else {
            return null;
        }
    }

    static final class Script {

        final String script;
        final List<String> args = new ArrayList<String>();

        public Script(String script) {
            this.script = script;
        }

        Script addArgs(String... argsToAdd) {
            if (argsToAdd == null) {
                return this;
            }
            for (String each : argsToAdd) {
                args.add(each);
            }
            return this;
        }
    }
}
