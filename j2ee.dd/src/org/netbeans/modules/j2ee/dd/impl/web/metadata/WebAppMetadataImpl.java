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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.j2ee.dd.impl.web.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.web.AbsoluteOrdering;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.RelativeOrdering;
import org.netbeans.modules.j2ee.dd.api.web.RelativeOrderingItems;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.web.WebFragment;
import org.netbeans.modules.j2ee.dd.api.web.WebFragmentProvider;
import org.netbeans.modules.j2ee.dd.api.web.model.FilterInfo;
import org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo;
import org.netbeans.modules.j2ee.dd.impl.web.annotation.AnnotationHelpers;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

/**
 * Implementation of WebAppMetadata interface.
 * The logic for fragments and annotation scanning is here.
 * Also merging of data from deployment descriptors and annotations is here.
 * 
 * @author Petr Slechta
 */
public class WebAppMetadataImpl implements WebAppMetadata {

    private static final Logger LOG = Logger.getLogger(WebAppMetadataImpl.class.getName());
    private WebAppMetadataModelImpl modelImpl;
    private AnnotationHelpers annoHelpers;
    private MetadataUnit metadataUnit;
    private WebApp webXml;
    private long webXmlLastModification = -1L;
    private List<FragmentRec> fragmentRecs = new ArrayList<FragmentRec>();

    public WebAppMetadataImpl(MetadataUnit metadataUnit, WebAppMetadataModelImpl modelImpl) {
        this.metadataUnit = metadataUnit;
        this.modelImpl = modelImpl;
        refreshWebXml();
    }

    // -------------------------------------------------------------------------
    // INTERFACE IMPLEMENTATION
    // -------------------------------------------------------------------------
    public WebApp getRoot() {
        refreshWebXml();
        return webXml;
    }

    public List<WebFragment> getFragments() {
        refreshFragments();
        List<WebFragment> res = new ArrayList<WebFragment>();
        for (FragmentRec fr : fragmentRecs) {
            res.add(fr.fragment);
        }
        return sortFragments(webXml, res);
    }

    public List<FileObject> getFragmentFiles() {
        refreshFragments();
        List<FileObject> res = new ArrayList<FileObject>();
        for (FragmentRec fr : fragmentRecs) {
            res.add(fr.source);
        }
        return res;
    }

    public List<ServletInfo> getServlets() {
        return doMerging(MergeEngines.servletsEngine());
    }

    public List<FilterInfo> getFilters() {
        return doMerging(MergeEngines.filtersEngine());
    }

    public List<String> getSecurityRoles() {
        return doMerging(MergeEngines.securityRolesEngine());
    }

    public List<ResourceRef> getResourceRefs() {
        return doMerging(MergeEngines.resourceRefsEngine());
    }

    public List<ResourceEnvRef> getResourceEnvRefs() {
        return doMerging(MergeEngines.resourceEnvRefsEngine());
    }

    public List<EnvEntry> getEnvEntries() {
        return doMerging(MergeEngines.resourceEnvEntriesEngine());
    }

    public List<MessageDestinationRef> getMessageDestinationRefs() {
        return doMerging(MergeEngines.resourceMsgDestsEngine());
    }

    public List<ServiceRef> getServiceRefs() {
        return doMerging(MergeEngines.resourceServicesEngine());
    }

    public List<EjbLocalRef> getEjbLocalRefs() {
        return doMerging(MergeEngines.ejbLocalRefsEngine());
    }

    public List<EjbRef> getEjbRefs() {
        return doMerging(MergeEngines.ejbRefsEngine());
    }

    // -------------------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------------------
    private <T> List<T> doMerging(MergeEngine<T> eng) {
        eng.clean();

        // from web.xml
        refreshWebXml();
        if (webXml != null) {
            eng.addItems(webXml);
            boolean complete;
            try {
                complete = webXml.isMetadataComplete();
            }
            catch (VersionNotSupportedException ex) {
                // old version of DD, let's suppose it is complete
                complete = true;
            }
            if (complete)
                return eng.getResult();
        }

        // from web-fragment.xml files
        for (WebFragment wf : getFragments()) {
            eng.addItems(wf);
        }

        // from annotations
        eng.addAnnotations(getAnnotationHelpers());

        return eng.getResult();
    }

    private void refreshWebXml() {
        FileObject dd = metadataUnit.getDeploymentDescriptor();
        if (dd == null) {
            webXml = null;
            webXmlLastModification = -1L;
            return;
        }
        dd.refresh();
        long lastModif = dd.lastModified().getTime();
        if (lastModif > webXmlLastModification) {
            try {
                webXml = DDProvider.getDefault().getDDRoot(dd, false);
                webXmlLastModification = lastModif;
            }
            catch (IOException ex) {
                LOG.log(Level.SEVERE, "Error during web.xml parsing!", ex);
                webXml = null;
            }
        }
    }

    // -------------------------------------------------------------------------
    private void refreshFragments() {
        List<FragmentRec> res = new ArrayList<FragmentRec>();
        List<FileObject> frgs = metadataUnit.getCompilePath().findAllResources("META-INF/web-fragment.xml");
        for (FileObject fo : frgs) {
            FragmentRec oldRec = findFragmentRec(fo);
            fo.refresh();
            long lastModif = fo.lastModified().getTime();
            if (oldRec == null || lastModif > oldRec.lastModification) {
                try {
                    FragmentRec newRec = new FragmentRec();
                    newRec.source = fo;
                    newRec.lastModification = lastModif;
                    newRec.fragment = WebFragmentProvider.getDefault().getWebFragmentRoot(fo);
                    res.add(newRec);
                }
                catch (Exception ex) {
                    LOG.log(Level.SEVERE, "Error during web-fragment.xml parsing! File: "+fo, ex);
                }
            }
            else {
                res.add(oldRec);
            }
        }
        fragmentRecs = res;
    }

    private FragmentRec findFragmentRec(FileObject fo) {
        for (FragmentRec fr : fragmentRecs) {
            try {
                if (fr.source.getURL().equals(fo.getURL()))
                    return fr;
            }
            catch (FileStateInvalidException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    private AnnotationHelpers getAnnotationHelpers() {
        if (annoHelpers == null) {
            annoHelpers = new AnnotationHelpers(modelImpl.getHelper());
        }
        return annoHelpers;
    }

    // -------------------------------------------------------------------------
    static List<WebFragment> sortFragments(WebApp webXml, List<WebFragment> list) {
        assert list != null;

        AbsoluteOrdering[] absOrder = null;
        try {
            if (webXml != null)
                absOrder = webXml.getAbsoluteOrdering();
        }
        catch (VersionNotSupportedException e) {
            // ignore (if not supported then web.xml does not contain absolute ordering)
        }
        return  (absOrder == null) ? sortFragmentsRelatively(list) : sortFragmentsAbsolutely(webXml, list);
    }

    // -------------------------------------------------------------------------
    private static List<WebFragment> sortFragmentsRelatively(List<WebFragment> frags) {
        List<Constraint> constraints = extractConstraints(frags);
        List<Integer> others = extractOthers(frags, constraints);
        List<Integer> sorted = sort(constraints);
        List<WebFragment> res = new ArrayList<WebFragment>();
        for (int f : sorted) {
            if (f == OTHERS) {
                for (int o : others) {
                    res.add(frags.get(o));
                }
            }
            else {
                res.add(frags.get(f));
            }
        }
        return res;
    }

    private static final int OTHERS = -1;
    private static final int NOT_FOUND = -2;

    private static List<Constraint> extractConstraints(List<WebFragment> list) {
        List<Constraint> res = new ArrayList<Constraint>();
        int no = -1;
        for (WebFragment f : list) {
            no++;
            RelativeOrdering[] os = f.getOrdering();
            if (os == null)
                continue;
            for (RelativeOrdering o : os) {
                RelativeOrderingItems after = o.getAfter();
                if (after != null) {
                    for (int i=0,maxi=after.sizeName(); i<maxi; i++) {
                        int fragNo = findFragment(list, after.getName(i));
                        if (fragNo != NOT_FOUND)
                            res.add(new Constraint(fragNo, no));
                    }
                    if (after.getOthers() != null)
                        res.add(new Constraint(OTHERS, no));
                }
                RelativeOrderingItems before = o.getBefore();
                if (before != null) {
                    for (int i=0,maxi=before.sizeName(); i<maxi; i++) {
                        int fragNo = findFragment(list, before.getName(i));
                        if (fragNo != NOT_FOUND)
                            res.add(new Constraint(no, fragNo));
                    }
                    if (before.getOthers() != null)
                        res.add(new Constraint(no, OTHERS));
                }
            }
        }
        return res;
    }

    private static List<Integer> extractOthers(List<WebFragment> list, List<Constraint> constraints) {
        List<Integer> res = new ArrayList<Integer>();
        for (int i=0,maxi=list.size(); i<maxi; i++) {
            boolean referenced = false;
            for (Constraint c : constraints) {
                if (c.references(i)) {
                    referenced = true;
                    break;
                }
            }
            if (!referenced)
                res.add(i);
        }
        return res;
    }

    private static List<Integer> sort(List<Constraint> constraints) {
        List<Integer> res = new ArrayList<Integer>();
        while (!constraints.isEmpty()) {
            int item = -1;
            Constraint c = null;
            for (int i=0,maxi=constraints.size(); i<maxi; i++) {
                c = constraints.get(i);
                if (isReady(constraints, c.op1, i)) {
                    item = i;
                    break;
                }
            }
            if (item < 0)
                return null;  // Cannot sort (cycle?)

            constraints.remove(item);
            if (!res.contains(c.op1))
                res.add(c.op1);
            if (!isReferenced(constraints, c.op2))
                res.add(c.op2);
        }
        return res;
    }

    private static boolean isReady(List<Constraint> constraints, int f, int except) {
        for (int i=0,maxi=constraints.size(); i<maxi; i++) {
            if (i == except)
                continue;
            Constraint c = constraints.get(i);
            if (c.op2 == f)
                return false;
        }
        return true;
    }

    private static boolean isReferenced(List<Constraint> constraints, int f) {
        for (Constraint c : constraints) {
            if (c.op1 == f || c.op2 == f)
                return true;
        }
        return false;
    }

    private static int findFragment(List<WebFragment> list, String name) {
        int res = -1;
        for (WebFragment f : list) {
            res++;
            try {
                String[] names = f.getName();
                if (names != null) {
                    for (String n : names) {
                        if (n.equals(name))
                            return res;
                    }
                }
            }
            catch (VersionNotSupportedException e) {
                // ignore (if not supported then web fragment does not have a name)
            }
        }
        return NOT_FOUND;
    }

    // -------------------------------------------------------------------------
    private static List<WebFragment> sortFragmentsAbsolutely(WebApp webXml, List<WebFragment> list) {
        assert webXml != null;

        AbsoluteOrdering[] order = null;
        try {
            order = webXml.getAbsoluteOrdering();
        }
        catch (VersionNotSupportedException e) {
            // this should never happen!
            LOG.log(Level.SEVERE, "sortFragmentsAbsolutely failed", e);
            return null;
        }
        assert order != null;

        List<Integer> res = new ArrayList<Integer>();

        // TODO <others/> tag not supported right now due to problem with schema2beans
        // Hack is used <name>OTHERS</name> which is temporary and should be fixed!!
        // FIXME implement web fragment model to support properly <others/> tag!!
        for (AbsoluteOrdering o : order) {
            addFragmentsIntoResult(res, list, o.getName());
        }
        res = insertOthers(res, list);

        List<WebFragment> finalResult = new ArrayList<WebFragment>();
        for (int i : res) {
            finalResult.add(list.get(i));
        }
        return finalResult;
    }

    private static void addFragmentsIntoResult(List<Integer> res, List<WebFragment> list, String[] names) {
        if (names != null) {
            for (String name : names) {
                // FIXME: hack -- should be fixed when support for <others/> is implemented
                if (name.equals("<others>")) {
                    res.add(OTHERS);
                }
                else {
                    int x = findFragment(list, name);
                    if (x != NOT_FOUND)
                        res.add(x);
                }
            }
        }
    }

    private static List<Integer> insertOthers(List<Integer> res, List<WebFragment> list) {
        List<Integer> others = new ArrayList<Integer>();
        for (int i=0,maxi=list.size(); i<maxi; i++) {
            if (!res.contains(i))
                others.add(i);
        }
        List<Integer> finalResult = new ArrayList<Integer>();
        for (int i : res) {
            if (i == OTHERS) {
                finalResult.addAll(others);
            }
            else {
                finalResult.add(i);
            }
        }
        return finalResult;
    }

    // -------------------------------------------------------------------------
    // INNER CLASSES
    // -------------------------------------------------------------------------
    private static class FragmentRec {
        WebFragment fragment;
        long lastModification;
        FileObject source;
    }

    private static class Constraint {
        int op1;
        int op2;

        Constraint(int op1, int op2) {
            this.op1 = op1;
            this.op2 = op2;
        }

        boolean references(int no) {
            return op1 == no || op2 == no;
        }
    }

}
