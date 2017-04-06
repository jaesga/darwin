# Getting started

1. Importing module.

    First of all you need import darwin module at the dependencies.yml project file and download the new dependencies:
    ```
    .../conf/dependencies.yml

    require:
        - play
        - 11PathsStable -> darwin 1.0.7SNAPSHOT

    repositories:
       - my modules repo:
            type:       http
            artifact:   "http://carlos.11paths.local/play-repo/modules/darwin-1.0.7SNAPSHOT.zip"
            contains:
                - 11PathsStable -> *
    ```
    `$ play deps`
    > Remember,  you needs stay connected into 11Paths VPN

2. Importing routes.

    Darwin provides many web features and you can import all them or only that you need!
    ```
    .../conf/routes

    # Darwin
    *           /                                     module:darwin

    ```

3. Init Darwin Factory.

    Darwin bring by default a MongoDB configuration, but if you want you can build your custom factory and use it.
    You need instantiate this factory at de Bootstrap Job.
    ```
    package jobs;

    import play.jobs.Job;
    import play.jobs.OnApplicationStart;
    import play.libs.F;


    @OnApplicationStart
    public class Bootstrap extends Job {

        public void doJob() {
            F.Promise promise = new Init().now();
            new Init()
                .now()
                .onRedeem(new F.Action<F.Promise>() {
                    @Override
                    public void invoke(F.Promise result) {
                        // TODO Custom OnApplicationStart features.
                    }
                });
        }
    }
    ```

4. Configuration

    *All you can do* with Darwin can be configured. Some features are mandatory for what the module works. You need add
    to the conf directory a copy of `darwin/conf/mandatory.conf -> ../conf/darwin.conf` and include it at the bottom of your application.conf
    ```
    .../conf/application.conf

    ...

    @include.darwin=darwin.conf
    ```

5. Start your App!

    Thats all! Start your application  and enjoy! :grin:

    `$ play run`
    > You can see all darwin app routes at darwin/conf/routes.