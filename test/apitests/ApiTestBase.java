package apitests;

import models.api.APIClient;
import models.api.DarwinAuth;
import models.factory.DarwinFactory;
import org.junit.After;
import org.junit.Before;
import play.test.FunctionalTest;

public abstract class ApiTestBase extends FunctionalTest {
    protected String email = "test_test@test.com";
    protected String apiClientId;
    protected String defaultId;
    protected String secret;
    protected DarwinAuth apiAuth;

    @Before
    public void setUp() throws Exception {
        APIClient apiClient = DarwinFactory.getInstance().buildAPIClient("Test Api Client", email);
        apiClient.save();
        defaultId = apiClient.getClientId();
        apiClientId = defaultId;
        secret = apiClient.getSecret();
        apiAuth = new DarwinAuth(apiClientId, secret);
        customSetUp();
    }

    @After
    public void tearDown() throws Exception {
        DarwinFactory.getInstance().loadAPIClient(apiClientId).remove();
        customTearDown();
    }

    protected abstract void customSetUp() throws Exception;
    protected abstract void customTearDown() throws Exception;
}
