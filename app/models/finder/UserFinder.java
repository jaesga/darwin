package models.finder;

import models.Constants;
import play.Logger;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Required;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class UserFinder {

    public enum ActiveState {
        BOTH,
        ACTIVE,
        INACTIVE
    }

    private static Set<String> fieldsAllowed = new HashSet<String>();

    static {
        fieldsAllowed.add(Constants.User.FIELD_NAME);
        fieldsAllowed.add(Constants.User.FIELD_EMAIL);
    }

    public static Set<String> getFieldsAllowed() {
        return fieldsAllowed;
    }

    public static void addFieldAllowed(String field) {
        fieldsAllowed.add(field);
    }

    @CheckWith(FieldCheck.class)
    protected String field;
    protected String value;

    @Required
    protected ActiveState activeState;

    public Map<String, Object> getQuery() {
        Map<String, Object> query = new HashMap<String, Object>();

        if (field != null && value != null) {
            Object value;
            try {
                value = Pattern.compile(this.value, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                Logger.error(e.getMessage());
                value = this.value;
            }
            query.put(field, value);
        }
        if (activeState != null) {
            if (activeState.equals(ActiveState.ACTIVE)) {
                query.put(Constants.User.FIELD_ACTIVE, true);
            } else if (activeState.equals(ActiveState.INACTIVE)) {
                query.put(Constants.User.FIELD_ACTIVE, false);
            }
        }
        return  query;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ActiveState getActiveState() {
        return activeState;
    }

    public void setActiveState(ActiveState activeState) {
        this.activeState = activeState;
    }

    static private class FieldCheck extends Check {
        @Override
        public boolean isSatisfied(Object userFinder, Object field) {
            return fieldsAllowed.contains(field);
        }
    }


}
