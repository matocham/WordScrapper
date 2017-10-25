package dictionary.entry;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class SpeechPart {
    String part;

    /**
     * required by JAXB
     */
    public SpeechPart() {

    }

    public SpeechPart(String part) {
        this.part = part;
    }

    @XmlElement(name = "i")
    public String getPart() {
        return part;
    }
}
