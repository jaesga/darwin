package models.factory;

import com.google.gson.JsonObject;
import com.mongodb.*;
import models.Config;
import models.Constants;
import models.api.APIClient;
import models.api.MongoAPIClient;
import models.api.errors.ErrorBase;
import models.changelog.ChangelogPoint;
import models.changelog.MongoChangeLogPoint;
import models.exception.DarwinErrorException;
import models.logger.Logger;
import models.logger.MongoLogger;
import models.roles.MongoUserRole;
import models.roles.UserRole;
import models.token.MongoToken;
import models.token.Token;
import models.token.TokenType;
import models.token.TokenTypeBase;
import models.user.MongoUser;
import models.user.User;
import models.user.UsersContainer;
import models.utils.AuthUtils;
import org.apache.commons.lang.StringUtils;
import play.Play;

import java.net.UnknownHostException;
import java.util.*;

public class MongoFactory extends DarwinFactory {

    private static final String MONGO_SCHEME = "mongodb://";
    private static MongoClient mongoClient = null;
    private static MongoClientOptions.Builder mongoClientOptionsBuilder = null;

    static {
        int connectionsPerHost = Play.configuration.getProperty("Config.maxConnectionsPerHost") != null ? Integer.parseInt(Play.configuration.getProperty("Config.maxConnectionsPerHost")) : 100;
        mongoClientOptionsBuilder = MongoClientOptions.builder().connectionsPerHost(connectionsPerHost);
    }

    /**
     * returns the established mongo connection, or create one to localhost
     * @return Mongo
     */
    public static Mongo createMongoClient() {
        return createMongoClient(null);
    }

    /**
     * Returns the established mongo connection, or creates one.
     * If a connection already exists, the mongoURI param is ignored, the connection returned will be the one
     * connected to whichever mongoURI was specified first.
     * @param mongoURI the mongo URI to connect to, null to connect to a single instance at localhost:27017.
     *           To connect  to a replica set: "mongodb://127.0.0.1:31000,127.0.0.1:31001,127.0.0.1:31002"
     */
    public static synchronized Mongo createMongoClient(String mongoURI) {
        if(MongoFactory.mongoClient == null) {
            try {
                if(mongoURI == null) {
                    MongoFactory.mongoClient = new MongoClient(new ServerAddress(), mongoClientOptionsBuilder.build());
                } else if (mongoURI.startsWith(MONGO_SCHEME)) {
                    MongoFactory.mongoClient = new MongoClient(new MongoClientURI(mongoURI, mongoClientOptionsBuilder));
                } else {
                    throw new RuntimeException("Invalid mongo URI: " + mongoURI);
                }
            } catch (UnknownHostException e) {
                play.Logger.error("Create mongo client UnknownHostException: " + e.getMessage());
            }
        }
        return MongoFactory.mongoClient;
    }

    /**
     * Get the default database.
     * @return the default database.
     */
    public static DB getDB() {
        return getDB(null, true);
    }

    /**
     * Get the database.
     * @param alias the database alias.
     * @return the database.
     */
    public static DB getDB(String alias) {
        return getDB(alias, true);
    }

    public static DB getDB(String alias, boolean auth){
        String dbName = (alias != null && !alias.isEmpty()) ? Config.getDBName(alias) : Config.getDBName();
        DB db = createMongoClient().getDB(dbName);
        if (auth && !db.isAuthenticated()){
            String user = Play.configuration.getProperty("Config.dbUser","");
            String pass = Play.configuration.getProperty("Config.dbPassword","");
            if (!user.isEmpty() && !pass.isEmpty()) {
                db.authenticate(user, pass.toCharArray());
            }
        }
        return db;
    }

    /** APIClient **/

    @Override
    public APIClient loadAPIClient(String clientId) {
        com.mongodb.DB db = MongoFactory.getDB();
        String apiClientsCollection = Constants.APIClient.COLLECTION_NAME;
        DBCollection clients = db.getCollection(apiClientsCollection);
        BasicDBObject apiClient = new BasicDBObject(Constants.APIClient.FIELD_ID, clientId);
        DBObject u = clients.findOne(apiClient);
        if (u != null) {
            return buildAPIClient(u);
        } else {
            return null;
        }
    }

    @Override
    public APIClient buildAPIClient(String name, String email) {
        return new MongoAPIClient(name, email);
    }

    protected APIClient buildAPIClient(DBObject apiClient) {
        return new MongoAPIClient(apiClient);
    }


    @Override
    public List<APIClient> retrieveUserAPIClients(String userId) {
        List<APIClient> apiClientList = new ArrayList<APIClient>();
        DB db = MongoFactory.getDB();
        DBCollection clients = db.getCollection(Constants.APIClient.COLLECTION_NAME);
        DBCursor cursorClients = clients.find(new BasicDBObject(Constants.APIClient.FIELD_EMAIL, userId));
        for (DBObject u : cursorClients) {
            if (u != null) {
                apiClientList.add(buildAPIClient(u));
            }
        }
        return apiClientList;
    }

    /** Logger **/

    @Override
    public Logger buildLogger() {
        return new MongoLogger();
    }

    /** Token **/

    @Override
    public Token loadToken(String token, TokenType tokenType) throws DarwinErrorException {
        DB db = MongoFactory.getDB();
        DBCollection accounts = db.getCollection(tokenType.getType());
        BasicDBObject storedToken = new BasicDBObject(Constants.Token.FIELD_TOKEN, token);
        storedToken.append(Constants.Token.FIELD_GENERATED, new BasicDBObject("$gt", new Date(System.currentTimeMillis() - tokenType.getExpirationMillis())));
        BasicDBObject u = (BasicDBObject) accounts.findOne(storedToken);
        if (u!=null && u.containsField(Constants.Token.FIELD_EMAIL)) {
            return new MongoToken(u, tokenType);
        }
        throw new DarwinErrorException(ErrorBase.E202, ErrorBase.E202.getMessage());
    }

    @Override
    public Token loadToken(User user, TokenType tokenType) throws DarwinErrorException {
        if (user != null) {
            DB db = MongoFactory.getDB();
            DBCollection accounts = db.getCollection(tokenType.getType());
            BasicDBObject storedToken = new BasicDBObject(Constants.Token.FIELD_EMAIL, user.getEmail());
            if (tokenType.checkExpirationOnLoad()) {
                storedToken.append(Constants.Token.FIELD_GENERATED, new BasicDBObject("$gt", new Date(System.currentTimeMillis() - tokenType.getExpirationMillis())));
            }
            BasicDBObject u = (BasicDBObject) accounts.findOne(storedToken);
            if (u != null && u.containsField(Constants.Token.FIELD_EMAIL)) {
                return new MongoToken(u, tokenType);
            }
        }
        throw new DarwinErrorException(ErrorBase.E202, ErrorBase.E202.getMessage());
    }

    @Override
    public Token buildToken(String email, JsonObject data, TokenType tokenType) {
        String token = AuthUtils.generateToken(tokenType.getLength());
        return new MongoToken(email, token, tokenType, data);
    }

    @Override
    public List<Token> retrieveTokens(TokenType tokenType) {
        List<Token> tokenList = new ArrayList<Token>();
        DB db = MongoFactory.getDB();
        DBCollection tokens = db.getCollection(tokenType.getType());
        DBCursor list = tokens.find();
        for (DBObject u : list){
            if (u != null) {
                tokenList.add(new MongoToken(u, tokenType));
            }
        }
        return tokenList;
    }

    /** User **/

    @Override
    public User loadUser(String email) {
        if (email != null && !email.isEmpty()) {
            DB db = MongoFactory.getDB();
            String userCollectionName = Constants.User.COLLECTION_NAME;
            DBCollection users = db.getCollection(userCollectionName);
            BasicDBObject userQuery = new BasicDBObject(Constants.User.FIELD_EMAIL, email.toLowerCase());
            DBObject user = users.findOne(userQuery);
            if (user != null) {
                return buildUser(user);
            }
        }
        return null;
    }

    @Override
    public User loadUserByActivationToken(String token) {
        if (token != null && !token.isEmpty()) {
            try {
                Token userToken = DarwinFactory.getInstance().loadToken(token, TokenTypeBase.ACTIVATE_ACCOUNT);
                return loadUser(userToken.getEmail());
            } catch (DarwinErrorException e) {
                play.Logger.error(e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public User loadUserByResetToken(String token) {
        if (token != null && !token.isEmpty()) {
            try {
                Token userToken = DarwinFactory.getInstance().loadToken(token, TokenTypeBase.RESET_PASSWORD);
                return loadUser(userToken.getEmail());
            } catch (DarwinErrorException e) {
                play.Logger.error(e.getMessage());
                return null;
            }
        }
        return null;
    }


    @Override
    public User buildUser(String name, String email, String password) {
        return new User(new MongoUser(name, email, password));
    }

    protected User buildUser(DBObject user) {
        return new User(new MongoUser(user));
    }

    @Override
    public UsersContainer retrieveUsers(String field, Object value, int limit, int offset) {
        Map<String, Object> query = new HashMap<String, Object>();
        if (field != null && value != null) {
            query.put(field, value);
        }
        return retrieveUsers(query, limit, offset);
    }

    @Override
    public UsersContainer retrieveUsers(Map<String, Object> query, int limit, int offset) {
        offset = offset > 0 ? ((offset-1)*limit) : 0;
        DB db = MongoFactory.getDB();
        String userCollectionName = Constants.User.COLLECTION_NAME;
        DBCollection accounts = db.getCollection(userCollectionName);
        DBCursor cursor;
        DBObject queryObject = buildUsersQuery(query);
        if (queryObject != null) {
            cursor = accounts.find(queryObject);
        } else {
            cursor = accounts.find();
        }
        int countBeforeLimit = cursor.count();
        cursor.skip(offset);
        cursor.limit(limit);
        cursor.sort(new BasicDBObject(Constants.User.FIELD_CREATED, 1));
        List<User> userList = new ArrayList<User>();
        while (cursor.hasNext()) {
            userList.add(buildUser(cursor.next()));
        }
        return new UsersContainer(userList, countBeforeLimit);
    }

    private DBObject buildUsersQuery(Map<String, Object> query) {
        BasicDBObject criteria = null;
        if (query != null && !query.isEmpty()) {
            criteria = new BasicDBObject();
            for (Map.Entry<String, Object> queryEntry : query.entrySet()) {
                criteria.append(queryEntry.getKey(), queryEntry.getValue());
            }
        }
        return criteria;
    }

    @Override
    public List<User> retrieveUsersByMobileConnectToken(String token) {
        List<User> userList = new ArrayList<User>();
        if (token != null && !token.isEmpty()) {
            DB db = MongoFactory.getDB();
            String userCollectionName = Constants.User.COLLECTION_NAME;
            DBCollection users = db.getCollection(userCollectionName);
            DBCursor cursor = users.find(new BasicDBObject(Constants.User.FIELD_MOBILE_CONNECT_ID, token));
            while (cursor.hasNext()) {
                userList.add(buildUser(cursor.next()));
            }
        }
        return userList;
    }

    /** UserRole **/

    @Override
    public UserRole loadUserRole(String id) {
        if (id != null && !id.isEmpty()) {
            DB db = MongoFactory.getDB();
            String roleCollectionName = Constants.UserRole.COLLECTION_NAME;
            DBCollection role = db.getCollection(roleCollectionName);
            BasicDBObject roleQuery = new BasicDBObject(Constants.UserRole.FIELD_ID, id);
            DBObject user = role.findOne(roleQuery);
            if (user != null) {
                return buildUserRole(user);
            }
        }
        return null;
    }

    @Override
    public UserRole buildUserRole(String name) {
        return new MongoUserRole(name);
    }

    @Override
    public UserRole buildUserRole(String id, String name, Set<String> permissions) {
        return new MongoUserRole(id, name, permissions);
    }

    protected UserRole buildUserRole(DBObject role) {
        return new MongoUserRole(role);
    }

    @Override
    public List<UserRole> retrieveRoles() {
        List<UserRole> rolesList = new ArrayList<UserRole>();
        DB db = MongoFactory.getDB();
        String rolesCollectionName = Constants.UserRole.COLLECTION_NAME;
        DBCollection roles = db.getCollection(rolesCollectionName);
        DBCursor list = roles.find();
        for (DBObject u : list){
            if (u != null) {
                rolesList.add(new MongoUserRole(u));
            }
        }
        return rolesList;
    }

    /** Changelog **/

    @Override
    public ChangelogPoint loadChangelogPoint(String id) {
        if (id != null) {
            DBCollection changelogCollection = getDB().getCollection(Constants.ChangeLogPoint.COLLECTION_NAME);
            DBObject u = changelogCollection.findOne(new BasicDBObject(Constants.ChangeLogPoint.FIELD_ID, id));
            if (u != null) {
                return new MongoChangeLogPoint(u);
            }
        }
        return null;

    }

    @Override
    public ChangelogPoint buildChangelogPoint(String version, Map<String, Map<String, String>> message) {
        return new MongoChangeLogPoint(version, message);
    }

    @Override
    public List<ChangelogPoint> retrieveChangelog(String version) {
        List<ChangelogPoint> changelog = new ArrayList<ChangelogPoint>();
        DBCollection changelogCollection = getDB().getCollection(Constants.ChangeLogPoint.COLLECTION_NAME);
        DBCursor cursor = (StringUtils.isEmpty(version)) ? changelogCollection.find() : changelogCollection.find(new BasicDBObject(Constants.ChangeLogPoint.FIELD_VERSION, version));
        cursor.sort(new BasicDBObject("_id", -1));
        for (DBObject u : cursor){
            if (u != null) {
                changelog.add(new MongoChangeLogPoint(u));
            }
        }
        return changelog;
    }

    @Override
    public List<String> retrieveChangelogVersions() {
        List<String> changelogVersions = new ArrayList<String>();
        DBCollection changelogCollection = getDB().getCollection(Constants.ChangeLogPoint.COLLECTION_NAME);
        List versionsList = changelogCollection.distinct(Constants.ChangeLogPoint.FIELD_VERSION);
        for (Object version : versionsList){
            if (version != null) {
                changelogVersions.add(version.toString());
            }
        }
        return changelogVersions;
    }

    @Override
    public void resetUsersChangelog() {
        DBCollection users = getDB().getCollection(Constants.User.COLLECTION_NAME);
        BasicDBObject updates = new BasicDBObject();
        updates.append(Constants.User.FIELD_CHANGELOG_READ, false);
        users.update(new BasicDBObject(Constants.User.FIELD_CHANGELOG_READ, true), new BasicDBObject("$set", updates));
    }
}
