package controllers;

import models.Constants;
import models.api.APIClient;
import models.factory.DarwinFactory;
import models.finder.UserFinder;
import models.latch.LatchSDK;
import models.roles.UserRole;
import models.user.User;
import models.user.UsersContainer;
import notifiers.DarwinMailer;
import play.data.validation.Valid;

import java.util.List;
import java.util.Map;

@Check("USER_READ")
public class Users extends WebSecurityController {

    private static void index() {
        index(null, 1);
    }

    public static void index(@Valid UserFinder finder, int page) {
        if (finder == null) {
            finder = new UserFinder();
        }
        Map<String, Object> query = (!validation.hasError("finder")) ? finder.getQuery() : null;
        UsersContainer  usersContainer = DarwinFactory.getInstance().retrieveUsers(query, Constants.Utils.MAX_RESULTS_PAGE, page);
        List<User> users  = usersContainer.getUsers();
        List<UserRole> roles = DarwinFactory.getInstance().retrieveRoles();
        int totalUsers = usersContainer.getCountBeforeLimit();
        render(finder, users, roles, totalUsers, page);
    }

    @Check("USER_ACTIVATE")
    public static void toggleActivationStatus(String id) {
        checkAuthenticity();

        if (id != null && !id.isEmpty()) {
            User user = DarwinFactory.getInstance().loadUser(id);
            if (user != null && !session.get("username").equals(id)) {
                if (user.isActive()) {
                    user.setActive(false);
                } else {
                    user.setActive(true);
                    user.createDefaultAPIClient();
                    DarwinMailer.adminActivateAccount(user);
                }
                user.save();
            }
        }

        index();
    }

    @Check("USER_DELETE")
    public static void deleteUser(String id) {
        checkAuthenticity();

        if (id != null && !id.isEmpty()) {
            User user = DarwinFactory.getInstance().loadUser(id);
            if (user != null && !session.get("username").equals(id)) {
                boolean isRemovalAllowed = (Boolean)DarwinHooks.AppHooks.invoke("onUserRemove", user);
                if (isRemovalAllowed) {
                    removeUserAPIClients(user);
                    user.remove();
                    if (user.isLatched()){
                        LatchSDK api = LatchSDK.getLatchAPI();
                        api.unpair(user.getLatchId());
                    }
                    DarwinHooks.AppHooks.invoke("onUserRemoved", user);
                }
            }
        }

        index();
    }

    protected static void removeUserAPIClients(User user) {
        List<APIClient> apiClients = DarwinFactory.getInstance().retrieveUserAPIClients(user.getEmail());
        for (APIClient apiClient : apiClients) {
            apiClient.remove();
        }
    }

    @Check("ADMIN")
    public static void setRole(String id, String roleId){
        checkAuthenticity();

        if (id != null && !id.isEmpty()) {
            User user = DarwinFactory.getInstance().loadUser(id);
            UserRole role = DarwinFactory.getInstance().loadUserRole(roleId);
            if (user != null && role != null && !session.get("username").equals(id)) {
                user.setRoleId(roleId);
                user.save();
            }
        }

        index();
    }
}
