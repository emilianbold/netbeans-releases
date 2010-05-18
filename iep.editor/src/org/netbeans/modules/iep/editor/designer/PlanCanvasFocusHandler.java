package org.netbeans.modules.iep.editor.designer;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.FocusManager;

import com.nwoods.jgo.JGoDocument;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;

public class PlanCanvasFocusHandler {

    private PlanCanvas mCanvas;
    
    private EntityNode mCurrentFocusedEntity;
    
    public PlanCanvasFocusHandler(PlanCanvas canvas) {
        this.mCanvas = canvas;
        
        
    }
    
    public void moveFocusToNextOperator() {
        mCurrentFocusedEntity = findNextEntityNode();
        if(mCurrentFocusedEntity != null) {
            mCanvas.mouseSelect(mCurrentFocusedEntity);
            mCanvas.scrollRectToVisible(mCurrentFocusedEntity.getBoundingRect());
        }
    }
    
    public void moveFocusToPreviousOperator() {
        
    }
    
    private EntityNode findNextEntityNode() {
        EntityNode node = null;
        
        EntityNode startnode = null;
        JGoObject obj = mCanvas.getSelection().getPrimarySelection();
        if (obj != null && obj instanceof EntityNode) {
            startnode = (EntityNode)obj;
        }
        
        Dimension docSize = mCanvas.getDocumentSize();
        Point nextPoint = new Point(0, 0);
        
        
        if (startnode != null) {
            nextPoint = new Point(startnode.getLocation());
            nextPoint = mCanvas.viewToDocCoords(nextPoint);
        }
        
        JGoObject nextObject = null;
        
        //first go width wise
        while(nextPoint.x < docSize.width) {
            nextObject = mCanvas.pickDocObject(nextPoint, true);
            if(nextObject != null && nextObject instanceof EntityNode) {
                if(!nextObject.equals(mCurrentFocusedEntity)) {
                    node = (EntityNode) nextObject;
                    return node;
                } else {
                    //shift x by 10 pixel and try again
                    nextPoint.x = nextPoint.x + 10;
                }
                
            }
        }
        
        nextPoint.x = 0;
        //then go height wise
        while(nextPoint.y < docSize.height) {
            nextObject = mCanvas.pickDocObject(nextPoint, true);
            if(nextObject != null && nextObject instanceof EntityNode) {
                if(!nextObject.equals(mCurrentFocusedEntity)) {
                    node = (EntityNode) nextObject;
                    return node;
                } else {
                    //shift y by 10 pixel and try again
                    nextPoint.y = nextPoint.y + 10;
                }
                
            }
        }
        
        if(node == null) {
            node = findFirstNode();
        }
        
        return node;
        
//        JGoListPosition pos = startpos;
//        if (pos != null) {
//            pos = doc.getNextObjectPosAtTop(pos);
//        }
//        
//        while (pos != null) {
//            obj = doc.getObjectAtPos(pos);
//            pos = doc.getNextObjectPosAtTop(pos);
//            
//            if (obj instanceof EntityNode) {
//                EntityNode pn = (EntityNode)obj;
//                mCanvas.mouseSelect(pn);
//                mCanvas.scrollRectToVisible(pn.getBoundingRect());
//            }
//        }
//        
//        pos = doc.getFirstObjectPos();
//        while (pos != null && pos != startpos) {
//            obj = doc.getObjectAtPos(pos);
//            pos = doc.getNextObjectPosAtTop(pos);
//            
//            if (obj instanceof EntityNode) {
//                EntityNode pn = (EntityNode)obj;
//                mCanvas.mouseSelect(pn);
//                mCanvas.scrollRectToVisible(pn.getBoundingRect());
//            }
//        }
//        
//        
    }
    
    
    private EntityNode findPreviousEntityNode() {
        EntityNode node = null;
        
        return node;
    }
    
    private EntityNode findFirstNode() {
        EntityNode node = null;
        Point nextPoint = new Point(0,0);
        Dimension docSize = mCanvas.getDocumentSize();
        
        JGoObject nextObject = null;
        
        //first go width wise
        while(nextPoint.x < docSize.width) {
            nextObject = mCanvas.pickDocObject(nextPoint, true);
            if(nextObject != null && nextObject instanceof EntityNode) {
                if(!nextObject.equals(mCurrentFocusedEntity)) {
                    node = (EntityNode) nextObject;
                    return node;
                } else {
                    //shift x by 10 pixel and try again
                    nextPoint.x = nextPoint.x + 10;
                }
                
            }
        }
        
        return null;
    }
}
