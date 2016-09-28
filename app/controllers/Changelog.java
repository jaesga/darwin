package controllers;

import models.Config;
import models.Constants;
import models.changelog.ChangelogPoint;
import models.factory.DarwinFactory;
import models.user.User;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Changelog extends WebSecurityController {

    @Check("ADMIN")
    public static void adminChangelog() {
        List<ChangelogPoint> changelog = DarwinFactory.getInstance().retrieveChangelog(null);
        render(changelog);
    }

    @Check("ADMIN")
    public static void upsert(String id) {
        checkAuthenticity();

        Map<String, String> titleMessages = new HashMap<String, String>();
        Map<String, String> contentMessages = new HashMap<String, String>();
        String version = params.get(Constants.ChangeLogPoint.FIELD_VERSION);
        for (String lang : getSupportedLanguages()) {
            String title = params.get(Constants.ChangeLogPoint.FIELD_TITLE + "_" + lang);
            String content = params.get(Constants.ChangeLogPoint.FIELD_CONTENT + "_" + lang);
            if (StringUtils.isNotEmpty(title) && StringUtils.isNotEmpty(content)) {
                titleMessages.put(lang, title);
                contentMessages.put(lang, content);
            }
        }
        if (!titleMessages.isEmpty() && !contentMessages.isEmpty() && StringUtils.isNotEmpty(version)) {
            Map<String, Map<String, String>> messages = new HashMap<String, Map<String, String>>();
            messages.put(Constants.ChangeLogPoint.FIELD_TITLE, titleMessages);
            messages.put(Constants.ChangeLogPoint.FIELD_CONTENT, contentMessages);

            ChangelogPoint changelogPoint = DarwinFactory.getInstance().loadChangelogPoint(id);
            if (changelogPoint == null) {
                changelogPoint = DarwinFactory.getInstance().buildChangelogPoint(version, messages);
            } else {
                changelogPoint.setVersion(version);
                changelogPoint.setMessage(messages);
            }
            changelogPoint.save();
        }

        adminChangelog();
    }

    @Check("ADMIN")
    public static void remove(String id) {
        checkAuthenticity();
        ChangelogPoint changelogPoint = DarwinFactory.getInstance().loadChangelogPoint(id);
        if (changelogPoint != null) {
            changelogPoint.remove();
        }
        adminChangelog();
    }

    @Check("ADMIN")
    public static void reset() {
        checkAuthenticity();
        DarwinFactory.getInstance().resetUsersChangelog();
        adminChangelog();
    }

    public static void userChangelog() {
        List<ChangelogPoint> changelog = DarwinFactory.getInstance().retrieveChangelog(null);
        User user = getCurrentUser();
        boolean ignoreChangelog = false;
        if (user != null) {
            ignoreChangelog = user.isIgnoreChangelog();
            if (!user.isChangelogRead()) {
                user.setChangelogRead(true);
                user.save();
            }
        }
        render(changelog, ignoreChangelog);
    }

    public static List<ChangelogPoint> getLastChangelog() {
        List<String> versions = DarwinFactory.getInstance().retrieveChangelogVersions();
        String lastVersion = (!versions.isEmpty()) ? versions.get(versions.size() - 1) : null;
        return DarwinFactory.getInstance().retrieveChangelog(lastVersion);
    }

    public static boolean showUserChangelog() {
        if (Config.isChangelogActivated()) {
            User user = getCurrentUser();
            return user != null && !user.isChangelogRead() && !user.isIgnoreChangelog();
        }
        return false;
    }

    public static void changelogRead(Boolean ignore) {
        checkAuthenticity();
        User user = getCurrentUser();
        if (user != null) {
            user.setChangelogRead(true);
            user.setIgnoreChangelog(ignore);
            user.save();
        }
    }
}
