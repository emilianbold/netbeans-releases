/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.embedder;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.versioning.VersionRange;

/**
 *
 * @author mkleint
 */
public class MyResolutionListener implements ResolutionListener {
    
    private static ThreadLocal<ResolutionListener> listener = new ThreadLocal<ResolutionListener>();
    /** Creates a new instance of MyResolutionListener */
    public MyResolutionListener() {
    }
    
    public static void setDelegateResolutionListener(ResolutionListener listen) {
        listener.set(listen);
    }
    
    public static void clearDelegateResolutionListener() {
        // 1.5 equivalent is remove()
        listener.set(null);
    }
    
    private ResolutionListener getDelegate() {
        return listener.get();
    }
    
    public void  testArtifact(Artifact node) {
        if (getDelegate() != null) {
            getDelegate().testArtifact(node);
        }
//        System.out.println("testArtifact" + node);
    }

    public void  startProcessChildren( Artifact artifact ) {
        if (getDelegate() != null) {
            getDelegate().startProcessChildren(artifact);
        }
//        System.out.println("startProcessChildren" + artifact);
    }

    public void  endProcessChildren( Artifact artifact ){
        if (getDelegate() != null) {
            getDelegate().endProcessChildren(artifact);
        }
//        System.out.println("endProcessChildren" + artifact);
    }

    public void  includeArtifact( Artifact artifact ){
        if (getDelegate() != null) {
            getDelegate().includeArtifact(artifact);
        }
//        System.out.println("includeArtifact" + artifact);
    }

    public void  omitForNearer( Artifact omitted, Artifact kept ) {
        if (getDelegate() != null) {
            getDelegate().omitForNearer(omitted, kept);
        }
//        System.out.println("omitted.. kept" + kept);
    }

    public void  updateScope( Artifact artifact, String scope ){
        if (getDelegate() != null) {
            getDelegate().updateScope(artifact, scope);
        }
//        System.out.println("update scope");
    }

    public void  manageArtifact( Artifact artifact, Artifact replacement ){
        if (getDelegate() != null) {
            getDelegate().manageArtifact(artifact, replacement);
        }
//        System.out.println("MANAGE Artifact=" + artifact + " replacement=" + replacement);
    }

    public void  omitForCycle( Artifact artifact ){
        if (getDelegate() != null) {
            getDelegate().omitForCycle(artifact);
        }
//        System.out.println("omit cycle" + artifact);
    }

    public void  updateScopeCurrentPom( Artifact artifact, String scope ){
        if (getDelegate() != null) {
            getDelegate().updateScopeCurrentPom(artifact, scope);
        }
//        System.out.println("update scope");
    }

    public void  selectVersionFromRange( Artifact artifact ){
        if (getDelegate() != null) {
            getDelegate().selectVersionFromRange(artifact);
        }
//        System.out.println("select version");
    }

    public void  restrictRange( Artifact artifact, Artifact replacement, VersionRange newRange ){
        if (getDelegate() != null) {
            getDelegate().restrictRange(artifact, replacement, newRange);
        }
//        System.out.println("restrict range");
    }

    
}
