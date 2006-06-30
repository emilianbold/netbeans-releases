/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.generator;

/*
 * NodeGenerator.java
 *
 * Created on August 21, 2002, 1:44 PM
 */

import java.io.*;
import java.util.*;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import org.netbeans.core.NbMainExplorer;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JMenuOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.modules.jemmysupport.I18NSupport;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;

import org.openide.src.*;
import org.openide.cookies.SourceCookie;
import org.openide.execution.NbClassLoader;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/** Node Generator main class
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class NodeGenerator {
    
    private static final String nodeTemplate = I18NSupport.filterI18N(NbBundle.getMessage(NodeGenerator.class, "NodeTemplate"));
    private static final String prefixInline = NbBundle.getMessage(NodeGenerator.class, "Prefix_inline");
    private static final String prefixNew = NbBundle.getMessage(NodeGenerator.class, "Prefix_new");
    
    /** abstract class representing action record bean */    
    public abstract class ActionRecord extends Object {

        /** getter of full class name
         * @return String full class name
         */        
        public abstract String getFullClassName();

        /** getter of action name
         * @return String action name
         */        
        public abstract String getName();

        /** getter of popup path
         * @return String popup path
         */        
        public abstract String getPopupPath();
        
        /** getter for constructor source code
         * @return String constructor source code
         */        
        public abstract String getConstructorCode();
        
        /** getter for inline status
         * @return boolean true when action should be generated as inline
         */        
        public abstract boolean isInline();

        /** getter for small version of action name
         * @return String small version of action name
         */        
        public String getSmallName() {
            String s=getName();
            if (s.length()>6 && s.endsWith("Action")) // NOI18N
                return Character.toLowerCase(s.charAt(0))+s.substring(1, s.length()-6);
            else
                return Character.toLowerCase(s.charAt(0))+s.substring(1);
        }

        /** checker if given popup is served by this action
         * @param popupPath String popup path
         * @return boolean result
         */        
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
        
        /** getter of tree node delegate for this action
         * @return DefaultMutableTreeNode
         */        
        public DefaultMutableTreeNode getNodeDelegate() {
            if (node==null) {
                node=new DefaultMutableTreeNode(this);
                NodeGenerator.this.getNodeDelegate().add(node);
            }
            return node;
        }
            
    }

    /** ActionRecord extension representing existing and compiled action */    
    public class ExistingActionRecord extends ActionRecord {
        Action action;
        /** created new ExistingActionRecord instance
         * @param actionInstance Action instance
         */        
        public ExistingActionRecord(Action actionInstance) {
            action=actionInstance;
        }
        
        /** getter of full class name
         * @return String full class name
         */        
        public String getFullClassName() {
            return action.getClass().getName();
        }

        /** getter of action name
         * @return String action name
         */        
        public String getName() {
            String s=getFullClassName();
            return s.substring(s.lastIndexOf('.')+1);
        }
        
        /** getter of popup path
         * @return String popup path
         */        
        public String getPopupPath() {
            return action.getPopupPath();
        }
        
        /** getter for constructor source code
         * @return String constructor source code
         */        
        public String getConstructorCode() {
            return getName()+"()"; // NOI18N
        }
        
        /** returns String representation of this bean
         * @return String representation of this bean
         */        
        public String toString() {
            return getFullClassName();
        }
        
        /** getter for inline status
         * @return boolean true when action should be generated as inline
         */        
        public boolean isInline() {
            return false;
        }
        
    }
    
    /** ActionRecord extension representing new action */    
    public class NewActionRecord extends ActionRecord {
        private String popupPath;
        private String name;
        private String menuPath;
        private String systemActionClass;
        private String shortcuts[];
        private boolean noBlock;
        private boolean inline;
        
        /** creates new NewActionRecord instance
         * @param popupPath String popup path
         * @param shortcut String shortcut name or null
         */        
        public NewActionRecord(String popupPath, String shortcut) {
            this.popupPath=popupPath;
            if (shortcut!=null && shortcut.length()>0) {
                this.shortcuts=new String[]{shortcut};
            }
            name=toJavaIdentifier(i18n.filterI18N(popupPath))+"Action"; // NOI18N
            noBlock=defaultNoBlock;
            inline=defaultInline;
        }
        
        /** getter of full class name
         * @return String full class name
         */        
        public String getFullClassName() {
            if (inline) return null;
            else return actionsPackage+"."+name; // NOI18N
        }
        
        /** getter of action name
         * @return String action name
         */        
        public String getName() {
            return name;
        }
        
        /** setter for name
         * @param newName String name
         */        
        public void setName(String newName) {
            name=newName;
            fireStateChanged(this);
        }

        /** getter for menu path
         * @return String menu path
         */        
        public String getMenuPath() {
            return menuPath;
        }
        
        /** setter for menu path
         * @param menuPath String menu path
         */        
        public void setMenuPath(String menuPath) {
            if (menuPath.equals("null")) menuPath=null; // NOI18N
            this.menuPath = menuPath;
            fireStateChanged(this);
        }
        
        /** getter for system action class
         * @return String system action class
         */        
        public String getSystemActionClass() {
            return systemActionClass;
        }
        
        /** setter for system action class
         * @param systemActionClass String system action class
         */        
        public void setSystemActionClass(String systemActionClass) {
            if (systemActionClass.equals("null")) systemActionClass=null; // NOI18N
            this.systemActionClass = systemActionClass;
            fireStateChanged(this);
        }
        
        /** getter for no block status
         * @return boolean no block status
         */        
        public boolean isNoBlock() {
            return noBlock;
        }
        
        /** setter for no block status
         * @param noBlock boolean no block status
         */        
        public void setNoBlock(boolean noBlock) {
            this.noBlock = noBlock;
            fireStateChanged(this);
        }
        
        /** getter for source code of action
         * @return String source code
         */        
        public String getSourceCode() {
            StringBuffer sb = new StringBuffer();
            sb.append("/*\n * "); // NOI18N
            sb.append(name);
            sb.append(".java\n *\n * Created on "); // NOI18N
            sb.append(new SimpleDateFormat().format(new Date()));
            sb.append("\n */\n\npackage "); // NOI18N
            sb.append(actionsPackage);
            sb.append(";\n\nimport org.netbeans.jellytools.actions.*;\nimport org.netbeans.jellytools.Bundle;\nimport java.awt.event.KeyEvent;\n\n/** "); // NOI18N
            sb.append(name);
            sb.append(" Class\n * @author "); // NOI18N
            sb.append(System.getProperty("user.name")); // NOI18N
            sb.append("\n */\npublic class "); // NOI18N
            sb.append(name);
            sb.append(" extends Action"); // NOI18N
            if (noBlock) sb.append("NoBlock"); // NOI18N
            sb.append(" {\n\n    /** creates new "); // NOI18N
            sb.append(name);
            sb.append(" instance */\n    public "); // NOI18N
            sb.append(name);
            sb.append("() {\n        super("); // NOI18N
            sb.append(getConstructorArgs());
            sb.append(");\n    }\n}\n"); // NOI18N
            return sb.toString();
        }
        
        /** getter for constructor arguments source code
         * @return String constructor arguments source code
         */        
        protected String getConstructorArgs() {
            StringBuffer sb = new StringBuffer();
            if (menuPath!=null && menuPath.length()>0) {
                sb.append(i18n.translatePath(menuPath));
                sb.append(", ");
            } else {
                sb.append("null, "); // NOI18N
            }
            if (popupPath!=null && popupPath.length()>0) {
                sb.append(i18n.translatePath(popupPath));
            } else {
                sb.append("null"); // NOI18N
            }
            if (systemActionClass!=null && systemActionClass.length()>0) {
                sb.append(", \""); // NOI18N
                sb.append(systemActionClass);
                sb.append('\"');
            }
            if (shortcuts!=null && shortcuts.length>0) {
                if (shortcuts.length==1) {
                    sb.append(", new Action.Shortcut("); // NOI18N
                    sb.append(translateShortcut(shortcuts[0]));
                    sb.append(')');
                } else {
                    sb.append(", new Action.Shortcut[] {new Action.Shortcut("); // NOI18N
                    sb.append(translateShortcut(shortcuts[0]));
                    sb.append(')');
                    for (int i=1; i<shortcuts.length; i++) {
                        sb.append(", new Action.Shortcut("); // NOI18N
                        sb.append(translateShortcut(shortcuts[i]));
                        sb.append(')');
                    }
                    sb.append('}');
                }
            }
            return sb.toString();
        }            
        
        private String translateShortcut(String shortcut) {
            StringTokenizer st=new StringTokenizer(shortcut, " ,;-+_"); // NOI18N
            String modifiers=""; // NOI18N
            String key="KeyEvent.VK_UNDEFINED"; // NOI18N
            while (st.hasMoreTokens()) {
                String token=st.nextToken().toUpperCase();
                if (token.equals("CONTROL")||token.equals("CTRL")) modifiers+="|KeyEvent.CTRL_MASK"; // NOI18N
                else if (token.equals("ALT")) modifiers+="|KeyEvent.ALT_MASK"; // NOI18N
                else if (token.equals("ALT")) modifiers+="|KeyEvent.ALT_MASK"; // NOI18N
                else if (token.equals("META")) modifiers+="|KeyEvent.META_MASK"; // NOI18N
                else if (token.equals("SHIFT")) modifiers+="|KeyEvent.SHIFT_MASK"; // NOI18N
                else key="KeyEvent.VK_"+token; // NOI18N
            }
            if (modifiers.length()>1) {
                return modifiers.substring(1)+", "+key; // NOI18N;
            }
            return key;
        }
            
        /** getter of popup path
         * @return String popup path
         */        
        public String getPopupPath() {
            return popupPath;
        }
        
        /** setter for popup path
         * @param popupPath Strin gpopup path
         */        
        public void setPopupPath(String popupPath) {
            if (popupPath.equals("null")) popupPath=null; // NOI18N
            this.popupPath = popupPath;
            fireStateChanged(this);
        }

        /** getter for inline status
         * @return boolean true when action should be generated as inline
         */        
        public boolean isInline() {
            return inline;
        }
        
        /** setter for inline status
         * @param inline boolean inline status
         */        
        public void setInline(boolean inline) {
            this.inline = inline;
            fireStateChanged(this);
        }
        
        /** getter for constructor source code
         * @return String constructor source code
         */        
        public String getConstructorCode() {
            if (inline) return "Action"+(noBlock?"NoBlock":"")+"("+getConstructorArgs()+")"; // NOI18N
            else return name+"()"; // NOI18N
        }
        
        /** returns String representation of this bean
         * @return String representation of this bean
         */        
        public String toString() {
            if (inline) return prefixInline+getConstructorCode(); // NOI18N
            else return prefixNew+getFullClassName(); // NOI18N
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
    private I18NSupport i18n;
    
    private NodeGenerator(String actionsPackage, String nodePackage, String nodeName, boolean defaultInline, boolean defaultNoBlock) {
        this.actionsPackage=actionsPackage;
        this.nodePackage=nodePackage;
        this.nodeName=nodeName;
        this.defaultInline=defaultInline;
        this.defaultNoBlock=defaultNoBlock;
        searchForActions();
        matchingRecords=new ArrayList();
        i18n=new I18NSupport();
    }
    
    /** Creates new instance of NodeGenerator
     * @param actionsPackage String package name for actions
     * @param nodePackage String package name for node
     * @param nodeName String node name
     * @param popupMenu JPopupMenuOperator op popup menu to be grabbed
     * @param defaultInline boolean true makes new actions inline by default
     * @param defaultNoBlock boolean true makes new actions no block by default
     */    
    public NodeGenerator(String actionsPackage, String nodePackage, String nodeName, JPopupMenuOperator popupMenu, boolean defaultInline, boolean defaultNoBlock) {
        this(actionsPackage, nodePackage, nodeName, defaultInline, defaultNoBlock);
        ArrayList paths=new ArrayList();
        tool=new EventTool();
        getPopupPaths("", popupMenu); // NOI18N
    }
    
    private void getPopupPaths(String path, JPopupMenuOperator popup) {
        tool.waitNoEvent(1000);
        MenuElement el[]=popup.getSubElements();
        for (int i=0; i<el.length; i++) {
            Component c=el[i].getComponent();
            if (c.isShowing()) {
                if (c instanceof JMenu && c.isEnabled()) {
                    JMenuOperator m=new JMenuOperator((JMenu)c);
                    m.push();
                    getPopupPaths(path+m.getText()+"|", new JPopupMenuOperator(m.getPopupMenu()));
                } else if (c instanceof JMenuItem) {
                    String text=((JMenuItem)c).getText();
                    if (text!=null && text.length()>0) {
                        ActionRecord rec=getActionRecord(path+text, getShortcutFor((JMenuItem)c));
                        if (!matchingRecords.contains(rec)) {
                            matchingRecords.add(rec);
                        }
                    }
                }
            }
        }
    }
    
    private String getShortcutFor(JMenuItem it) {
        KeyStroke key=it.getAccelerator();
        if (key==null) return null;
        String s=KeyEvent.getKeyModifiersText(key.getModifiers());
        return s+(s.length()>0?"+":"")+KeyEvent.getKeyText(key.getKeyCode()); // NOI18N
    }
    
    /** getter for node name
     * @return String node name
     */    
    public String getNodeName() {
        return nodeName;
    }
    
    /** setter for node name
     * @param name String node name
     */    
    public void setNodeName(String name) {
        nodeName=name;
        fireStateChanged(this);
    }
    
    /** getter for node package
     * @return String node package
     */    
    public String getNodePackage() {
        return nodePackage;
    }
   
    /** getter for actions package
     * @return String actions package
     */    
    public String getActionsPackage() {
        return actionsPackage;
    }
    
    /** getter for defaultInline
     * @return boolean default inline
     */    
    public boolean isDefaultInline() {
        return defaultInline;
    }
    
    /** getter for defaultNoBlock
     * @return boolean defaultNoBlock
     */    
    public boolean isDefaultNoBlock() {
        return defaultNoBlock;
    }
    
    private static EventTool tool;
    
    private ActionRecord getActionRecord(String popupPath, String shortcut) {
        Iterator it = allRecords.iterator();
        ActionRecord record=null;
        while (it.hasNext() && !(record=(ActionRecord)it.next()).isForPopup(popupPath));
        if (record==null || !record.isForPopup(popupPath)) {
            record = new NewActionRecord(popupPath, shortcut);
            allRecords.add(record);
        }
        return record;
    }
    
    private static boolean isAccessible(ClassElement clazz) {
        if (!clazz.isClass() || clazz.isInner()) return false;
        Identifier superc = clazz.getSuperclass();
        if (superc==null) return false;
        if (!superc.getFullName().equals("org.netbeans.jellytools.actions.Action") && // NOI18N
        !superc.getFullName().equals("org.netbeans.jellytools.actions.ActionNoBlock")) return false; // NOI18N
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
        ClassLoader loader=new NbClassLoader(Repository.getDefault().toArray(), Action.class.getClassLoader());
        while (fsystems.hasMoreElements()) {
            Enumeration fobjects = ((FileSystem)fsystems.nextElement()).getRoot().getData(true);
            while (fobjects.hasMoreElements()) {
                FileObject fo = (FileObject)fobjects.nextElement();
                if (fo.getName().endsWith("Action")) try { // NOI18N
                    DataObject dob=DataObject.find(fo);
                    SourceCookie source = (SourceCookie)dob.getCookie(SourceCookie.class);
                    if (source!=null) {
                        ClassElement classes[] = source.getSource().getClasses();
                        for (int i=0; i<classes.length; i++) {
                            if (isAccessible(classes[i])) {
                                String className = classes[i].getName().getFullName();
                                Action action = (Action)Class.forName(className, true, loader).getDeclaredConstructor(null).newInstance(null);
                                allRecords.add(new ExistingActionRecord(action));
//                                System.out.println(className);
                            }
                            
                        }
                    }
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
//                    e.printStackTrace();
                }
            }
        }
    }
    
    private String getImportCode() {
        TreeMap imports=new TreeMap();
        imports.put("org.netbeans.jellytools.actions", "*"); // NOI18N
        imports.put("org.netbeans.jellytools.nodes", "Node"); // NOI18N
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
                            imports.put(pack, "*"); // NOI18N
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
            sb.append("import "); // NOI18N
            sb.append(s);
            sb.append('.');
            sb.append(imports.get(s));
            sb.append(";\n"); // NOI18N
        }
        return sb.toString();
    }
    
    private String getDeclarationCode() {
        StringBuffer sb=new StringBuffer();
        Iterator it=matchingRecords.iterator();
        while (it.hasNext()) {
            ActionRecord rec=(ActionRecord)it.next();
            sb.append("    private static final Action "); // NOI18N
            sb.append(rec.getSmallName());
            sb.append("Action = new "); // NOI18N
            sb.append(rec.getConstructorCode());
            sb.append(";\n"); // NOI18N
        }
        return sb.toString();
    }
    
    private String getFunctionalCode() {
        StringBuffer sb=new StringBuffer();
        Iterator it=matchingRecords.iterator();
        while (it.hasNext()) {
            ActionRecord rec=(ActionRecord)it.next();
            sb.append("\n    /** performs "); // NOI18N
            sb.append(rec.getName());
            sb.append(" with this node */\n    public void "); // NOI18N
            sb.append(rec.getSmallName());
            sb.append("() {\n        "); // NOI18N
            sb.append(rec.getSmallName());
            sb.append("Action.perform(this);\n    }\n"); // NOI18N
        }
        return sb.toString();
    }
    
    private String getVerificationCode() {
        StringBuffer sb=new StringBuffer();
        Iterator it=matchingRecords.iterator();
        if (it.hasNext()) {
            sb.append("            "); // NOI18N
            sb.append(((ActionRecord)it.next()).getSmallName());
            sb.append("Action"); // NOI18N
        }
        while (it.hasNext()) {
            sb.append(",\n            "); // NOI18N
            sb.append(((ActionRecord)it.next()).getSmallName());
            sb.append("Action"); // NOI18N
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
    
    /** getter for source code of node
     * @return String source code
     */    
    public String getSourceCode() {
        StringBuffer sb=new StringBuffer(nodeTemplate);
        replace(sb, "__IMPORTS__", getImportCode()); // NOI18N
        replace(sb, "__DECLARATION__", getDeclarationCode()); // NOI18N
        replace(sb, "__FUNCTIONAL__", getFunctionalCode()); // NOI18N
        replace(sb, "__VERIFICATION__", getVerificationCode()); // NOI18N
        replace(sb, "__PACKAGE__", nodePackage); // NOI18N
        replace(sb, "__NAME__", nodeName); // NOI18N
        replace(sb, "__DATE__", new SimpleDateFormat().format(new Date())); // NOI18N
        replace(sb, "__USER__", System.getProperty("user.name")); // NOI18N
        return sb.toString();
    }
    
    /** getter for list of all action records
     * @return List of ActionRecords
     */    
    public List getActionRecords() {
        return matchingRecords;
    }
    
    /** saves all new sources (node and actions), refreshes target folder
     * and opens source for node in editor
     * @param targetDataFolder DataFolder of the root of packages where sources have to be saved
     * @throws IOException when some problem with directories occures
     * @throws DataObjectNotFoundException when no DataObject is found for generated node
     */    
    public void saveNewSources(DataFolder targetDataFolder) throws IOException, DataObjectNotFoundException {
        Iterator it = matchingRecords.iterator();
        while (it.hasNext()) {
            Object o=it.next();
            if (o instanceof NewActionRecord) {
                NewActionRecord rec=(NewActionRecord)o;
                if (!rec.isInline()) {
                    // create action
                    FileObject fo = FileUtil.createData(targetDataFolder.getPrimaryFile(), rec.getFullClassName().replace('.', '/')+".java"); // NOI18N
                    PrintStream out = new PrintStream(fo.getOutputStream(fo.lock()));
                    // write generated source to a stream
                    out.println(rec.getSourceCode());
                    out.close();
                }
            }
        }
        // create node
        FileObject fo = FileUtil.createData(targetDataFolder.getPrimaryFile(), nodePackage.replace('.', '/')+"/"+nodeName+".java"); // NOI18N
        PrintStream out = new PrintStream(fo.getOutputStream(fo.lock()));
        // write generated source to a stream
        out.println(getSourceCode());
        out.close();
        // refresh target data folder
        targetDataFolder.getPrimaryFile().refresh();
        // open FileObject representing generated node in source editor
        openInEditor(fo);
    }
    
    /** Opens given FileObject in source editor and selects node in explorer. 
     * It is expected that FileObject is java DataObject and has EditorCookie.
     * @param fo FileObject to be opened in source editor. It should be java
     * DataObject
     */
    private void openInEditor(FileObject fo) throws DataObjectNotFoundException {
        DataObject dob = DataObject.find(fo);
        // selects node in explorer
        NbMainExplorer.RepositoryTab.getDefaultRepositoryTab().doSelectNode(dob);
        // open in editor
        ((EditorCookie)dob.getCookie(EditorCookie.class)).open();
    }

   
    /** returns String representation of Node bean
     * @return String repreentation of Node bean
     */    
    public String toString() {
        return nodePackage+"."+nodeName; // NOI18N
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

    /** getter for tree node delegate
     * @return DefaultMutableTreeNode
     */    
    public DefaultMutableTreeNode getNodeDelegate() {
        if (node==null) {
            node=new DefaultMutableTreeNode(this);
        }
        return node;
    }
}
