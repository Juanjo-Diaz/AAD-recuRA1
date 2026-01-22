package es.cifpcarlos3.examenRA1.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jugador implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String nombreUsuario;
    private String email;
    private int puntuacion;

    @Override
    public String toString() {
        return "Usuario: " + nombreUsuario +
                " | Email: " + email +
                " | Puntuación: " + puntuacion;
    }
}

