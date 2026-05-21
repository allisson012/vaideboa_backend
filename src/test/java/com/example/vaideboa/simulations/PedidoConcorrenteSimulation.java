package com.example.vaideboa.simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class PedidoConcorrenteSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    FeederBuilder.FileBased<String> feeder =
            csv("users.csv").circular();
   // teste
    ScenarioBuilder scn = scenario("Pedido de Carona concorrente")

            .feed(feeder)

            .exec(
                    http("Login")
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

                            .check(bodyString().saveAs("token"))
            )
 
            .exec(
                    http("Agendar Carona")
                            .post("/pedido/agendar")
                            .header(
                                    "Authorization",
                                    session -> "Bearer " + session.getString("token")
                            )
                            .body(
                                    StringBody(
                                            """
                                            {
                                              "idCarona": 1,
                                              "saidaLat": -22.570111,
                                              "saidaLng": -44.902776,
                                              "destinoLat": -22.5866443,
                                              "destinoLng": -44.9625176
                                            }
                                            """
                                    )
                            )
                            .asJson()

                            .check(status().in(200, 400))
            );

    {
        setUp(
                scn.injectOpen(
                        atOnceUsers(50)
                )
        ).protocols(httpProtocol);
    }
}