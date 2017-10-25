package matocham.loader;

import matocham.comparator.NodeWrapper;
import matocham.properties.MMProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mateusz on 28.03.2017.
 */
public class XmlLoader {
    private static final String LEXICON_TAG_NAME = MMProperties.getString("export.tag.lexicon");

    public static List<NodeWrapper> getNodesAsList(NodeList nodes){
        List<NodeWrapper> allNodes = new ArrayList<>();
        for(int i=0; i<nodes.getLength();i++){
            Node n = nodes.item(i);
            if(n.getNodeType()==Node.ELEMENT_NODE)
            allNodes.add(new NodeWrapper(nodes.item(i)));
        }
        return allNodes;
    }

    public static NodeList getDictionaryEntries(String fileName) {
        Document doc = getXmlDocument(fileName);
        Node lexicon = doc.getElementsByTagName(LEXICON_TAG_NAME).item(0);
        NodeList nList = lexicon.getChildNodes();
        return nList;
    }

    private static Document getXmlDocument(String fileName) {
        Document doc = null;
        try {
            File fXmlFile = new File(fileName);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return doc;
    }
}
