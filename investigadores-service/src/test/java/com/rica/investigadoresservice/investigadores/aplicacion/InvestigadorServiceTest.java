package com.rica.investigadoresservice.investigadores.aplicacion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.rica.investigadoresservice.compartido.RecursoNoEncontradoException;
import com.rica.investigadoresservice.investigadores.aplicacion.InvestigadorFactory;
import com.rica.investigadoresservice.investigadores.aplicacion.InvestigadorService;
import com.rica.investigadoresservice.investigadores.aplicacion.RepositorioInvestigadores;
import com.rica.investigadoresservice.investigadores.dominio.CorreoDuplicadoException;
import com.rica.investigadoresservice.investigadores.dominio.CorreoInstitucional;
import com.rica.investigadoresservice.investigadores.dominio.Investigador;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestigadorServiceTest {

    @Mock
    private RepositorioInvestigadores repositorioInvestigadores;

    @InjectMocks
    private InvestigadorService investigadorService;

    @Mock
    private InvestigadorFactory investigadorFactory;

    @Mock
    private PublicadorEventos publicadorEventos;

    @Test
    void buscarPorIdDevuelveElInvestigadorCuandoExiste() {
        Investigador investigador = new Investigador(1L, "Ana Torres", new CorreoInstitucional("ana.torres@uptc.edu.co"), "GIT-UPTC");
        when(repositorioInvestigadores.buscarPorId(1L)).thenReturn(Optional.of(investigador));

        Investigador resultado = investigadorService.buscarPorId(1L);

        assertThat(resultado.getNombreCompleto()).isEqualTo("Ana Torres");
    }

    @Test
    void buscarPorIdLanzaExcepcionCuandoNoExiste() {
        when(repositorioInvestigadores.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> investigadorService.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void registrarRechazaCorreoInstitucionalDuplicado() {
        Investigador nuevo = new Investigador(null, "Carlos Ruiz", new CorreoInstitucional("carlos.ruiz@uptc.edu.co"), "GIT-UPTC");

        when(investigadorFactory.crear(
                nuevo.getNombreCompleto(),
                nuevo.getCorreoInstitucional().valor(),
                nuevo.getGrupoInvestigacion()))
                .thenThrow(new CorreoDuplicadoException("El correo ya existe"));

        assertThatThrownBy(() -> investigadorService.registrar(nuevo.getNombreCompleto(),
                nuevo.getCorreoInstitucional().valor(), nuevo.getGrupoInvestigacion()))
                .isInstanceOf(CorreoDuplicadoException.class);
    }

}
