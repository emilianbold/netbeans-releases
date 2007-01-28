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
