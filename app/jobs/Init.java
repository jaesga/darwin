package jobs;

import models.Constants;
import models.factory.DarwinFactory;
import models.factory.MongoFactory;
import models.roles.UserRole;
import models.roles.permission.UserPermission;
import models.utils.AuthUtils;
import play.Logger;
import play.Play;
import play.jobs.Job;

import java.util.HashSet;
import java.util.Set;

public class Init extends Job {

    private DarwinFactory darwinFactory;

    public Init() {
        this.darwinFactory = new MongoFactory();
    }

    public Init(DarwinFactory darwinFactory) {
        this.darwinFactory = darwinFactory;
    }

    public void doJob() {
        DarwinFactory.registerFactory(this.darwinFactory);
        createMongoClient();
        createSuperAdminRole();
        createDefaultRole();
        configureSalt();
    }

    private void createSuperAdminRole() {
        Set<String> permissions = UserPermission.getPermissions();
        UserRole userRole = DarwinFactory.getInstance().buildUserRole(Constants.UserRole.SUPER_ADMIN, Constants.UserRole.SUPER_ADMIN, permissions);
        userRole.save();
    }

    private void createDefaultRole() {
        Set<String> permissions = new HashSet<String>();
        if (DarwinFactory.getInstance().loadUserRole(Constants.UserRole.DEFAULT_ROLE) == null) {
            UserRole userRole = DarwinFactory.getInstance().buildUserRole(Constants.UserRole.DEFAULT_ROLE, Constants.UserRole.DEFAULT_ROLE, permissions);
            userRole.save();
        }
    }

    private void createMongoClient() {
        if(Play.configuration.containsKey("mongo_uri") && !Play.configuration.getProperty("mongo_uri").isEmpty()) {
            Logger.debug(Play.configuration.getProperty("mongo_uri"));
            MongoFactory.createMongoClient(Play.configuration.getProperty("mongo_uri"));
        }
    }

    private void configureSalt() {
        AuthUtils.configureSalt();
    }
}
