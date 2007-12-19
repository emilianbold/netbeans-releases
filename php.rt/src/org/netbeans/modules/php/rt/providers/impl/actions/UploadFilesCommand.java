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
package org.netbeans.modules.php.rt.providers.impl.actions;

import java.io.IOException;

import org.netbeans.api.project.Project;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.modules.php.rt.utils.PhpCommandUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public abstract class UploadFilesCommand extends AbstractFilesTransferCommand
        implements Cloneable 
{
    
    static final String LBL_UPLOAD              = "LBL_PutFilesToServer";   // NOI18N
    
    static final String UPLOAD                  = "upload";                 // NOI18N
    
    private static final String LBL_SRC_DIR_READ_ERROR  
            = "LBL_SrcDirReadError";    // NOI18N

    private static final String LBL_UPLOADED_COUNT_MSG      
            = "LBL_UploadedFilesCnt";   // NOI18N
    
    public UploadFilesCommand( Project project ,  WebServerProvider provider )
    {
        this( project , true , provider);
    }
    
    public UploadFilesCommand( Project project , boolean notify , 
            WebServerProvider provider)
    {
        super( project, notify, provider );
    }
    
    public boolean isEnabled() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Command#getId()
     */
    public String getId() {
        return UPLOAD;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Command#getLabel()
     */
    public String getLabel() {
        return NbBundle.getMessage( UploadFilesCommand.class, LBL_UPLOAD );
    }

    @Override
    protected void refresh() {
        super.refresh();
        myIsSuccess = true;
        myInterrupted = false;
    }
    
    protected void setUnsuccess() {
        myIsSuccess = false;
    }
    
    protected boolean wasSuccessfull() {
        return myIsSuccess;
    } 
    
    protected void setInterrupted() {
        myInterrupted = true;
    }
    
    protected boolean wasInterrupted() {
        return myInterrupted;
    } 
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    protected void checkSources(){
        FileObject[] sources = getSourceObjects( getProject() );
        for (FileObject object : sources) {
            if ( !object.isValid() || !object.canRead() ){
                notifyMsg(LBL_SRC_DIR_READ_ERROR , object.getName() );
            }
        }
    }
    
    protected void saveFile(FileObject fromObject) throws IOException {
        PhpCommandUtils.saveFile(fromObject);
    }

    protected void notifyCopiedCount(){
        notifyMsg( LBL_UPLOADED_COUNT_MSG, 
                UploadFilesCommand.class, getCopiedFilesCnt()  );
    }

    private boolean myIsSuccess = true;

    private boolean myInterrupted = false;
}
