# Securing your controllers

First of all you need create a new class at the `controllers` package and extends `WebSecurityController`:
```java
package controllers;

public class PrivateContent extends WebSecurityController {
    
    public static void index() {
        render();
    }
}
```


if you prefer you can use *@With* annotation:
```java
package controllers;

@With(WebSecurityController.class)
public class PrivateContent extends Controller {
    
    public static void index() {
        render();
    }
}
```


WebSecurityController provides:
- WebController functionality.
- Check if the user is authenticated.
- Check password policy.
- Check latch mandatory mode.
