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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.clearcase.client;

import java.io.File;
import org.netbeans.modules.clearcase.ClearcaseException;

import java.util.*;

/**
 * Simple "lstype" command.
 * 
 * 
 */
public class LsTypeCommand extends ClearcaseCommand {    

    public static enum Kind { Attribute, Branch, Element, Hyperlink, Label, Trigger };
    
    private final Kind kind;
    private final List<String> types = new ArrayList<String>();
        
    public LsTypeCommand(File commandWorkingDirectory, Kind kind) {
        this.commandWorkingDirectory = commandWorkingDirectory;
        this.kind = kind;
    }
    
    public void prepareCommand(Arguments arguments) throws ClearcaseException {
        arguments.add("lstype");
        arguments.add("-kind");
        switch(kind) {
            case Attribute: arguments.add("attype"); break;
            case Branch:    arguments.add("brtype"); break;
            case Element:   arguments.add("eltype"); break;
            case Hyperlink: arguments.add("hltype"); break;
            case Label:     arguments.add("lbtype"); break;
            case Trigger:   arguments.add("trtype"); break;
            default: throw new IllegalStateException("Illegal type-kind: " + kind);                            
        }
        arguments.add("-short");
    }

    @Override
    public void commandStarted() {
        super.commandStarted();
        types.clear();
    }

    @Override
    public void outputText(String line) {
        if(line == null) {
            return;
        }        
        super.outputText(line);
        line = line.trim();
        if(!line.equals("")) {
            types.add(line);
        }
    }

    public List<String> getTypes() {
        return types;
    }
}
