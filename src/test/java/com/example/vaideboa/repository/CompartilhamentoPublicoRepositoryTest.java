package com.example.vaideboa.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import com.example.vaideboa.model.enums.StatusCarona;

class CompartilhamentoPublicoRepositoryTest {
    @Test
    void validaMapeamentoEConsultasSemAcessarBanco() throws Exception {
        DataSource dados = mock(DataSource.class);
        LocalContainerEntityManagerFactoryBean fabrica = new LocalContainerEntityManagerFactoryBean();
        fabrica.setDataSource(dados);
        fabrica.setPackagesToScan("com.example.vaideboa.model");
        fabrica.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        fabrica.setJpaPropertyMap(Map.of(
                "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
                "hibernate.boot.allow_jdbc_metadata_access", false,
                "hibernate.hbm2ddl.auto", "none"));
        try {
            fabrica.afterPropertiesSet();
            try (var em = fabrica.getObject().createEntityManager()) {
                String consulta = CompartilhamentoPublicoRepository.class
                        .getMethod("buscarLocalizacaoValida", String.class, Instant.class, StatusCarona.class)
                        .getAnnotation(Query.class).value();
                assertDoesNotThrow(() -> em.createQuery(consulta)
                        .setParameter("token", "a".repeat(43))
                        .setParameter("agora", Instant.now())
                        .setParameter("status", StatusCarona.EM_ANDAMENTO));
                String atualizacao = CompartilhamentoPublicoRepository.class
                        .getMethod("atualizarLocalizacao", Long.class, Point.class, Instant.class, StatusCarona.class)
                        .getAnnotation(Query.class).value();
                assertDoesNotThrow(() -> em.createQuery(atualizacao)
                        .setParameter("idCarona", 1L)
                        .setParameter("posicao", new GeometryFactory(new PrecisionModel(), 4326)
                                .createPoint(new Coordinate(-46, -23)))
                        .setParameter("recebidaEm", Instant.now())
                        .setParameter("status", StatusCarona.EM_ANDAMENTO));
            }
            verify(dados, never()).getConnection();
        } finally {
            fabrica.destroy();
        }
    }
}
