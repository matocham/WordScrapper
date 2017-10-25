package matocham.comparator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by Mateusz on 03.04.2017.
 */
public class NodeWrapper {
    String name;
    Node node;

    public NodeWrapper(Node node) {
        Node kNode = null;
        NodeList childNodes = node.getChildNodes();
        for(int i=0;i<childNodes.getLength();i++){
            if(childNodes.item(i).getNodeName()=="k"){
                kNode = childNodes.item(i);
                break;
            }
        }
        String name = kNode.getTextContent();
        this.name = name;
        this.node = node;
    }

    public String getName() {
        return name;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }

        if (!(obj instanceof NodeWrapper)) {
            return false;
        }
        NodeWrapper second = (NodeWrapper) obj;
        return this.name.equals(second.getName());
    }
}
