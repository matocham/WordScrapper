package dictionary;

import dictionary.builder.XmlExportBuilder;
import dictionary.entry.Translation;
import dictionary.loader.XmlLoader;
import dictionary.properties.MMProperties;
import dictionary.range.Ranges;
import dictionary.scrapper.DikiScrapper;
import dictionary.utils.PaddingUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.MessageFormat;

/**
 * Created by Mateusz on 28.03.2017.
 */
public class Scrapper {
    private static final String FILE_EXTENSION = ".html";
    private static final String USING_ORIGINAL_MESSAGE = " - taking original word!";
    private static final String OK_MESSAGE = " OK";
    private static final String MISSING_MESSAGE = " missing!";
    private static final String OUT_FILE_NAME = MMProperties.getString("scrapper.out.filename");
    private static final String IN_FILE_NAME = MMProperties.getString("scrapper.in.filename");
    private static final boolean USE_ORIGINALS = MMProperties.getBoolean("scrapper.useOriginals");
    private static final boolean ALTERNATIVES_WITH_HYPHENS = MMProperties.getBoolean("scrapper.dict.retry.withHyphens");
    private static final boolean ALTERNATIVES_WITHOUT_HYPHENS = MMProperties.getBoolean("scrapper.dict.retry.withoutHyphens");
    private static final String IGNORING_ALTERNATE_MESSAGE = MMProperties.getString("scrapper.ignoreAlternates");
    private static final int TOTAL_WORD_LENGTH = MMProperties.getInteger("scrapper.log.totalWordLength");
    private static final Ranges IGNORE_INTERNET_TRANSLATION_RANGES = new Ranges(MMProperties.getString("scrapper.ignore.internetTranslation.ranges"));
    private static final Ranges IGNORE_TRANSLATION_RANGES = new Ranges(MMProperties.getString("scrapper.ignore.checkTranslation.ranges"));

    private static final Logger logger = Logger.getLogger(Scrapper.class);
    public static final String TAKING_ORIGINAL_VERSION_FROM_XML_MESSAGE = "Taking original version from xml";

    private static Integer counter;
    private static Integer hitCounter;
    private static Integer missingCounter;
    private static Integer withHyphensHitCounter;
    private static Integer withoutHyphensHitCounter;
    private static Integer alternativesHits;
    private static Integer ignoredCount;

    private static XmlExportBuilder builder;

    public static void main(String[] args) {
        int i = 0;
        String mode = "";
        if (args.length > 0) {
            mode = args[0];
            if (args.length > 1) {
                i = Integer.parseInt(args[1]);
            }
        }
        switch (mode) {
            case "-D":
                downloadTranslations(i);
                break;
            case "-T":
                processDictionary(i);
                break;
            default:
                System.err.println("Please run program with args -D(download)/-T(translate) <startIndex>");
        }
    }

    private static void downloadTranslations(int startIndex) {
        NodeList entries = XmlLoader.getOriginalDictionaryEntries(IN_FILE_NAME);
        for (int i = startIndex; i < entries.getLength(); i++) {
            Node n = entries.item(i);
            String searchedWord = n.getFirstChild().getTextContent().trim();
            DikiScrapper.downloadTranslationPage(searchedWord, i + FILE_EXTENSION);
        }
    }

    private static void processDictionary(int startIndex) {
        builder = new XmlExportBuilder();
        builder.startDocument();
        counter = 0;
        hitCounter = 0;
        missingCounter = 0;
        withHyphensHitCounter = 0;
        withoutHyphensHitCounter = 0;
        alternativesHits = 0;
        ignoredCount = 0;
        long start = System.currentTimeMillis();
        NodeList entries = XmlLoader.getOriginalDictionaryEntries(IN_FILE_NAME);
        for (int i = startIndex; i < entries.getLength(); i++) {
            StringBuilder logMessage = new StringBuilder();
            Node n = entries.item(i);
            String searchedWord = n.getFirstChild().getTextContent().trim();
            logMessage.append(PaddingUtils.padLeft(getWorldString(i, searchedWord), TOTAL_WORD_LENGTH));
            if (IGNORE_TRANSLATION_RANGES.isInAnyRange(i)) {
                if(USE_ORIGINALS){
                    logMessage.append(TAKING_ORIGINAL_VERSION_FROM_XML_MESSAGE);
                    builder.appendNode(n);
                    ignoredCount++;
                }
            } else {
                translate(logMessage, searchedWord, i);
                if (logMessage.indexOf(MISSING_MESSAGE) > 0 && USE_ORIGINALS) {
                    builder.appendNode(n);
                    logMessage.append(USING_ORIGINAL_MESSAGE);
                }
            }
            logger.info(logMessage.toString());
            counter++;
        }

        logger.info(MessageFormat.format("Starting export to file {0} ...", OUT_FILE_NAME));
        builder.export(OUT_FILE_NAME);
        logger.info("Export finished!");
        logger.info("Time : " + ((System.currentTimeMillis() - start) / 1000.0) + "seconds.");
        logger.info("Total words: " + counter + "\thits: " + hitCounter + "\tmisses= " + missingCounter);
        logger.info("alternatives hits: " + alternativesHits + "\thyphens alt. hits: " + withHyphensHitCounter + "\twithout hyp. alt. hits= " + withoutHyphensHitCounter);
        logger.info("ignored word: "+ignoredCount);
    }

    private static String getWorldString(int i, String searchedWord) {
        return new StringBuilder().append(i).append(". ").append(searchedWord).toString();
    }

    private static void translate(StringBuilder logMessage, String searchedWord, int index) {
        Translation tr = DikiScrapper.getTranslationFromFile(searchedWord, index + FILE_EXTENSION);
        if (tr != null) {
            builder.appendTranslation(tr);
            hitCounter++;
            logMessage.append(OK_MESSAGE);
        } else {
            if (IGNORE_INTERNET_TRANSLATION_RANGES.isInAnyRange(index)) {
                logMessage.append(IGNORING_ALTERNATE_MESSAGE).append(MISSING_MESSAGE);
                missingCounter++;
            } else {
                translateAlternatives(logMessage, searchedWord);
            }
        }
    }

    private static void translateAlternatives(StringBuilder logMessage, String searchedWord) {
        Translation tr;
        if (hasHyphensOnRightPosition(searchedWord) && ALTERNATIVES_WITHOUT_HYPHENS) {
            searchedWord = getWordWithoutHyphens(searchedWord);
            tr = DikiScrapper.getOnlineTranslation(searchedWord);
            logMessage.append(" retry without hyphens ... ");
            appendTranslation(logMessage, tr);
            if (tr != null) {
                withoutHyphensHitCounter++;
                alternativesHits++;
            }
        } else if (searchedWord.contains(" ") && ALTERNATIVES_WITH_HYPHENS) {
            searchedWord = searchedWord.replace(" ", "-").trim();
            tr = DikiScrapper.getOnlineTranslation(searchedWord);
            logMessage.append(" retry with hyphens ... ");
            appendTranslation(logMessage, tr);
            if (tr != null) {
                withHyphensHitCounter++;
                alternativesHits++;
            }
        } else {
            logMessage.append(MISSING_MESSAGE);
            missingCounter++;
        }
    }

    private static String getWordWithoutHyphens(String searchedWord) {
        StringBuilder output = new StringBuilder();
        if (searchedWord.length() > 0) {
            output.append(searchedWord.charAt(0));
        }
        for (int i = 1; i < searchedWord.length() - 1; i++) {
            if (searchedWord.charAt(i) == '-') {
                output.append(" ");
            } else {
                output.append(searchedWord.charAt(i));
            }
        }
        if (searchedWord.length() > 1) {
            output.append(searchedWord.charAt(searchedWord.length() - 1));
        }
        return output.toString().trim();
    }

    private static boolean hasHyphensOnRightPosition(String searchedWord) {
        int hyphensCount = 0;
        int edgeHyphensCount = 0;
        if (!searchedWord.contains("-")) {
            return false;
        }
        for (int i = 0; i < searchedWord.length(); i++) {
            if (searchedWord.charAt(i) == '-') {
                hyphensCount++;
            }
        }

        if (searchedWord.startsWith("-")) {
            edgeHyphensCount++;
        }
        if (searchedWord.endsWith("-")) {
            edgeHyphensCount++;
        }
        if (hyphensCount > edgeHyphensCount) {
            return true;
        }
        return false;
    }

    private static void appendTranslation(StringBuilder logMessage, Translation tr) {
        if (tr != null) {
            builder.appendTranslation(tr);
            logMessage.append(OK_MESSAGE);
            hitCounter++;
        } else {
            logMessage.append(MISSING_MESSAGE);
        }
    }
}
