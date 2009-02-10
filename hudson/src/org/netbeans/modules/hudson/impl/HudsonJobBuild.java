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

package org.netbeans.modules.hudson.impl;

import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.constants.HudsonXmlApiConstants;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import static org.netbeans.modules.hudson.constants.HudsonJobBuildConstants.*;
import org.netbeans.modules.hudson.util.HudsonPropertiesSupport;
import org.openide.util.Lookup;
import org.w3c.dom.Document;

/**
 * Information about one build of a job.
 */
public class HudsonJobBuild {

    private final HudsonConnector connector;
    private final HudsonJob job;
    private final int build;

    HudsonJobBuild(HudsonConnector connector, HudsonJob job, int build) {
        this.connector = connector;
        this.job = job;
        this.build = build;
    }
    
    public enum Result {
        SUCCESS, FAILURE
    }
    
    private final HudsonPropertiesSupport properties = new HudsonPropertiesSupport();
    
    public void putProperty(String name, Object o) {
        properties.putProperty(name, o);
    }
    
    public boolean isBuilding() {
        Boolean building = properties.getProperty(JOB_BUILD_BUILDING, Boolean.class);
        return building != null ? building : false;
    }
    
    public int getDuration() {
        return (int) (properties.getProperty(JOB_BUILD_DURATION, Long.class) / 60000);
    }
    
    public Date getDate() {
        return new Date(properties.getProperty(JOB_BUILD_TIMESTAMP, Long.class));
    }
    
    public Result getResult() {
        return properties.getProperty(JOB_BUILD_RESULT, Result.class);
    }

    private Collection<? extends HudsonJobChangeItem> changes;
    /**
     * Gets a changelog for the build.
     * This requires SCM-specific parsing using {@link HudsonSCM#parseChangeSet}.
     * @return a list of changes, possibly empty (including if it could not be parsed)
     */
    public Collection<? extends HudsonJobChangeItem> getChanges() {
        if (changes == null) {
            Document changeSet = connector.getDocument(job.getUrl() + build + "/" +
                    HudsonXmlApiConstants.XML_API_URL + "?xpath=/*/changeSet");
            if (changeSet != null) {
                for (HudsonSCM scm : Lookup.getDefault().lookupAll(HudsonSCM.class)) {
                    changes = scm.parseChangeSet(changeSet.getDocumentElement());
                    if (changes != null) {
                        break;
                    }
                }
            }
            if (changes == null) {
                changes = Collections.emptyList();
            }
        }
        return changes;
    }
    
}
