package dictionary.entry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by Mateusz on 28.03.2017.
 */
@XmlType(propOrder={"orig","translated"})
public class Example {

    String orig;
    String translated;

    @XmlElement(name = "ex_orig")
    public String getOrig() {
        return orig;
    }

    public void setOrig(String orig) {
        this.orig = orig;
    }

    @XmlElement(name = "ex_tran")
    public String getTranslated() {
        return translated;
    }

    public void setTranslated(String translated) {
        this.translated = translated;
    }
}
