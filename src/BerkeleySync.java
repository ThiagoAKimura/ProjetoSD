import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BerkeleySync {

    public static String sincronizar(List<String> horarios) {
        int totalSegundos = 0;

        // Converte os horários para segundos
        for (String horario : horarios) {
            String[] partes = horario.split(":");
            int horas = Integer.parseInt(partes[0]) * 3600;
            int minutos = Integer.parseInt(partes[1]) * 60;
            int segundos = Integer.parseInt(partes[2]);
            totalSegundos += (horas + minutos + segundos);
        }

        // Calcula a média
        int mediaSegundos = totalSegundos / horarios.size();

        // Converte de volta para formato HH:mm:ss
        int horas = mediaSegundos / 3600;
        int minutos = (mediaSegundos % 3600) / 60;
        int segundos = mediaSegundos % 60;

        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }
}
