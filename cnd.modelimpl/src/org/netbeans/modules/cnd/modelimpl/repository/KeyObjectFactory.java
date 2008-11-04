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

package org.netbeans.modules.cnd.modelimpl.repository;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.support.KeyFactory;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 *
 * @author Nickolay Dalmatov
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.repository.support.KeyFactory.class)
public class KeyObjectFactory extends KeyFactory {
    
    /** Creates a new instance of KeyObjectFactory */
    public KeyObjectFactory() {
    }
    
    
    public void writeKey(Key aKey, DataOutput aStream) throws IOException {
        assert aKey instanceof SelfPersistent;
        super.writeSelfPersistent((SelfPersistent)aKey, aStream);
    }
    
    public Key readKey(DataInput aStream) throws IOException {
        assert aStream != null;
        SelfPersistent out = super.readSelfPersistent(aStream);
        assert out instanceof Key;
        return (Key)out;
    }
    
    public void writeKeyCollection(Collection<Key> aCollection, DataOutput aStream ) throws IOException {
        assert aCollection != null;
        assert aStream != null;
        
        int collSize = aCollection.size();
        aStream.writeInt(collSize);
        
        Iterator <Key> iter = aCollection.iterator();
        
        while (iter.hasNext()) {
            Key aKey = iter.next();
            assert aKey != null;
            writeKey(aKey, aStream);
        }
    }
    
    public void readKeyCollection(Collection<Key> aCollection, DataInput aStream) throws IOException {
        assert aCollection != null;
        assert aStream != null;
        
        int collSize = aStream.readInt();
        
        for (int i = 0; i < collSize; ++i) {
            Key aKey = readKey(aStream);
            assert aKey != null;
            aCollection.add(aKey);
        }
    }
    
    protected int getHandler(Object object) {
        int aHandle ;
        
        if (object instanceof ProjectKey ) {
            aHandle = KEY_PROJECT_KEY;
        } else if (object instanceof NamespaceDeclararationContainerKey) {
            aHandle = KEY_NS_DECLARATION_CONTAINER_KEY;
        }  else if (object instanceof NamespaceKey) {
            aHandle = KEY_NAMESPACE_KEY;
        } else if (object instanceof FileKey ) {
            aHandle = KEY_FILE_KEY;
        } else if (object instanceof MacroKey) {
            aHandle = KEY_MACRO_KEY;
        } else if (object instanceof IncludeKey) {
            aHandle = KEY_INCLUDE_KEY;
        } else if (object instanceof OffsetableDeclarationKey) {
            aHandle = KEY_DECLARATION_KEY;
        } else if (object instanceof ProjectSettingsValidatorKey) {
            aHandle = KEY_PRJ_VALIDATOR_KEY;
        } else if (object instanceof DeclarationContainerKey) {
            aHandle = KEY_DECLARATION_CONTAINER_KEY;
        } else if (object instanceof FileContainerKey) {
            aHandle = KEY_FILE_CONTAINER_KEY;
        } else if (object instanceof GraphContainerKey) {
            aHandle = KEY_GRAPH_CONTAINER_KEY;
        } else {
            throw new IllegalArgumentException("The Key is an instance of the unknown final class " + object.getClass().getName());  // NOI18N
        }
        
        return aHandle;
    }
    
    protected SelfPersistent createObject(int handler, DataInput aStream) throws IOException {
        SelfPersistent aKey;
        
        switch (handler) {
            case KEY_PROJECT_KEY:
                aKey = new ProjectKey(aStream);
                break;
            case KEY_NAMESPACE_KEY:
                aKey = new NamespaceKey(aStream);
                break;
            case KEY_FILE_KEY:
                aKey = new FileKey(aStream);
                break;
            case KEY_MACRO_KEY:
                aKey = new MacroKey(aStream);
                break;
            case KEY_INCLUDE_KEY:
                aKey = new IncludeKey(aStream);
                break;
            case KEY_DECLARATION_KEY:
                aKey = new OffsetableDeclarationKey(aStream);
                break;
	    case KEY_PRJ_VALIDATOR_KEY:
		aKey = new ProjectSettingsValidatorKey(aStream);
		break;
	    case KEY_DECLARATION_CONTAINER_KEY:
		aKey = new DeclarationContainerKey(aStream);
		break;
	    case KEY_FILE_CONTAINER_KEY:
		aKey = new FileContainerKey(aStream);
		break;
	    case KEY_GRAPH_CONTAINER_KEY:
		aKey = new GraphContainerKey(aStream);
		break;
	    case KEY_NS_DECLARATION_CONTAINER_KEY:
		aKey = new NamespaceDeclararationContainerKey(aStream);
		break;
            default:
                throw new IllegalArgumentException("Unknown hander was provided: " + handler);  // NOI18N
        }
        
        return aKey;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // constants which defines the handle of a key in the stream
    
    private static final int FIRST_INDEX        = AbstractObjectFactory.LAST_INDEX + 1;
    
    public static final int KEY_PROJECT_KEY    = FIRST_INDEX;
    public static final int KEY_NAMESPACE_KEY  = KEY_PROJECT_KEY + 1;
    public static final int KEY_FILE_KEY       = KEY_NAMESPACE_KEY + 1;
    public static final int KEY_MACRO_KEY      = KEY_FILE_KEY + 1;
    public static final int KEY_INCLUDE_KEY    = KEY_MACRO_KEY + 1;
    public static final int KEY_DECLARATION_KEY = KEY_INCLUDE_KEY + 1;
    public static final int KEY_PRJ_VALIDATOR_KEY = KEY_DECLARATION_KEY + 1;
    
    public static final int KEY_DECLARATION_CONTAINER_KEY = KEY_PRJ_VALIDATOR_KEY + 1;
    public static final int KEY_FILE_CONTAINER_KEY = KEY_DECLARATION_CONTAINER_KEY + 1;
    public static final int KEY_GRAPH_CONTAINER_KEY = KEY_FILE_CONTAINER_KEY    + 1;
    public static final int KEY_NS_DECLARATION_CONTAINER_KEY = KEY_GRAPH_CONTAINER_KEY + 1;
    
    // index to be used in another factory (but only in one) 
    // to start own indeces from the next after LAST_INDEX    
    public static final int LAST_INDEX          = KEY_NS_DECLARATION_CONTAINER_KEY;
}
