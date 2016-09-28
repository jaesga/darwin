package controllers;

import com.google.gson.JsonObject;
import models.Config;
import models.Constants;
import models.exception.DarwinErrorException;
import models.factory.DarwinFactory;
import models.roles.UserRole;
import models.roles.permission.UserPermission;
import models.token.Token;
import models.token.TokenTypeBase;
import models.user.ActivationType;
import notifiers.DarwinMailer;
import play.Logger;
import play.data.validation.Required;
import play.i18n.Messages;

import java.util.ArrayList;
import java.util.List;

@Check("ADMIN")
public class Admin extends WebSecurityController {

    public static void roles() {
        List<UserRole> roles = DarwinFactory.getInstance().retrieveRoles();
        List<String> permissions = new ArrayList<String>(UserPermission.getPermissions());
        render(roles, permissions);
    }

    public static void editRole(@Required(message = "Roles.validation.name.invalidOrEmpty") String name, String roleId, String[] permissions) {
        checkAuthenticity();
        checkSpecialRole(roleId, true);

        name = controllers.Security.cleanInput(name);
        validation.required(name).message("Roles.validation.name.invalidOrEmpty").key("name");
        if (validation.hasErrors()) {
            params.flash();
            validation.keep();
            roles();
        }

        UserRole role = DarwinFactory.getInstance().loadUserRole(roleId);
        if (role == null) {
            role = DarwinFactory.getInstance().buildUserRole(name);
        } else if (!Constants.UserRole.specialRole.containsKey(roleId)) {
            role.setName(name);
        }
        role.setPermissions(permissions);
        role.save();

        roles();
    }

    public static void deleteRole(@Required String roleId) {
        checkAuthenticity();
        checkSpecialRole(roleId, false);

        UserRole role = DarwinFactory.getInstance().loadUserRole(roleId);
        if (role != null) {
            role.remove();
        }

        roles();
    }

    private static void checkSpecialRole(String roleId, boolean editAction) {
        if (Constants.UserRole.specialRole.containsKey(roleId)) {
            Boolean editable = Constants.UserRole.specialRole.get(roleId);
            if (!editable || !editAction) {
                String message = Messages.get("Roles.validation.forbiddenEditAdminRole", roleId);
                flash.put("forbiddenEditAdminRole", message);
                roles();
            }
        }
    }

    public static void invitations() {
        checkInvitationConfiguration();

        List<Token> tokens = DarwinFactory.getInstance().retrieveTokens(TokenTypeBase.INVITE_USER);

        render(tokens);
    }

    public static void inviteUser(@Required String email, @Required String lang) {
        checkInvitationConfiguration();
        checkAuthenticity();

        email = controllers.Security.cleanInput(email);
        lang = controllers.Security.cleanInput(lang);
        validation.required(email).message("Roles.validation.name.invalidOrEmpty").key("email");
        if (validation.hasErrors()){
            params.flash();
            validation.keep();
            invitations();
        }
        JsonObject data = new JsonObject();
        data.addProperty(Constants.Token.DATA_LANG, lang);
        Token token = DarwinFactory.getInstance().buildToken(email, data, TokenTypeBase.INVITE_USER);
        DarwinMailer.inviteAccount(email, token.getToken(), lang);
        token.save();
        invitations();
    }

    public static void deleteInvitationToken(@Required String token) {
        checkInvitationConfiguration();
        checkAuthenticity();

        try {
            Token invitationToken = DarwinFactory.getInstance().loadToken(token, TokenTypeBase.INVITE_USER);
            invitationToken.remove();
        } catch (DarwinErrorException e) {
            Logger.error(e.getMessage());
        }

        invitations();
    }

    public static void renewInvitationToken(@Required String email, @Required String token, @Required String lang) {
        checkInvitationConfiguration();
        checkAuthenticity();

        email = controllers.Security.cleanInput(email);
        lang = controllers.Security.cleanInput(lang);
        validation.required(email).message("Roles.validation.name.invalidOrEmpty").key("email");
        if (validation.hasErrors()){
            params.flash();
            validation.keep();
            invitations();
        }
        try {
            Token invitationToken = DarwinFactory.getInstance().loadToken(token, TokenTypeBase.INVITE_USER);
            JsonObject data = invitationToken.getData();
            if (data == null) {
                data = new JsonObject();
            }
            data.addProperty(Constants.Token.DATA_LANG, lang);
            invitationToken.setData(data);
            invitationToken.renew();
            DarwinMailer.inviteAccount(email, invitationToken.getToken(), lang);
        } catch (DarwinErrorException e) {
            Logger.error(e.getMessage());
        }

        invitations();
    }

    private static void checkInvitationConfiguration() {
        if (!ActivationType.INVITATION.equals(Config.getUserActivationType())) {
            notFound();
        }
    }

}
