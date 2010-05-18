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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.border.EtchedBorder;

/*
 * ProgressWindow.java
 *
 * Created on July 30, 2004, 1:47 PM
 */

/**
 * @author  Winston Prakash
 */
public class ProgressWindow extends JDialog {
    
    private JLabel progressLabel;
    private JPanel progressPanel;
    
    private static ProgressWindow progressWindow = null;
    private static JFrame frame;
    
    String msg = null ;
    
    
    /** Creates new form ProgressWindow */
    public ProgressWindow(JFrame frame) {

        super(frame, true);
        try {
            msg = ResourceBundle.getBundle("Bundle").getString("Progress_MSG");
        }
        catch(java.util.MissingResourceException ree) {
            msg="Starting Sun Java Application Server. Please wait ..." ; //NOI18N
        }
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        progressPanel = new JPanel();
        progressLabel = new JLabel();
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setFocusable(false);
        setFocusableWindowState(false);
        setResizable(false);
        setUndecorated(true);
        progressPanel.setLayout(new BorderLayout());
        
        progressPanel.setBorder(new EtchedBorder());
        progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
        progressLabel.setText(msg);
        progressPanel.add(progressLabel, BorderLayout.CENTER);
        
        getContentPane().add(progressPanel, BorderLayout.CENTER);
        
    }
    public static void setMessage(String newmsg) {
        if (progressWindow != null ) {
            progressWindow.progressLabel.setText(newmsg) ;
        }
    }
    public void adjustBound(){
        int width = progressLabel.getFontMetrics(progressLabel.getFont()).stringWidth(msg) + 75;
        int height = progressLabel.getFontMetrics(progressLabel.getFont()).getHeight() + 75;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-width)/2, (screenSize.height-height)/2, width, height);
    }
    
    public static void showProgress(final String startMsg){
        Thread showThread = new Thread(){
            public void run() {
                frame = new JFrame();
                progressWindow = new ProgressWindow(frame);
                progressWindow.adjustBound();
                if ( startMsg != null ) progressWindow.setMessage(startMsg) ;
                progressWindow.show();
            }
        };
        showThread.start();
    }
    public static void showProgress() {
        showProgress( null ) ;
    }
    
    public static void hideProgress(){
        if(progressWindow != null){
            progressWindow.hide();
            progressWindow.dispose();
            frame.hide();
            frame.dispose();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new ProgressWindow(new JFrame()).show();
    }
}
