package org.netbeans.modules.wsdlextensions.ldap.ldif;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class GenerateWSDLAction extends CookieAction {

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0) {
            return;
        }
        DataObject dataObject = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
        // TODO use dataObject
        Iterator it = dataObject.files().iterator();

        while (it.hasNext()) {
            FileObject fo = (FileObject) it.next();
            File projDir = FileUtil.toFile(fo.getParent());
            File wsdlDir = new File(projDir.getAbsolutePath() + File.separator + "ldapwsdls");
            wsdlDir.mkdirs();
            File ldiffile = FileUtil.toFile(fo);
            LdifParser parser = new LdifParser(ldiffile);

            try {
                List objs = parser.parse();

                if (objs != null) {
                    for (int i = 0; i < objs.size(); i++) {
//                        LdifObjectClass objclass = (LdifObjectClass) objs.get(i);
//                        GenerateXSD genXsd = new GenerateXSD(wsdlDir, objclass,
//                                "Search");
//                        GenerateWSDL genWSDL = new GenerateWSDL(wsdlDir,
//                                objclass, "Search");
//                        genXsd.generate();
//                        genWSDL.generate();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            fo.getParent().refresh();
        }
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(GenerateWSDLAction.class, "CTL_GenerateWSDLAction");
    }

    protected Class[] cookieClasses() {
        return new Class[]{
            DataObject.class
        };
    }

    protected String iconResource() {
        return "org/netbeans/modules/wsdlextensions/ldap/resources/ldiffile16x16.png";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }
}

