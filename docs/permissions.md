# Permissions and how its works

Darwin allows functionality for manage `roles and permissions`.

1. Permissions

    By Default exists the following **permissions**:
    * USER_READ
    * USER_DELETE
    * USER_ACTIVATE
    * USER_SELF_DELETE
    * USER_READ_PROFILES
    * ADMIN
    * API_CLIENTS

    If you want add new app permissions you can do it at the **darwin.conf**:
    ```yaml
    .../conf/darwin.conf
    
    permissions=PERMISION1,PERMISION2
    ```

2. Roles

    By Default exists the following **roles**:
    * SuperAdmin => All access granted
    * RegularUser => By default has not any permissions
    
    When an user sign up in the portal darwin gives him the role RegularUser, unless this email user are placed at **darwin.conf**:
    
    ```yaml
    .../conf/darwin.conf
    
    auto_admin_users=user1@example.org,user2@example.org
    ```
    
    > Roles management at `http://localhost:9000/admin/roles`
    
    
## Usage

You can protect single actions or all actions in controller:

```java
   package controllers;

   @Check("USER_READ")
   public class Users extends WebSecurityController {
       
       public static void list() {}
       
       @Check("USER_DELETE")
       public static void deleteUser(String id) {}
       
   }
```
In this case you need the **USER_READ** permissions to do all actions in Users controller and if you want delete an user
you need the **USER_DELETE** permission too.

> This functionality only works at the WebSecurityController or Secure controllers.