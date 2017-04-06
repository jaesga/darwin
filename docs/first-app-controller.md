# First app controller

First of all you need create a new class at the `controllers` package and extends `WebController`:
```java
package controllers;

public class PublicContent extends WebController {
    
    public static void index() {
        render();
    }
}
```

if you prefer you can use *@With* annotation:
```java
package controllers;

@With(WebController.class)
public class PublicContent extends Controller {
    
    public static void index() {
        render();
    }
}

WebController provides:
- i18n.
- Gives the current authenticated user.
- Global onBefore hook