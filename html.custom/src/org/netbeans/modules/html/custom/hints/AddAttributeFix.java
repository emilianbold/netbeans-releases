/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.custom.hints;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.html.custom.conf.Attribute;
import org.netbeans.modules.html.custom.conf.Configuration;
import org.netbeans.modules.html.custom.conf.Tag;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author marek
 */
@NbBundle.Messages(value = {
    "declareGlobalAttr=Declare global attribute \"{0}\"",
    "declareGlobalAttrs=Declare \"{0}\" attributes as global",
    "declareElementAttr=Declare \"{0}\" as attribute of element \"{1}\"",
    "declareElementAttrs=Declare \"{0}\" as attributes of element \"{1}\""})
public final class AddAttributeFix implements HintFix {
    private final Collection<String> attributeNames;
    private final String elementContextName;
    private final Snapshot snapshot;

    public AddAttributeFix(Collection<String> attributeNames, String elementContextName, Snapshot snapshot) {
        this.attributeNames = attributeNames;
        this.elementContextName = elementContextName;
        this.snapshot = snapshot;
    }
    
    public AddAttributeFix(String attributeName, String elementContextName, Snapshot snapshot) {
        this(Collections.singleton(attributeName), elementContextName, snapshot);
    }

    @Override
    public String getDescription() {
        if(elementContextName == null) {
            return attributeNames.size() == 1 
                    ? Bundle.declareGlobalAttr(getAttributeNamesList())
                    : Bundle.declareGlobalAttrs(getAttributeNamesList());
        } else {
            return attributeNames.size() == 1
                ? Bundle.declareElementAttr(getAttributeNamesList(), elementContextName)
                : Bundle.declareElementAttrs(getAttributeNamesList(), elementContextName);
        }
    }
    
    private String getAttributeNamesList() {
        StringBuilder sb = new StringBuilder();
        Iterator<String> i = attributeNames.iterator();
        while(i.hasNext()) {
            String aName = i.next();
            sb.append(aName);
            if(i.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public void implement() throws Exception {
        Configuration conf = Configuration.get(snapshot.getSource().getFileObject());
        
        if(elementContextName != null) {
            //attr in context
            for(String aName : attributeNames) {
                Tag tag = conf.getTag(elementContextName);
                tag.add( new Attribute(aName));
            }
        } else {
            //contextfree attribute
            for(String aName : attributeNames) {
                conf.add(new Attribute(aName));
            }
        }
        conf.store();
        LexerUtils.rebuildTokenHierarchy(snapshot.getSource().getDocument(true));
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }
    
}
