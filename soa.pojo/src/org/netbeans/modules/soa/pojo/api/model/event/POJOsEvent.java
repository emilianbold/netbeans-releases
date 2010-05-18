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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.soa.pojo.api.model.event;

/**
 *
 * @author gpatil
 */
public interface POJOsEvent {
    public enum POJOsEventType {
        EVENT_POJO_ADDED,
        EVENT_POJO_CHANGED,
        EVENT_POJO_DELETED,
        EVENT_JBI_REF_ADDED,
        EVENT_JBI_REF_CHANGED,
        EVENT_JBI_REF_DELETED,
        EVENT_CONFIG_CHANGED,
        EVENT_CFG_FILE_EDITED
    } ;

    public POJOsEventType getEventType();
    public Object getSource();
    public Object getOldValue();
    public Object getNewValue();
}
