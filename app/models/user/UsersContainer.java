package models.user;

import java.util.ArrayList;
import java.util.List;

public class UsersContainer {

    private List<User> users;
    private int countBeforeLimit;

    public UsersContainer() {
        users = new ArrayList<User>();
        countBeforeLimit = 0;
    }

    public UsersContainer(List<User> users, int countBeforeLimit) {
        this.users = users;
        this.countBeforeLimit = countBeforeLimit;
    }

    public List<User> getUsers() {
        return users;
    }

    public int getCount() {
        return users.size();
    }

    public int getCountBeforeLimit() {
        return countBeforeLimit;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void setCountBeforeLimit(int countBeforeLimit) {
        this.countBeforeLimit = countBeforeLimit;
    }
}
