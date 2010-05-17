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
package org.netbeans.modules.collab.core.bridge;

import com.sun.collablet.*;

import org.openide.util.*;

import java.beans.*;

import java.util.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class NbCollabletFactoryManager extends CollabletFactoryManager implements LookupListener {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Lookup.Result results;

    /**
     *
     *
     */
    public NbCollabletFactoryManager() {
        super();

        // Lookup all the CollabletFactories in the system.  By default, these
        // should be registered in the /Services/Collaboration/Channels
        // folder.  The results will be updated as they change.
        results = Lookup.getDefault().lookup(new Lookup.Template(CollabletFactory.class));
        results.addLookupListener(this);

        // Workaround for not finding objects registered through lookup
        //		FileObject fileObject=FileUtil.getConfigFile("Services/Collaboration/Channels"); // NOI18N
        //		FileObject[] children=fileObject.getChildren();
        //
        //Debug.out.println("Number of children = "+children.length);
        //		assert children!=null && children.length>0:
        //			"Standard channels not found in system filesystem";
        //for (int i=0; i<children.length; i++)
        //{
        //	Debug.out.println("- Child "+i+": "+children[i]);
        //	try
        //	{
        //		DataObject.find(children[i]);
        //	}
        //	catch (Exception e)
        //	{
        //		Debug.debugNotify(e);
        //	}
        //}
    }

    /**
     *
     *
     */
    public synchronized CollabletFactory[] getCollabletFactories() {
        //		Lookup.Result results=Lookup.getDefault().lookup(
        //			new Lookup.Template(ChannelProvider.class));
        Collection providers = results.allInstances();

        return (CollabletFactory[]) providers.toArray(new CollabletFactory[providers.size()]);
    }

    /**
     *
     *
     */
    public void resultChanged(LookupEvent lookupEvent) {
        getChangeSupport().firePropertyChange(PROP_COLLABLET_FACTORIES, null, null);
    }
}
