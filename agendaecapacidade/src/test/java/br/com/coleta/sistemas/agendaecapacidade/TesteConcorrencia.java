
package br.com.coleta.sistemas.agendaecapacidade;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TesteConcorrencia {

    public static void main(String[] args) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        List<CompletableFuture<Integer>> requisicoes = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            String id = UUID.randomUUID().toString();

            String json = """
                    {
                        "solicitacaoId": "%s",
                        "bairro": "Itoupava",
                        "dataColeta": "2026-11-28"
                    }
                    """.formatted(id);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/agenda/reservar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            CompletableFuture<Integer> resposta =
                    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                            .thenApply(response -> {
                                System.out.println("Status: " + response.statusCode());
                                return response.statusCode();
                            });

            requisicoes.add(resposta);
        }

        CompletableFuture.allOf(
                requisicoes.toArray(new CompletableFuture[0])
        ).join();

        long aceitas = requisicoes.stream()
                .map(CompletableFuture::join)
                .filter(status -> status == 202)
                .count();

        long recusadas = requisicoes.stream()
                .map(CompletableFuture::join)
                .filter(status -> status == 409)
                .count();

        System.out.println("\n===== RESULTADO =====");
        System.out.println("Reservas aceitas: " + aceitas);
        System.out.println("Reservas recusadas: " + recusadas);
    }
}
