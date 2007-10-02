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
package org.netbeans.modules.ruby.rubyproject.classpath;

import java.beans.PropertyChangeEvent;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.spi.gsfpath.classpath.ClassPathImplementation;
import org.netbeans.spi.gsfpath.classpath.PathResourceImplementation;
import org.netbeans.spi.gsfpath.classpath.support.ClassPathSupport;
import org.netbeans.api.gsfpath.classpath.ClassPath;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.openide.util.WeakListeners;

final class BootClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {

//    private static final String PLATFORM_ACTIVE = "platform.active";        //NOI18N
//    private static final String ANT_NAME = "platform.ant.name";             //NOI18N
//    private static final String J2SE = "j2se";                              //NOI18N

    private final PropertyEvaluator evaluator;
//    private JavaPlatformManager platformManager;
    //name of project active platform
    private String activePlatformName;
    //active platform is valid (not broken reference)
    private boolean isActivePlatformValid;
    private List<PathResourceImplementation> resourcesCache;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public BootClassPathImplementation(PropertyEvaluator evaluator) {
        if (evaluator != null) {
            assert evaluator != null;
            this.evaluator = evaluator;
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        } else {
            this.evaluator = null;
        }
    }

    public synchronized List<PathResourceImplementation> getResources() {
        if (this.resourcesCache == null) {
//            JavaPlatform jp = findActivePlatform ();
//            if (jp != null) {
                //TODO: May also listen on CP, but from Platform it should be fixed.
                List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
//                for (ClassPath.Entry entry : jp.getBootstrapLibraries().entries()) {
                for (ClassPath.Entry entry : RubyInstallation.getInstance().getClassPathEntries()) {                
                    result.add(ClassPathSupport.createResource(entry.getURL()));
                }

                resourcesCache = Collections.unmodifiableList (result);
                RubyInstallation.getInstance().removePropertyChangeListener(this);
                RubyInstallation.getInstance().addPropertyChangeListener(this);
//            }
//            else {
//                resourcesCache = Collections.emptyList();
//            }
        }
        return this.resourcesCache;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

//    private JavaPlatform findActivePlatform () {
//        if (this.platformManager == null) {
//            this.platformManager = JavaPlatformManager.getDefault();
//            this.platformManager.addPropertyChangeListener(WeakListeners.propertyChange(this, this.platformManager));
//        }                
//        this.activePlatformName = evaluator.getProperty(PLATFORM_ACTIVE);
//        final JavaPlatform activePlatform = RubyProjectUtil.getActivePlatform (this.activePlatformName);
//        this.isActivePlatformValid = activePlatform != null;
//        return activePlatform;
//    }
//    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == RubyInstallation.getInstance() && evt.getPropertyName().equals("roots")) {
            resetCache();
        }
//        if (evt.getSource() == this.evaluator && evt.getPropertyName().equals(PLATFORM_ACTIVE)) {
//            //Active platform was changed
//            resetCache ();
//        }
//        else if (evt.getSource() == this.platformManager && JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(evt.getPropertyName()) && activePlatformName != null) {
//            //Platform definitions were changed, check if the platform was not resolved or deleted
//            if (this.isActivePlatformValid) {
//                if (RubyProjectUtil.getActivePlatform (this.activePlatformName) == null) {
//                    //the platform was not removed
//                    this.resetCache();
//                }
//            }
//            else {
//                if (RubyProjectUtil.getActivePlatform (this.activePlatformName) != null) {
//                    this.resetCache();
//                }
//            }
//        }
    }
    
    /**
     * Resets the cache and firesPropertyChange
     */
    private void resetCache () {
        synchronized (this) {
            resourcesCache = null;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }
    
}
