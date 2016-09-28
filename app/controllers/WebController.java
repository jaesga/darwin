package controllers;

import models.factory.DarwinFactory;
import models.user.User;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.i18n.Lang;
import play.mvc.Before;
import play.mvc.Http;

import java.util.Arrays;
import java.util.List;

public class WebController extends DarwinController {

    public static final Boolean DEFAULT_NAV_ACTIVATED = Play.configuration.getProperty("Config.defaultNav") != null ? Boolean.valueOf(Play.configuration.getProperty("Config.defaultNav")) : true;
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String LANGUAGE_SESSION_KEY = "lang";
    private static List<String> supportedLanguages = null;

    static {
        //init supportedLanguages
        String languageConfiguration = Play.configuration.getProperty("application.langs");
        if (!StringUtils.isEmpty(languageConfiguration)) {
            supportedLanguages = Arrays.asList(languageConfiguration.split(","));
        } else {
            supportedLanguages = Arrays.asList("en");
        }
    }
    
    @Before
    private static void setUp() {
        configureLanguage();
    }

    private static void loadSessionUser() {
        if (session.contains("username")) {
            User user = DarwinFactory.getInstance().loadUser(session.get("username"));
            if (user != null) {
                renderArgs.put("currentUser", user);
            } else {
                Security.doLogout();
            }
        }
    }

    public static User getCurrentUser() {
        if (renderArgs.get("currentUser") == null)  {
            loadSessionUser();
        }

        if (renderArgs.get("currentUser")!= null) {
            return (User) renderArgs.get("currentUser");
        }else{
            return null;
        }
    }

    protected static void configureLanguage() {
        String sessionLang;
        User user = getCurrentUser();
        if (user != null) {
            String userPreferredLang = user.getPreferredLang();
            boolean validUserPreferredLang = false;
            if (userPreferredLang != null) {
                userPreferredLang = userPreferredLang.toLowerCase();
                validUserPreferredLang = StringUtils.isNotEmpty(userPreferredLang) &&  supportedLanguages.contains(userPreferredLang);
            }

            sessionLang = session.get(LANGUAGE_SESSION_KEY);
            if (StringUtils.isEmpty(sessionLang) && validUserPreferredLang) {
                session.put(LANGUAGE_SESSION_KEY, userPreferredLang);
            } else if (StringUtils.isNotEmpty(sessionLang) && !sessionLang.equals(userPreferredLang)) {
                user.setPreferredLang(sessionLang);
                user.save();
            }
        }

        sessionLang = session.get(LANGUAGE_SESSION_KEY);
        if (StringUtils.isNotEmpty(sessionLang) && supportedLanguages.contains(sessionLang.toLowerCase())) {
            Lang.change(sessionLang.toLowerCase());
        }
    }

    public static void setLanguage(String language) {
        checkAuthenticity();
        Http.Header referer = request.headers.get("referer");
        
        language = language.toLowerCase();
        if (supportedLanguages.contains(language)) {
            session.put(LANGUAGE_SESSION_KEY, language);
        } else {
            session.put(LANGUAGE_SESSION_KEY, DEFAULT_LANGUAGE);
        }
        if (referer != null) {
            redirect(referer.value());
        } else {
            redirect(getDefaultURLForRedirection());
        }
    }

    public static String getLanguage() {
        return session.contains(LANGUAGE_SESSION_KEY) ? session.get(LANGUAGE_SESSION_KEY) : DEFAULT_LANGUAGE;
    }

    protected static String getDefaultURLForRedirection() {
        return Play.configuration.getProperty("application.baseUrl");
    }

    public static List<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    public static boolean getDefaultNavActivated() {
        return DEFAULT_NAV_ACTIVATED;
    }



}
