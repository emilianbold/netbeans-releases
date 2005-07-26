/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx;

import java.util.ArrayList;
import java.util.List;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author tl156378
 */
public class MBeanDO {
    private List/*<MBeanAttribute>*/ attributes;
    private List/*<MBeanOperation>*/ operations;
    private List/*<MBeanNotification>*/ notifs;
    
    private String name;
    private String packageName;
    private String description;
    private String type;
    private boolean needDateImport = false;
    private boolean needObjectNameImport = false;
    private boolean notificationEmitter = false;
    private boolean implMBeanRegist = false;
    private boolean keepPreRegistRef = false;
    private boolean genBroadcastDeleg = false;
    private boolean genSeqNumber = false;
    
    private DataFolder folder;
    private DataObject template;
    
    public MBeanDO(List attributes, List operations) {
        this.attributes = attributes;
        this.operations = operations;
        this.notifs = new ArrayList();
    }
    
    public MBeanDO() {
        this.attributes = new ArrayList();
        this.operations = new ArrayList();
        this.notifs = new ArrayList();
    }
    
    public List getAttributes() {
        return attributes;
    }
    
    public List getOperations() {
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

    public void setAttributes(List attributes) {
        this.attributes = attributes;
    }

    public void setOperations(List operations) {
        this.operations = operations;
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
}
