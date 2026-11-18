package com.rica.publicacionesservice.publicaciones;

import com.rica.publicacionesservice.compartido.RecursoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicacionServiceTest {

    @Mock
    private PublicacionRepository publicacionRepository;

    @Mock
    private VerificadorInvestigador verificadorInvestigador;

    @Mock
    private LimitePublicacionesAnualesService limiteService;

    @InjectMocks
    private PublicacionService publicacionService;

    private Publicacion publicacionDe(String correo) {
        Publicacion publicacion = new Publicacion();
        publicacion.setInvestigadorCorreo(correo);
        publicacion.setAnio(2025);
        return publicacion;
    }

    @Test
    void registrarRechazaUnInvestigadorInexistente() {
        Publicacion publicacion = publicacionDe("nadie@uptc.edu.co");
        when(verificadorInvestigador.existe("nadie@uptc.edu.co")).thenReturn(false);

        assertThatThrownBy(() -> publicacionService.registrar(publicacion))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(publicacionRepository, never()).save(publicacion);
    }

    @Test
    void registrarGuardaLaPublicacionDeUnInvestigadorExistente() {
        Publicacion publicacion = publicacionDe("ana.torres@uptc.edu.co");
        when(verificadorInvestigador.existe("ana.torres@uptc.edu.co")).thenReturn(true);
        when(limiteService.puedeRegistrar(publicacion)).thenReturn(true);
        when(publicacionRepository.save(publicacion)).thenReturn(publicacion);

        assertThat(publicacionService.registrar(publicacion)).isSameAs(publicacion);
    }
}
