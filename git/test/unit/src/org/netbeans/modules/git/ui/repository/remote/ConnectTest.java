/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git.ui.repository.remote;

import java.awt.EventQueue;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.libs.git.utils.GitURI;
import org.netbeans.modules.git.AbstractGitTestCase;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.versioning.util.KeyringSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbPreferences;

/**
 *
 * @author ondra
 */
public class ConnectTest extends AbstractGitTestCase {
    private GitClient client;
    private static final String URL = "http://bugtracking-test.cz.oracle.com/git/repo";
    private static final String RECENT_GURI = "recent_guri";
    private static final String DELIMITER               = "<=~=>";              // NOI18N
    private static final String GURI_PASSWORD           = "guri_password";
    private Preferences prefs;
    
    public ConnectTest (String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", new File(repositoryLocation.getParentFile(), "home").getAbsolutePath());
        client = Git.getInstance().getClient(repositoryLocation, null, false);
        prefs = NbPreferences.forModule(GitModuleConfig.class);
        prefs.clear();
    }

    public void testConnectNoCredentials () throws Exception {
        try {
            client.listRemoteBranches(URL, ProgressMonitor.NULL_PROGRESS_MONITOR);
            fail();
        } catch (GitException.AuthorizationException ex) {
            // OK
        }
    }
    
    public void testConnectDoNotStoreCredentials () throws Exception {
        RemoteRepository repository = new RemoteRepository(URL);
        waitForInit(repository);
        RemoteRepositoryPanel panel = getPanel(repository);
        panel.urlComboBox.getEditor().setItem(URL);
        panel.userTextField.setText("user");
        panel.userPasswordField.setText("heslo");
        panel.savePasswordCheckBox.setSelected(false);
        
        assertUris(Collections.<GitURI>emptyList());
        repository.store();
        assertUris(Arrays.asList(new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user").setPass("heslo")));
        
        // command passes?
        client.listRemoteBranches(URL, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(Arrays.asList(toPrefsString(new GitURI("http://bugtracking-test.cz.oracle.com/git/repo"), false)), Utils.getStringList(prefs, RECENT_GURI));
        assertNull(KeyringSupport.read(GURI_PASSWORD, new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user").toString()));
    }
    
    public void testConnectStoreCredentials () throws Exception {
        RemoteRepository repository = new RemoteRepository(URL);
        waitForInit(repository);
        RemoteRepositoryPanel panel = getPanel(repository);
        panel.urlComboBox.getEditor().setItem(URL);
        panel.userTextField.setText("user");
        panel.userPasswordField.setText("heslo");
        panel.savePasswordCheckBox.setSelected(true);
        
        assertUris(Collections.<GitURI>emptyList());
        repository.store();
        assertUris(Arrays.asList(new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user").setPass("heslo")));
        
        // command passes?
        client.listRemoteBranches(URL, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(Arrays.asList(toPrefsString(new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user"), true)), Utils.getStringList(prefs, RECENT_GURI));
        assertEquals("heslo", new String(KeyringSupport.read(GURI_PASSWORD, new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user").toString())));
    }
    
    public void testConnectDoNotStoreCredentials_EmptyPassword () throws Exception {
        RemoteRepository repository = new RemoteRepository(URL);
        waitForInit(repository);
        RemoteRepositoryPanel panel = getPanel(repository);
        panel.urlComboBox.getEditor().setItem(URL);
        panel.userTextField.setText("user2");
        panel.userPasswordField.setText("");
        panel.savePasswordCheckBox.setSelected(false);
        
        assertUris(Collections.<GitURI>emptyList());
        repository.store();
        assertUris(Arrays.asList(new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user2").setPass("")));
        
        // command passes?
        client.listRemoteBranches(URL, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(Arrays.asList(toPrefsString(new GitURI("http://bugtracking-test.cz.oracle.com/git/repo"), false)), Utils.getStringList(prefs, RECENT_GURI));
        assertNull(KeyringSupport.read(GURI_PASSWORD, new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user2").toString()));
    }
    
    public void testConnectStoreCredentials_EmptyPassword () throws Exception {
        RemoteRepository repository = new RemoteRepository(URL);
        waitForInit(repository);
        RemoteRepositoryPanel panel = getPanel(repository);
        panel.urlComboBox.getEditor().setItem(URL);
        panel.userTextField.setText("user2");
        panel.userPasswordField.setText("");
        panel.savePasswordCheckBox.setSelected(true);
        
        assertUris(Collections.<GitURI>emptyList());
        repository.store();
        assertUris(Arrays.asList(new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user2").setPass("")));
        
        // command passes?
        client.listRemoteBranches(URL, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(Arrays.asList(toPrefsString(new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user2"), true)), Utils.getStringList(prefs, RECENT_GURI));
        assertEquals("", new String(KeyringSupport.read(GURI_PASSWORD, new GitURI("http://bugtracking-test.cz.oracle.com/git/repo").setUser("user2").toString())));
    }
    
    public void testSupportedProtocols () throws Exception {
        try {
            client.listRemoteBranches("ftp://host.name/resource", ProgressMonitor.NULL_PROGRESS_MONITOR);
            fail("Protocol is now supported, add to RemoteRepository.Scheme");
        } catch (GitException ex) {
            assertEquals("URI not supported: ftp://host.name/resource", ex.getMessage());
        }
        try {
            client.listRemoteBranches("ftps://host.name/resource", ProgressMonitor.NULL_PROGRESS_MONITOR);
            fail("Protocol is now supported, add to RemoteRepository.Scheme");
        } catch (GitException ex) {
            assertEquals("URI not supported: ftps://host.name/resource", ex.getMessage());
        }
        try {
            client.listRemoteBranches("rsync://host.name/resource", ProgressMonitor.NULL_PROGRESS_MONITOR);
            fail("Protocol is now supported, add to RemoteRepository.Scheme");
        } catch (GitException ex) {
            assertEquals("URI not supported: rsync://host.name/resource", ex.getMessage());
        }
    }

    private RemoteRepositoryPanel getPanel (RemoteRepository repository) throws Exception {
        Field f = RemoteRepository.class.getDeclaredField("panel");
        f.setAccessible(true);
        return (RemoteRepositoryPanel) f.get(repository);
    }

    private void assertUris (List<GitURI> expectedUris) {
        List<GitURI> uris = GitModuleConfig.getDefault().getRecentUrls();
        assertEquals(expectedUris.size(), uris.size());
        for (GitURI expected : expectedUris) {
            boolean ok = false;
            for (ListIterator<GitURI> it = uris.listIterator(); it.hasNext(); ) {
                GitURI uri = it.next();
                if (expected.setUser(null).setPass(null).toString().equals(uri.setUser(null).setPass(null).toString())
                        && (expected.getUser() == null && uri.getUser() == null || expected.getUser() != null && expected.getUser().equals(uri.getUser()))
                        && (expected.getPass() == null && uri.getPass() == null || expected.getPass() != null && expected.getPass().equals(uri.getPass()))) {
                    it.remove();
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                fail(expected.toString());
            }
        }
    }

    private void waitForInit (final RemoteRepository repository) throws InterruptedException {
        final boolean[] valid = new boolean[1];
        repository.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        valid[0] = repository.isValid();
                    }
                });
            }
        });
        for (int i = 0; i < 100; ++i) {
            if (valid[0]) {
                break;
            }
            Thread.sleep(100);
        }
    }
    
    private String toPrefsString (GitURI uri, boolean saveCreds) {
        StringBuilder sb = new StringBuilder();
        sb.append(uri.setUser(null).setPass(null).toString());
        sb.append(DELIMITER);
        sb.append(saveCreds ? uri.getUser() : ""); //NOI18N
        sb.append(DELIMITER);
        sb.append(saveCreds);
        return sb.toString();
    }
}
