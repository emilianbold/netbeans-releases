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

/*
 * KeyStoreRepositoryTest.java
 * JUnit based test
 *
 * Created on 16 February 2006, 11:30
 */
package org.netbeans.modules.mobility.project.security;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Set;
import org.netbeans.core.startup.NbRepository;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.project.TestUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lukas
 */
public class KeyStoreRepositoryTest extends NbTestCase {
    static
    {
        TestUtil.setLookup(new Lookup[] {Lookups.fixed(new Object[] {KeyStoreRepository.class},new InstanceContent.Convertor() {
            public Object convert(Object obj) {
                if (obj == KeyStoreRepository.class)
                    return KeyStoreRepository.createRepository();
                if (obj == Repository.class)
                    return Repository.getDefault();
                return null;
            }
            
            public Class type(Object obj) {
                return (Class)obj;
            }
            
            public String id(Object obj) {
                return obj.toString();
            }
            
            public String displayName(Object obj) {
                return ((Class)obj).getName();
            }
        } ),Lookups.metaInfServices(NbRepository.class.getClassLoader())});
    }
    
    public KeyStoreRepositoryTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("system.dir",getWorkDir().getAbsolutePath());
    }
    
    protected void tearDown() throws Exception {
    }
    
    
    //This method must be the first of test methods executed to make sure that getDefault is using correct path
    public void testBean() throws IOException {
        KeyStoreRepository defRep=KeyStoreRepository.getDefault();
        assertNotNull(defRep);
        List l=defRep.getKeyStores();
        assertNotNull(l);
        assertTrue(l.size()==1);
        KeyStoreRepository.KeyStoreBean bean=(KeyStoreRepository.KeyStoreBean)l.get(0);
        assertNotNull(bean);
        assertTrue(bean.isValid());
        assertTrue(bean.isOpened());
        File f=bean.getKeyStoreFile();
        assertNotNull(f);
        assertTrue(f.getPath().equals(getWorkDir().getPath()+ File.separator +"j2me" + File.separator + "builtin.ks"));
        String s=bean.getKeyStorePath();
        assertTrue(s.equals(getWorkDir().getPath()+ File.separator +"j2me" + File.separator + "builtin.ks"));
        
        String pass=bean.getPassword();
        assertEquals(pass,"password");
        bean.setPassword("newPassword");
        pass=bean.getPassword();
        assertEquals(pass,"newPassword");
        
        Set set=bean.aliasses();
        assertNotNull(set);
        assertTrue(set.size()==3);
        assertTrue(bean.getType().equals("JKS"));
        assertNull(bean.getAlias("fake"));
        KeyStoreRepository.KeyStoreBean.KeyAliasBean alias=bean.createInvalidKeyAliasBean("fake");
        assertNotNull(alias);
        
        //just to get code coverage right
        bean.hashCode();
        bean.getStore();
        bean.setKeyStoreFile(bean.getKeyStoreFile());
    }
    
    /**
     *  This should not fail, I think!
    public void testAlias() throws IOException {
        Date date;
        
        assertFalse(KeyStoreRepository.isDefaultKeystore(null));
        KeyStoreRepository defRep=KeyStoreRepository.getDefault();
        assertNotNull(defRep);
        List l=defRep.getKeyStores();
        assertNotNull(l);
        assertTrue(l.size()==1);
        KeyStoreRepository.KeyStoreBean bean=(KeyStoreRepository.KeyStoreBean)l.get(0);
        assertNotNull(bean);
        Set set=bean.aliasses();
        assertNotNull(set);
        assertTrue(set.size()==3);
        Object als[]=set.toArray();
        KeyAliasBean alias=(KeyAliasBean) als[1];
        assertEquals("CN=minimal", alias.getIssuerName());
        
        //MD5 can't be tested so i called it just to get coverage
        alias.getMd5();
        
        //Serial Number can't be tested so i called it just to get coverage
        alias.getSerialNumber();
        
        //SHa can't be tested so i called it just to get coverage
        alias.getSha();
        
        assertEquals(alias.getPassword(),"password");
        String result=alias.getSubjectName();
        assertEquals(result,"CN=minimal");
        long now=System.currentTimeMillis();
        Calendar cal=Calendar.getInstance();
        cal.setTimeInMillis(now);
        now/=(1000*60*60*24);
        cal.add(Calendar.DAY_OF_YEAR,180);
        date=alias.getNotAfter();
        long after=date.getTime()/(1000*60*60*24);
        date=alias.getNotBefore();
        long before=date.getTime()/(1000*60*60*24);
        assertTrue(now==before);
        now=cal.getTimeInMillis()/(1000*60*60*24);
        assertTrue(now==after);
        assertTrue(alias.isValid());
        
        //just to get code coverage right
        alias.hashCode();
    }
     */
    
    public void testKeystore() throws Exception {
        KeyStoreRepository defRep=KeyStoreRepository.getDefault();
        assertNotNull(defRep);
        KeyStoreRepository.KeyStoreBean bean=defRep.getKeyStore("testKeyStore",false);
        assertNull(bean);
        bean=defRep.getKeyStore("testKeyStore",true);
        assertNotNull(bean);
        assertFalse(bean.isValid());
        assertFalse(bean.isOpened());
        assertFalse(defRep.isDefaultKeystore(bean));
        Object o=defRep.getPassword("testFile");
        assertNull(o);
        Object o1=new String[] {"test1","test2"};
        o=defRep.putPassword("testFile",o1);
        assertNull(o);
        o=defRep.getPassword("testFile");
        assertEquals(o1,o);
        o=defRep.removePassword("testFile");
        assertEquals(o1,o);
        o=defRep.getPassword("testFile");
        assertNull(o);
        bean=KeyStoreRepository.KeyStoreBean.create(getWorkDir().getAbsolutePath()+"/testStore.p12","pass123456");
        defRep.addKeyStore(bean);
        KeyStoreRepository.KeyStoreBean bean1=defRep.getKeyStore(getWorkDir().getAbsolutePath()+"/testStore.p12",false);
        assertEquals(bean,bean1);
        
        PropertyChangeListener listener=new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {}
        };
        
        defRep.addPropertyChangeListener(listener);
        
        //Check defaulting of type
        bean.setType("fake");
        
        //And now correct type
        bean.setType("pkcs12");
        assertTrue(bean.openKeyStore(true));
        bean.addKeyToStore("trusted", "CN=trusted", "password", -1); // NOI18N
        bean.addKeyToStore("untrusted", "CN=untrusted", "password", -1); // NOI18N
        bean.addKeyToStore("minimal", "CN=minimal", "password", -1); // NOI18N
        Set set=bean.aliasses();
        Object als[]=set.toArray();
        KeyStoreRepository.KeyStoreBean.KeyAliasBean alias=(KeyStoreRepository.KeyStoreBean.KeyAliasBean)als[0];
        assertNotNull(bean.getAlias(alias.getAlias()));
        assertTrue(bean.removeAliasFromStore(alias));
        Set set1=bean.aliasses();
        assertTrue(set.size()-1==set1.size());
        assertNull(bean.getAlias(alias.getAlias()));
        
        ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(getWorkDir()+"/testStore"));
        defRep.writeExternal(out);
        out.close();
        
        defRep.removeKeyStore(bean);
        bean1=defRep.getKeyStore(bean.getKeyStorePath(),false);
        assertNull(bean1);
        
        assertTrue(bean.closeKeyStore());
        assertTrue(!bean.isOpened());
        bean.openKeyStore();
        
        ObjectInputStream in=new ObjectInputStream(new FileInputStream(getWorkDir()+"/testStore"));
        defRep.readExternal(in);
        in.close();
        bean1=defRep.getKeyStore(bean.getKeyStorePath(),false);
        assertEquals(bean,bean1);
        
        defRep.removePropertyChangeListener(listener);
    }
}
