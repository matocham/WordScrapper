package dictionary.loader;

import dictionary.properties.MMProperties;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Created by Mateusz on 28.03.2017.
 */
public class XmlLoader {
    private static final String DICTIONARY_ENTRY_TAG = MMProperties.getString("loader.dictionary.entry");
    private static  final Logger logger = Logger.getLogger(XmlLoader.class);

    public static NodeList getOriginalDictionaryEntries(String fileName) {
        logger.info("Loading dictionary file ...");
        Document doc = getXmlDocument(fileName);
        NodeList nList = doc.getElementsByTagName(DICTIONARY_ENTRY_TAG);
        logger.info("File loaded!");
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
