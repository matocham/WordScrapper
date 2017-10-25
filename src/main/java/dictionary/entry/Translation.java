package dictionary.entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mateusz on 28.03.2017.
 */
@XmlRootElement(name = "ar")
@XmlType(propOrder = {"word", "translationEntries", "links"})
public class Translation {
    String word;
    List<TranslationEntry> translationEntries;
    List<String> links;

    @XmlElement(name = "k")
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @XmlElement(name = "def")
    public List<TranslationEntry> getTranslationEntries() {
        return translationEntries;
    }

    public void setTranslationEntries(List<TranslationEntry> translationEntries) {
        this.translationEntries = translationEntries;
    }

    @XmlElement(name = "kref")
    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    public void addToLinks(List<String> newLinks) {
        if (links == null) {
            links = new ArrayList<>();
        }
        links.addAll(newLinks);
    }
}
