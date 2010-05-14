package org.netbeans.modules.wsdlextensions.sap.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A BAPI Object.
 *
 * Example:
 *      id=001691,
 *      type=0,
 *      level=04,
 *      parent=001667,
 *      child=000000,
 *      name=BUS6035,
 *      ext_name=AcctngDocument
 *      short_text=Accounting Document
 *
 * @author jqian
 */
/*
        <BOR_TREE>
           <ID, NUM, 12/>
           <TYPE, CHAR, 8/>
           <NAME, CHAR, 60/>
           <LEVEL, NUM, 4/>
           <PARENT, NUM, 12/>
           <CHILD, NUM, 12/>
           <NEXT, NUM, 12/>
           <EXPANDABLE, CHAR, 2/>
           <INT_ID, CHAR, 20/>
           <EXT_NAME, CHAR, 64/>
           <SHORT_TEXT, CHAR, 150/>
           <IS_MODELLD, CHAR, 2/>
           <IS_IMPLEM, CHAR, 2/>
           <IS_RELEASD, CHAR, 2/>
           <IS_OBSOLET, CHAR, 2/>
           <IS_BUSOBJ, CHAR, 2/>
           <IS_GEN, CHAR, 2/>
        </BOR_TREE>
 */
public class BAPIObject {

    private String id;
    private String name;
    private String extName;
    private String shortText;
    private BAPIObject parent;
    private List<BAPIObject> children;
    private List<BAPIMethod> methods;

    public BAPIObject(String id, String name, String extName, String shortText) {
        this.id = id;
        this.name = name;
        this.extName = extName;
        this.shortText = shortText;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExtName() {
        return extName;
    }

    public String getShortText() {
        return shortText;
    }

    public BAPIObject getParent() {
        return parent;
    }

    public void setParent(BAPIObject parent) {
        this.parent = parent;
    }

    public void addChild(BAPIObject childObject) {
        assert childObject != null;

        if (children == null) {
            children = new ArrayList<BAPIObject>();
        }

        children.add(childObject);
        childObject.setParent(this);
    }

    public void removeChild(BAPIObject childObject) {
        assert childObject != null;
        assert children != null && children.contains(childObject);

        if (children.remove(childObject)) {
            ; // do nothing
        } else  {
            System.out.printf("Failed to remove %s from %s\n", childObject.getName(), getName());
        }
        childObject.setParent(null);
    }

    public void addMethod(BAPIMethod method) {
        assert method != null;

        if (methods == null) {
            methods = new ArrayList<BAPIMethod>();
        }
        methods.add(method);
    }

    public boolean isLeaf() {
        return children == null || children.size() == 0;
    }

    public List<BAPIObject> getChildren() {
        return children;
    }

    public List<BAPIMethod> getMethods() {
        return methods;
    }

    public boolean hasMethods() {
        return methods != null && methods.size() > 0;
    }

    public boolean childOrSelfHasMethods() {
        if (isLeaf()) {
            return hasMethods();
        } else {
            if (hasMethods()) {
                System.out.println("non leaf node has methods: " + getShortText());
            }
            for (BAPIObject child: getChildren()) {
                if (child.childOrSelfHasMethods()) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public String toString() {
        //return getShortText();
        return "BAPIObject [id=" + getID() + ", name=" + getName() +
                ", extName=" + getExtName() + "shortText=" + getShortText() + "]";
    }
}
