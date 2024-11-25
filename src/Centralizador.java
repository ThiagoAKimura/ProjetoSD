import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Centralizador {
    private static final int PORT = 5000;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) {
        try {
            // Inicializa o servidor na porta especificada
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Centralizador iniciado na porta " + PORT);

            // Lista para armazenar os processos conectados
            List<Socket> processosConectados = new ArrayList<>();
            List<String> horariosRecebidos = new ArrayList<>();

            while (true) {
                // Aceita conexões de novos processos
                Socket socket = serverSocket.accept();
                processosConectados.add(socket);
                System.out.println("Novo processo conectado: " + socket.getInetAddress());

                // Inicia uma thread para lidar com o processo
                new Thread(() -> handleProcess(socket, processosConectados, horariosRecebidos)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Função para lidar com as mensagens de um processo
    private static void handleProcess(Socket socket, List<Socket> processosConectados, List<String> horariosRecebidos) {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String mensagem = in.readUTF();
                System.out.println("Recebido: " + mensagem);

                // Extração do horário da mensagem
                String horarioRecebido = extrairHorario(mensagem);
                if (horarioRecebido != null) {
                    horariosRecebidos.add(horarioRecebido);

                    // Verifica se precisa sincronizar
                    if (precisaSincronizar(horarioRecebido)) {
                        System.out.println("Discrepância detectada. Iniciando sincronização...");
                        String novoHorario = executarSincronizacao(horariosRecebidos);

                        // Envia o novo horário para os processos
                        for (Socket processo : processosConectados) {
                            DataOutputStream processOut = new DataOutputStream(processo.getOutputStream());
                            processOut.writeUTF("SYNC " + novoHorario);
                        }

                        System.out.println("Sincronização concluída. Novo horário: " + novoHorario);
                        horariosRecebidos.clear(); // Limpa os horários para o próximo ciclo
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Conexão encerrada com o processo: " + socket.getInetAddress());
        }
    }

    // Função para verificar se há discrepâncias maiores que 1 segundo
    private static boolean precisaSincronizar(String horarioRecebido) {
        LocalTime horaLocal = LocalTime.now();
        LocalTime horaRecebida = LocalTime.parse(horarioRecebido, FORMATTER);

        return Math.abs(horaLocal.toSecondOfDay() - horaRecebida.toSecondOfDay()) > 1;
    }

    // Função que implementa o algoritmo de Berkeley para sincronizar horários
    private static String executarSincronizacao(List<String> horariosRecebidos) {
        LocalTime horaLocal = LocalTime.now();
        int totalSegundos = horaLocal.toSecondOfDay();

        // Soma os segundos de todos os horários recebidos
        for (String horario : horariosRecebidos) {
            LocalTime horaRecebida = LocalTime.parse(horario, FORMATTER);
            totalSegundos += horaRecebida.toSecondOfDay();
        }

        // Calcula a média
        int mediaSegundos = totalSegundos / (horariosRecebidos.size() + 1);

        // Converte a média de segundos de volta para o formato HH:mm:ss
        int horas = mediaSegundos / 3600;
        int minutos = (mediaSegundos % 3600) / 60;
        int segundos = mediaSegundos % 60;

        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }

    // Função para extrair o horário de uma mensagem
    private static String extrairHorario(String mensagem) {
        try {
            int inicio = mensagem.indexOf("Hora:") + 5;
            return mensagem.substring(inicio).trim();
        } catch (Exception e) {
            return null;
        }
    }
}
