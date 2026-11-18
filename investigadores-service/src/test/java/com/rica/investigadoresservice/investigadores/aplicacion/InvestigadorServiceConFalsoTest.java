package com.rica.investigadoresservice.investigadores.aplicacion;
import org.junit.jupiter.api.Test;
import com.rica.investigadoresservice.investigadores.aplicacion.InvestigadorFactory;
import com.rica.investigadoresservice.investigadores.aplicacion.InvestigadorService;
import com.rica.investigadoresservice.investigadores.dominio.Investigador;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvestigadorServiceConFalsoTest {

    private final List<String> topicos = new ArrayList<>();
    private final PublicadorEventos publicador = (topico, clave, contenido) -> topicos.add(topico);

    @Test
    void registraYRecuperaUnInvestigadorSinNingunaDependenciaDeSpringNiDeBaseDeDatos() {
        RepositorioInvestigadoresFalso repositorio = new RepositorioInvestigadoresFalso();
        InvestigadorFactory factory = new InvestigadorFactory(repositorio);
        InvestigadorService service = new InvestigadorService(repositorio, factory, publicador);

        Investigador guardado = service.registrar("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        assertThat(guardado.getId()).isNotNull();
        assertThat(service.buscarPorId(guardado.getId()).getNombreCompleto()).isEqualTo("Ana Torres");
        assertThat(topicos).containsExactly("investigadores.registrados");
    }

    @Test
    void laSagaDeEliminacionMarcaPublicaYSoloBorraAlConfirmar() {
        RepositorioInvestigadoresFalso repositorio = new RepositorioInvestigadoresFalso();
        InvestigadorService service = new InvestigadorService(repositorio, new InvestigadorFactory(repositorio), publicador);
        Investigador guardado = service.registrar("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        service.iniciarEliminacion(guardado.getId());
        service.iniciarEliminacion(guardado.getId());

        assertThat(service.buscarPorId(guardado.getId()).estaEnEliminacion()).isTrue();
        assertThat(topicos).containsExactly("investigadores.registrados", "investigadores.en-eliminacion");

        service.confirmarEliminacion("ana.torres@uptc.edu.co");

        assertThat(repositorio.listarTodos()).isEmpty();
    }

    @Test
    void confirmarEliminacionIgnoraInvestigadoresQueNoEstanEnEliminacion() {
        RepositorioInvestigadoresFalso repositorio = new RepositorioInvestigadoresFalso();
        InvestigadorService service = new InvestigadorService(repositorio, new InvestigadorFactory(repositorio), publicador);
        service.registrar("Ana Torres", "ana.torres@uptc.edu.co", "GIT-UPTC");

        service.confirmarEliminacion("ana.torres@uptc.edu.co");

        assertThat(repositorio.listarTodos()).hasSize(1);
    }
}
