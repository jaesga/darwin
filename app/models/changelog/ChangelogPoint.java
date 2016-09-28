package models.changelog;

import models.Constants;
import models.utils.AuthUtils;

import java.util.Map;

public abstract class ChangelogPoint {

    protected String id;
    protected String version;
    protected Map<String, Map<String, String>> message;

    public ChangelogPoint() {}

    public ChangelogPoint(String version, Map<String, Map<String, String>> message) {
        this.id = AuthUtils.generateToken(Constants.ChangeLogPoint.ID_LENGTH);
        this.version = version;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, Map<String, String>> getMessage() {
        return message;
    }

    public void setMessage(Map<String, Map<String, String>> message) {
        this.message = message;
    }

    public String getTitle(String lang) {
        String title = message.get(Constants.ChangeLogPoint.FIELD_TITLE).get(lang);
        if (title == null || title.isEmpty()) {
            title = message.get(Constants.ChangeLogPoint.FIELD_TITLE).get(Constants.ChangeLogPoint.DEFAULT_LANGUAGE);
        }
        return title;
    }

    public String getContent(String lang) {
        String content = message.get(Constants.ChangeLogPoint.FIELD_CONTENT).get(lang);
        if (content == null || content.isEmpty()) {
            content = message.get(Constants.ChangeLogPoint.FIELD_CONTENT).get(Constants.ChangeLogPoint.DEFAULT_LANGUAGE);
        }
        return content;
    }

    public abstract void save();
    public abstract void remove();
}
