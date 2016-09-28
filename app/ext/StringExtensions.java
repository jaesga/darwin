package ext;

import models.utils.AuthUtils;
import play.templates.JavaExtensions;

public class StringExtensions extends JavaExtensions {

    public static String hash(String value) {
        return AuthUtils.hashId(value);
    }
}
