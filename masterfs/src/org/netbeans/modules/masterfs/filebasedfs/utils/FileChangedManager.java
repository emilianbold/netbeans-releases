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
package org.netbeans.modules.masterfs.filebasedfs.utils;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.openide.util.Lookup;

/**
 *
 * @author Radek Matous
 */
@org.openide.util.lookup.ServiceProvider(service=java.lang.SecurityManager.class)
public class FileChangedManager extends SecurityManager {
    private static  FileChangedManager INSTANCE;
    private static final int CREATE_HINT = 2;
    private static final int DELETE_HINT = 1;
    private static final int AMBIGOUS_HINT = 3;
    
    
    private final ConcurrentHashMap<Integer,Integer> hints = new ConcurrentHashMap<Integer,Integer>();
    private long shrinkTime = System.currentTimeMillis();
    
    public FileChangedManager() {
        INSTANCE = this;
    }
    
    public static FileChangedManager getInstance() {
        if (INSTANCE == null) {
            Lookup.getDefault().lookup(SecurityManager.class);
            assert INSTANCE != null;
        }
        return INSTANCE;
    }
    
    @Override
    public void checkDelete(String file) {
        put(file, false);
    }

    @Override
    public void checkWrite(String file) {
        put(file, true);
    }
        
    public boolean impeachExistence(File f, boolean expectedExixts) {
        Integer hint = remove(getKey(f));
        boolean retval = (hint == null) ? false : true;
        if (retval) {
            if (hint == AMBIGOUS_HINT) {
                return true;
            } else {
                retval = (expectedExixts != toState(hint));
            }
        }
        return retval;
    }    
    
    public boolean exists(File file) {
        boolean retval = file.exists();
        remove(getKey(file));
        put(file,retval);
        return retval;
    }
    
    private Integer put(int id, boolean state) {
        shrinkTime = System.currentTimeMillis();
        int val = toValue(state);
        Integer retval = hints.putIfAbsent(id,val);
        if (retval != null) {
            if (retval != AMBIGOUS_HINT && retval != val) {
                hints.put(id,AMBIGOUS_HINT);
            } 
        }                
        return retval;
    }
    
    private int toValue(boolean state) {
        return state ? CREATE_HINT : DELETE_HINT;
    }
    
    private boolean toState(int value) {
        switch(value) {
            case DELETE_HINT:
                return false;
            case CREATE_HINT:
                return true;
        }  
        return false;
    }
    
    
    
    private Integer get(int id) {
        long now = System.currentTimeMillis();
        if ((now - shrinkTime) > 5000) {
            int size = hints.size();
            if (size > 1500) {                
                shrink();
            }
            shrinkTime = now;
        } 
        return hints.get(id);
    }

    
    private void shrink() {
        hints.keySet().clear();
    }
    
    private Integer remove(int id) {
        return hints.remove(id);
    }                
    
    private static int getKey(File f) {
        return NamingFactory.createID(f);
    }
    private static int getKey(String f) {
        return getKey(new File(f));
    }  

    private Integer put(String f, boolean value) {
        return put(getKey(f), value);
    }
    
    private Integer put(File f, boolean value) {
        return put(getKey(f), value);
    }
    
    private Integer get(String file) {
        return get(getKey(file));
    }

    private Integer get(File file) {
        return get(getKey(file));        
    }
}
