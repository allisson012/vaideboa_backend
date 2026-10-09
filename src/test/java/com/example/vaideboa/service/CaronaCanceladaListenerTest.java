package com.example.vaideboa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.vaideboa.event.NotificacaoCancelamentoCaronaEvent;

class CaronaCanceladaListenerTest {

    private AnnotationConfigApplicationContext context;
    private NotificacoesRegistradas notificacoes;
    private TransactionTemplate transaction;
    private boolean commitConfirmado;

    @BeforeEach
    void configurar() {
        context = new AnnotationConfigApplicationContext();
        notificacoes = new NotificacoesRegistradas();
        context.registerBean(TransactionalEventListenerFactory.class);
        context.registerBean(NotificacoesService.class, () -> notificacoes);
        context.registerBean(CaronaCanceladaListener.class);
        context.refresh();

        // A transação usa a sincronização real do Spring, sem banco ou envio externo.
        transaction = new TransactionTemplate(new AbstractPlatformTransactionManager() {
            @Override
            protected Object doGetTransaction() {
                return new Object();
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) {
                commitConfirmado = true;
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) {
            }
        });
    }

    @AfterEach
    void fecharContexto() {
        context.close();
    }

    @Test
    void enviaSomenteDepoisDoCommit() {
        transaction.executeWithoutResult(status -> {
            publicar("passageiro");
            assertTrue(notificacoes.tentativas.isEmpty());
        });

        assertTrue(commitConfirmado);
        assertEquals(List.of("passageiro"), notificacoes.tentativas);
    }

    @Test
    void naoEnviaQuandoOcorreRollback() {
        transaction.executeWithoutResult(status -> {
            publicar("passageiro");
            status.setRollbackOnly();
        });

        assertTrue(notificacoes.tentativas.isEmpty());
    }

    @Test
    void falhaDeUmDestinatarioNaoImpedeOsDemais() {
        transaction.executeWithoutResult(status -> {
            publicar("falha");
            publicar("passageiro");
        });

        assertTrue(commitConfirmado);
        assertEquals(List.of("falha", "passageiro"), notificacoes.tentativas);
    }

    @Test
    void naoEnviaSemTransacao() {
        publicar("passageiro");

        assertTrue(notificacoes.tentativas.isEmpty());
    }

    private void publicar(String username) {
        context.publishEvent(new NotificacaoCancelamentoCaronaEvent(
                username, "Carona cancelada", "Mensagem de e-mail", "Mensagem push"));
    }

    private class NotificacoesRegistradas extends NotificacoesService {
        private final List<String> tentativas = new ArrayList<>();

        NotificacoesRegistradas() {
            super(null, null, null, null, null);
        }

        @Override
        public void enviarNotificacao(String username, String titulo, String mensagemEmail, String mensagemPush) {
            assertTrue(commitConfirmado);
            tentativas.add(username);
            if ("falha".equals(username)) {
                throw new IllegalStateException("Falha simulada no envio");
            }
        }
    }
}
