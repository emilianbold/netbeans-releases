/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.netbeans.modules.jmx.common.WizardConstants;

/**
 *
 * @author tl156378
 */
public class MBeanDO {
    private List<MBeanAttribute> attributes;
    private List<MBeanOperation> operations;
    private List<MBeanNotification> notifs;

    private String name;
    private String packageName;
    private String description;
    private String type;
    private String wrappedClassName;
    private boolean isWrapppedClass = false;
    private boolean needDateImport = false;
    private boolean needObjectNameImport = false;
    private boolean notificationEmitter = false;
    private boolean implMBeanRegist = false;
    private boolean keepPreRegistRef = false;
    private boolean genBroadcastDeleg = false;
    private boolean genSeqNumber = false;
    private boolean isWrapppedClassMXBean;
    
    private DataFolder folder;
    private DataObject template;
    
    public MBeanDO(List attributes, List operations) {
        this.attributes = attributes;
        Collections.sort(this.attributes);
        this.operations = operations;
        Collections.sort(this.operations);
        this.notifs = new ArrayList<MBeanNotification>();
    }
    
    public MBeanDO() {
        this.attributes = new ArrayList<MBeanAttribute>();
        this.operations = new ArrayList<MBeanOperation>();
        this.notifs = new ArrayList<MBeanNotification>();
    }
    
    public boolean isExtendedStandardMBean() {
        return isWrapppedClass() || 
                getType().equals(WizardConstants.MBEAN_EXTENDED);
    }
    
    public List<MBeanAttribute> getAttributes() {
        return attributes;
    }
    
    public List<MBeanOperation> getOperations() {
        return operations;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List getNotifs() {
        return notifs;
    }

    public void setNotifs(List notifs) {
        this.notifs = notifs;
    }

    public void setAttributes(List<MBeanAttribute> attributes) {
        this.attributes = attributes;
        Collections.sort(this.attributes);
    }

    public void setOperations(List<MBeanOperation> operations) {
        this.operations = operations;
        Collections.sort(this.operations);
    }

    public DataFolder getDataFolder() {
        return folder;
    }

    public void setDataFolder(DataFolder folder) {
        this.folder = folder;
    }

    public DataObject getTemplate() {
        return template;
    }

    public void setTemplate(DataObject template) {
        this.template = template;
    }

    public boolean isNotificationEmitter() {
        return notificationEmitter;
    }

    public void setNotificationEmitter(boolean notificationEmitter) {
        this.notificationEmitter = notificationEmitter;
    }

    public boolean implMBeanRegist() {
        return implMBeanRegist;
    }

    public void setImplMBeanRegist(boolean implMBeanRegist) {
        this.implMBeanRegist = implMBeanRegist;
    }

    public boolean isGenBroadcastDeleg() {
        return genBroadcastDeleg;
    }

    public void setGenBroadcastDeleg(boolean genBroadcastDeleg) {
        this.genBroadcastDeleg = genBroadcastDeleg;
    }

    public boolean isGenSeqNumber() {
        return genSeqNumber;
    }

    public void setGenSeqNumber(boolean genSeqNumber) {
        this.genSeqNumber = genSeqNumber;
    }

    public boolean isKeepPreRegistRef() {
        return keepPreRegistRef;
    }

    public void setKeepPreRegistRef(boolean keepPreRegistRef) {
        this.keepPreRegistRef = keepPreRegistRef;
    }

    public String getWrappedClassName() {
        return wrappedClassName;
    }

    public void setWrappedClassName(String wrappedClassName) {
        this.wrappedClassName = wrappedClassName;
    }

    public boolean isWrapppedClass() {
        return isWrapppedClass;
    }
    public void setWrapppedClass(boolean isWrapppedClass) {
        this.isWrapppedClass = isWrapppedClass;
    }
    public void setWrapppedClassIsMXBean(boolean isWrapppedClassMXBean) {
        this.isWrapppedClassMXBean = isWrapppedClassMXBean;
    }
    public boolean isWrapppedClassMXBean() {
        return isWrapppedClassMXBean;
    }
        
}
