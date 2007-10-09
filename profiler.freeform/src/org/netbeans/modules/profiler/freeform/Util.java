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

package org.netbeans.modules.profiler.freeform;

import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.netbeans.api.project.Project;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.event.ChangeListener;


/**
 * Miscellaneous utilities.
 * (Ian Formanek: Copied from FreeForm module)
 *
 * @author Jesse Glick
 */
public final class Util {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static final class ErrH implements ErrorHandler {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public ErrH() {
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void error(final SAXParseException exception)
                   throws SAXException {
            throw exception;
        }

        public void fatalError(final SAXParseException exception)
                        throws SAXException {
            throw exception;
        }

        public void warning(final SAXParseException exception)
                     throws SAXException {
            // ignore that
        }
    }

    private static final class TrivialAntProjectCookie implements AntProjectCookie.ParseStatus {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private final Document doc;
        private final FileObject fo;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public TrivialAntProjectCookie(final FileObject fo, final Document doc) {
            this.fo = fo;
            this.doc = doc;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public Document getDocument() {
            return doc;
        }

        public File getFile() {
            return FileUtil.toFile(fo);
        }

        public FileObject getFileObject() {
            return fo;
        }

        public Throwable getParseException() {
            return null;
        }

        public boolean isParsed() {
            return true;
        }

        public Element getProjectElement() {
            return doc.getDocumentElement();
        }

        public void addChangeListener(final ChangeListener l) {
        }

        public void removeChangeListener(final ChangeListener l) {
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String PARSE_ERROR_MSG = NbBundle.getMessage(Util.class, "Util_ParseErrorMsg"); // NOI18N
                                                                                                         // -----
    public static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.profiler.freeform"); // NOI18N

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    private Util() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Returns name of the Ant script represented by the given file object.
     *
     * @param fo Ant script which name should be returned
     * @return name of the Ant script as specified in name attribute of
     *         project element or null if fo does not represent valid Ant script
     *         or the script is anonymous
     */
    public static String getAntScriptName(final FileObject fo) {
        final AntProjectCookie apc = getAntProjectCookie(fo);

        if (apc == null) {
            return null;
        }

        final Element projEl = apc.getProjectElement();

        if (projEl == null) {
            return null;
        }

        final String name = projEl.getAttribute("name"); // NOI18N
                                                         // returns "" if no such attribute

        return (name.length() > 0) ? name : null;
    }

    /**
     * Returns XML element representing the requested target or null if the target does not exist.
     *
     * @param fo         Ant script which target names should be returned
     * @param targetName the String name of the requested target
     * @return XML element representing the requested target or null if the target does not exist.
     */
    public static Element getAntScriptTarget(final FileObject fo, final String targetName) {
        if (fo == null) {
            throw new IllegalArgumentException("Cannot call Util.getAntScriptTargetNames with null"); // NOI18N
        }

        final AntProjectCookie apc = getAntProjectCookie(fo);

        if (apc == null) {
            return null;
        }

        final Set /*TargetLister.Target*/ allTargets;

        try {
            allTargets = TargetLister.getTargets(apc);
        } catch (IOException e) {
            err.notify(ErrorManager.INFORMATIONAL, e);

            return null;
        }

        final Iterator it = allTargets.iterator();

        while (it.hasNext()) {
            final TargetLister.Target target = (TargetLister.Target) it.next();

            if (target.isOverridden()) {
                // Cannot call it directly.
                continue;
            }

            if (target.isInternal()) {
                // Should not be called from outside.
                continue;
            }

            if (targetName.equals(target.getName())) {
                return target.getElement();
            }
        }

        return null;
    }

    /**
     * Returns sorted list of targets name of the Ant script represented by the
     * given file object.
     *
     * @param fo Ant script which target names should be returned
     * @return sorted list of target names or null if fo does not represent
     *         valid Ant script
     */
    public static List /*<String>*/ getAntScriptTargetNames(final FileObject fo) {
        final List targets = getAntScriptTargets(fo);
        final SortedSet targetNames = new TreeSet(Collator.getInstance());
        final Iterator it = targets.iterator();

        while (it.hasNext()) {
            targetNames.add(((TargetLister.Target) it.next()).getName());
        }

        return new ArrayList(targetNames);
    }

    public static List /*TargetLister.Target*/ getAntScriptTargets(final FileObject buildScript) {
        if (buildScript == null) {
            throw new IllegalArgumentException("Cannot call Util.getAntScriptTargetNames with null"); // NOI18N
        }

        final AntProjectCookie apc = getAntProjectCookie(buildScript);

        if (apc == null) {
            return null;
        }

        final Set /*TargetLister.Target*/ allTargets;

        try {
            allTargets = TargetLister.getTargets(apc);
        } catch (IOException e) {
            err.notify(ErrorManager.INFORMATIONAL, e);

            return null;
        }

        final ArrayList targets = new ArrayList();
        final Iterator it = allTargets.iterator();

        while (it.hasNext()) {
            final TargetLister.Target target = (TargetLister.Target) it.next();

            if (target.isOverridden()) {
                // Cannot call it directly.
                continue;
            }

            if (target.isInternal()) {
                // Should not be called from outside.
                continue;
            }

            targets.add(target);
        }

        return targets;
    }

    public static FileObject getProjectBuildScript(final Project project) {
        return org.netbeans.modules.ant.freeform.spi.support.Util.getDefaultAntScript(project);
    }

    private static AntProjectCookie getAntProjectCookie(final FileObject fo) {
        final DataObject dob;

        try {
            dob = DataObject.find(fo);
        } catch (DataObjectNotFoundException ex) {
            Util.err.notify(ErrorManager.INFORMATIONAL, ex);

            return null;
        }

        AntProjectCookie apc = (AntProjectCookie) dob.getCookie(AntProjectCookie.class);

        if ((apc == null) && fo.getMIMEType().equals("text/xml")) { // NOI18N
                                                                    // Some file that *could* be an Ant script and just wasn't recognized
                                                                    // as such? Cf. also TargetLister.getAntProjectCookie, which has the
                                                                    // advantage of being inside the Ant module and therefore able to
                                                                    // directly instantiate AntProjectSupport.

            try {
                apc = forceParse(fo);
            } catch (IOException e) {
                err.notify(ErrorManager.INFORMATIONAL, e);
            } catch (SAXException e) {
                err.log(MessageFormat.format(PARSE_ERROR_MSG, new Object[] { fo, e }));
            }
        }

        return apc;
    }

    /**
     * Try to parse a (presumably XML) file even though it is not known to be an Ant script.
     */
    private static AntProjectCookie forceParse(final FileObject fo)
                                        throws IOException, SAXException {
        final Document doc = XMLUtil.parse(new InputSource(fo.getURL().toExternalForm()), false, true, new ErrH(), null);

        return new TrivialAntProjectCookie(fo, doc);
    }
}
