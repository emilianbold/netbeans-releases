/*
 * NodeGenerator.java
 *
 * Created on August 21, 2002, 1:44 PM
 */

package org.netbeans.modules.testtools.generator;

import java.io.*;
import java.util.*;
import java.awt.Component;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.MenuElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jellytools.actions.Action;

import org.openide.src.*;
import org.openide.cookies.SourceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class NodeGenerator {
    
    private static final String nodeTemplate = ResourceBundle.getBundle("org.netbeans.modules.testtools.generator.NodeGenerator").getString("NodeTemplate");
    
    public abstract class ActionRecord extends Object {

        public abstract String getFullClassName();

        public abstract String getName();

        protected abstract String getPopupPath();
        
        public abstract String getConstructorCode();
        
        public abstract boolean isInline();

        public String getSmallName() {
            String s=getName();
            if (s.length()>6 && s.endsWith("Action"))
                return Character.toLowerCase(s.charAt(0))+s.substring(1, s.length()-6);
            else
                return Character.toLowerCase(s.charAt(0))+s.substring(1);
        }

        public boolean isForPopup(String popupPath) {
            String popup = getPopupPath();
            if (popup==null) return false;
            StringTokenizer popup1 = new StringTokenizer(popupPath, "|");
            StringTokenizer popup2 = new StringTokenizer(popup, "|");
            if (popup1.countTokens()!=popup2.countTokens()) return false;
            while (popup1.hasMoreTokens())
                //if (popup1.nextToken().indexOf(popup2.nextToken())<0) return false;
                if (!popup1.nextToken().equals(popup2.nextToken())) return false;
            return true;
        }
        
        private DefaultMutableTreeNode node;
        
        public DefaultMutableTreeNode getNodeDelegate() {
            if (node==null) {
                node=new DefaultMutableTreeNode(this);
                NodeGenerator.this.getNodeDelegate().add(node);
            }
            return node;
        }
            
    }

    public class ExistingActionRecord extends ActionRecord {
        Action action;
        public ExistingActionRecord(Action actionInstance) {
            action=actionInstance;
        }
        
        public String getFullClassName() {
            return action.getClass().getName();
        }

        public String getName() {
            String s=getFullClassName();
            return s.substring(s.lastIndexOf('.')+1);
        }
        
        public String getPopupPath() {
            return action.getPopupPath();
        }
        
        public String getConstructorCode() {
            return getName()+"()";
        }
        
        public String toString() {
            return getFullClassName();
        }
        
        public boolean isInline() {
            return false;
        }
        
    }
    
    public class NewActionRecord extends ActionRecord {
        private String popupPath;
        private String name;
        private String menuPath;
        private String systemActionClass;
        private String shortcuts[];
        private boolean noBlock;
        private boolean inline;
        
        public NewActionRecord(String popupPath) {
            this.popupPath=popupPath;
            name=toJavaIdentifier(popupPath)+"Action";
            noBlock=defaultNoBlock;
            inline=defaultInline;
        }
        
        public String getFullClassName() {
            if (inline) return null;
            else return actionsPackage+"."+name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String newName) {
            name=newName;
            fireStateChanged(this);
        }

        public String getMenuPath() {
            return menuPath;
        }
        
        public void setMenuPath(String menuPath) {
            this.menuPath = menuPath;
            fireStateChanged(this);
        }
        
        public String getSystemActionClass() {
            return systemActionClass;
        }
        
        public void setSystemActionClass(String systemActionClass) {
            this.systemActionClass = systemActionClass;
            fireStateChanged(this);
        }
        
        public boolean isNoBlock() {
            return noBlock;
        }
        
        public void setNoBlock(boolean noBlock) {
            this.noBlock = noBlock;
            fireStateChanged(this);
        }
        
        public String getSourceCode() {
            StringBuffer sb = new StringBuffer();
            sb.append("/*\n * ");
            sb.append(name);
            sb.append(".java\n *\n * Created on ");
            sb.append(new SimpleDateFormat().format(new Date()));
            sb.append("\n */\n\npackage ");
            sb.append(actionsPackage);
            sb.append(";\n\nimport org.netbeans.jellytools.actions.*;\n\n/** ");
            sb.append(name);
            sb.append(" Class\n * author ");
            sb.append(System.getProperty("user.name"));
            sb.append("\n */\npublic class ");
            sb.append(name);
            sb.append(" extends Action");
            if (noBlock) sb.append("NoBlock");
            sb.append(" {\n\n    /** creates new ");
            sb.append(name);
            sb.append(" instance */\n    public ");
            sb.append(name);
            sb.append("() {\n        super(");
            sb.append(getConstructorArgs());
            sb.append(");\n    }\n}\n");
            return sb.toString();
        }
        
        protected String getConstructorArgs() {
            StringBuffer sb = new StringBuffer();
            if (menuPath!=null && menuPath.length()>0) {
                sb.append('\"');
                sb.append(menuPath);
                sb.append("\", ");
            } else {
                sb.append("null, ");
            }
            if (popupPath!=null && popupPath.length()>0) {
                sb.append('\"');
                sb.append(popupPath);
                sb.append('\"');
            } else {
                sb.append("null");
            }
            if (systemActionClass!=null && systemActionClass.length()>0) {
                sb.append(", \"");
                sb.append(systemActionClass);
                sb.append('\"');
            }
            if (shortcuts!=null && shortcuts.length>0) {
                if (shortcuts.length==1) {
                    sb.append(", new Shortcut(");
                    sb.append(translateShortcut(shortcuts[0]));
                    sb.append(')');
                } else {
                    sb.append(", new Shortcut[] {new ShortCut(");
                    sb.append(translateShortcut(shortcuts[0]));
                    sb.append(')');
                    for (int i=1; i<shortcuts.length; i++) {
                        sb.append(", new Shortcut(");
                        sb.append(translateShortcut(shortcuts[i]));
                        sb.append(')');
                    }
                    sb.append('}');
                }
            }
            return sb.toString();
        }            
        
        private String translateShortcut(String shortcut) {
            StringTokenizer st=new StringTokenizer(shortcut, " ,;-+_");
            String modifiers="";
            String key="KeyEvent.VK_UNDEFINED";
            while (st.hasMoreTokens()) {
                String token=st.nextToken().toUpperCase();
                if (token.equals("CONTROL")||token.equals("CTRL")) modifiers+="|KeyEvent.CTRL_MASK";
                else if (token.equals("ALT")) modifiers+="|KeyEvent.ALT_MASK";
                else if (token.equals("ALT")) modifiers+="|KeyEvent.ALT_MASK";
                else if (token.equals("META")) modifiers+="|KeyEvent.META_MASK";
                else if (token.equals("SHIFT")) modifiers+="|KeyEvent.SHIFT_MASK";
                else key="KeyEvent.VK_"+token;
            }
            if (modifiers.length()>0) modifiers=modifiers.substring(1);
            return modifiers+", "+key;
        }
            
        protected String getPopupPath() {
            return popupPath;
        }
        
        public boolean isInline() {
            return inline;
        }
        
        public void setInline(boolean inline) {
            this.inline = inline;
            fireStateChanged(this);
        }
        
        public String getConstructorCode() {
            if (inline) return "Action"+(noBlock?"NoBlock":"")+"("+getConstructorArgs()+")";
            else return name+"()";
        }
        
        public String toString() {
            if (inline) return "inline "+getConstructorCode();
            else return "new "+getFullClassName();
        }
        
        /** Indexed getter for property shortcuts.
         * @param index Index of the property.
         * @return Value of the property at <CODE>index</CODE>.
         *
         */
        public String getShortcuts(int index) {
            return shortcuts[index];
        }
        
        /** Getter for property shortcuts.
         * @return Value of property shortcuts.
         *
         */
        public String[] getShortcuts() {
            return shortcuts;
        }
        
        /** Indexed setter for property shortcuts.
         * @param index Index of the property.
         * @param shortcuts New value of the property at <CODE>index</CODE>.
         *
         */
        public void setShortcuts(int index, String shortcuts) {
            this.shortcuts[index] = shortcuts;
            fireStateChanged(this);
        }
        
        /** Setter for property shortcuts.
         * @param shortcuts New value of property shortcuts.
         *
         */
        public void setShortcuts(String[] shortcuts) {
            this.shortcuts = shortcuts;
            fireStateChanged(this);
        }
        
    }
    
    private ArrayList matchingRecords;
    private ArrayList allRecords;
    private String actionsPackage;
    private String nodePackage;
    private String nodeName;
    private boolean defaultInline;
    private boolean defaultNoBlock;
    
    public NodeGenerator(String actionsPackage, String nodePackage, String nodeName, JPopupMenuOperator popupMenu, boolean defaultInline, boolean defaultNoBlock) {
        this(actionsPackage, nodePackage, nodeName, getPopupPaths(popupMenu), defaultInline, defaultNoBlock);
    }
    
    public NodeGenerator(String actionsPackage, String nodePackage, String nodeName, String popupPaths[], boolean defaultInline, boolean defaultNoBlock) {
        this.actionsPackage=actionsPackage;
        this.nodePackage=nodePackage;
        this.nodeName=nodeName;
        this.defaultInline=defaultInline;
        this.defaultNoBlock=defaultNoBlock;
        searchForActions();
        matchingRecords=new ArrayList();
        for (int i=0; i<popupPaths.length; i++)
            matchingRecords.add(getActionRecord(popupPaths[i]));
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public void setNodeName(String name) {
        nodeName=name;
        fireStateChanged(this);
    }
    
    public String getNodePackage() {
        return nodePackage;
    }
   
    public String getActionsPackage() {
        return actionsPackage;
    }
    
    public boolean isDefaultInline() {
        return defaultInline;
    }
    
    public boolean isDefaultNoBlock() {
        return defaultNoBlock;
    }
    
    private static EventTool tool;
    
    private static String[] getPopupPaths(JPopupMenuOperator popup) {
        ArrayList paths=new ArrayList();
        tool=new EventTool();
        getPopupPaths(paths, "", popup);
        return (String[])paths.toArray(new String[0]);
    }
    
    private static void getPopupPaths(ArrayList paths, String path, JPopupMenuOperator popup) {
        tool.waitNoEvent(1000);
        MenuElement el[]=popup.getSubElements();
        for (int i=0; i<el.length; i++) {
            Component c=el[i].getComponent();
            if (c.isShowing()) {
                if (c instanceof JMenu && c.isEnabled()) {
                    JMenuOperator m=new JMenuOperator((JMenu)c);
                    m.push();
                    getPopupPaths(paths, path+m.getText()+"|", new JPopupMenuOperator(m.getPopupMenu()));
                } else if (c instanceof AbstractButton) {
                    String text=((AbstractButton)c).getText();
                    if (text!=null && text.length()>0 && !paths.contains(path+text))
                        paths.add(path+text);
                }
            }
        }
    }
    
    private ActionRecord getActionRecord(String popupPath) {
        Iterator it = allRecords.iterator();
        ActionRecord record=null;
        while (it.hasNext() && !(record=(ActionRecord)it.next()).isForPopup(popupPath));
        if (!record.isForPopup(popupPath)) {
            record = new NewActionRecord(popupPath);
            allRecords.add(record);
        }
        return record;
    }
    
    private static boolean isAccessible(ClassElement clazz) {
        if (!clazz.isClass() || clazz.isInner()) return false;
        Identifier superc = clazz.getSuperclass();
        if (superc==null) return false;
        if (!superc.getFullName().equals("org.netbeans.jellytools.actions.Action") &&
        !superc.getFullName().equals("org.netbeans.jellytools.actions.ActionNoBlock")) return false;
        int mod = clazz.getModifiers();
        if (Modifier.isAbstract(mod) || !Modifier.isPublic(mod)) return false;
        ConstructorElement constructor = clazz.getConstructor(new Type[0]);
        if (constructor==null || !Modifier.isPublic(constructor.getModifiers())) return false;
        return true;
    }
    
    static String toJavaIdentifier(String s) {
        StringBuffer sb = new StringBuffer();
        int i;
        if (null!=s) {
            char ch;
            boolean shift=true;
            for (i=0; i<s.length(); i++) {
                ch = s.charAt(i);
                if (Character.isJavaIdentifierPart(ch)) {
                    if (shift) {
                        shift = false;
                        ch = Character.toUpperCase(ch);
                    }
                    sb.append(ch);
                } else {
                    shift = true;
                }
            }
        } 
        return sb.toString();
    }
    
    private void searchForActions() {
        allRecords = new ArrayList();
        Enumeration fsystems = Repository.getDefault().fileSystems();
        while (fsystems.hasMoreElements()) {
            Enumeration fobjects = ((FileSystem)fsystems.nextElement()).getRoot().getData(true);
            while (fobjects.hasMoreElements()) {
                FileObject fo = (FileObject)fobjects.nextElement();
                if (fo.getName().endsWith("Action")) try {
                    DataObject dob=DataObject.find(fo);
                    SourceCookie source = (SourceCookie)dob.getCookie(SourceCookie.class);
                    if (source!=null) {
                        ClassElement classes[] = source.getSource().getClasses();
                        for (int i=0; i<classes.length; i++) {
                            if (isAccessible(classes[i])) {
                                String className = classes[i].getName().getFullName();
                                Action action = (Action)Class.forName(className).getDeclaredConstructor(null).newInstance(null);
                                allRecords.add(new ExistingActionRecord(action));
//                                System.out.println(className);
                            }
                            
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private String getImportCode() {
        TreeMap imports=new TreeMap();
        imports.put("org.netbeans.jellytools.actions", "*");
        imports.put("org.netbeans.jellytools.nodes", "Node");
        Iterator it=matchingRecords.iterator();
        while (it.hasNext()) {
            ActionRecord rec=(ActionRecord)it.next();
            if (!rec.isInline()) {
                String pack=rec.getFullClassName();
                int i=pack.lastIndexOf('.');
                if (i>=0) {
                    pack=pack.substring(0, i);
                    if (!pack.equals(nodePackage)) {
                        if (imports.containsKey(pack)) {
                            imports.put(pack, "*");
                        } else {
                            imports.put(pack,  rec.getName());
                        }
                    }
                }
            }
        }
        it=imports.keySet().iterator();
        StringBuffer sb=new StringBuffer();
        while (it.hasNext()) {
            String s=(String)it.next();
            sb.append("import ");
            sb.append(s);
            sb.append('.');
            sb.append(imports.get(s));
            sb.append(";\n");
        }
        return sb.toString();
    }
    
    private String getDeclarationCode() {
        StringBuffer sb=new StringBuffer();
        Iterator it=matchingRecords.iterator();
        while (it.hasNext()) {
            ActionRecord rec=(ActionRecord)it.next();
            sb.append("    private static final Action ");
            sb.append(rec.getSmallName());
            sb.append("Action = new ");
            sb.append(rec.getConstructorCode());
            sb.append(";\n");
        }
        return sb.toString();
    }
    
    private String getFunctionalCode() {
        StringBuffer sb=new StringBuffer();
        Iterator it=matchingRecords.iterator();
        while (it.hasNext()) {
            ActionRecord rec=(ActionRecord)it.next();
            sb.append("\n    /** performs ");
            sb.append(rec.getName());
            sb.append(" with this node */\n    public void ");
            sb.append(rec.getSmallName());
            sb.append("() {\n        ");
            sb.append(rec.getSmallName());
            sb.append("Action.perform(this);\n    }\n");
        }
        return sb.toString();
    }
    
    private String getVerificationCode() {
        StringBuffer sb=new StringBuffer();
        Iterator it=matchingRecords.iterator();
        if (it.hasNext()) {
            sb.append("            ");
            sb.append(((ActionRecord)it.next()).getSmallName());
            sb.append("Action");
        }
        while (it.hasNext()) {
            sb.append(",\n            ");
            sb.append(((ActionRecord)it.next()).getSmallName());
            sb.append("Action");
        }
        return sb.toString();
    }
    
    static void replace(StringBuffer sb, String x, String y) {
        int i;
        while ((i=sb.toString().indexOf(x))>=0) {
            sb.delete(i,i+x.length());
            sb.insert(i,y);
        }
    }
    
    public String getSourceCode() {
        StringBuffer sb=new StringBuffer(nodeTemplate);
        replace(sb, "__IMPORTS__", getImportCode());
        replace(sb, "__DECLARATION__", getDeclarationCode());
        replace(sb, "__FUNCTIONAL__", getFunctionalCode());
        replace(sb, "__VERIFICATION__", getVerificationCode());
        replace(sb, "__PACKAGE__", nodePackage);
        replace(sb, "__NAME__", nodeName);
        replace(sb, "__DATE__", new SimpleDateFormat().format(new Date()));
        replace(sb, "__USER__", System.getProperty("user.name"));
        return sb.toString();
    }
    
    public List getActionRecords() {
        return matchingRecords;
    }
    
    public void saveNewSources(String packagesRootDir) throws FileNotFoundException {
        Iterator it = matchingRecords.iterator();
        while (it.hasNext()) {
            Object o=it.next();
            if (o instanceof NewActionRecord) {
                NewActionRecord rec=(NewActionRecord)o;
                if (!rec.isInline()) {
                    File f=new File(packagesRootDir, rec.getFullClassName().replace('.', '/')+".java");
                    f.getParentFile().mkdirs();
                    PrintStream out=new PrintStream(new FileOutputStream(f));
                    out.print(rec.getSourceCode());
                    out.close();
                }
            }
        }
        File f=new File(packagesRootDir, nodePackage.replace('.', '/')+"/"+nodeName+".java");
        f.getParentFile().mkdirs();
        PrintStream out=new PrintStream(new FileOutputStream(f));
        out.print(getSourceCode());
        out.close();
    }
   
    public String toString() {
        return nodePackage+"."+nodeName;
    }
        
    /** Utility field holding list of ChangeListeners. */
    private transient ChangeListener changeListener;
        
    /** Registers PropertyChangeListener to receive events.
     * @param listener The listener to register.
     */
    public synchronized void addChangeListener(ChangeListener listener) {
        changeListener = listener;
    }

    /** Removes ChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeChangeListener(ChangeListener listener) {
        changeListener = null;
    }

    /** Notifies all registered listeners about the event.
     */
    void fireStateChanged(Object source) {
        ChangeEvent e = new ChangeEvent(source);
        try {
            if (changeListener != null) 
                changeListener.stateChanged(e);
        } catch (Exception ex) {}
    }
    
    private DefaultMutableTreeNode node;

    public DefaultMutableTreeNode getNodeDelegate() {
        if (node==null) {
            node=new DefaultMutableTreeNode(this);
        }
        return node;
    }

    public static void main(String[] args) throws Exception {
        NodeGenerator gen=new NodeGenerator("org.mypackage.actions", "org.mypackage.nodes", "MyNewNode", new JPopupMenuOperator(), false, false);
        NodeEditorPanel.showDialog(gen);
        //gen.saveNewSources("c:/xxx");
    }
}
