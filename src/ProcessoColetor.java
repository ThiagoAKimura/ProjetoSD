import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class ProcessoColetor {
    private static final int PORT = 5000; // Porta do centralizador
    private static final String HOST = "localhost"; // Endereço do centralizador
    private static final Random random = new Random();
    private static LocalTime horarioLocal = LocalTime.now(); // Horário simulado do processo

    public static void main(String[] args) {
        try {
            // Conexão com o centralizador
            Socket socket = new Socket(HOST, PORT);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // Simulação do ciclo de coleta
            for (int i = 0; i < 200; i++) { // 200 ciclos
                // Coleta de temperatura (simulada)
                int temperaturaCPU = 40 + random.nextInt(30); // Temperatura entre 40°C e 70°C
                String horario = getHorarioAtual();

                // Criação da mensagem
                String mensagem = String.format("Processo:%s, Temp:%d°C, Hora:%s",
                        InetAddress.getLocalHost().getHostName(),
                        temperaturaCPU, horario);

                // Envio da mensagem ao centralizador
                out.writeUTF(mensagem);
                System.out.println("Enviado: " + mensagem);

                // Recebe sincronização do Centralizador
                if (in.available() > 0) { // Verifica se há resposta pendente
                    String resposta = in.readUTF();
                    if (resposta.startsWith("SYNC")) {
                        String novoHorario = resposta.split(" ")[1];
                        sincronizarHorario(novoHorario);
                    }
                }

                // Espera antes do próximo ciclo (simula tempo variável)
                Thread.sleep(5000 + random.nextInt(5000)); // Entre 5s e 10s
            }

            // Fechar conexões
            out.close();
            in.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Função para recuperar o horário atual (simula um relógio com variação aleatória)
    private static String getHorarioAtual() {
        horarioLocal = horarioLocal.plusSeconds(random.nextInt(3) - 1); // Varia entre -1 e +1 segundos
        return horarioLocal.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // Função para sincronizar o horário com o valor recebido
    private static void sincronizarHorario(String novoHorario) {
        horarioLocal = LocalTime.parse(novoHorario, DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.println("Horário sincronizado com o centralizador: " + horarioLocal);
    }
}
