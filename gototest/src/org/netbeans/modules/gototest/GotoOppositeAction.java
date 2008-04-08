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

package org.netbeans.modules.gototest;

import java.awt.EventQueue;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import org.netbeans.spi.gototest.TestLocator;
import org.netbeans.spi.gototest.TestLocator.FileType;
import org.netbeans.spi.gototest.TestLocator.LocationListener;
import org.netbeans.spi.gototest.TestLocator.LocationResult;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;

/**
 * Action which jumps to the opposite test file given a current file.
 * This action delegates to specific framework implementations (JUnit, Ruby etc.)
 * which perform logic appropriate for the file type being opened.
 * <p>
 * Much of this is based on the original JUnit action by Marian Petras.
 * 
 * @author  Marian Petras
 * @author Tor Norbye
 */
public class GotoOppositeAction extends CallableSystemAction {
    private TestLocator cachedLocator;
    private FileObject cachedLocatorFo;
    private FileObject cachedFileTypeFo;
    private FileType cachedFileType;

    public GotoOppositeAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N

        // Not sure what the following is used for - a grep for trimmed-text
        // doesn't reveal any clients. Obsolete code perhaps?
        String trimmedName = NbBundle.getMessage(
                GotoOppositeAction.class,
                "LBL_Action_GoToTest_trimmed"); //NOI18N
        putValue("trimmed-text", trimmedName); //NOI18N
    }
    
    public String getName() {
        return NbBundle.getMessage(getClass(),
                                   getCurrentFileType() == FileType.TEST
                                        ? "LBL_Action_GoToSource" //NOI18N
                                        : "LBL_Action_GoToTest"); //NOI18N
    }
    
    @Override
    public boolean isEnabled() {
        assert EventQueue.isDispatchThread();
        
        return getCurrentFileType() != FileType.NEITHER;
    }

    public HelpCtx getHelpCtx() {
        // TODO - delegate to file locators!
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected void initialize () {
	super.initialize ();
        putProperty(Action.SHORT_DESCRIPTION,
                    NbBundle.getMessage(getClass(),
                                        "HINT_Action_GoToTest"));       //NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    public void performAction() {
        int caretOffsetHolder[] = new int[1];
        FileObject fo = getApplicableFileObject(caretOffsetHolder);
        int caretOffset = caretOffsetHolder[0];

        if (fo != null) {
            TestLocator locator = getLocatorFor(fo);
            if (locator != null) {
                if (locator.appliesTo(fo)) {
                    if (locator.asynchronous()) {
                        locator.findOpposite(fo, caretOffset, new LocationListener() {
                            public void foundLocation(FileObject fo, LocationResult location) {
                                if (location != null) {
                                    handleResult(location);
                                }
                            }
                        });
                    } else {
                        LocationResult opposite = locator.findOpposite(fo, caretOffset);

                        if (opposite != null) {
                            handleResult(opposite);
                        }
                    }
                }
            }
        }
    }

    private void handleResult(LocationResult opposite) {
        if (opposite.getFileObject() != null) {
            openFile(opposite.getFileObject());
        } else if (opposite.getErrorMessage() != null) {
            String msg = opposite.getErrorMessage();
            NotifyDescriptor descr = new NotifyDescriptor.Message(msg, 
                    NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(descr);
        }
    }
    
    private TestLocator getLocatorFor(FileObject fo) {
        if (fo == cachedLocatorFo) {
            return cachedLocator;
        }
        cachedLocatorFo = fo;
        cachedLocator = null;

        Collection<? extends TestLocator> locators = Lookup.getDefault().lookupAll(TestLocator.class);
        for (TestLocator locator : locators) {
            if (locator.appliesTo(fo)) {
                cachedLocator = locator;
                
                break;
            }
        }
        
        return cachedLocator;
    }
    
    private FileType getFileType(FileObject fo) {
        if (fo == cachedFileTypeFo) {
            return cachedFileType;
        }
        
        cachedFileTypeFo = fo;
        cachedFileType = FileType.NEITHER;
        
        TestLocator locator = getLocatorFor(fo);
        if (locator != null) {
            cachedFileType = locator.getFileType(fo);
        }
        
        return cachedFileType;
    }
    
    private FileType getCurrentFileType() {
        FileObject fo = getApplicableFileObject(null);
        
        return (fo != null) ? getFileType(fo) : FileType.NEITHER;
    }
    
    /**
     * Open given file in editor.
     * @return true if file was opened or false
     */
    public static boolean openFile(FileObject fo) {
        DataObject dobj;
        try {
            dobj = DataObject.find(fo);
        } catch (DataObjectNotFoundException e) {
            Exceptions.printStackTrace(e);
            return false;
        }
        assert dobj != null;
        
        EditorCookie editorCookie = dobj.getCookie(EditorCookie.class);
        if (editorCookie != null) {
            editorCookie.open();
            return true;
        }
        
        OpenCookie openCookie = dobj.getCookie(OpenCookie.class);
        if (openCookie != null) {
            openCookie.open();                
            return true;
        }
        
        return false;
    }
    
    private FileObject getApplicableFileObject(int[] caretPosHolder) {
        // TODO: Use the new editor library to compute this:
        // JTextComponent pane = EditorRegistry.lastFocusedComponent();
        TopComponent comp = TopComponent.getRegistry().getActivated();
        if (comp == null) {
            return null;
        }

        if (comp instanceof CloneableEditorSupport.Pane) {
            JEditorPane editorPane = ((CloneableEditorSupport.Pane)comp).getEditorPane();
            if (editorPane != null) {
                if (caretPosHolder != null && editorPane.getCaret() != null) {
                    caretPosHolder[0] = editorPane.getCaret().getDot();
                }
                Document document = editorPane.getDocument();

                Object sdp = document.getProperty(Document.StreamDescriptionProperty);
                if (sdp instanceof FileObject) {
                    return (FileObject)sdp;
                } else if (sdp instanceof DataObject) {
                    return ((DataObject) sdp).getPrimaryFile();
                }
            }
        } else {
            if (caretPosHolder != null) {
                caretPosHolder[0] = -1;
            }
            Node[] selectedNodes = comp.getActivatedNodes();
            if (selectedNodes != null && selectedNodes.length == 1) {
                DataObject dataObj = selectedNodes[0].getLookup().lookup(DataObject.class);
                if (dataObj != null) {
                    return dataObj.getPrimaryFile();
                }
            }
        }
        
        return null;
    }
}
