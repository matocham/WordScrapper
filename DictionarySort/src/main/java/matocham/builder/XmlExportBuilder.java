package matocham.builder;

import matocham.properties.MMProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Mateusz on 28.03.2017.
 */
public class XmlExportBuilder {
    private static final String XDXF_ROOT_ELEMENT = MMProperties.getString("export.tag.root");
    private static final String LANG_FROM_ATTR = MMProperties.getString("export.tag.lang.from");
    private static final String LANG_TO_ATTR = MMProperties.getString("export.tag.lang.to");
    private static final String LANG_ENG = MMProperties.getString("export.lang.eng");
    private static final String LANG_POL = MMProperties.getString("export.lang.pol");
    private static final String FORMAT_ATTR = MMProperties.getString("export.tag.format.name");
    private static final String FORMAT_ATTR_LOGICAL = MMProperties.getString("export.tag.format.value");
    private static final String LEXICON_TAG_NAME = MMProperties.getString("export.tag.lexicon");
    private static final String META_INFO_TAG_NAME = MMProperties.getString("export.tag.metaInfo");
    private static final String TITLE_TAG_NAME = MMProperties.getString("export.tag.title.name");
    private static final String FULL_TITLE_TAG_NAME = MMProperties.getString("export.tag.titleFull.name");
    private static final String DIKI_DICTIONARY_TAG_TEXT = MMProperties.getString("export.tag.title.value");
    private static final String DIKI_DICTIONARY_FULL_NAME_TAG_TEXT = MMProperties.getString("export.tag.titleFull.value");
    private static final String CREATION_DATE_TAG_NAME = MMProperties.getString("export.tag.createDate");
    private static final String DATE_FORMAT = MMProperties.getString("export.date.format");
    private static final boolean PRETY_PRINT = MMProperties.getBoolean("export.pretyPrint");
    private static final String INTEND_VALUE = MMProperties.getString("export.intend.value");
    private static final String FULLNAME_TAG_NAME = MMProperties.getString("export.tag.fullName.name");
    private static final String FULLNAME_TAG_VALUE = MMProperties.getString("export.tag.fullName.value");

    Document document;
    Element rootElement;
    Element lexiconElement;

    public XmlExportBuilder() {
    }

    public void startDocument() {
        try {
            createNewDocument();
            createRootElement();
            addMetaInfo();
            addLexiconElement();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void addLexiconElement() {
        lexiconElement = document.createElement(LEXICON_TAG_NAME);
        rootElement.appendChild(lexiconElement);
    }

    private void addMetaInfo() {
        Element metaInfo = document.createElement(META_INFO_TAG_NAME);

        Element fullName = document.createElement(FULLNAME_TAG_NAME);
        fullName.setTextContent(FULLNAME_TAG_VALUE);
        metaInfo.appendChild(fullName);

        Element title = document.createElement(TITLE_TAG_NAME);
        title.setTextContent(DIKI_DICTIONARY_TAG_TEXT);
        metaInfo.appendChild(title);

        Element fullTitle = document.createElement(FULL_TITLE_TAG_NAME);
        fullTitle.setTextContent(DIKI_DICTIONARY_FULL_NAME_TAG_TEXT);
        metaInfo.appendChild(fullTitle);

        Element createDate = document.createElement(CREATION_DATE_TAG_NAME);
        createDate.setTextContent(new SimpleDateFormat(DATE_FORMAT).format(new Date()));
        metaInfo.appendChild(createDate);

        rootElement.appendChild(metaInfo);
    }

    private void createRootElement() {
        rootElement = document.createElement(XDXF_ROOT_ELEMENT);
        rootElement.setAttribute(LANG_FROM_ATTR, LANG_ENG);
        rootElement.setAttribute(LANG_TO_ATTR, LANG_POL);
        rootElement.setAttribute(FORMAT_ATTR, FORMAT_ATTR_LOGICAL);
        document.appendChild(rootElement);
    }

    private void createNewDocument() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        document = docBuilder.newDocument();
    }

    public void appendNode(Node node){
        Node impotedNode = document.importNode(node, true);
        lexiconElement.appendChild(impotedNode);
    }

    public void export(String fileName) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, PRETY_PRINT ? "yes" : "no");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", INTEND_VALUE);
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(fileName));
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}
