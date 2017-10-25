package dictionary.builder;

import dictionary.entry.Translation;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.dom.DOMResult;

/**
 * Created by Mateusz on 28.03.2017.
 */
public class TranslationMarshaller{
    private static Marshaller marshaller;

    public static void initializeMarshaller(Class<?> rootClass) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(rootClass);
        marshaller = context.createMarshaller();
        //marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    }

    public static  Document getMarshalledDocument(Translation translation) throws JAXBException {
        DOMResult res = new DOMResult();
        marshaller.marshal(translation, res);
        return (Document) res.getNode();
    }
}
