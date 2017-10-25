package dictionary.scrapper;

import dictionary.entry.*;
import dictionary.properties.MMProperties;
import dictionary.utils.PaddingUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mateusz on 29.03.2017.
 */
public class DikiScrapper {
    private static final String BASE_URL = MMProperties.getString("scrapper.baseUrl");
    private static final String DICTIONARY_ENTITY_SELECTOR = MMProperties.getString("scrapper.dict.selector.entity");
    private static final String ORIGINAL_WORDS_CLASS_SELECTOR = MMProperties.getString("scrapper.dict.selector.original");
    private static final String WORD_CLASS_SELECTOR = MMProperties.getString("scrapper.dict.selector.word");
    private static final String STOPWORD_CLASS_SELECTOR = MMProperties.getString("scrapper.dict.selector.stopword");
    private static final String PART_OF_SPEECH_ELEMENT_CLASS_SELECTOR = MMProperties.getString("scrapper.dict.selector.partOfSpeech");
    private static final String MEANING_ELEMENT_SELECTOR = MMProperties.getString("scrapper.dict.selector.meaning");
    private static final String EXAMPLE_SENTENCE_CLASS_SELECTOR = MMProperties.getString("scrapper.dict.selector.example");
    private static final String EXAMPLE_SENTENCE_TRANSLATION_CLASS_SELECTOR = MMProperties.getString("scrapper.dict.selector.example.translation");
    private static final boolean WITH_EXAMPLES = MMProperties.getBoolean("scrapper.dict.withExamples");
    private static final boolean WITH_SINGLE_EXAMPLE = MMProperties.getBoolean("scrapper.dict.withSingleExample");
    private static final boolean ORIGINAL_WITH_STOPWORDS = MMProperties.getBoolean("scrapper.dict.original.withStopWord");
    private static final boolean TRANSLATIONS_WITH_STOPWORDS = MMProperties.getBoolean("scrapper.dict.translation.withStopword");
    private static final String TRANSLATED_ELEMENT_CLASS_SELECTOR = MMProperties.getString("scrapper.dict.selector.translatedElement");
    private static final String TRANSLATED_ELEMENT_CLASS = MMProperties.getString("scrapper.dict.class.translatedElement");
    private static final String TRANSLATION_CONTAINER_CLASS_SELECTOR = MMProperties.getString("scrapper.dict.selector.translationContainer");
    private static final String LINK_TAG_NAME = MMProperties.getString("scrapper.dict.tag.link");
    private static final String TRANSLATED_WORDS_DELIMITER = MMProperties.getString("scrapper.dict.translatedWord.delimiter");
    private static final String ORGINAL_WORDS_DELIMITER = MMProperties.getString("scrapper.dict.originalWord.delimiter");
    private static final String SPACE_URL_REPLACEMENT = MMProperties.getString("scrapper.dict.spaceReplacement");
    private static final int WAIT_TIME = 1000 * MMProperties.getInteger("scrapper.dict.waitTime");
    private static final String TRANSLATION_SAVE_PATH = MMProperties.getString("scrapper.save.baseFilePath");
    private static final String RETRY_MESSAGE = MMProperties.getString("scrapper.retyMessage");
    private static final Logger logger = Logger.getLogger(DikiScrapper.class);
    public static final int TOTAL_WORD_LENGTH = MMProperties.getInteger("scrapper.log.totalWordLength");
    public static final String BASE_DIKI_URI = MMProperties.getString("scrapper.baseUrl.diki");
    private static final boolean IGNORE_LINKS = MMProperties.getBoolean("scrapper.ignoreLinks");

    private static String stripSpaces(String original) {
        return original.trim().replace(" ", SPACE_URL_REPLACEMENT);
    }

    public static Translation getOnlineTranslation(String word) {
        Document document = getTranslationPage(word);
        return getTranslation(word, document);
    }

    public static Translation getTranslationFromFile(String word, String file) {
        Document document = getTranslationPageFromFile(TRANSLATION_SAVE_PATH + file);
        if (document == null) {
            return null;
        }
        return getTranslation(word, document);
    }

    private static Translation getTranslation(String word, Document document) {
        if (!hasResponse(document)) {
            return null;
        }
        Elements translations = getDictionaryEntities(document);
        if (translations == null) {
            return null;
        }
        Element mainTranslation = getMainTranslation(translations, word);
        if (mainTranslation == null) {
            return null;
        }

        Translation translation = parseTranslationEelement(mainTranslation, word);
        if (!IGNORE_LINKS) {
            translations.remove(mainTranslation);
            List<String> additionalWords = getAdditionalWords(translations, word);
            translation.addToLinks(additionalWords);
        }
        return translation;
    }

    private static Element getMainTranslation(Elements translations, String word) {
        for (Element translationEntry : translations) {
            String originalWords = getOriginalWords(translationEntry);
            if (originalWords.equals(word)) {
                return translationEntry;
            }
        }
        Element main = getContainingTranslation(translations, word);
        if (main == null) {
            main = getTranslationWithStopWords(translations, word);
        }

        return main;
    }

    private static Element getContainingTranslation(Elements translations, String word) {
        for (Element translationEntry : translations) {
            String originalWords = getOriginalWords(translationEntry);
            if (originalWords.contains(word)) {
                return translationEntry;
            }
        }
        return null;
    }

    private static Element getTranslationWithStopWords(Elements translations, String word) {
        for (Element translationEntry : translations) {
            String originalWords = getOriginalWordsWithStop(translationEntry);
            if (originalWords.contains(word)) {
                return translationEntry;
            }
        }
        return null;
    }

    private static String getOriginalWordsWithStop(Element translationEntry) {
        Elements originalWords = translationEntry.select(ORIGINAL_WORDS_CLASS_SELECTOR);
        String original = "";
        if (!originalWords.isEmpty()) {
            original = extractOriginalWordsWithStop(originalWords.first());
        }
        return original;
    }

    private static String extractOriginalWordsWithStop(Element wordElement) {
        Elements wordElements = getAllWordsTags(wordElement);

        StringBuilder result = new StringBuilder();
        for (Element word : wordElements) {
            result.append(word.text().trim()).append(ORGINAL_WORDS_DELIMITER);
        }
        if (result.charAt(result.length() - 1) == ',') {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

    private static String getOriginalWords(Element translationEntry) {
        Elements originalWords = translationEntry.select(ORIGINAL_WORDS_CLASS_SELECTOR);
        String original = "";
        if (!originalWords.isEmpty()) {
            original = extractOriginalWords(originalWords.first());
        }
        return original;
    }

    private static String extractOriginalWords(Element wordElement) {
        Elements wordElements = getAllWordsTags(wordElement);

        StringBuilder result = new StringBuilder();
        for (Element word : wordElements) {
            extractWordsFromNode(result, word);
        }
        if (result.charAt(result.length() - 1) == ',') {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

    private static Elements getAllWordsTags(Element wordElement) {
        Elements wordElements = new Elements();
        Elements additionalWords = wordElement.select(WORD_CLASS_SELECTOR);
        for (Element e : additionalWords) {
            Elements links = e.getElementsByTag(LINK_TAG_NAME);
            if (links.isEmpty()) {
                wordElements.add(e);
            } else {
                wordElements.addAll(links);
            }
        }
        return wordElements;
    }

    private static void extractWordsFromNode(StringBuilder result, Element word) {
        String original = word.ownText().trim();
        if (ORIGINAL_WITH_STOPWORDS) {
            original = getWithStopWord(word);
        }
        if (!original.isEmpty()) {
            result.append(original).append(ORGINAL_WORDS_DELIMITER);
        }
    }

    private static String getWithStopWord(Element word) {
        String wholeWord = word.text().trim();
        String stopWord = getStopWord(word);
        if (!stopWord.isEmpty()) {
            wholeWord = wholeWord.replace(stopWord, "[" + stopWord + "]");
        }
        return wholeWord;
    }

    private static String getStopWord(Element word) {
        Element stopWord = word.select(STOPWORD_CLASS_SELECTOR).first();
        if (stopWord != null) {
            return stopWord.ownText().trim();
        }
        return "";
    }

    private static List<String> getAdditionalWords(Elements translations, String word) {
        List<String> additionalOrigs = new ArrayList<>();
        for (Element translation : translations) {
            String additionalWords = getOriginalWords(translation).trim();
            List<String> parsedWords = parseWordsToList(word, additionalWords);
            for (String newWord : parsedWords) {
                if (!additionalOrigs.contains(newWord)) {
                    additionalOrigs.add(newWord);
                }
            }
        }
        return additionalOrigs;
    }

    private static List<String> parseWordsToList(String word, String additionalWords) {
        if (hasForbiddenValue(word, additionalWords)) {
            return new ArrayList<>();
        }
        List<String> wordsInDefinition = new ArrayList<>();
        if (additionalWords.contains(ORGINAL_WORDS_DELIMITER)) {
            String[] allWords = additionalWords.split(ORGINAL_WORDS_DELIMITER);
            for (String ww : allWords) {
                if (hasForbiddenValue(word, ww)) {
                    continue;
                }
                wordsInDefinition.add(ww);
            }
        } else {
            wordsInDefinition.add(additionalWords);
        }
        return wordsInDefinition;
    }

    private static boolean hasForbiddenValue(String excludedWord, String additionalWords) {
        return additionalWords.equals(excludedWord) || additionalWords.contains("-") || additionalWords.length() < 2;
    }

    private static Translation parseTranslationEelement(Element mainTranslation, String originalWord) {
        Translation translation = initializeTranslation(mainTranslation, originalWord);
        List<TranslationEntry> entries = getEntriesDividedByPartOfSpeech(mainTranslation);
        translation.setTranslationEntries(entries);
        return translation;
    }

    private static Translation initializeTranslation(Element mainTranslation, String originalWord) {
        Translation translation = new Translation();
        List<String> links = new ArrayList<>();
        String[] originalWords = getOriginalWords(mainTranslation).split(ORGINAL_WORDS_DELIMITER);
        for (String word : originalWords) {
            if (word.equals(originalWord)) {
                translation.setWord(word);
            } else {
                links.add(word);
            }
        }
        if (translation.getWord() == null) {
            translation = matchWordWithStop(mainTranslation, originalWord);
        }
        if (translation.getWord() == null) {
            if (originalWords.length > 0) {
                translation.setWord(originalWords[0]);
                links.remove(originalWords[0]);
            } else {
                translation.setWord(originalWord);
            }
            translation.setLinks(links);
        }
        return translation;
    }

    private static Translation matchWordWithStop(Element mainTranslation, String original) {
        Translation translation = new Translation();
        List<String> links = new ArrayList<>();
        String[] originalWordsWithStop = getOriginalWordsWithStop(mainTranslation).split(ORGINAL_WORDS_DELIMITER);
        for (String word : originalWordsWithStop) {
            if (word.equals(original)) {
                translation.setWord(word);
            } else {
                links.add(word);
            }
        }
        if (translation.getWord() == null) {
            if (originalWordsWithStop.length > 0) {
                translation.setWord(originalWordsWithStop[0]);
                links.remove(originalWordsWithStop[0]);
            }
        }
        translation.setLinks(links);
        return translation;
    }

    private static List<TranslationEntry> getEntriesDividedByPartOfSpeech(Element mainTranslation) {
        List<TranslationEntry> entries = new ArrayList<>();
        Elements partOfSpeechElements = mainTranslation.select(PART_OF_SPEECH_ELEMENT_CLASS_SELECTOR);
        for (Element partOfSpeech : partOfSpeechElements) {
            TranslationEntry entry = getEntryForSpeechPart(partOfSpeech);
            entries.add(entry);
        }
        return entries;
    }

    private static TranslationEntry getEntryForSpeechPart(Element partOfSpeech) {
        TranslationEntry entry = new TranslationEntry();
        entry.setSpeechPart(new SpeechPart(partOfSpeech.text()));
        Element definitions = partOfSpeech.nextElementSibling();
        while (!definitions.className().equals(TRANSLATED_ELEMENT_CLASS)) {
            definitions = definitions.nextElementSibling();
        }

        List<TranslationItem> translationItems = getTranlationItems(definitions);
        entry.setTranslationItems(translationItems);
        return entry;
    }

    private static List<TranslationItem> getTranlationItems(Element definitions) {
        Elements translationItems = definitions.select(MEANING_ELEMENT_SELECTOR);
        return extractTranslations(translationItems);
    }

    private static List<TranslationItem> extractTranslations(Elements translationItems) {
        List<TranslationItem> items = new ArrayList<>();
        for (Element transItem : translationItems) {
            TranslationItem item = getTranslationItem(transItem);
            items.add(item);
        }
        return items;
    }

    private static TranslationItem getTranslationItem(Element meaningItem) {
        TranslationItem item = new TranslationItem();

        Elements words = getAllWordsTags(meaningItem);
        item.setTranslatedSentence(getTranslatedSentence(words));
        if (WITH_EXAMPLES) {
            appendExamples(meaningItem, item);
        }
        return item;
    }

    private static String getTranslatedSentence(Elements words) {
        StringBuilder sentence = new StringBuilder();
        for (Element element : words) {
            sentence.append(getWordText(element)).append(TRANSLATED_WORDS_DELIMITER).append(" ");
            ;
        }
        return sentence.substring(0, sentence.lastIndexOf(TRANSLATED_WORDS_DELIMITER));
    }

    private static String getWordText(Element element) {
        String content = element.ownText();
        if (TRANSLATIONS_WITH_STOPWORDS) {
            content = getWithStopWord(element);
        }
        return content;
    }

    private static void appendExamples(Element transItem, TranslationItem item) {
        List<Example> examples = getExamples(transItem);
        item.setExamples(examples);
    }

    private static List<Example> getExamples(Element transItem) {
        List<Example> examples = new ArrayList<>();

        Elements sentences = transItem.select(EXAMPLE_SENTENCE_CLASS_SELECTOR);
        if (sentences != null && !sentences.isEmpty()) {
            for (Element sentence : sentences) {
                Example example = getExample(sentence);
                if (example != null) examples.add(example);
                if (WITH_SINGLE_EXAMPLE) break;
            }
        }
        return examples;
    }

    private static Example getExample(Element sentence) {
        Example example = new Example();
        if (sentence == null) {
            return null;
        }
        String originalExample = sentence.ownText();
        example.setOrig(originalExample);

        String translatedExample = getTranslatedExample(sentence);
        example.setTranslated(translatedExample);
        return example;
    }

    private static String getTranslatedExample(Element sentence) {
        String translatedExample = sentence.select(EXAMPLE_SENTENCE_TRANSLATION_CLASS_SELECTOR).text();
        if (translatedExample != null) {
            translatedExample = translatedExample.trim();
        }
        return translatedExample;
    }

    public static void downloadTranslationPage(String word, String file) {
        Connection.Response response;
        String logIndex = file.substring(0, file.lastIndexOf("."));
        try {
            String stripped = stripSpaces(word);
            response = Jsoup.connect(BASE_URL + stripped).execute();
            if (hasResponse(response.parse())) {
                saveToFile(TRANSLATION_SAVE_PATH + file, response);
                logger.info(PaddingUtils.padLeft(logIndex + ". " + word, TOTAL_WORD_LENGTH) + " OK");
            } else {
                logger.info(PaddingUtils.padLeft(logIndex + ". " + word, TOTAL_WORD_LENGTH) + " FAIL");
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn(MessageFormat.format(RETRY_MESSAGE, WAIT_TIME / 1000.0));
            try {
                Thread.sleep(WAIT_TIME);
                downloadTranslationPage(word, file);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    private static void saveToFile(String file, Connection.Response response) throws IOException {
        FileUtils.writeStringToFile(new File(file), response.body(), "UTF-8");
    }

    private static Document getTranslationPage(String word) {
        Document doc = null;
        try {
            String stripped = stripSpaces(word);
            doc = Jsoup.connect(BASE_URL + stripped).get();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(MessageFormat.format(RETRY_MESSAGE, WAIT_TIME / 1000.0));
            try {
                Thread.sleep(WAIT_TIME);
                return getTranslationPage(word);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        return doc;
    }

    private static Document getTranslationPageFromFile(String file) {
        Document doc = null;
        try {
            doc = Jsoup.parse(new File(file), "UTF-8", BASE_DIKI_URI);
        } catch (IOException e) {
        }
        return doc;
    }

    private static boolean hasResponse(Document doc) {
        Element mainEntry = doc.select(TRANSLATION_CONTAINER_CLASS_SELECTOR).first();
        if (mainEntry == null) {
            return false;
        }

        Elements dictionaryEntries = mainEntry.select(DICTIONARY_ENTITY_SELECTOR);
        boolean hasTranslation = false;
        for (Element e : dictionaryEntries) {
            if (!e.select(TRANSLATED_ELEMENT_CLASS_SELECTOR).isEmpty()) {
                hasTranslation = true;
                break;
            }
        }
        return hasTranslation;
    }

    private static Elements getDictionaryEntities(Document doc) {
        Elements translationContainers = doc.select(TRANSLATION_CONTAINER_CLASS_SELECTOR);
        for (Element container : translationContainers) {
            if (!container.select(TRANSLATED_ELEMENT_CLASS_SELECTOR).isEmpty()) {
                Elements entries = container.select(DICTIONARY_ENTITY_SELECTOR);
                return entries;
            }
        }
        return null;
    }
}
