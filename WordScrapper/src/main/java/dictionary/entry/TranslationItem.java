package dictionary.entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mateusz on 28.03.2017.
 */

@XmlType(propOrder={"translatedSentence","examples"})
public class TranslationItem {

    String translatedSentence;
    List<Example> examples;

    @XmlElement(name = "deftext") //dtrn - direct translation one word
    public String getTranslatedSentence() {
        return translatedSentence;
    }

    public void setTranslatedSentence(String translatedSentence) {
        this.translatedSentence = translatedSentence;
    }

    @XmlElement(name = "ex")
    public List<Example> getExamples() {
        return examples;
    }

    public void setExamples(List<Example> examples) {
        this.examples = examples;
    }
}
