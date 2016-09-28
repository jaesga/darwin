package models.factory;

import com.google.gson.JsonObject;
import models.api.APIClient;
import models.changelog.ChangelogPoint;
import models.exception.DarwinErrorException;
import models.exception.UndefinedDarwinFactoryException;
import models.logger.Logger;
import models.roles.UserRole;
import models.token.Token;
import models.token.TokenType;
import models.user.User;
import models.user.UsersContainer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DarwinFactory {

    private static DarwinFactory instance;

    public static synchronized DarwinFactory getInstance() throws UndefinedDarwinFactoryException {
        if (instance == null) {
            throw new UndefinedDarwinFactoryException("Undefined DarwinFactory, you must register one at OnApplicationStart job");
        } else {
            return instance;
        }
    }

    public static void registerFactory(DarwinFactory factory) {
        instance = factory;
    }

    /** APIClient **/
    public abstract APIClient loadAPIClient(String clientId);
    public abstract APIClient buildAPIClient(String name, String email);
    public abstract List<APIClient> retrieveUserAPIClients(String userId);

    /** Logger **/
    public abstract Logger buildLogger();

    /** Token **/
    public abstract Token loadToken(String token, TokenType tokenType) throws DarwinErrorException;
    public abstract Token loadToken(User user, TokenType tokenType) throws DarwinErrorException;
    public Token buildToken(String email, TokenType tokenType) {
        return buildToken(email, null, tokenType);
    }
    public abstract Token buildToken(String email, JsonObject data, TokenType tokenType);
    public abstract List<Token> retrieveTokens(TokenType tokenType);

    /** User **/
    public abstract User loadUser(String id);
    public abstract User loadUserByActivationToken(String token);
    public abstract User loadUserByResetToken(String token);
    public abstract User buildUser(String name, String email, String password);
    public abstract UsersContainer retrieveUsers(Map<String, Object> query, int limit, int offset);
    public abstract UsersContainer retrieveUsers(String field, Object value, int limit, int offset);
    public abstract List<User> retrieveUsersByMobileConnectToken(String token);

    /** UserRole **/
    public abstract UserRole loadUserRole(String id);
    public abstract UserRole buildUserRole(String name);
    public abstract UserRole buildUserRole(String id, String name, Set<String> permissions);
    public abstract List<UserRole> retrieveRoles();

    /** Changelog **/
    public abstract ChangelogPoint loadChangelogPoint(String id);
    public abstract ChangelogPoint buildChangelogPoint(String version, Map<String, Map<String, String>> message);
    public abstract List<ChangelogPoint> retrieveChangelog(String version);
    public abstract List<String> retrieveChangelogVersions();
    public abstract void resetUsersChangelog();
}
