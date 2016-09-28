package models.notifiers;

import notifiers.DarwinMailer;
import play.Play;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TranslatedTemplatePathResolver {

    private static final String DEFAULT_LANGUAGE_CODE = "en";
    private static final String MODULE_MAILER_FOLDER = DarwinMailer.class.getSimpleName();
    private static final String TEMPLATE_EXTENSION = ".html";

    private List<String> userDefinedExclusions = null;
    private String mailerFolder;
    private String lang;

    public TranslatedTemplatePathResolver() {
        this(MODULE_MAILER_FOLDER, DEFAULT_LANGUAGE_CODE);
    }

    public TranslatedTemplatePathResolver(String lang) {
        this(MODULE_MAILER_FOLDER, lang);
    }

    public TranslatedTemplatePathResolver(String mailerFolder, String lang) {
        this(null, mailerFolder, lang);
    }

    public TranslatedTemplatePathResolver(List<String> userDefinedExclusions, String mailerFolder, String lang) {
        this.userDefinedExclusions = (userDefinedExclusions != null) ? userDefinedExclusions : new ArrayList<String>();
        this.mailerFolder = mailerFolder;
        this.lang = (isSupported(lang)) ? lang : DEFAULT_LANGUAGE_CODE;
    }

    public String getEmailContent(String template) {
        return getStaticContentPathWithoutExtension(mailerFolder, template);
    }

    public String getInternationalizedEmailContent(String template) {
        return mailerFolder + "/" + template;
    }

    private String getStaticContentPath(String controllerName, String template) {
        return controllerName + "/" + lang + "/" + template + TEMPLATE_EXTENSION;
    }

    private String getStaticContentPathWithoutExtension(String controllerName, String template) {
        return getStaticContentPath(controllerName, template).replace(TEMPLATE_EXTENSION, "");
    }

    private boolean isSupported(String languageCode) {
        String playLanguageConfiguration = getPlayLanguageConfiguration();
        List<String> supportedLanguages = Arrays.asList(playLanguageConfiguration.split(","));
        return supportedLanguages.contains(languageCode);
    }

    private String getPlayLanguageConfiguration() {
        return Play.configuration.getProperty("application.langs");
    }

    private boolean isExcluded(String languageCode) {
        return userDefinedExclusions.contains(languageCode);
    }
}
