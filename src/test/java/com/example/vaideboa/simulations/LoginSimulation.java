package com.example.vaideboa.simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class LoginSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    FeederBuilder.FileBased<String> feeder =
            csv("users.csv").circular();

    ScenarioBuilder scn = scenario("Login Simultaneo")
            .feed(feeder)
            .exec(
                    http("Login Request")
                            .post("/authenticate")
                            .body(
                                    StringBody(session ->
                                            "{"
                                                    + "\"username\":\"" + session.getString("username") + "\","
                                                    + "\"password\":\"" + session.getString("password") + "\""
                                                    + "}"
                                    )
                            )
                            .asJson()
                            .check(status().is(200))
            );

    {
        setUp(
                scn.injectOpen(
                        atOnceUsers(50)
                )
        ).protocols(httpProtocol);
    }
}