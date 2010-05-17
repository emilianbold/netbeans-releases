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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.editor.hints;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.python.editor.PythonAstUtils;
import org.netbeans.modules.python.editor.lexer.PythonLexerUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.HintFix;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.RuleContext;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.alias;

/**
 * Handle deprecaton warnings, for modules listed as obsolete or
 * deprecated in PEP4:
 *   http://www.python.org/dev/peps/pep-0004/
 *
 * Todo: Add a hint to enforce this from PEP8:
- Comparisons to singletons like None should always be done with
'is' or 'is not', never the equality operators.
 *  In general, see the "Programming Recommendations" list from
 *    http://www.python.org/dev/peps/pep-0008/ - there are lots
 *    of thins to check from there.  Check the PyLint list as well.
 *
 *
 * @author Tor Norbye
 */
public class Deprecations extends PythonAstRule {
    private static final Map<String, String> deprecated = new HashMap<String, String>();


    static {
        for (String module : new String[]{"cl", "sv", "timing"}) {
            deprecated.put(module, "Listed as obsolete in the library documentation");
        }

        for (String module : new String[]{
                    "addpack", "cmp", "cmpcache", "codehack", "dircmp", "dump", "find", "fmt",
                    "grep", "lockfile", "newdir", "ni", "packmail", "Para", "poly",
                    "rand", "reconvert", "regex", "regsub", "statcache", "tb", "tzparse",
                    "util", "whatsound", "whrandom", "zmod"}) {
            deprecated.put(module, "Obsolete module, removed in Python 2.5");

        }

        for (String module : new String[]{"gopherlib", "rgbimg", "macfs"}) {
            deprecated.put(module, "Obsolete module, removed in Python 2.6");
        }

        /*
        al.rst:    The :mod:`al` module has been deprecated for removal in Python 3.0.
        al.rst:   The :mod:`AL` module has been deprecated for removal in Python 3.0.
        bsddb.rst:    The :mod:`bsddb` module has been deprecated for removal in Python 3.0.
        cd.rst:    The :mod:`cd` module has been deprecated for removal in Python 3.0.
        dbhash.rst:    The :mod:`dbhash` module has been deprecated for removal in Python 3.0.
        fl.rst:    The :mod:`fl` module has been deprecated for removal in Python 3.0.
        fl.rst:    The :mod:`FL` module has been deprecated for removal in Python 3.0.
        fl.rst:    The :mod:`flp` module has been deprecated for removal in Python 3.0.
        fm.rst:   The :mod:`fm` module has been deprecated for removal in Python 3.0.
        gl.rst:    The :mod:`gl` module has been deprecated for removal in Python 3.0.
        gl.rst:    The :mod:`DEVICE` module has been deprecated for removal in Python 3.0.
        gl.rst:    The :mod:`GL` module has been deprecated for removal in Python 3.0.
        imgfile.rst:   The :mod:`imgfile` module has been deprecated for removal in Python 3.0.
        jpeg.rst:   The :mod:`jpeg` module has been deprecated for removal in Python 3.0.
        statvfs.rst:   The :mod:`statvfs` module has been deprecated for removal in Python 3.0.
        sunaudio.rst:   The :mod:`sunaudiodev` module has been deprecated for removal in Python 3.0.
        sunaudio.rst:   The :mod:`SUNAUDIODEV` module has been deprecated for removal in Python 3.0.
        tarfile.rst:      The :class:`TarFileCompat` class has been deprecated for removal in Python 3.0.
         */

        deprecated.put("posixfile",
                "Locking is better done by fcntl.lockf().");

        deprecated.put("gopherlib",
                "The gopher protocol is not in active use anymore.");

        deprecated.put("rgbimgmodule",
                "");

        deprecated.put("pre",
                "The underlying PCRE engine doesn't support Unicode, and has been unmaintained since Python 1.5.2.");

        deprecated.put("whrandom",
                "The module's default seed computation was inherently insecure; the random module should be used instead.");

        deprecated.put("rfc822",
                "Supplanted by Python 2.2's email package.");

        deprecated.put("mimetools",
                "Supplanted by Python 2.2's email package.");

        deprecated.put("MimeWriter",
                "Supplanted by Python 2.2's email package.");

        deprecated.put("mimify",
                "Supplanted by Python 2.2's email package.");

        deprecated.put("rotor",
                "Uses insecure algorithm.");

        deprecated.put("TERMIOS.py",
                "The constants in this file are now in the 'termios' module.");

        deprecated.put("statcache",
                "Using the cache can be fragile and error-prone; applications should just use os.stat() directly.");

        deprecated.put("mpz",
                "Third-party packages provide similiar features and wrap more of GMP's API.");


        deprecated.put("xreadlines",
                "Using 'for line in file', introduced in 2.3, is preferable.");

        deprecated.put("multifile",
                "Supplanted by the email package.");

        deprecated.put("sets",
                "The built-in set/frozenset types, introduced in Python 2.4, supplant the module.");

        deprecated.put("buildtools",
                "");

        deprecated.put("cfmfile",
                "");

        deprecated.put("macfs",
                "");

        deprecated.put("md5",
                "Replaced by the 'hashlib' module.");

        deprecated.put("sha",
                "Replaced by the 'hashlib' module.");
    }

    public static boolean isDeprecatedModule(String module) {
        return deprecated.containsKey(module);
    }

    @Override
    public Set<Class> getKinds() {
        HashSet<Class> kinds = new HashSet<Class>();
        kinds.add(Import.class);
        kinds.add(ImportFrom.class);

        return kinds;
    }

    @Override
    public void run(PythonRuleContext context, List<Hint> result) {
        PythonTree node = context.node;
        if (node instanceof Import) {
            Import imp = (Import)node;
            List<alias> names = imp.getInternalNames();
            if (names != null) {
                for (alias alias : names) {
                    String name = alias.getInternalName();
                    if (deprecated.containsKey(name)) {
                        addDeprecation(name, deprecated.get(name), context, result);
                    }
                }
            }
        } else {
            assert node instanceof ImportFrom;
            ImportFrom imp = (ImportFrom)node;
            String name = imp.getInternalModule();
            if (deprecated.containsKey(name)) {
                addDeprecation(name, deprecated.get(name), context, result);
            }
        }
    }

    private void addDeprecation(String module, String rationale, PythonRuleContext context, List<Hint> result) {
        CompilationInfo info = context.compilationInfo;
        OffsetRange astOffsets = PythonAstUtils.getNameRange(info, context.node);
        OffsetRange lexOffsets = PythonLexerUtils.getLexerOffsets(info, astOffsets);
        BaseDocument doc = context.doc;
        try {
            if (lexOffsets != OffsetRange.NONE && lexOffsets.getStart() < doc.getLength() &&
                    (context.caretOffset == -1 ||
                    Utilities.getRowStart(doc, context.caretOffset) == Utilities.getRowStart(doc, lexOffsets.getStart()))) {
                List<HintFix> fixList = Collections.emptyList();
                String displayName;
                if (rationale.length() > 0) {
                    displayName = NbBundle.getMessage(Deprecations.class, "DeprecationsMsgDetail", module, rationale);
                } else {
                    displayName = NbBundle.getMessage(Deprecations.class, "DeprecationsMsg", module);
                }
                Hint desc = new Hint(this, displayName, info.getFileObject(), lexOffsets, fixList, 1500);
                result.add(desc);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public String getId() {
        return "Deprecations"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(Deprecations.class, "Deprecations");
    }

    public String getDescription() {
        return NbBundle.getMessage(Deprecations.class, "DeprecationsDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }
}
