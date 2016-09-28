package controllers;

import java.lang.reflect.InvocationTargetException;

import models.user.User;
import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.utils.Java;

public class DarwinHooks extends Controller{

    public static class AppHooks extends Controller {

        private static final String URL_AFTER_LOGIN = Play.configuration.getProperty("http.path", "") + Play.configuration.getProperty("url.after.login", "/apiclients");

        /**
         * This method is called during the authentication process. This is where you check if
         * the user is allowed to log in into the system. This is the actual authentication process
         * against a third party system (most of the time a DB).
         *
         * @param username
         * @param password
         * @param user
         * @return true if the authentication process succeeded
         */
        static Security.AuthResult authenticate(String username, String password, User user) {
            return new Security.AuthResult();
        }

        /**
         * This method is called after a successful authentication.
         * You need to override this method if you with to perform specific actions (eg. Record the time the user signed in)
         */
        static void onAuthenticated() {
        }

        /**
         * This method is called before a user tries to sign off.
         * You need to override this method if you wish to perform specific actions (eg. Record the name of the user who signed off)
         */
        static void onDisconnect() {
        }

        /**
         * This method is called after a successful sign off.
         * 
         * <p>You need to override this method if you wish to perform specific
         * actions (eg. Record the time the user signed off).</p>
         * 
         * <p>Final redirection after sign off can be controlled through the
         * method's return value, as this return value, if not null, will be
         * used as URL for the redirection.</p>
         * 
         * @return The URL to redirect to after sign off.
         */
        static String onDisconnected() {
            return null;
        }

        static boolean onLatchPair(String token, User user){
            return true;
        }

        static void onLatchPaired(User user){}

        static boolean onLatchUnpair(User user){
            return true;
        }

        static void onLatchUnpaired(User user){}

        static boolean onUserRemove(User user) {return true;}

        static void onUserRemoved(User user) {}

        /**
         * This hooks is triggered after every WebController action
         */
        static void onBefore() {}

        static String onUrlAfterLogin() {
            return URL_AFTER_LOGIN;
        }

        static String onUrlBase() {
            return null;
        }

        static boolean onIsAuthTimeExpired(Boolean isAuthTimeExpired) {
            return isAuthTimeExpired;
        }

        public static Object invoke(String m, Object... args) {
            try {
                return Java.invokeChildOrStatic(AppHooks.class, m, args);
            } catch (InvocationTargetException e) {
                Logger.error(e.getMessage());
            } catch (Exception e) {
                Logger.error(e.getMessage(),e);
            }
            return null;
        }
    }
}
