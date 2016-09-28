package controllers;

import models.api.APIClient;
import models.factory.DarwinFactory;
import play.data.validation.Required;

import java.util.List;

@Check("API_CLIENTS")
public class APIClients extends WebSecurityController {

    public static void index() {
        List<APIClient> apiClientList = DarwinFactory.getInstance().retrieveUserAPIClients(session.get("username"));

        render(apiClientList);
    }

    public static void create(@Required(message="validation.apiClient.name.invalidOrEmpty") String name) {
        checkAuthenticity();

        name = Security.cleanInput(name);
        validation.required(name).message("validation.apiClient.name.invalidOrEmpty").key("name");

        if (validation.hasErrors()) {
            validation.keep();
            index();
        }
        APIClient apiClient = DarwinFactory.getInstance().buildAPIClient(name, session.get("username"));
        boolean existingApiClient = apiClient.isExistingAPIClient();
        validation.isTrue(!existingApiClient).message("validation.apiClient.name.apiClientAlreadyExists").key("name");
        if (validation.hasErrors()) {
            validation.keep();
            index();
        } else {
            apiClient.save();
        }

        index();
    }

    public static void remove(@Required String id) {
        checkAuthenticity();

        APIClient apiClient = DarwinFactory.getInstance().loadAPIClient(id);
        if (apiClient != null && apiClient.getEmail().equals(session.get("username"))){
            apiClient.remove();
        }

        index();
    }

    public static void renewSecret(@Required String id) {
        checkAuthenticity();

        APIClient apiClient = DarwinFactory.getInstance().loadAPIClient(id);
        if (apiClient != null && apiClient.getEmail().equals(session.get("username"))){
            apiClient.renewSecret();
        }

        index();
    }

    public static void editApiClientName(@Required String name, @Required String id) {
        checkAuthenticity();

        name = Security.cleanInput(name);
        validation.required(name).message("validation.apiClient.name.invalidOrEmpty").key("name");

        if (validation.hasErrors()) {
            validation.keep();
            index();
        }
        APIClient apiClient = DarwinFactory.getInstance().loadAPIClient(id);
        apiClient.setName(name);
        apiClient.save();

        index();
    }
}