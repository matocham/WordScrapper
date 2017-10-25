package dictionary.entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mateusz on 28.03.2017.
 */

@XmlType(propOrder = {"speechPart", "translationItems"})
public class TranslationEntry {

    List<TranslationItem> translationItems;
    SpeechPart speechPart;

    @XmlElement(name = "def")
    public List<TranslationItem> getTranslationItems() {
        return translationItems;
    }

    public void setTranslationItems(List<TranslationItem> translationItems) {
        this.translationItems = translationItems;
    }

    @XmlElement(name = "gr")
    public SpeechPart getSpeechPart() {
        return speechPart;
    }

    public void setSpeechPart(SpeechPart speechPart) {
        this.speechPart = speechPart;
    }
}
