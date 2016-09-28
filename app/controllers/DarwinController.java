package controllers;


import play.mvc.Before;
import play.mvc.Controller;

public class DarwinController extends Controller {
    @Before
    private static void hook() {
        DarwinHooks.AppHooks.invoke("onBefore");
    }
}
